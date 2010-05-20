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
package org.jboss.ws.server;

// $Id$

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.handler.HandlerChain;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.binding.BindingProvider;
import org.jboss.ws.binding.BindingProviderRegistry;
import org.jboss.ws.binding.EndpointInvocation;
import org.jboss.ws.handler.HandlerChainBaseImpl;
import org.jboss.ws.jaxrpc.SOAPFaultExceptionHelper;
import org.jboss.ws.metadata.EndpointMetaData;
import org.jboss.ws.metadata.OperationMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData.HandlerType;
import org.jboss.ws.soap.MessageContextAssociation;
import org.jboss.ws.soap.SOAPMessageContextImpl;
import org.jboss.ws.soap.SOAPMessageImpl;
import org.jboss.ws.utils.JavaUtils;

/** An implementation of handles invocations on the endpoint
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-Jan-2005
 */
public abstract class ServiceEndpointInvoker
{
   // provide logging
   private static Logger log = Logger.getLogger(ServiceEndpointInvoker.class);

   protected ObjectName objectName;

   /** Initialize the service endpoint */
   public abstract void initServiceEndpoint(ServiceEndpointInfo seInfo) throws ServiceException;

   /** Load the SEI implementation bean if necessary */
   public abstract Class loadServiceEndpoint(ServiceEndpointInfo seInfo) throws ServiceException, ClassNotFoundException;

   /** Create the instance of the SEI implementation bean if necessary */
   public abstract Object createServiceEndpoint(ServiceEndpointInfo seInfo, Object endpointContext, Class seiImplClass) throws IllegalAccessException,
         InstantiationException, ServiceException;

   /** Invoke the the service endpoint */
   public SOAPMessage invoke(ServiceEndpointInfo seInfo, Object endpointContext) throws Exception
   {
      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
      EndpointMetaData epMetaData = msgContext.getEndpointMetaData();
      SOAPMessageImpl reqMessage = (SOAPMessageImpl)msgContext.getMessage();

      // Load the endpoint implementation bean
      Class seImpl = loadServiceEndpoint(seInfo);

      // Create an instance of the endpoint implementation bean
      Object seInstance = createServiceEndpoint(seInfo, endpointContext, seImpl);

      try
      {
         boolean oneway = false;

         // call the handler chain
         boolean handlersPass = callRequestHandlerChain(seInfo, HandlerType.PRE);
         handlersPass = handlersPass && callRequestHandlerChain(seInfo, HandlerType.JAXRPC);
         handlersPass = handlersPass && callRequestHandlerChain(seInfo, HandlerType.POST);
         
         if (handlersPass)
         {
            // Get the binding provider for the given bindingURI
            BindingProvider bindingProvider = BindingProviderRegistry.getDefaultProvider();

            // Get the operation meta data from the SOAP message
            OperationMetaData opMetaData = getDispatchDestination(epMetaData, reqMessage);
            msgContext.setOperationMetaData(opMetaData);
            oneway = opMetaData.isOneWayOperation();

            // Unbind the request message
            EndpointInvocation epInv = bindingProvider.unbindRequestMessage(opMetaData, reqMessage);

            // Invoke the service endpoint
            invokeServiceEndpoint(seInfo, seInstance, epInv);

            // Bind the response message
            SOAPMessage resMessage = bindingProvider.bindResponseMessage(opMetaData, epInv);
            msgContext.setMessage(resMessage);
         }

         // call the handler chain
         if (oneway == false)
         {
            handlersPass = callResponseHandlerChain(seInfo, HandlerType.POST);
            handlersPass = handlersPass && callResponseHandlerChain(seInfo, HandlerType.JAXRPC);
            handlersPass = handlersPass && callResponseHandlerChain(seInfo, HandlerType.PRE);
         }

         SOAPMessage resMessage = msgContext.getMessage();
         return resMessage;
      }
      catch (Exception ex)
      {
         try
         {
            SOAPMessage faultMessage = SOAPFaultExceptionHelper.exceptionToFaultMessage(ex);
            msgContext.setMessage(faultMessage);
            
            // call the handler chain
            boolean handlersPass = callFaultHandlerChain(seInfo, HandlerType.POST, ex);
            handlersPass = handlersPass && callFaultHandlerChain(seInfo, HandlerType.JAXRPC, ex);
            handlersPass = handlersPass && callFaultHandlerChain(seInfo, HandlerType.PRE, ex);
         }
         catch (Exception subEx)
         {
            log.warn("Cannot process handlerChain.handleFault, ignoring: ", subEx);
         }
         throw ex;
      }
      finally
      {
         destroyServiceEndpoint(seInfo, seInstance);
      }
   }

   private OperationMetaData getDispatchDestination(EndpointMetaData epMetaData, SOAPMessageImpl reqMessage) throws SOAPException
   {
      OperationMetaData opMetaData = reqMessage.getOperationMetaData(epMetaData);
      SOAPHeader soapHeader = reqMessage.getSOAPHeader();

      // Report a MustUnderstand fault
      if (opMetaData == null)
      {
         SOAPBody soapBody = reqMessage.getSOAPBody();
         SOAPBodyElement soapBodyElement = (SOAPBodyElement)soapBody.getChildElements().next();
         Name soapName = soapBodyElement.getElementName();

         // R2724 If an INSTANCE receives a message that is inconsistent with its WSDL description, it SHOULD generate a soap:Fault
         // with a faultcode of "Client", unless a "MustUnderstand" or "VersionMismatch" fault is generated.
         if (soapHeader != null && soapHeader.examineMustUnderstandHeaderElements(Constants.URI_SOAP11_NEXT_ACTOR).hasNext())
         {
            QName faultCode = Constants.SOAP11_FAULT_CODE_MUST_UNDERSTAND;
            String faultString = "Endpoint " + epMetaData.getName() + " does not contain operation meta data for: " + soapName;
            throw new SOAPFaultException(faultCode, faultString, null, null);
         }
         else
         {
            QName faultCode = Constants.SOAP11_FAULT_CODE_CLIENT;
            String faultString = "Endpoint " + epMetaData.getName() + " does not contain operation meta data for: " + soapName;
            throw new SOAPFaultException(faultCode, faultString, null, null);
         }
      }
      return opMetaData;
   }

   protected Method getImplMethod(Class implClass, Method seiMethod) throws ClassNotFoundException, NoSuchMethodException
   {
      String methodName = seiMethod.getName();
      Class[] paramTypes = seiMethod.getParameterTypes();
      for (int i = 0; i < paramTypes.length; i++)
      {
         Class paramType = paramTypes[i];
         if (JavaUtils.isPrimitive(paramType) == false)
         {
            String paramTypeName = paramType.getName();
            paramType = JavaUtils.loadJavaType(paramTypeName);
            paramTypes[i] = paramType;
         }
      }

      Method implMethod = implClass.getMethod(methodName, paramTypes);
      return implMethod;
   }

   protected boolean callRequestHandlerChain(ServiceEndpointInfo seInfo, HandlerType type)
   {
      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();

      boolean status = true;
      String[] roles = null;

      HandlerChain handlerChain = null;
      if (type == HandlerType.PRE)
         handlerChain = seInfo.getPreHandlerChain();
      else if (type == HandlerType.JAXRPC)
         handlerChain = seInfo.getJaxRpcHandlerChain();
      else if (type == HandlerType.POST)
         handlerChain = seInfo.getPostHandlerChain();
      
      if (handlerChain != null)
      {
         roles = handlerChain.getRoles();
         status = handlerChain.handleRequest(msgContext);
      }

      // BP-1.0 R1027
      if (type == HandlerType.POST)
         HandlerChainBaseImpl.checkMustUnderstand(msgContext, roles);
      
      return status;
   }

   protected boolean callResponseHandlerChain(ServiceEndpointInfo seInfo, HandlerType type)
   {
      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
      
      HandlerChain handlerChain = null;
      if (type == HandlerType.PRE)
         handlerChain = seInfo.getPreHandlerChain();
      else if (type == HandlerType.JAXRPC)
         handlerChain = seInfo.getJaxRpcHandlerChain();
      else if (type == HandlerType.POST)
         handlerChain = seInfo.getPostHandlerChain();
      
      return (handlerChain != null ? handlerChain.handleResponse(msgContext) : true);
   }

   protected boolean callFaultHandlerChain(ServiceEndpointInfo seInfo, HandlerType type, Exception ex)
   {
      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();

      HandlerChain handlerChain = null;
      if (type == HandlerType.PRE)
         handlerChain = seInfo.getPreHandlerChain();
      else if (type == HandlerType.JAXRPC)
         handlerChain = seInfo.getJaxRpcHandlerChain();
      else if (type == HandlerType.POST)
         handlerChain = seInfo.getPostHandlerChain();
      
      return (handlerChain != null ? handlerChain.handleFault(msgContext) : true);
   }

   /** Invoke the instance of the SEI implementation bean */
   public abstract void invokeServiceEndpoint(ServiceEndpointInfo seInfo, Object seiImpl, EndpointInvocation epInv) throws SOAPFaultException;

   /** Destroy the instance of the SEI implementation bean if necessary */
   public abstract void destroyServiceEndpoint(ServiceEndpointInfo seInfo, Object seiImpl);

   /** handle invokation exceptions */
   protected void handleInvocationException(Throwable th) throws SOAPFaultException
   {
      if (th instanceof RuntimeException)
         throw (RuntimeException)th;

      if (th instanceof InvocationTargetException)
      {
         InvocationTargetException targetException = (InvocationTargetException)th;
         Throwable targetEx = targetException.getTargetException();
         if (targetEx instanceof SOAPFaultException)
         {
            throw (SOAPFaultException)targetEx;
         }
         else
         {
            String faultString = targetEx.toString();
            SOAPFaultException soapFaultEx = new SOAPFaultException(Constants.SOAP11_FAULT_CODE_CLIENT, faultString, null, null);
            soapFaultEx.initCause(targetEx);
            throw soapFaultEx;
         }
      }

      if (th instanceof MBeanException)
      {
         Exception targetEx = ((MBeanException)th).getTargetException();
         if (targetEx instanceof SOAPFaultException)
         {
            throw (SOAPFaultException)targetEx;
         }
         else
         {
            String faultString = targetEx.toString();
            SOAPFaultException soapFaultEx = new SOAPFaultException(Constants.SOAP11_FAULT_CODE_CLIENT, faultString, null, null);
            soapFaultEx.initCause(targetEx);
            throw soapFaultEx;
         }
      }

      String faultString = th.toString();
      SOAPFaultException soapFaultEx = new SOAPFaultException(Constants.SOAP11_FAULT_CODE_CLIENT, faultString, null, null);
      soapFaultEx.initCause(th);
      throw soapFaultEx;
   }
}
