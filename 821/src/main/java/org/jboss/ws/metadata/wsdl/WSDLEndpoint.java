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

import org.jboss.ws.WSException;

/**
 * An Endpoint component defines the particulars of a specific endpoint at which a given service is available.
 * Endpoint components are local to a given Service component; they cannot be referred to by QName.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-Oct-2004
 */
public class WSDLEndpoint extends Extendable 
{
   private static final long serialVersionUID = 4991302339046047865L;

   // The parent service
   private WSDLService wsdlService;

   /** The REQUIRED name attribute information item together with the targetNamespace attribute information item
    * of the definitions element information item forms the QName of the endpoint. */
   private NCName name;
   
   /** Derived QName identifier. 
    */
   private QName qname;

   /** The REQUIRED binding attribute information item refers, by QName, to a Binding component */
   private QName binding;

   /** The OPTIONAL address attribute information item specifies the address of the endpoint. */
   private String address;

   public WSDLEndpoint(WSDLService wsdlService)
   {
      this.wsdlService = wsdlService;
   }
   
   public WSDLService getWsdlService()
   {
      return wsdlService;
   }

   /** Get the WSDLInteraface associated to this endpoint
    *
    * @return A WSDLInterface or null
    */
   public WSDLInterface getInterface()
   {
      WSDLInterface wsdlInterface = null;

      WSDLDefinitions wsdlDefinitions = wsdlService.getWsdlDefinitions();
      if (wsdlService.getInterfaceName() != null)
      {
         QName qname = wsdlService.getInterfaceName();
         wsdlInterface = wsdlDefinitions.getInterface(new NCName(qname));
      }
      else
      {
         WSDLBinding wsdlBinding = wsdlDefinitions.getBinding(new NCName(binding));
         if (wsdlBinding == null)
            throw new WSException("Cannot obtain the binding: " + binding);

         if (wsdlBinding.getInterfaceName() != null)
         {
            QName qname = wsdlBinding.getInterfaceName();
            wsdlInterface = wsdlDefinitions.getInterface(new NCName(qname));
         }
      }

      if (wsdlInterface == null)
         throw new WSException("Cannot obtain the interface associated with this endpoint: " + name);

      return wsdlInterface;
   }

   /** Get the WSDLInterafceOperation for the given opName
    *
    * @param opName the operation name
    * @return A WSDLOperation or null
    */
   public WSDLInterfaceOperation getInterfaceOperation(NCName opName)
   {
      WSDLInterface wsdlInterface = getInterface();
      WSDLInterfaceOperation wsdlInterfaceOperation = wsdlInterface.getWSDLInterfaceOperation(opName);
      return wsdlInterfaceOperation;
   }

   public NCName getName()
   {
      return name;
   }

   public void setName(NCName name)
   {
      this.name = name;
   }

   public QName getQName()
   {
      if (qname == null)
      {
         String tnsURI = wsdlService.getWsdlDefinitions().getTargetNamespace();
         qname = new QName(tnsURI, name.toString());
      }
      return qname;
   }

   
   public void setQName(QName qname)
   {
      this.qname = qname;
   }

   public QName getBinding()
   {
      return binding;
   }

   public void setBinding(QName binding)
   {
      this.binding = binding;
   }

   public String getAddress()
   {
      return address;
   }

   public void setAddress(String address)
   {
      this.address = address;
   }
}
