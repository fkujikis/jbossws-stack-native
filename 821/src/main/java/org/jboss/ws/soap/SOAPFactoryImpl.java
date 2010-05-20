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

import javax.xml.soap.Detail;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;

import org.jboss.logging.Logger;
import org.jboss.util.xml.DOMUtils;
import org.jboss.ws.Constants;
import org.jboss.ws.xop.XOPContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * SOAPFactory implementation.
 *
 * @author Thomas.Diesler@jboss.org
 */
public class SOAPFactoryImpl extends SOAPFactory
{
   private static Logger log = Logger.getLogger(SOAPFactoryImpl.class);

   public SOAPElement createElement(Name name) throws SOAPException
   {
      return new SOAPElementImpl(name);
   }

   public SOAPElement createElement(String localName) throws SOAPException
   {
      return new SOAPElementImpl(localName);
   }

   public SOAPElement createElement(String localName, String prefix, String uri) throws SOAPException
   {
      return new SOAPElementImpl(localName, prefix, uri);
   }

   /**
    * Create a SOAPElement from a DOM Element.
    * This method is not part of the javax.xml.soap.SOAPFactory interface.
    */
   public SOAPElementImpl createElement(Element domNode, boolean deep) throws SOAPException
   {
      if (domNode == null)
         throw new IllegalArgumentException("Source node cannot be null");
      
      String localName = domNode.getLocalName();
      String prefix = domNode.getPrefix() != null ? domNode.getPrefix() : "";
      String nsURI = domNode.getNamespaceURI() != null ? domNode.getNamespaceURI() : "";
      
      SOAPElementImpl soapElement = new SOAPElementImpl(localName, prefix, nsURI);

      // Add the child elements as well
      if (deep)
      {
         if (domNode instanceof Element)
            DOMUtils.copyAttributes(soapElement, (Element)domNode);
         
         NodeList nlist = domNode.getChildNodes();
         for (int i = 0; i < nlist.getLength(); i++)
         {
            Node child = nlist.item(i);
            short nodeType = child.getNodeType();
            if (nodeType == Node.ELEMENT_NODE)
            {
               SOAPElementImpl soapChild = createElement((Element)child, true);
               soapElement.addChildElement(soapChild);
            }
            else if (nodeType == Node.TEXT_NODE)
            {
               String nodeValue = child.getNodeValue();
               soapElement.addTextNode(nodeValue);
            }
            else if (nodeType == Node.CDATA_SECTION_NODE)
            {
               String nodeValue = child.getNodeValue();
               soapElement.addTextNode(nodeValue);
            }
            else
            {
               log.trace("Ignore child type: " + nodeType);
            }
         }
      }

      return soapElement;
   }

   public Detail createDetail() throws SOAPException
   {
      return new DetailImpl();
   }

   public Name createName(String localName, String prefix, String uri) throws SOAPException
   {
      return new NameImpl(localName, prefix, uri);
   }

   public Name createName(String localName) throws SOAPException
   {
      return new NameImpl(localName);
   }
}
