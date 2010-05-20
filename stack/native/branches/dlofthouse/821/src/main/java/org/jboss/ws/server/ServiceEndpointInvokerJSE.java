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

import java.lang.reflect.Method;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;
import javax.xml.rpc.soap.SOAPFaultException;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.binding.EndpointInvocation;
import org.jboss.ws.metadata.ServerEndpointMetaData;
import org.jboss.ws.server.ServiceEndpointInfo;
import org.jboss.ws.server.ServiceEndpointInvoker;

/**
 * Handles invocations on JSE endpoints.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-Jan-2005
 */
public class ServiceEndpointInvokerJSE extends ServiceEndpointInvoker
{
   // provide logging
   private Logger log = Logger.getLogger(ServiceEndpointInvokerJSE.class);

   /** Initialize the service endpoint */
   public void initServiceEndpoint(ServiceEndpointInfo seInfo) throws ServiceException
   {
      // nothing to do
   }
   
   /** Load the SEI implementation bean if necessary */
   public Class loadServiceEndpoint(ServiceEndpointInfo seInfo) throws ClassNotFoundException
   {
      ServerEndpointMetaData epMetaData = seInfo.getServerEndpointMetaData();
      ClassLoader cl = epMetaData.getClassLoader();
      String seiImplName = epMetaData.getServiceEndpointImplName();
      Class seiImplClass = cl.loadClass(seiImplName);
      return seiImplClass;
   }

   /** Create an instance of the SEI implementation bean if necessary */
   public Object createServiceEndpoint(ServiceEndpointInfo seInfo, Object endpointContext, Class seiImplClass) throws IllegalAccessException,
         InstantiationException, ServiceException
   {
      Object seiImpl = seiImplClass.newInstance();
      if (seiImpl instanceof ServiceLifecycle)
      {
         if ((endpointContext instanceof ServletEndpointContext) == false)
            throw new WSException("Invalid endpoint context: " + endpointContext); 
            
         ServiceLifecycle serviceLifecycle = ((ServiceLifecycle)seiImpl);
         ServletEndpointContext servletEndpointContext = (ServletEndpointContext)endpointContext;
         serviceLifecycle.init(servletEndpointContext);
      }
      return seiImpl;
   }

   /** Invoke an instance of the SEI implementation bean */
   public void invokeServiceEndpoint(ServiceEndpointInfo seInfo, Object seiImpl, EndpointInvocation epInv) throws SOAPFaultException
   {
      log.debug("invokeServiceEndpoint: " + epInv.getJavaMethod().getName());
      try
      {
         Class implClass = seiImpl.getClass();
         Method seiMethod = epInv.getJavaMethod();
         Method implMethod = getImplMethod(implClass, seiMethod);

         Object[] args = epInv.getRequestPayload();
         Object retObj = implMethod.invoke(seiImpl, args);
         epInv.setReturnValue(retObj);
      }
      catch (Exception e)
      {
         handleInvocationException(e);
      }
   }

   /** Destroy an instance of the SEI implementation bean if necessary */
   public void destroyServiceEndpoint(ServiceEndpointInfo seInfo, Object seiImpl)
   {
      if (seiImpl instanceof ServiceLifecycle)
      {
         ((ServiceLifecycle)seiImpl).destroy();
      }
   }
}
