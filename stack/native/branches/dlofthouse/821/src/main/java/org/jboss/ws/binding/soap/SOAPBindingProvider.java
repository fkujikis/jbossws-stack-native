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
package org.jboss.ws.binding.soap;

// $Id$

import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSTypeDefinition;
import org.jboss.logging.Logger;
import org.jboss.util.xml.DOMUtils;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.binding.BindingException;
import org.jboss.ws.binding.BindingProvider;
import org.jboss.ws.binding.EndpointInvocation;
import org.jboss.ws.binding.UnboundHeader;
import org.jboss.ws.jaxrpc.ParameterWrapping;
import org.jboss.ws.jaxrpc.SOAPFaultExceptionHelper;
import org.jboss.ws.jaxrpc.Style;
import org.jboss.ws.metadata.OperationMetaData;
import org.jboss.ws.metadata.ParameterMetaData;
import org.jboss.ws.metadata.TypesMetaData;
import org.jboss.ws.soap.*;
import org.jboss.ws.soap.attachment.AttachmentPartImpl;
import org.jboss.ws.soap.attachment.CIDGenerator;
import org.jboss.ws.utils.JavaUtils;
import org.jboss.ws.utils.MimeUtils;
import org.jboss.ws.utils.ThreadLocalAssociation;
import org.jboss.xb.binding.NamespaceRegistry;
import org.w3c.dom.Element;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** An abstract BindingProvider for SOAP that is independent of the SOAP version.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 16-Oct-2004
 */
public abstract class SOAPBindingProvider implements BindingProvider
{
   // provide logging
   private static final Logger log = Logger.getLogger(SOAPBindingProvider.class);

   /** Create the SOAPMessage */
   protected abstract SOAPMessage createMessage(OperationMetaData opMetaData) throws SOAPException;

   /** On the client side, generate the payload from IN parameters. */
   public SOAPMessage bindRequestMessage(OperationMetaData opMetaData, EndpointInvocation epInv, Map<QName, UnboundHeader> unboundHeaders) throws BindingException
   {
      log.debug("bindRequestMessage: " + opMetaData.getXmlName());

      try
      {
         // disable DOMExpansion
         ThreadLocalAssociation.localDomExpansion().set(Boolean.FALSE);
         SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
         if (msgContext == null)
            throw new WSException("MessageContext not available");

         // Associate current message with message context
         SOAPMessageImpl reqMessage = (SOAPMessageImpl)createMessage(opMetaData);
         msgContext.setMessage(reqMessage);

         SOAPEnvelopeImpl soapEnvelope = (SOAPEnvelopeImpl)reqMessage.getSOAPPart().getEnvelope();         
         SOAPBody soapBody = soapEnvelope.getBody();
         SOAPHeader soapHeader = soapEnvelope.getHeader();

         // Get the namespace registry
         NamespaceRegistry namespaceRegistry = msgContext.getNamespaceRegistry();

         Style style = opMetaData.getStyle();
         if (style == Style.RPC)
         {
            QName opQName = opMetaData.getXmlName();
            Name opName = new NameImpl(namespaceRegistry.registerQName(opQName));

            log.debug("Create RPC body element: " + opName);
            SOAPBodyElement soapBodyElement = new SOAPBodyElementRpc(opName);

            soapBodyElement = (SOAPBodyElement)soapBody.addChildElement(soapBodyElement);

            for (ParameterMetaData paramMetaData : opMetaData.getInputParameters())
            {
               QName xmlName = paramMetaData.getXmlName();
               Object value = epInv.getRequestParamValue(xmlName);

               if (paramMetaData.isSwA())
               {
                  CIDGenerator cidGenerator = reqMessage.getCidGenerator();
                  AttachmentPart part = createAttachmentPart(paramMetaData, value, cidGenerator);
                  reqMessage.addAttachmentPart(part);
               }
               else
               {
                  SOAPElement soapElement = paramMetaData.isInHeader() ? (SOAPElement)soapHeader : soapBodyElement;
                  SOAPContentElement contentElement = addParameterToMessage(paramMetaData, value, soapElement);
               }
            }
         }
         else if (style == Style.DOCUMENT)
         {
            for (ParameterMetaData paramMetaData : opMetaData.getInputParameters())
            {
               QName xmlName = paramMetaData.getXmlName();
               Object value = epInv.getRequestParamValue(xmlName);

               if (paramMetaData.isSwA())
               {
                  CIDGenerator cidGenerator = reqMessage.getCidGenerator();
                  AttachmentPart part = createAttachmentPart(paramMetaData, value, cidGenerator);
                  reqMessage.addAttachmentPart(part);
               }
               else
               {
                  SOAPElement soapElement = paramMetaData.isInHeader() ? (SOAPElement)soapHeader : soapBody;
                  addParameterToMessage(paramMetaData, value, soapElement);
               }
            }
         }
         else
         {
            throw new WSException("Unsupported message style: " + style);
         }

         // Add unbound headers
         if (unboundHeaders != null)
         {
            Iterator it = unboundHeaders.values().iterator();
            while (it.hasNext())
            {
               UnboundHeader unboundHeader = (UnboundHeader)it.next();
               if (unboundHeader.getMode() != ParameterMode.OUT)
               {
                  QName xmlName = unboundHeader.getXmlName();
                  Object value = unboundHeader.getHeaderValue();

                  xmlName = namespaceRegistry.registerQName(xmlName);
                  Name soapName = new NameImpl(xmlName.getLocalPart(), xmlName.getPrefix(), xmlName.getNamespaceURI());

                  SOAPContentElement contentElement = new SOAPHeaderElementImpl(soapName);
                  contentElement.setParamMetaData(unboundHeader.toParameterMetaData(opMetaData));
                  contentElement.setObjectValue(value);

                  log.debug("Add unboundHeader element: " + soapName);
                  soapHeader.addChildElement(contentElement);
               }
            }
         }

         return reqMessage;
      }
      catch (Exception e)
      {
         handleException(e);
         return null;
      }
      finally{
         ThreadLocalAssociation.localDomExpansion().set(Boolean.TRUE);
      }
   }

   /** On the server side, extract the IN parameters from the payload and populate an Invocation object */
   public EndpointInvocation unbindRequestMessage(OperationMetaData opMetaData, SOAPMessage reqMessage) throws BindingException
   {
      log.debug("unbindRequestMessage: " + opMetaData.getXmlName());

      try
      {
         // Read the SOAPEnvelope from the reqMessage
         SOAPEnvelope soapEnvelope = reqMessage.getSOAPPart().getEnvelope();
         SOAPHeader soapHeader = soapEnvelope.getHeader();
         SOAPBody soapBody = soapEnvelope.getBody();

         // Construct the endpoint invocation object
         EndpointInvocation epInv = new EndpointInvocation(opMetaData);

         SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
         if (msgContext == null)
            throw new WSException("MessageContext not available");

         // Get the namespace registry
         NamespaceRegistry namespaceRegistry = msgContext.getNamespaceRegistry();

         if (opMetaData.isMessageEndpoint() == false)
         {
            Style style = opMetaData.getStyle();
            if (style == Style.RPC)
            {
               SOAPBodyElement soapBodyElement = (SOAPBodyElement)soapBody.getChildElements().next();
               Name elName = soapBodyElement.getElementName();

               QName elQName = new QName(elName.getURI(), elName.getLocalName(), elName.getPrefix());
               elQName = namespaceRegistry.registerQName(elQName);

               for (ParameterMetaData paramMetaData : opMetaData.getParameters())
               {
                  QName xmlName = paramMetaData.getXmlName();
                  if (paramMetaData.getMode() == ParameterMode.OUT)
                  {
                     epInv.setRequestParamValue(xmlName, null);
                  }
                  else
                  {
                     if (paramMetaData.isSwA())
                     {
                        Object value = getAttachmentFromMessage(paramMetaData, reqMessage);
                        epInv.setRequestParamValue(xmlName, value);
                     }
                     else
                     {
                        if (paramMetaData.isInHeader() == false)
                        {
                           Object value = getParameterFromMessage(paramMetaData, soapBodyElement, false);
                           epInv.setRequestParamValue(xmlName, value);
                        }
                        else
                        {
                           Object value = getParameterFromMessage(paramMetaData, soapHeader, false);
                           epInv.setRequestParamValue(xmlName, value);
                        }
                     }
                  }
               }
            }

            // Document style
            else
            {
               for (ParameterMetaData paramMetaData : opMetaData.getParameters())
               {
                  QName xmlName = paramMetaData.getXmlName();
                  if (paramMetaData.isSwA())
                  {
                     Object value = getAttachmentFromMessage(paramMetaData, reqMessage);
                     epInv.setRequestParamValue(xmlName, value);
                  }
                  else
                  {
                     if (paramMetaData.isInHeader())
                     {
                        if (paramMetaData.getMode() == ParameterMode.IN)
                        {
                           Object value = getParameterFromMessage(paramMetaData, soapHeader, false);
                           epInv.setRequestParamValue(xmlName, value);
                        }
                        else
                        {
                           Object value = getParameterFromMessage(paramMetaData, soapHeader, true);
                           epInv.setRequestParamValue(xmlName, value);
                        }
                     }
                     else
                     {
                        Object value = getParameterFromMessage(paramMetaData, soapBody, false);
                        epInv.setRequestParamValue(xmlName, value);
                     }
                  }
               }
            }
         }

         // Generic message endpoint
         else
         {
            for (ParameterMetaData paramMetaData : opMetaData.getParameters())
            {
               QName xmlName = paramMetaData.getXmlName();
               Object value = soapBody.getChildElements().next();
               epInv.setRequestParamValue(xmlName, value);
            }
         }

         return epInv;
      }
      catch (Exception e)
      {
         handleException(e);
         return null;
      }
   }

   /** On the server side, generate the payload from OUT parameters. */
   public SOAPMessage bindResponseMessage(OperationMetaData opMetaData, EndpointInvocation epInv) throws BindingException
   {
      log.debug("bindResponseMessage: " + opMetaData.getXmlName());

      try
      {
         ThreadLocalAssociation.localDomExpansion().set(Boolean.FALSE);
         SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
         if (msgContext == null)
            throw new WSException("MessageContext not available");

         // Associate current message with message context
         SOAPMessageImpl resMessage = (SOAPMessageImpl)createMessage(opMetaData);
         msgContext.setMessage(resMessage);

         // R2714 For one-way operations, an INSTANCE MUST NOT return a HTTP response that contains a SOAP envelope.
         // Specifically, the HTTP response entity-body must be empty.
         if (opMetaData.isOneWayOperation())
         {
            resMessage.getSOAPPart().setContent(null);
            return resMessage;
         }

         SOAPEnvelope soapEnvelope = resMessage.getSOAPPart().getEnvelope();
         SOAPHeader soapHeader = soapEnvelope.getHeader();
         SOAPBody soapBody = soapEnvelope.getBody();

         // Get the namespace registry
         NamespaceRegistry namespaceRegistry = msgContext.getNamespaceRegistry();

         Style style = opMetaData.getStyle();
         if (style == Style.RPC)
         {
            QName opQName = opMetaData.getResponseName();

            Name opName = new NameImpl(namespaceRegistry.registerQName(opQName));
            SOAPBodyElement soapBodyElement = new SOAPBodyElementRpc(opName);

            soapBodyElement = (SOAPBodyElement)soapBody.addChildElement(soapBodyElement);

            // Add the return to the message
            ParameterMetaData retMetaData = opMetaData.getReturnParameter();
            if (retMetaData != null)
            {
               Object value = epInv.getReturnValue();
               if (retMetaData.isSwA())
               {
                  CIDGenerator cidGenerator = resMessage.getCidGenerator();
                  AttachmentPart part = createAttachmentPart(retMetaData, value, cidGenerator);
                  resMessage.addAttachmentPart(part);
                  epInv.setReturnValue(part);
               }
               else
               {
                  SOAPContentElement soapElement = addParameterToMessage(retMetaData, value, soapBodyElement);
                  epInv.setReturnValue(soapElement);
                  soapElement.setObjectValue(value);
               }
            }

            // Add the out parameters to the message
            for (ParameterMetaData paramMetaData : opMetaData.getOutputParameters())
            {
               QName xmlName = paramMetaData.getXmlName();
               Object value = epInv.getResponseParamValue(xmlName);
               if (paramMetaData.isSwA())
               {
                  CIDGenerator cidGenerator = resMessage.getCidGenerator();
                  AttachmentPart part = createAttachmentPart(retMetaData, value, cidGenerator);
                  resMessage.addAttachmentPart(part);
               }
               else
               {
                  if (paramMetaData.isInHeader())
                  {
                     addParameterToMessage(paramMetaData, value, soapHeader);
                  }
                  else
                  {
                     addParameterToMessage(paramMetaData, value, soapBodyElement);
                  }
               }
            }
         }
         else if (style == Style.DOCUMENT)
         {
            ParameterMetaData retMetaData = opMetaData.getReturnParameter();
            if (retMetaData != null)
            {
               Object value = epInv.getReturnValue();
               if (opMetaData.isDocumentWrapped())
                  value = ParameterWrapping.wrapResponseParameter(opMetaData, value);

               if (retMetaData.isSwA())
               {
                  CIDGenerator cidGenerator = resMessage.getCidGenerator();
                  AttachmentPart part = createAttachmentPart(retMetaData, value, cidGenerator);
                  resMessage.addAttachmentPart(part);
                  epInv.setReturnValue(part);
               }
               else
               {
                  SOAPContentElement soapElement = addParameterToMessage(retMetaData, value, soapBody);
                  epInv.setReturnValue(soapElement);
               }
            }

            // Add the out header parameters to the message
            for (ParameterMetaData paramMetaData : opMetaData.getOutputParameters())
            {
               QName xmlName = paramMetaData.getXmlName();
               if (paramMetaData.isInHeader())
               {
                  Object value = epInv.getResponseParamValue(xmlName);
                  addParameterToMessage(paramMetaData, value, soapHeader);
               }
            }
         }
         else
         {
            throw new WSException("Unsupported message style: " + style);
         }

         return resMessage;
      }
      catch (Exception e)
      {
         handleException(e);
         return null;
      }
      finally {
         ThreadLocalAssociation.localDomExpansion().set(Boolean.TRUE);
      }
   }

   /** On the client side, extract the OUT parameters from the payload and return them to the client. */
   public void unbindResponseMessage(OperationMetaData opMetaData, SOAPMessage resMessage, EndpointInvocation epInv, Map<QName, UnboundHeader> unboundHeaders)
         throws BindingException
   {
      log.debug("unbindResponseMessage: " + opMetaData.getXmlName());

      try
      {
         ThreadLocalAssociation.localDomExpansion().set(Boolean.FALSE);
         // R2714 For one-way operations, an INSTANCE MUST NOT return a HTTP response that contains a SOAP envelope.
         // Specifically, the HTTP response entity-body must be empty.
         if (opMetaData.isOneWayOperation() == true)
         {
            return;
         }

         // WS-Addressing might redirect the response, which results in an empty envelope
         SOAPEnvelope soapEnvelope = resMessage.getSOAPPart().getEnvelope();
         if (soapEnvelope == null)
         {
            return;
         }

         // Get the SOAP message context that is associated with the current thread
         SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
         if (msgContext == null)
            throw new WSException("MessageContext not available");

         SOAPHeader soapHeader = soapEnvelope.getHeader();
         SOAPBody soapBody = soapEnvelope.getBody();
         Iterator bodyChildren = soapBody.getChildElements();

         SOAPBodyElement soapBodyElement = null;
         if (bodyChildren.hasNext() != false)
            soapBodyElement = (SOAPBodyElement)bodyChildren.next();

         // Translate the SOAPFault to an exception and throw it
         if (soapBodyElement instanceof SOAPFaultImpl)
         {
            SOAPFaultImpl soapFault = (SOAPFaultImpl)soapBodyElement;
            SOAPFaultException faultEx = SOAPFaultExceptionHelper.getSOAPFaultException(soapFault);
            throw faultEx;
         }

         // Extract unbound OUT headers
         if (unboundHeaders != null)
         {
            Map<QName, UnboundHeader> outHeaders = new HashMap<QName, UnboundHeader>();
            Iterator itHeaderElements = soapHeader.getChildElements();
            while (itHeaderElements.hasNext())
            {
               SOAPContentElement soapHeaderElement = (SOAPHeaderElementImpl)itHeaderElements.next();
               Name elName = soapHeaderElement.getElementName();
               QName xmlName = new QName(elName.getURI(), elName.getLocalName());

               UnboundHeader unboundHeader = (UnboundHeader)unboundHeaders.get(xmlName);
               if (unboundHeader != null)
               {
                  soapHeaderElement.setParamMetaData(unboundHeader.toParameterMetaData(opMetaData));

                  // Do the unmarshalling
                  Object value = soapHeaderElement.getObjectValue();
                  unboundHeader.setHeaderValue(value);
                  outHeaders.put(xmlName, unboundHeader);
               }
            }
            unboundHeaders.clear();
            unboundHeaders.putAll(outHeaders);
         }

         Style style = opMetaData.getStyle();
         if (style == Style.RPC)
         {
            ParameterMetaData retMetaData = opMetaData.getReturnParameter();
            if (retMetaData != null)
            {
               if (retMetaData.isSwA())
               {
                  Object value = getAttachmentFromMessage(retMetaData, resMessage);
                  epInv.setReturnValue(value);
               }
               else
               {
                  Object value = getParameterFromMessage(retMetaData, soapBodyElement, false);
                  epInv.setReturnValue(value);
               }
            }

            for (ParameterMetaData paramMetaData : opMetaData.getOutputParameters())
            {
               QName xmlName = paramMetaData.getXmlName();
               if (paramMetaData.isSwA())
               {
                  Object value = getAttachmentFromMessage(paramMetaData, resMessage);
                  epInv.setResponseParamValue(xmlName, value);
               }
               else
               {
                  SOAPElement soapElement = paramMetaData.isInHeader() ? soapHeader : (SOAPElement)soapBodyElement;
                  Object value = getParameterFromMessage(paramMetaData, soapElement, false);
                  epInv.setResponseParamValue(xmlName, value);
               }
            }
         }
         else if (style == Style.DOCUMENT)
         {
            ParameterMetaData retMetaData = opMetaData.getReturnParameter();

            // WS-Eventing has no message part for UnsubscribeResponseMsg
            if (retMetaData != null)
            {
               if (retMetaData.isSwA())
               {
                  Object value = getAttachmentFromMessage(retMetaData, resMessage);
                  epInv.setReturnValue(value);
               }
               else
               {
                  Object value = getParameterFromMessage(retMetaData, soapBody, false);
                  epInv.setReturnValue(value);
               }

               for (ParameterMetaData paramMetaData : opMetaData.getOutputParameters())
               {
                  QName xmlName = paramMetaData.getXmlName();
                  if (paramMetaData.isInHeader())
                  {
                     Object value = getParameterFromMessage(paramMetaData, soapHeader, false);
                     epInv.setResponseParamValue(xmlName, value);
                  }
               }
            }
         }
         else
         {
            throw new WSException("Unsupported message style: " + style);
         }
      }
      catch (Exception e)
      {
         handleException(e);
      }
      finally{
         ThreadLocalAssociation.localDomExpansion().set(Boolean.TRUE);
      }
   }

   private AttachmentPart createAttachmentPart(ParameterMetaData paramMetaData, Object value, CIDGenerator cidGenerator) throws SOAPException, BindingException
   {
      String partName = paramMetaData.getXmlName().getLocalPart();
      Set mimeTypes = paramMetaData.getMimeTypes();

      AttachmentPart part = new AttachmentPartImpl();
      if (value instanceof DataHandler)
      {
         DataHandler handler = (DataHandler)value;
         String mimeType = MimeUtils.getBaseMimeType(handler.getContentType());

         if (mimeTypes != null && !MimeUtils.isMemberOf(mimeType, mimeTypes))
            throw new BindingException("Mime type " + mimeType + " not allowed for parameter " + partName + " allowed types are " + mimeTypes);

         part.setDataHandler((DataHandler)value);
      }
      else
      {
         String mimeType = null;
         if (mimeTypes != null && mimeTypes.size() > 0)
         {
            mimeType = (String)mimeTypes.iterator().next();
         }
         else
         {
            mimeType = MimeUtils.resolveMimeType(value);
         }

         if (mimeType == null)
            throw new BindingException("Could not determine mime type for attachment parameter: " + partName);

         part.setContent(value, mimeType);
      }

      if (paramMetaData.isSwA())
      {
         String swaCID = "<" + partName + "=" + cidGenerator.generateFromCount() + ">";
         part.setContentId(swaCID);
      }
      if (paramMetaData.isXOP())
      {
         String xopCID = "<" + cidGenerator.generateFromName(partName) + ">";
         part.setContentId(xopCID);
      }

      return part;
   }

   private Object getAttachmentFromMessage(ParameterMetaData paramMetaData, SOAPMessage message) throws SOAPException, BindingException
   {
      QName xmlName = paramMetaData.getXmlName();

      AttachmentPart part = ((SOAPMessageImpl)message).getAttachmentByPartName(xmlName.getLocalPart());
      if (part == null)
         throw new BindingException("Could not locate attachment for parameter: " + paramMetaData.getXmlName());

      return part;
   }

   /** Marshall the given parameter and add it to the SOAPMessage */
   private SOAPContentElement addParameterToMessage(ParameterMetaData paramMetaData, Object value, SOAPElement soapElement) throws SOAPException, BindingException
   {
      QName xmlName = paramMetaData.getXmlName();
      Class javaType = paramMetaData.getJavaType();

      if (value != null && paramMetaData.isXOP() == false)
      {
         Class valueType = value.getClass();
         if (JavaUtils.isAssignableFrom(javaType, valueType) == false)
            throw new BindingException("javaType " + javaType.getName() + " is not assignable from: " + valueType.getName());
      }

      // Make sure we have a prefix on qualified names
      if (xmlName.getNamespaceURI().length() > 0)
      {
         SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
         NamespaceRegistry namespaceRegistry = msgContext.getNamespaceRegistry();
         xmlName = namespaceRegistry.registerQName(xmlName);
      }

      Name soapName = new NameImpl(xmlName.getLocalPart(), xmlName.getPrefix(), xmlName.getNamespaceURI());
      if (paramMetaData.isSOAPArrayParam())
         soapName = new NameImpl("Array", Constants.PREFIX_SOAP11_ENC, Constants.URI_SOAP11_ENC);

      SOAPContentElement contentElement;
      if (soapElement instanceof SOAPHeader)
      {
         contentElement = new SOAPHeaderElementImpl(soapName);
         soapElement.addChildElement(contentElement);
      }
      else
      {
         Style style = paramMetaData.getOperationMetaData().getStyle();
         if (style == Style.DOCUMENT)
         {
            contentElement = new SOAPBodyElementDoc(soapName);
            soapElement.addChildElement(contentElement);
         }
         else
         {
            contentElement = new SOAPContentElement(soapName);
            soapElement.addChildElement(contentElement);
         }
      }

      contentElement.setParamMetaData(paramMetaData);

      if (paramMetaData.isSOAPArrayParam())
         contentElement.addNamespaceDeclaration(Constants.PREFIX_SOAP11_ENC, Constants.URI_SOAP11_ENC);

      // The object value needs to be set after xmime:contentType
      if (paramMetaData.isXOP() )
      {
         SOAPMessageImpl soapMessage = (SOAPMessageImpl)
             MessageContextAssociation.peekMessageContext().getMessage();
         soapMessage.setXOPMessage(true);
      }

      contentElement.setObjectValue(value);

      return contentElement;
   }

   /** Unmarshall a message element and add it to the parameter list
    * @param optional
    **/
   private Object getParameterFromMessage(ParameterMetaData paramMetaData, SOAPElement soapElement, boolean optional) throws BindingException
   {
      Name xmlName = new NameImpl(paramMetaData.getXmlName());
      Name soapArrayName = new NameImpl("Array", Constants.PREFIX_SOAP11_ENC, Constants.URI_SOAP11_ENC);

      SOAPContentElement soapContentElement = null;
      Iterator childElements = soapElement.getChildElements();
      while (childElements.hasNext())
      {
         SOAPElementImpl childElement = (SOAPElementImpl)childElements.next();

         // If this message was manipulated by a handler the child may not be a content element
         if (!(childElement instanceof SOAPContentElement))
            childElement = (SOAPContentElement)soapElement.replaceChild(new SOAPContentElement(childElement), childElement);

         // The parameters are expected to be lazy
         SOAPContentElement aux = (SOAPContentElement)childElement;
         Name elName = aux.getElementName();

         if (xmlName.equals(elName))
         {
            soapContentElement = aux;
            soapContentElement.setParamMetaData(paramMetaData);
            break;
         }

         if (soapArrayName.equals(elName))
         {
            Boolean domExpansion = ThreadLocalAssociation.localDomExpansion().get();
            ThreadLocalAssociation.localDomExpansion().set(Boolean.TRUE);
            try
            {
               QName compXMLName = paramMetaData.getXmlName();
               Element compElement = DOMUtils.getFirstChildElement(aux);
               if (compElement.getNodeName().equals(compXMLName.getLocalPart()))
               {
                  soapContentElement = aux;
                  soapContentElement.setParamMetaData(paramMetaData);
                  break;
               }
            }
            finally
            {
               ThreadLocalAssociation.localDomExpansion().set(domExpansion);
            }
         }
      }

      // If matching by name fails, try to match by xmlType
      // This maybe necessary when wsa:Action dispatches to the operation
      if (soapContentElement == null)
      {
         childElements = soapElement.getChildElements();
         OperationMetaData opMetaData = paramMetaData.getOperationMetaData();
         TypesMetaData typesMetaData = opMetaData.getEndpointMetaData().getServiceMetaData().getTypesMetaData();
         if (childElements.hasNext() && opMetaData.getStyle() == Style.DOCUMENT)
         {
            SOAPElementImpl childElement = (SOAPElementImpl)childElements.next();

            // The parameters are expected to be lazy
            SOAPContentElement aux = (SOAPContentElement)childElement;
            Name elName = aux.getElementName();
            QName elType = null;

            XSElementDeclaration xsdElement = typesMetaData.getSchemaModel().getElementDeclaration(elName.getLocalName(), elName.getURI());
            if (xsdElement != null && xsdElement.getTypeDefinition() != null)
            {
               XSTypeDefinition xsdType = xsdElement.getTypeDefinition();
               elType = new QName(xsdType.getNamespace(), xsdType.getName());
            }

            if (paramMetaData.getXmlType().equals(elType))
            {
               soapContentElement = aux;
               soapContentElement.setParamMetaData(paramMetaData);
            }
         }
      }

      if (soapContentElement == null && optional == false)
         throw new JAXRPCException("Cannot find child element: " + xmlName);

      if(paramMetaData.isXOP())
      {
         SOAPMessageImpl soapMessage = (SOAPMessageImpl)
             MessageContextAssociation.peekMessageContext().getMessage();
         soapMessage.setXOPMessage(true);
      }

      return soapContentElement;
   }

   private void handleException(Exception ex) throws BindingException
   {
      if (ex instanceof RuntimeException)
         throw (RuntimeException)ex;

      if (ex instanceof BindingException)
         throw (BindingException)ex;

      throw new BindingException(ex);
   }
}
