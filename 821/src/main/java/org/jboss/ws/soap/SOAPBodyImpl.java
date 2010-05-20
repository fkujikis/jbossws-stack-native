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
package org.jboss.ws.soap;

// $Id$

import java.util.Iterator;
import java.util.Locale;
import java.io.Writer;
import java.io.IOException;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An object that represents the contents of the SOAP body element in a SOAP message.
 * A SOAP body element consists of XML data that affects the way the application-specific content is processed.
 *
 * A SOAPBody object contains SOAPBodyElement objects, which have the content for the SOAP body.
 * A SOAPFault object, which carries status and/or error information, is an example of a SOAPBodyElement object.
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 */
public class SOAPBodyImpl extends SOAPElementImpl implements SOAPBody
{
   private static Logger log = Logger.getLogger(SOAPBodyImpl.class);

   public SOAPBodyImpl(String prefix, String namespace)
   {
      super("Body", prefix, namespace);
   }

   /** Convert the child into a SOAPBodyElement */
   public SOAPElement addChildElement(SOAPElement child) throws SOAPException
   {
      if (!(child instanceof SOAPBodyElement))
      {
         child = isFault(child) ? convertToSOAPFault(child) : convertToBodyElement(child);
      }

      child = super.addChildElement(child);
      return child;
   }

   private boolean isFault(Node node)
   {
      return "Fault".equals(node.getLocalName()) && getNamespaceURI().equals(node.getNamespaceURI());
   }

   private SOAPElement convertToSOAPFault(Node node)
   {
      if (!(node instanceof SOAPElementImpl))
         throw new IllegalArgumentException("SOAPElementImpl expected");

      SOAPElementImpl element = (SOAPElementImpl) node;
      element.detachNode();
      return new SOAPFaultImpl(element);
   }

   private SOAPBodyElementDoc convertToBodyElement(Node node)
   {
      if (!(node instanceof SOAPElementImpl))
         throw new IllegalArgumentException("SOAPElementImpl expected");

      SOAPElementImpl element = (SOAPElementImpl) node;
      element.detachNode();
      return new SOAPBodyElementDoc(element);
   }

   public SOAPBodyElement addBodyElement(Name name) throws SOAPException
   {
      SOAPBodyElement child = new SOAPBodyElementDoc(name);
      return (SOAPBodyElement)addChildElement(child);
   }

   public SOAPBodyElement addDocument(Document doc) throws SOAPException
   {
      Element rootElement = doc.getDocumentElement();
      SOAPFactoryImpl soapFactory = new SOAPFactoryImpl();
      SOAPElementImpl soapElement = soapFactory.createElement(rootElement, true);
      return (SOAPBodyElement)addChildElement(soapElement);
   }

   public SOAPFault addFault() throws SOAPException
   {
      if (hasFault())
         throw new SOAPException("A SOAPBody may contain at most one SOAPFault child element");

      return addFault(new NameImpl(Constants.SOAP11_FAULT_CODE_SERVER), "Generic server fault");
   }

   public SOAPFault addFault(Name faultCode, String faultString) throws SOAPException
   {
      if (hasFault())
         throw new SOAPException("A SOAPBody may contain at most one SOAPFault child element");

      SOAPFaultImpl soapFault = new SOAPFaultImpl(getNamespaceURI());
      soapFault = (SOAPFaultImpl)addChildElement(soapFault);
      soapFault.setFaultCode(faultCode);
      soapFault.setFaultString(faultString);
      return soapFault;
   }

   public SOAPFault addFault(Name faultCode, String faultString, Locale locale) throws SOAPException
   {
      if (hasFault())
         throw new SOAPException("A SOAPBody may contain at most one SOAPFault child element");

      SOAPFaultImpl soapFault = new SOAPFaultImpl(getNamespaceURI());
      soapFault.setFaultCode(faultCode);
      soapFault.setFaultString(faultString, locale);
      addChildElement(soapFault);
      return soapFault;
   }

   public SOAPFault getFault()
   {
      Iterator it = getChildElements(new NameImpl("Fault", Constants.PREFIX_ENV, getSOAPEnvelope().getNamespaceURI()));
      return (it.hasNext() ? (SOAPFault)it.next() : null);
   }

   public boolean hasFault()
   {
      return getChildElements(Constants.SOAP11_FAULT).hasNext();
   }

   public Node appendChild(Node newChild) throws DOMException
   {
      if (!(newChild instanceof SOAPBodyElement || newChild instanceof DocumentFragment))
      {
         newChild = isFault(newChild) ? convertToSOAPFault(newChild) : convertToBodyElement(newChild);
      }

      return super.appendChild(newChild);
   }

   public Node insertBefore(Node newChild, Node refChild) throws DOMException
   {
      if (!(newChild instanceof SOAPBodyElement || newChild instanceof DocumentFragment))
      {
         newChild = isFault(newChild) ? convertToSOAPFault(newChild) : convertToBodyElement(newChild);
      }

      return super.insertBefore(newChild, refChild);
   }

   public Node replaceChild(Node newChild, Node oldChild) throws DOMException
   {
      if (!(newChild instanceof SOAPBodyElement || newChild instanceof DocumentFragment))
      {
         newChild = isFault(newChild) ? convertToSOAPFault(newChild) : convertToBodyElement(newChild);
      }

      return super.replaceChild(newChild, oldChild);
   }

   public String write(Writer writer, boolean pretty) {
      try
      {
         writer.write("<");
         writer.write(getParentElement().getPrefix()+":Body");

         // namespaces
         Iterator nsPrefixes = getNamespacePrefixes();
         while(nsPrefixes.hasNext())
         {
            String prefix = (String)nsPrefixes.next();
            writer.write(" xmlns:"+prefix+"='"+getNamespaceURI(prefix)+"'");
         }

         // attributes
         Iterator attNames = getAllAttributes();
         while(attNames.hasNext())
         {
            NameImpl name = (NameImpl)attNames.next();
            String attPrefix = name.getPrefix()!=null ? name.getPrefix():"";
            String attFqn = attPrefix.length()>0 ? attPrefix+":"+name.getLocalName() : name.getLocalName();
            writer.write(" "+attFqn);
            writer.write("='"+getAttributeValue(name)+"'");
         }

         writer.write(">");

         if(pretty)
            writer.write("\n");

         return ("</"+getParentElement().getPrefix()+":Body>");
      }
      catch (IOException e)
      {
         throw new WSException(e.getMessage());
      }
   }
}