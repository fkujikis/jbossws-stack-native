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
package org.jboss.ws.jaxrpc.encoding;

// $Id: XOPSerializer.java 275 2006-05-04 21:36:29Z jason.greene@jboss.com $

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.binding.BindingException;
import org.jboss.ws.soap.MessageContextAssociation;
import org.jboss.ws.soap.SOAPMessageContextImpl;
import org.jboss.ws.soap.SOAPMessageImpl;
import org.jboss.ws.soap.attachment.MimeConstants;
import org.jboss.xb.binding.NamespaceRegistry;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Serializer for XOP values.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 17-Jan-2006
 */
public class XOPSerializer extends SerializerSupport
{
   // provide logging
   private static final Logger log = Logger.getLogger(XOPSerializer.class);

   /**
    *  Serializes an XOP object into an xop:Include
    */
   public String serialize(QName xmlName, QName xmlType, Object value, SerializationContextImpl serContext, NamedNodeMap attributes) throws BindingException
   {
      log.debug("serialize: [xmlName=" + xmlName + ",xmlType=" + xmlType + "]");

      NamespaceRegistry nsRegistry = serContext.getNamespaceRegistry();

      // Add the xop:Include element
      StringBuilder xopInclude = new StringBuilder("<" + Constants.PREFIX_XOP + ":Include ");
      xopInclude.append("xmlns:" + Constants.PREFIX_XOP + "='" + Constants.NS_XOP + "' ");
      
      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
      SOAPMessageImpl soapMessage = (SOAPMessageImpl)msgContext.getMessage();
      
      String cid = soapMessage.getCidGenerator().generateFromName(xmlName.getLocalPart());
      xopInclude.append("href='" + cid + "'/>");
      
      Node attr = attributes.getNamedItemNS(Constants.NS_XML_MIME, "contentType");
      if (attr == null)
         throw new WSException("Cannot obtain xmime:contentType");
      
      String contentType = attr.getNodeValue();
      
      AttachmentPart xopPart;
      if (value instanceof DataHandler)
      {
         DataHandler dataHandler = (DataHandler)value;
         xopPart = soapMessage.createAttachmentPart(dataHandler);
         if (contentType.equals(dataHandler.getContentType()) == false)
            log.warn("ContentType missmatch " + contentType + "!=" + dataHandler.getContentType());
      }
      else
      {
         xopPart = soapMessage.createAttachmentPart(value, contentType);
      }
      
      xopPart.addMimeHeader(MimeConstants.CONTENT_ID, cid);
      soapMessage.addAttachmentPart(xopPart);
      
      String xmlFragment = wrapValueStr(xmlName, xopInclude.toString(), nsRegistry, attributes);
      return xmlFragment;
   }
}
