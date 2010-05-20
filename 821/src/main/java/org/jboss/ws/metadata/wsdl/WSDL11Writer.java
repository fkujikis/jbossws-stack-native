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

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import org.jboss.util.xml.DOMUtils;
import org.jboss.util.xml.DOMWriter;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.w3c.dom.Element;

/**
 * A WSDL Writer that writes a WSDL 1.1 file. It works off
 * of the WSDL20 Object Graph.
 * @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 */
public class WSDL11Writer extends WSDLWriter
{
   //Used Internally
   private String wsdlStyle = Constants.RPC_LITERAL;

   // Used to prevent duplicates
   private HashSet<String> writtenFaultMessages = new HashSet<String>();

   /** Use WSDLDefinitions.writeWSDL instead. */
   public WSDL11Writer(WSDLDefinitions wsdl)
   {
      super(wsdl);
   }

   public void write(Writer writer) throws IOException
   {
      write(writer, null);
   }

   public void write(Writer writer, String charset) throws IOException
   {


      // Write out the wsdl-1.1 represention (only path to obtain is from WSDL11Reader)
      if (wsdl.getWsdlOneOneDefinition() != null)
      {
        Definition wsdlDefinition = wsdl.getWsdlOneOneDefinition();
        try
        {
           WSDLFactory wsdlFactory = WSDLFactory.newInstance();
           javax.wsdl.xml.WSDLWriter wsdlWriter = wsdlFactory.newWSDLWriter();
           wsdlWriter.writeWSDL(wsdlDefinition, writer);
        }
        catch (WSDLException e)
        {
           this.logException(e);
           throw new IOException(e.toString());
        }
      }
      else
      {
         StringBuilder buffer = new StringBuilder();

         //Detect the WSDL Style early
         wsdlStyle = utils.getWSDLStyle(wsdl);

         writtenFaultMessages.clear();

         appendDefinitions(buffer);
         appendTypes(buffer);
         appendMessages(buffer);
         appendPortTypes(buffer);
         appendBindings(buffer);
         appendServices(buffer);

         buffer.append("</definitions>");
         Element element = DOMUtils.parse(buffer.toString());

         if (charset != null)
            writer.write("<?xml version=\"1.0\" encoding=\"" + charset + "\"?>\n");
         new DOMWriter(writer).setPrettyprint(true).print(element);
      }
   }

   protected void appendMessages(StringBuilder buffer)
   {
      WSDLInterface[] interfaces = wsdl.getInterfaces();
      int len = interfaces != null ? interfaces.length : 0;
      for (int i = 0; i < len; i++)
      {
         WSDLInterface intf = interfaces[i];
         WSDLInterfaceOperation[] operations = intf.getSortedOperations();
         int lenOps = operations.length;
         for (int j = 0; j < lenOps; j++)
         {
            appendMessage(buffer, operations[j]);
            appendMessagesForExceptions(buffer, operations[j]);
         }//end for
      }//end for
   }

   private void appendMessage(StringBuilder buffer, WSDLInterfaceOperation operation)
   {
      String opname = operation.getName().toString();
      //Determine the style of the wsdl
      if (Constants.URI_STYLE_RPC.equals(operation.getStyle()) == false)
         wsdlStyle = Constants.DOCUMENT_LITERAL; //Not RPC/Literal

      String interfaceName = operation.getWsdlInterface().getName().toString();
      buffer.append("<message name='" + interfaceName + "_" + opname + "' >");
      WSDLInterfaceOperationInput[] inputs = operation.getInputs();
      int lenin = inputs.length;
      for (int i = 0; i < lenin; i++)
      {
         WSDLInterfaceOperationInput input = inputs[i];
         appendInputParts(buffer, input, i);
      }

      buffer.append("</message>");

      //Now the return type
      WSDLInterfaceOperationOutput[] outputs = operation.getOutputs();
      int lenout = outputs != null ? outputs.length : 0;

      if (Constants.WSDL20_PATTERN_IN_ONLY.equals(operation.getPattern()))
         return;

      buffer.append("<message name='" + interfaceName + "_" + opname + "Response' >");
      if (lenout > 0)
      {
         //Since the outputs have been obtained from a linkedhashmap
         //they will be in the order of insertion i.e. in case of
         //holders, the holder types will be inserted first. So let
         //us iterate in the reverse order
         String str = "";
         for (int i = 0; i < lenout; i++)
         {
            WSDLInterfaceOperationOutput out = outputs[i];
            str += appendOutputParts(out, i);
         }
         buffer.append(str);
      }//end if
      buffer.append("</message>");
   }

   private void appendMessagesForExceptions(StringBuilder buffer, WSDLInterfaceOperation operation)
   {
      //Get the outfaults
      WSDLInterfaceOperationOutfault[] faults = operation.getOutfaults();
      int len = faults != null ? faults.length : 0;

      for (int i = 0; i < len; i++)
      {
         WSDLInterfaceOperationOutfault fault = faults[i];
         QName xmlName = fault.getRef();
         String exceptionName = xmlName.getLocalPart();
         if (writtenFaultMessages.contains(exceptionName))
            continue;

         buffer.append("<message name='" + exceptionName + "' >");
         String prefix = wsdl.getPrefix(xmlName.getNamespaceURI());
         String xmlNameStr = prefix + ":" + xmlName.getLocalPart();
         buffer.append("<part name='" + exceptionName + "' element='" + xmlNameStr + "' />");
         buffer.append("</message>");

         writtenFaultMessages.add(exceptionName);
      }
   }

   private void appendInputParts(StringBuilder buffer, WSDLInterfaceOperationInput input, int index)
   {
      boolean header = input.getProperty(Constants.WSDL_PROPERTY_APPLICATION_DATA) != null;
      if (wsdlStyle.equals(Constants.RPC_LITERAL) && !header)
      {
         QName el = input.getElement();
         QName xmlType = input.getXMLType();
         String prefix = wsdl.getPrefix(xmlType.getNamespaceURI() );
         buffer.append("<part name='" + el.getLocalPart() + "'");
         buffer.append(" type='" + prefix + ":" + xmlType.getLocalPart() + "'>");
         buffer.append("</part>");
      }
      //Doc-literal case
      else
      {
         //TODO:Handle this better later
         //In the case of doc/lit, the input element will give the partname
         QName elm = input.getElement();
         buffer.append("<part name='").append(header ? elm.getLocalPart() : "parameters").append("'");
         String part = wsdl.getPrefix(elm.getNamespaceURI()) + ":" + elm.getLocalPart();
         buffer.append(" element='" + part + "'>");
         buffer.append("</part>");
      }
   }

   private String appendOutputParts(WSDLInterfaceOperationOutput out, int index)
   {
      boolean header = out.getProperty(Constants.WSDL_PROPERTY_APPLICATION_DATA) != null;
      StringBuilder buf = new StringBuilder("");
      if (wsdlStyle.equals(Constants.RPC_LITERAL) && !header)
      {
         //Get the XMLName
         QName xmlName = out.getElement();
         QName xmlType = out.getXMLType();
         String prefix = wsdl.getPrefix(xmlType.getNamespaceURI() );

         buf.append("<part name='" + xmlName.getLocalPart() + "'");
         buf.append(" type='" + prefix + ":" + xmlType.getLocalPart() + "'>");
         buf.append("</part>");
      }
      else
      {
         QName elm = out.getElement();
         buf.append("<part name='").append(header ? elm.getLocalPart() : Constants.DEFAULT_RPC_RETURN_NAME).append("'");
         String value = wsdl.getPrefix(elm.getNamespaceURI()) + ":" + elm.getLocalPart();
         //String value = this.getXMLTypeFromWSDLProperty(out);
         buf.append(" element='" + value + "'>");
         buf.append("</part>");
      }
      return buf.toString();
   }

   protected void appendInterfaces(StringBuilder buffer)
   {
   }

   protected void appendPortTypes(StringBuilder buffer)
   {
      WSDLInterface[] intfs = wsdl.getInterfaces();
      for (int i = 0; i < intfs.length; i++)
      {
         WSDLInterface intf = intfs[i];
         if (i == 0)
            buffer.append("<portType name='" + intf.getName() + "'>");
         appendPortOperations(buffer, intf);
      }
      buffer.append("</portType>");
   }

   protected void appendPortOperations(StringBuilder buffer, WSDLInterface intf)
   {
      String targetPrefix = wsdl.getTargetPrefix();
      WSDLInterfaceOperation[] operations = intf.getSortedOperations();
      for (int i = 0; i < operations.length; i++)
      {
         WSDLInterfaceOperation operation = operations[i];
         WSDLInterfaceOperationInput[] inputs = operation.getInputs();
         WSDLInterfaceOperationOutput[] outputs = operation.getOutputs();
         buffer.append("<operation name='" + operation.getName().toString() + "'");

         if (wsdlStyle.equals(Constants.RPC_LITERAL))
         {
            StringBuilder order = new StringBuilder();
            Set<QName> inParameters = new HashSet<QName>();
            for (int j = 0; j < inputs.length; j++)
            {
               WSDLInterfaceOperationInput input = inputs[j];
               if (order.length() > 0)
                  order.append(" ");
               QName el = input.getElement();
               order.append(el.getLocalPart());
               inParameters.add(el);
            }

            for (int j = 0; j < outputs.length; j++)
            {
               WSDLInterfaceOperationOutput output = outputs[j];
               // The return value is ommitted as a hint to other parameter based binding layers.
               // Also, INOUT parameters need to only appear once
               if (output.getProperty(Constants.WSDL_PROPERTY_RETURN_PART) == null)
               {
                  QName el = output.getElement();
                  if (! inParameters.contains(el))
                  {
                     if (order.length() > 0)
                        order.append(" ");
                     order.append(el.getLocalPart());
                  }
               }
            }

            if (order.length() > 0)
               buffer.append(" parameterOrder='").append(order.toString()).append("'");
         }

         buffer.append(">"); //End operation element

         String opname = operation.getName().toString();
         String interfaceName = operation.getWsdlInterface().getName().toString();
         String msgEl = targetPrefix + ":" + interfaceName + "_" + opname;

         buffer.append("<input message='" + msgEl + "'>").append("</input>");

         if (! Constants.WSDL20_PATTERN_IN_ONLY.equals(operation.getPattern()))
         {
            buffer.append("<output message='" + msgEl + "Response'>");
            buffer.append("</output>");
         }

         //Append the Faults
         WSDLInterfaceOperationOutfault[] faults = operation.getOutfaults();
         //WSDLInterfaceFault[] faults = intf.getFaults();
         int lenf = faults != null ? faults.length : 0;
         for (int k = 0; k < lenf; k++)
         {
            //WSDLInterfaceFault flt = faults[k];
            WSDLInterfaceOperationOutfault flt = faults[k];
            QName elt = flt.getRef();

            String targetNS = wsdl.getTargetNamespace();
            //Writing the fault, the prefix should always be the one for wsdl target namespace
            elt = new QName(targetNS, elt.getLocalPart(), wsdl.getPrefix(targetNS));
            //Remove Fault or Error appended to the name
            String cleanname = utils.chop(utils.chop(elt.getLocalPart(), "Error"), "Fault");

            String n = "name='" + cleanname + "'";

            String cleanref = utils.chop(utils.getFormattedString(elt), "Error");
            buffer.append("<fault  message='" + cleanref + "' " + n + ">");
            buffer.append("</fault>");
         }

         buffer.append("</operation>");
      }
   }

   protected void appendBindings(StringBuilder buffer)
   {
      WSDLBinding[] bindings = wsdl.getBindings();
      for (int i = 0; i < bindings.length; i++)
      {
         WSDLBinding binding = bindings[i];
         buffer.append("<binding name='" + binding.getName() + "' type='" + getQNameRef(binding.getInterfaceName()) + "'>");
         //TODO:Need to derive the WSDLStyle from the Style attribute of InterfaceOperation
         if (wsdlStyle == null)
            throw new IllegalArgumentException("WSDL Style is null (should be rpc or document");
         String style = "rpc";
         if (wsdlStyle.equals(Constants.DOCUMENT_LITERAL))
            style = "document";
         buffer.append("<soap:binding transport='http://schemas.xmlsoap.org/soap/http' style='" + style + "'/>");
         appendBindingOperations(buffer, binding);
         buffer.append("</binding>");
      }
   }

   private boolean isHeaderInput(WSDLBindingOperationInput input)
   {
      WSDLBindingOperation operation = input.getWsdlBindingOperation();
      WSDLBinding binding = operation.getWsdlBinding();
      WSDLInterface wsdlInterface = binding.getInterface();
      if (wsdlInterface == null)
         return false;

      WSDLInterfaceOperation interfaceOperation = wsdlInterface.getOperation(new NCName(operation.getRef().getLocalPart()));
      if (interfaceOperation == null)
         return false;

      WSDLInterfaceOperationInput interfaceInput = interfaceOperation.getInputByPartName(input.getMessageLabel().toString());
      if (interfaceInput == null)
         return false;

      if (interfaceInput.getProperty(Constants.WSDL_PROPERTY_APPLICATION_DATA) == null)
         return false;
      else
         return true;
   }

   private boolean isHeaderOutput(WSDLBindingOperationOutput output)
   {
      WSDLBindingOperation operation = output.getWsdlBindingOperation();
      WSDLBinding binding = operation.getWsdlBinding();
      WSDLInterface wsdlInterface = binding.getInterface();
      if (wsdlInterface == null)
         return false;

      WSDLInterfaceOperation interfaceOperation = wsdlInterface.getOperation(new NCName(operation.getRef().getLocalPart()));
      if (interfaceOperation == null)
         return false;

      WSDLInterfaceOperationOutput interfaceOutput = interfaceOperation.getOutputByPartName(output.getMessageLabel().toString());
      if (interfaceOutput == null)
         return false;

      if (interfaceOutput.getProperty(Constants.WSDL_PROPERTY_APPLICATION_DATA) == null)
         return false;
      else
         return true;
   }

   protected void appendBindingOperations(StringBuilder buffer, WSDLBinding binding)
   {
      WSDLBindingOperation[] operations = binding.getOperations();
      Arrays.sort(operations);

      String tns = wsdl.getTargetNamespace();
      for (int i = 0; i < operations.length; i++)
      {
         WSDLBindingOperation operation = operations[i];
         String interfaceName = operation.getWsdlBinding().getInterfaceName().getLocalPart();

         buffer.append("<operation name='" + operation.getRef().getLocalPart() + "'>");
         String soapAction = (operation.getSOAPAction() != null ? operation.getSOAPAction() : "");
         buffer.append("<soap:operation soapAction=\"" + soapAction + "\"/>");
         buffer.append("<input>");

         StringBuilder bodyParts = new StringBuilder();
         boolean hasHeader = false;
         WSDLBindingOperationInput[] inputs = operation.getInputs();
         for (int j = 0; j < inputs.length; j++)
         {
            WSDLBindingOperationInput input = inputs[j];
            if (isHeaderInput(input))
            {
               String messageName = interfaceName + "_" + input.getWsdlBindingOperation().getRef().getLocalPart();
               buffer.append("<soap:header use='literal' message='tns:" + messageName + "' part='" + input.getMessageLabel() + "'/>");
               hasHeader = true;
            }
            else
            {
               if (bodyParts.length() > 0)
                  bodyParts.append(" ");
               bodyParts.append(input.getMessageLabel());
            }
         }

         buffer.append("<soap:body use='literal'");
         if (hasHeader)
            buffer.append(" parts='").append(bodyParts.toString()).append("'");
         if (wsdlStyle != Constants.DOCUMENT_LITERAL)
            buffer.append(" namespace='" + tns + "'");
         buffer.append("/>");

         buffer.append("</input>");

         if (! Constants.WSDL20_PATTERN_IN_ONLY.equals(getBindingOperationPattern(operation)))
         {
            buffer.append("<output>");
            bodyParts = new StringBuilder();
            hasHeader = false;
            WSDLBindingOperationOutput[] outputs = operation.getOutputs();
            for (int j = 0; j < outputs.length; j++)
            {
               WSDLBindingOperationOutput output = outputs[j];
               if (isHeaderOutput(output))
               {
                  String messageName = interfaceName + "_" + output.getWsdlBindingOperation().getRef().getLocalPart() + "Response";

                  buffer.append("<soap:header use='literal' message='tns:" + messageName + "' part='" + output.getMessageLabel() + "'/>");
                  hasHeader = true;
               }
               else
               {
                  if (bodyParts.length() > 0)
                     bodyParts.append(" ");
                  bodyParts.append(output.getMessageLabel());
               }
            }

            buffer.append("<soap:body use='literal'");
            if (hasHeader)
               buffer.append(" parts='").append(bodyParts.toString()).append("'");
            if (wsdlStyle != Constants.DOCUMENT_LITERAL)
               buffer.append(" namespace='" + tns + "'");
            buffer.append("/>");

            buffer.append("</output>");
         }

         //Append faults
         QName intfname = operation.getWsdlBinding().getInterfaceName();
         WSDLInterface intf = wsdl.getInterface(new NCName(intfname.getLocalPart()));
         if (intf == null)
            throw new WSException("WSDL Interface should not be null");
         WSDLInterfaceOperation interfaceOperation = intf.getOperation(new NCName(operation.getRef().getLocalPart()));
         WSDLInterfaceOperationOutfault[] faults = interfaceOperation.getOutfaults();
         int lenf = faults != null ? faults.length : 0;
         for (int k = 0; k < lenf; k++)
         {
            WSDLInterfaceOperationOutfault flt = faults[k];
            //Remove Fault or Error appended to the name
            String cleanname = utils.chop(utils.chop(flt.getRef().getLocalPart(), "Error"), "Fault");
            String n = "name='" + cleanname + "'";

            //String cleanref = utils.chop(utils.getFormattedString(elt), "Error");
            buffer.append("<fault  " + n + ">");
            buffer.append("<soap:fault  " + n + " use='literal' />");
            buffer.append("</fault>");
         }

         buffer.append("</operation>");
      }
   }

   private String getBindingOperationPattern(WSDLBindingOperation operation)
   {
      WSDLBinding binding = operation.getWsdlBinding();
      String pattern = binding.getInterface().getOperation(new NCName(operation.getRef().getLocalPart())).getPattern();

      return pattern;
   }

   protected void appendServices(StringBuilder buffer)
   {
      WSDLService[] services = wsdl.getServices();
      int len = services.length;
      for (int i = 0; i < len; i++)
      {

         WSDLService service = services[i];
         buffer.append("<service name='" + service.getName() + "'>");
         WSDLEndpoint[] endpoints = service.getEndpoints();
         int lenend = endpoints.length;
         for (int j = 0; j < lenend; j++)
         {
            WSDLEndpoint endpoint = endpoints[j];
            appendServicePort(buffer, endpoint);
         }

         buffer.append("</service>");
      }
   }

   protected void appendServicePort(StringBuilder buffer, WSDLEndpoint endpoint)
   {
      String name = endpoint.getName().toString();
      QName endpointBinding = endpoint.getBinding();
      String prefix = endpointBinding.getPrefix();
      if (prefix == null || prefix == "")
         prefix = wsdl.getTargetPrefix();
      String ebname = prefix + ":" + endpointBinding.getLocalPart();
      buffer.append("<port name='" + name + "' binding='" + ebname + "'>");
      buffer.append("<soap:address location='" + endpoint.getAddress() + "'/>");
      buffer.append("</port>");
   }

   private String getXMLTypeFromWSDLProperty(WSDLInterfaceOperationOutput wiout)
   {
      String value = "";
      WSDLProperty outprop = wiout.getProperty(Constants.WSDL_PROPERTY_RPC_XMLTYPE);
      if (outprop != null)
         value = outprop.getValue();
      return value;
   }

}
