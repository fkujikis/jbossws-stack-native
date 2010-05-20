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
package org.jboss.ws.metadata.wsdl;

// $Id$

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;

/**
 * A Message Reference component associates a defined type with a message exchanged in an operation. By
 * default, the type system is based upon the XML Infoset
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-Oct-2004
 */
public abstract class WSDLInterfaceOperationPart extends Extendable implements Comparable
{
   // provide logging
   protected Logger log = Logger.getLogger(getClass());

   // The parent interface operation
   private WSDLInterfaceOperation wsdlOperation;

   /** The OPTIONAL messageLabel attribute information item identifies the role of this message in the message
    * exchange pattern of the given operation element information item.
    */
   private NCName messageLabel;

   /** The OPTIONAL element attribute information item is the element declaration from the {element declarations}
    * property resolved by the value of the element attribute information item, otherwise empty.
    */
   private QName element;

   public WSDLInterfaceOperationPart(WSDLInterfaceOperation wsdlOperation)
   {
      log.trace("New part for wsdlOperation: " + wsdlOperation.getName());
      this.wsdlOperation = wsdlOperation;
   }

   public WSDLInterfaceOperation getWsdlOperation()
   {
      return wsdlOperation;
   }

   public NCName getMessageLabel()
   {
      return messageLabel;
   }

   public void setMessageLabel(NCName messageLabel)
   {
      log.trace("setMessageLabel: " + messageLabel);
      this.messageLabel = messageLabel;
   }

   public QName getElement()
   {
      return element;
   }

   public void setElement(QName element)
   {
      log.trace("setElement: " + element);
      this.element = element;
   }

   /** Get the xmlType for this operation part.
    */
   public QName getXMLType()
   {
      QName xmlType = null;

      // First try to read it from the schema
      WSDLDefinitions wsdlDefinitions = wsdlOperation.getWsdlInterface().getWsdlDefinitions();
      WSDLTypes wsdlTypes = wsdlDefinitions.getWsdlTypes();
      xmlType = wsdlTypes.getXMLType(element);

      // Fall back to the property
      if (xmlType == null)
      {
         WSDLProperty property = getProperty(Constants.WSDL_PROPERTY_RPC_XMLTYPE);
         if (property != null)
         {
            String qnameRef = property.getValue();
            int colIndex = qnameRef.indexOf(':');
            String prefix = qnameRef.substring(0, colIndex);
            String localPart = qnameRef.substring(colIndex + 1);
            String nsURI = wsdlDefinitions.getNamespaceURI(prefix);
            xmlType = new QName(nsURI, localPart, prefix);
         }
      }

      if (xmlType == null)
         throw new WSException("Cannot obtain xmlType for element: " + element);

      return xmlType;
   }

   public int compareTo(Object o)
   {
      int c = -1;
      if (o instanceof WSDLInterfaceOperationPart)
      {
         WSDLInterfaceOperationPart w = (WSDLInterfaceOperationPart)o;
         String oname = w.getElement().getLocalPart();
         String myname = getElement().getLocalPart();
         c = myname.compareTo(oname);
      }
      return c;
   }
}