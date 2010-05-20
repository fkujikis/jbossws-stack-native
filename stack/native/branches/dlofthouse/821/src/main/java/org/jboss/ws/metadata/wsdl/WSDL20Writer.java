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

import org.jboss.logging.Logger;

/**
 * A WSDL writer that writes out a WSDL-2.0 compliant wsdl.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Anil.Saldhana@jboss.org
 * @since 10-Oct-2004
 */
public class WSDL20Writer extends WSDLWriter
{
   // provide logging
   protected static final Logger log = Logger.getLogger(WSDL20Writer.class);

   protected WSDLUtils utils = WSDLUtils.getInstance();

   /** Use WSDLDefinitions.writeWSDL instead. */
   public WSDL20Writer(WSDLDefinitions wsdl)
   {
      super(wsdl);
   }


   protected void appendInterfaces(StringBuilder buffer)
   {
      WSDLInterface[] interfaces = wsdl.getInterfaces();
      for (int i = 0; i < interfaces.length; i++)
      {
         WSDLInterface intf = interfaces[i];
         buffer.append("<interface name='" + intf.getName() + "'>");
         appendInterfaceFaults(buffer, intf);
         appendInterfaceOperations(buffer, intf);
         buffer.append("</interface>");
      }
   }

   private void appendInterfaceFaults(StringBuilder buffer, WSDLInterface intf)
   {
      WSDLInterfaceFault[] faults = intf.getFaults();
      for (int i = 0; i < faults.length; i++)
      {
         WSDLInterfaceFault fault = faults[i];
         buffer.append("<fault name='" + fault.getName() + "'");
         buffer.append(" element='" + getQNameRef(fault.getXmlName()) + "'>");
         if (fault.getDocumentation() != null)
         {
            buffer.append(fault.getDocumentation());
         }
         buffer.append("</fault>");
      }
   }

   private void appendInterfaceOperations(StringBuilder buffer, WSDLInterface intf)
   {
      WSDLInterfaceOperation[] operations = intf.getOperations();
      for (int i = 0; i < operations.length; i++)
      {
         WSDLInterfaceOperation operation = operations[i];
         buffer.append("<operation name='" + operation.getName() + "'>");

         WSDLInterfaceOperationInput[] inputs = operation.getInputs();
         for (int j = 0; j < inputs.length; j++)
         {
            WSDLInterfaceOperationInput ip = inputs[j];
            buffer.append("<input element='" + getQNameRef(ip.getElement()) + "'");
            if (ip.getMessageLabel() != null)
            {
               buffer.append(" messageLabel='" + ip.getMessageLabel() + "'");
            }
            buffer.append(">");
            appendProperties(buffer, ip.getProperties());
            buffer.append("</input>");
         }

         WSDLInterfaceOperationOutput[] outputs = operation.getOutputs();
         for (int j = 0; j < outputs.length; j++)
         {
            WSDLInterfaceOperationOutput op = outputs[j];
            buffer.append("<output element='" + getQNameRef(op.getElement()) + "'");
            if (op.getMessageLabel() != null)
            {
               buffer.append(" messageLabel='" + op.getMessageLabel() + "'");
            }
            buffer.append(">");
            appendProperties(buffer, op.getProperties());
            buffer.append("</output>");
         }

         WSDLInterfaceOperationOutfault[] outfaults = operation.getOutfaults();
         for (int j = 0; j < outfaults.length; j++)
         {
            WSDLInterfaceOperationOutfault outfault = outfaults[j];
            buffer.append("<outfault ref='" + getQNameRef(outfault.getRef()) + "'>");
            if (outfault.getMessageLabel() != null)
            {
               buffer.append(" messageLabel='" + outfault.getMessageLabel() + "'");
            }
            buffer.append("</outfault>");
         }
         buffer.append("</operation>");
      }
   }

   private void appendProperties(StringBuilder buffer, WSDLProperty[] wsdlProperties)
   {
      for (int i = 0; i < wsdlProperties.length; i++)
      {
         WSDLProperty wsdlProperty = wsdlProperties[i];
         String uri = wsdlProperty.getURI();
         String value = wsdlProperty.getValue();
         buffer.append("<property uri='" + uri + "'>" + (value != null ? "<value>" + value + "</value>" : "") + "</property>");
      }
   }

   protected void appendBindings(StringBuilder buffer)
   {
      WSDLBinding[] bindings = wsdl.getBindings();
      for (int i = 0; i < bindings.length; i++)
      {
         WSDLBinding binding = bindings[i];
         buffer.append("<binding name='" + binding.getName() + "'");
         if (binding.getInterfaceName() != null)
         {
            buffer.append(" interface='" + getQNameRef(binding.getInterfaceName()) + "'");
         }
         buffer.append(">");
         appendBindingOperations(buffer, binding);
         buffer.append("</binding>");
      }
   }

   protected void appendBindingOperations(StringBuilder buffer, WSDLBinding binding)
   {
      WSDLBindingOperation[] operations = binding.getOperations();
      for (int i = 0; i < operations.length; i++)
      {
         WSDLBindingOperation operation = operations[i];
         buffer.append("<operation ref='" + getQNameRef(operation.getRef()) + "'>");
         buffer.append("</operation>");
      }
   }

   protected void appendServices(StringBuilder buffer)
   {
      WSDLService[] services = wsdl.getServices();
      for (int i = 0; i < services.length; i++)
      {
         WSDLService service = services[i];
         buffer.append("<service name='" + service.getName() + "'");
         if (service.getInterfaceName() != null)
         {
            buffer.append(" interface='" + getQNameRef(service.getInterfaceName()) + "'");
         }
         buffer.append(">");

         WSDLEndpoint[] endpoints = service.getEndpoints();
         for (int j = 0; j < endpoints.length; j++)
         {
            WSDLEndpoint endpoint = endpoints[j];
            buffer.append("<endpoint name='" + endpoint.getName() + "'");
            if (endpoint.getBinding() != null)
            {
               buffer.append(" binding='" + getQNameRef(endpoint.getBinding()) + "'");
            }
            if (endpoint.getAddress() != null)
            {
               buffer.append(" address='" + endpoint.getAddress() + "'");
            }
            buffer.append("></endpoint>");
         }

         buffer.append("</service>");
      }
   }
}
