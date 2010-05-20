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
package org.jboss.ws.integration.jboss;

// $Id: ServiceEndpointInterceptor.java 275 2006-05-04 21:36:29Z jason.greene@jboss.com $

import javax.xml.rpc.handler.MessageContext;
import javax.xml.soap.SOAPMessage;

import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.logging.Logger;
import org.jboss.ws.binding.BindingProvider;
import org.jboss.ws.binding.BindingProviderRegistry;
import org.jboss.ws.binding.EndpointInvocation;
import org.jboss.ws.metadata.OperationMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData.HandlerType;
import org.jboss.ws.soap.SOAPMessageContextImpl;

/**
 * This Interceptor does the ws4ee handler processing.
 * 
 * According to the ws4ee spec the handler logic must be invoked after the container
 * applied method level security to the invocation. 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 21-Sep-2005
 */
public class ServiceEndpointInterceptor extends AbstractInterceptor
{
   // provide logging
   private static Logger log = Logger.getLogger(ServiceEndpointInterceptor.class);
   
   // Interceptor implementation --------------------------------------

   /** Before and after we call the service endpoint bean, we process the handler chains.
    */
   public Object invoke(final Invocation mi) throws Exception
   {
      // If no msgContext, it's not for us
      MessageContext msgContext = (MessageContext)mi.getPayloadValue(InvocationKey.SOAP_MESSAGE_CONTEXT);
      if (msgContext == null)
      {
         return getNext().invoke(mi);
      }

      // Get the endpoint invocation 
      EndpointInvocation epInv = (EndpointInvocation)mi.getValue(EndpointInvocation.class.getName());
      OperationMetaData opMetaData = epInv.getOperationMetaData();

      // Get the handler callback 
      String key = ServiceEndpointInvokerEJB21.HandlerCallback.class.getName();
      ServiceEndpointInvokerEJB21.HandlerCallback callback = (ServiceEndpointInvokerEJB21.HandlerCallback)mi.getValue(key);
      
      // Handlers need to be Tx. Therefore we must invoke the handler chain after the TransactionInterceptor.
      if (callback != null && epInv != null)
      {
         try
         {
            // call the request handlers
            boolean handlersPass = callback.callRequestHandlerChain(HandlerType.JAXRPC);
            handlersPass = handlersPass && callback.callRequestHandlerChain(HandlerType.POST);

            // Call the next interceptor in the chain
            if (handlersPass)
            {
               // The SOAPContentElements stored in the EndpointInvocation might have changed after
               // handler processing. Get the updated request payload. This should be a noop if request
               // handlers did not modify the incomming SOAP message.
               Object[] reqParams = epInv.getRequestPayload();
               mi.setArguments(reqParams);
               Object resObj = getNext().invoke(mi);
               epInv.setReturnValue(resObj);
               
               // Bind the response message
               BindingProvider bindingProvider = BindingProviderRegistry.getDefaultProvider();
               SOAPMessage resMessage = bindingProvider.bindResponseMessage(opMetaData, epInv);
               ((SOAPMessageContextImpl)msgContext).setMessage(resMessage);
            }
            
            // call the response handlers
            handlersPass = callback.callResponseHandlerChain(HandlerType.POST);
            handlersPass = handlersPass && callback.callResponseHandlerChain(HandlerType.JAXRPC);
            
            // update the return value after response handler processing
            Object resObj = epInv.getReturnValue();
            
            return resObj;
         }
         catch (Exception ex)
         {
            // call the fault handlers
            boolean handlersPass = callback.callFaultHandlerChain(HandlerType.POST, ex);
            handlersPass = handlersPass && callback.callFaultHandlerChain(HandlerType.JAXRPC, ex);
            
            throw ex;
         }
         finally
         {
            // do nothing
         }
      }
      else
      {
         log.warn("Handler callback not available");
         return getNext().invoke(mi);
      }
   }
}
