/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.ws.xop;

import org.jboss.logging.Logger;
import org.jboss.util.xml.DOMUtils;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.jaxrpc.StubExt;
import org.jboss.ws.soap.MessageContextAssociation;
import org.jboss.ws.soap.NameImpl;
import org.jboss.ws.soap.SOAPMessageContextImpl;
import org.jboss.ws.soap.SOAPMessageImpl;
import org.jboss.ws.utils.MimeUtils;
import org.jboss.ws.utils.ThreadLocalAssociation;
import org.jboss.ws.utils.JavaUtils;
import org.jboss.xb.binding.SimpleTypeBindings;
import org.jboss.xb.binding.sunday.xop.XOPMarshaller;
import org.jboss.xb.binding.sunday.xop.XOPObject;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

/**
 * XOP context associated with a message context.
 * Acts as a facade to the current soap message and supports the various
 * XOP transitions.<p>
 * A good starting point to understand how MTOM in JBossWS works is to take a
 * look at the SOAPContentElement implementation.
 *
 * @see ThreadLocalAssociation
 * @see org.jboss.ws.soap.SOAPContentElement#handleMTOMTransitions()
 * @see XOPUnmarshallerImpl
 * @see XOPMarshallerImpl
 *
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since May 10, 2006
 * @version $Id$
 */
public class XOPContext {

   private static final Logger log = Logger.getLogger(XOPContext.class);

   private static final String NS_XOP_JBOSSWS = "http://org.jboss.ws/xop";

   /**
    * Check if the current soap message flagged as a XOP package?
    */
   public static boolean isXOPPackage() {
      boolean isXOP = false;
      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
      if(msgContext!=null) {
         SOAPMessageImpl soapMessage = (SOAPMessageImpl)msgContext.getMessage();
         isXOP = (soapMessage != null && soapMessage.isXOPMessage());
      }
      return isXOP;
   }

   /**
    * Check if MTOM is disabled through a message context property.
    * (<code>org.jboss.ws.mtom.enabled</code>)<br>
    * Defaults to TRUE if the property is not set.
    */
   public static boolean isMTOMEnabled()
   {
      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
      boolean mtomEnabled = msgContext.getProperty(StubExt.PROPERTY_MTOM_ENABLED) == null ?
          true :
          ((String)msgContext.getProperty(StubExt.PROPERTY_MTOM_ENABLED)).equalsIgnoreCase("true");
      return mtomEnabled;
   }

   /**
    * Replace all <code>xop:Include</code> elements with it's base64 representation.
    * This happens when the associated SOAPContentElement transitions to state dom-valid.<br>
    * All attachement parts will be removed.
    */
   public static void inlineXOPData(SOAPElement xopElement)
   {
      String ns = xopElement.getNamespaceURI()!=null ? xopElement.getNamespaceURI(): "";
      String localName = xopElement.getLocalName();

      // rpc/lit
      if(ns.equals(Constants.NS_XOP) && localName.equals("Include"))
      {
         replaceXOPInclude(xopElement.getParentElement(), xopElement);
      }
      else
      {
         // doc/lit
         Iterator it = DOMUtils.getChildElements(xopElement);
         while(it.hasNext())
         {
            SOAPElement childElement = (SOAPElement)it.next();
            String childNS = childElement.getNamespaceURI()!=null ? childElement.getNamespaceURI(): "";
            String childName = childElement.getLocalName();
            if(childNS.equals(Constants.NS_XOP) && childName.equals("Include"))
            {
               replaceXOPInclude(xopElement, childElement);
            }
            else
            {
               inlineXOPData(childElement);
            }
         }
      }
   }

   /**
    * Restore previously inlined XOP elements.
    * All base64 representations will be replaced by <code>xop:Include</code>
    * elements and the attachment parts will be recreated. <br>
    * This happens when a SOAPContentElement is written to an output stream.
    */
   public static void restoreXOPData(SOAPElement xopElement)
   {
      String contentType = xopElement.getAttributeNS(NS_XOP_JBOSSWS, "content-type");
      if(contentType != null && contentType.length()>0)
      {
         replaceBase64Representation(xopElement, contentType);
         xopElement.removeAttribute(new NameImpl(new QName(NS_XOP_JBOSSWS, "content-type")));
      }
      else
      {
         Iterator it = DOMUtils.getChildElements(xopElement);
         while(it.hasNext())
         {
            SOAPElement childElement = (SOAPElement)it.next();
            restoreXOPData(childElement);
         }
      }
   }

   private static void replaceBase64Representation(SOAPElement xopElement, String contentType) {

      SOAPElement parentElement = xopElement.getParentElement();
      log.debug("Replace base64 representation on element [xmlName=" + parentElement.getLocalName()+"]");

      String base64 = xopElement.getValue();
      byte[] data = SimpleTypeBindings.unmarshalBase64(base64);

      MimeUtils.ByteArrayConverter converter = MimeUtils.getConverterForContentType(contentType);
      Object converted = converter.readFrom(new ByteArrayInputStream(data));

      XOPObject xopObject = new XOPObject(converted);
      xopObject.setContentType(contentType);

      XOPMarshaller xopMarshaller = new XOPMarshallerImpl();
      String cid = xopMarshaller.addMtomAttachment(xopObject, xopElement.getNamespaceURI(), xopElement.getLocalName());      

      // remove base64 node with the xop:Include element
      org.w3c.dom.Node child = (org.w3c.dom.Node)xopElement.getFirstChild();
      xopElement.removeChild(child);

      try
      {
         SOAPElement xopInclude = xopElement.addChildElement("Include", "xop", Constants.NS_XOP);
         xopInclude.setAttribute("href", cid);
         log.debug("Restored xop:Include element on {" + xopElement.getNamespaceURI()+"}"+xopElement.getLocalName());
      }
      catch (SOAPException e)
      {
         throw new WSException("Failed to create XOP include element", e);
      }

   }

   private static void replaceXOPInclude(SOAPElement parent, SOAPElement xopIncludeElement)
   {

      log.debug("Replace xop:Include on element [xmlName=" + parent.getLocalName() +"]");

      String cid = xopIncludeElement.getAttribute("href");
      byte[] data;
      String contentType;

      try
      {
         AttachmentPart part = XOPContext.getAttachmentByCID(cid);
         DataHandler dh = part.getDataHandler();
         contentType = dh.getContentType();

         // TODO: can't we create base64 directly from stream?
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         dh.writeTo(bout);
         data = bout.toByteArray();

      }
      catch (Exception e)
      {
         throw new WSException("Failed to inline XOP data", e);
      }

      // create base64 contents
      String base64 = SimpleTypeBindings.marshalBase64(data);
      parent.removeChild(xopIncludeElement);
      parent.setValue(base64);
      parent.setAttributeNS(NS_XOP_JBOSSWS, "content-type", contentType);

      log.debug("Created base64 representation for content-type " + contentType);

      // cleanup the attachment part
      SOAPMessageContextImpl msgContext = (SOAPMessageContextImpl)MessageContextAssociation.peekMessageContext();
      SOAPMessageImpl soapMessage = (SOAPMessageImpl)msgContext.getMessage();

      if(cid.startsWith("cid:")) cid = cid.substring(4);
      cid = '<'+cid+'>';

      AttachmentPart removedPart = soapMessage.removeAttachmentByContentId(cid);
      if(null == removedPart)
         throw new WSException("Unable to remove attachment part " + cid);

      log.debug("Removed attachment part " + cid);

   }

   /**
    * Access an XOP attachment part by content id (CID).
    */
   public static AttachmentPart getAttachmentByCID(String cid) throws SOAPException
   {
      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
      SOAPMessageImpl soapMessage = (SOAPMessageImpl)msgContext.getMessage();

      // RFC2392 requires the 'cid:' part to be stripped from the cid
      if(cid.startsWith("cid:")) cid = cid.substring(4);
      cid = '<'+cid+'>';

      AttachmentPart part = soapMessage.getAttachmentByContentId(cid);
      if (part == null)
         throw new WSException("Cannot find attachment part for: " + cid);

      return part;
   }

   /**
    * Create a <code>DataHandler</code> for an object.
    * The handlers content type is based on the java type.
    */
   public static DataHandler createDataHandler(XOPObject xopObject)
   {
      DataHandler dataHandler;
      Object o = xopObject.getContent();

      if(o instanceof DataHandler)
      {
         dataHandler = (DataHandler)o;
      }
      else if(xopObject.getContentType() != null)
      {
         dataHandler = new DataHandler(o, xopObject.getContentType());
      }
      else if(! getContentTypeForClazz(o.getClass()).equals("application/octet-stream"))
      {
         dataHandler = new DataHandler(o, getContentTypeForClazz(o.getClass()));
      }
      else
      {
         DataSource ds = new SimpleDataSource(o, "application/octet-stream");
         dataHandler = new DataHandler(ds);
      }

      return dataHandler;
   }

   public static String getContentTypeForClazz(Class clazz)
   {
      if(JavaUtils.isAssignableFrom(java.awt.Image.class, clazz))
      {
         return "image/jpeg";
      }
      else if (JavaUtils.isAssignableFrom(javax.xml.transform.Source.class, clazz))
      {
         return "application/xml";
      }
      else if (JavaUtils.isAssignableFrom(java.lang.String.class, clazz))
      {
         return "text/plain";
      }
      else
      {
         return "application/octet-stream";
      }
   }
}
