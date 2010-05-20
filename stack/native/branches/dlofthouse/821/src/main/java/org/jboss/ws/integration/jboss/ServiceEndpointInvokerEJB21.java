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

// $Id: ServiceEndpointInvokerEJB21.java 312 2006-05-11 10:49:22Z thomas.diesler@jboss.com $

import java.lang.reflect.Method;
import java.security.Principal;

import javax.management.MBeanServer;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.handler.HandlerChain;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.SOAPMessage;

import org.jboss.ejb.EjbModule;
import org.jboss.ejb.Interceptor;
import org.jboss.ejb.StatelessSessionContainer;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.PayloadKey;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.security.SecurityAssociation;
import org.jboss.ws.WSException;
import org.jboss.ws.binding.EndpointInvocation;
import org.jboss.ws.handler.HandlerChainBaseImpl;
import org.jboss.ws.jaxrpc.SOAPFaultExceptionHelper;
import org.jboss.ws.metadata.ServerEndpointMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedApplicationMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedBeanMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData.HandlerType;
import org.jboss.ws.server.ServiceEndpointInfo;
import org.jboss.ws.server.ServiceEndpointInvoker;
import org.jboss.ws.soap.MessageContextAssociation;
import org.jboss.ws.soap.SOAPMessageContextImpl;
import org.jboss.ws.utils.ObjectNameFactory;

/**
 * Handles invocations on EJB2.1 endpoints.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-Jan-2005
 */
public class ServiceEndpointInvokerEJB21 extends ServiceEndpointInvoker 
{
   // provide logging
   private Logger log = Logger.getLogger(ServiceEndpointInvokerEJB21.class);

   private String jndiName;
   private MBeanServer server;

   public ServiceEndpointInvokerEJB21()
   {
      server = MBeanServerLocator.locateJBoss();
   }

   /** Initialize the service endpoint */
   public void initServiceEndpoint(ServiceEndpointInfo seInfo) throws ServiceException
   {
      ServerEndpointMetaData endpointMetaData = seInfo.getServerEndpointMetaData();
      String ejbName = endpointMetaData.getLinkName();
      if (ejbName == null)
         throw new WSException("Cannot obtain ejb-link from port component");

      UnifiedApplicationMetaData applMetaData = (UnifiedApplicationMetaData)seInfo.getUnifiedDeploymentInfo().metaData;
      UnifiedBeanMetaData beanMetaData = (UnifiedBeanMetaData)applMetaData.getBeanByEjbName(ejbName);
      if (beanMetaData == null)
         throw new WSException("Cannot obtain ejb meta data for: " + ejbName);

      // verify the service endpoint
      String seiName = endpointMetaData.getServiceEndpointInterfaceName();
      if ((!endpointMetaData.isAnnotated()) && seiName != null)
      {
         String bmdSEI = beanMetaData.getServiceEndpoint();
         if (seiName.equals(bmdSEI) == false)
            throw new WSException("Endpoint meta data defines SEI '" + seiName + "', <service-endpoint> in ejb-jar.xml defines '" + bmdSEI + "'");
      }

      // get the bean's JNDI name
      jndiName = beanMetaData.getContainerObjectNameJndiName();
      if (jndiName == null)
         throw new WSException("Cannot obtain JNDI name for: " + ejbName);

      objectName = ObjectNameFactory.create("jboss.j2ee:jndiName=" + jndiName + ",service=EJB");

      // Dynamically add the service endpoint interceptor
      // http://jira.jboss.org/jira/browse/JBWS-758
      try
      {
         EjbModule ejbModule = (EjbModule)server.getAttribute(objectName, "EjbModule");
         StatelessSessionContainer container = (StatelessSessionContainer)ejbModule.getContainer(ejbName);
         
         boolean injectionPointFound = false;
         Interceptor prev = container.getInterceptor();
         while (prev != null && prev.getNext() != null)
         {
            Interceptor next = prev.getNext();
            if (next.getNext() == null)
            {
               log.debug("Inject service endpoint interceptor after: " + prev.getClass().getName());
               ServiceEndpointInterceptor sepInterceptor = new ServiceEndpointInterceptor();
               prev.setNext(sepInterceptor);
               sepInterceptor.setNext(next);
               injectionPointFound = true;
            }
            prev = next;
         }
         if (injectionPointFound == false)
            log.warn("Cannot service endpoint interceptor injection point");
      }
      catch (Exception ex)
      {
         log.warn("Cannot add service endpoint interceptor", ex);
      }
   }

   /** Load the SEI implementation bean if necessary 
    */
   public Class loadServiceEndpoint(ServiceEndpointInfo seInfo) throws ServiceException
   {
      if (server.isRegistered(objectName) == false)
         throw new ServiceException("Cannot find service endpoint target: " + objectName);

      return null;
   }

   /** Create an instance of the SEI implementation bean if necessary */
   public Object createServiceEndpoint(ServiceEndpointInfo seInfo, Object endpointContext, Class seiImplClass)
   {
      return null;
   }

   /** Invoke an instance of the SEI implementation bean */
   public void invokeServiceEndpoint(ServiceEndpointInfo seInfo, Object seiImpl, EndpointInvocation epInv) throws SOAPFaultException
   {
      log.debug("invokeServiceEndpoint: " + epInv.getJavaMethod().getName());

      // these are provided by the ServerLoginHandler
      Principal principal = SecurityAssociation.getPrincipal();
      Object credential = SecurityAssociation.getCredential();

      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();

      // invoke on the container
      try
      {
         // setup the invocation
         Method method = epInv.getJavaMethod();
         Object[] args = epInv.getRequestPayload();
         Invocation inv = new Invocation(null, method, args, null, principal, credential);
         inv.setValue(InvocationKey.SOAP_MESSAGE_CONTEXT, msgContext);
         inv.setValue(InvocationKey.SOAP_MESSAGE, msgContext.getMessage());
         inv.setType(InvocationType.SERVICE_ENDPOINT);

         // Set the handler callback and endpoint invocation
         inv.setValue(HandlerCallback.class.getName(), new HandlerCallback(seInfo), PayloadKey.TRANSIENT);
         inv.setValue(EndpointInvocation.class.getName(), epInv, PayloadKey.TRANSIENT);

         String[] sig = { Invocation.class.getName() };
         Object retObj = server.invoke(objectName, "invoke", new Object[] { inv }, sig);
         epInv.setReturnValue(retObj);
      }
      catch (Exception e)
      {
         handleInvocationException(e);
      }
   }

   protected boolean callRequestHandlerChain(ServiceEndpointInfo seInfo, HandlerType type)
   {
      if (type == HandlerType.PRE)
         return super.callRequestHandlerChain(seInfo, type);
      else return true;
   }

   protected boolean callResponseHandlerChain(ServiceEndpointInfo seInfo, HandlerType type)
   {
      if (type == HandlerType.PRE)
         return super.callResponseHandlerChain(seInfo, type);
      else return true;
   }

   protected boolean callFaultHandlerChain(ServiceEndpointInfo seInfo, HandlerType type, Exception ex)
   {
      if (type == HandlerType.PRE)
         return super.callFaultHandlerChain(seInfo, type, ex);
      else return true;
   }

   /** Create an instance of the SEI implementation bean if necessary */
   public void destroyServiceEndpoint(ServiceEndpointInfo seInfo, Object seiImpl)
   {
      // do nothing
   }

   // The ServiceEndpointInterceptor calls the methods in this callback
   public static class HandlerCallback
   {
      private ServiceEndpointInfo seInfo;

      public HandlerCallback(ServiceEndpointInfo seInfo)
      {
         this.seInfo = seInfo;
      }

      public boolean callRequestHandlerChain(HandlerType type)
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

      public boolean callResponseHandlerChain(HandlerType type)
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

      public boolean callFaultHandlerChain(HandlerType type, Exception ex)
      {
         SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
         SOAPMessage faultMessage = SOAPFaultExceptionHelper.exceptionToFaultMessage(ex);
         msgContext.setMessage(faultMessage);

         HandlerChain handlerChain = null;
         if (type == HandlerType.PRE)
            handlerChain = seInfo.getPreHandlerChain();
         else if (type == HandlerType.JAXRPC)
            handlerChain = seInfo.getJaxRpcHandlerChain();
         else if (type == HandlerType.POST)
            handlerChain = seInfo.getPostHandlerChain();

         return (handlerChain != null ? handlerChain.handleFault(msgContext) : true);
      }
   }
}
