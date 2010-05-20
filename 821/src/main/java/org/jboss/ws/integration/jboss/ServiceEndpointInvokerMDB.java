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

// $Id: ServiceEndpointInvokerMDB.java 275 2006-05-04 21:36:29Z jason.greene@jboss.com $

import java.lang.reflect.Method;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.soap.SOAPFaultException;

import org.jboss.logging.Logger;
import org.jboss.ws.binding.EndpointInvocation;
import org.jboss.ws.server.ServiceEndpointInfo;
import org.jboss.ws.server.ServiceEndpointInvoker;
import org.jboss.ws.utils.ThreadLocalAssociation;

/**
 * Handles invocations on MDB endpoints.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 21-Mar-2006
 */
public class ServiceEndpointInvokerMDB extends ServiceEndpointInvoker
{
   // provide logging
   private Logger log = Logger.getLogger(ServiceEndpointInvokerMDB.class);

   /** Initialize the service endpoint */
   public void initServiceEndpoint(ServiceEndpointInfo seInfo) throws ServiceException
   {
   }

   /** Load the SEI implementation bean if necessary
    */
   public Class loadServiceEndpoint(ServiceEndpointInfo seInfo) throws ServiceException, ClassNotFoundException
   {
      return null;
   }

   // The dispatcher sets the target bean object
   public void setTargetBeanObject(Object targetMDB)
   {
      ThreadLocalAssociation.localInvokerMDBAssoc().set(targetMDB);
   }

   /** Create an instance of the SEI implementation bean if necessary
    */
   public Object createServiceEndpoint(ServiceEndpointInfo seInfo, Object endpointContext, Class seiImplClass) throws InstantiationException, IllegalAccessException
   {
      return ThreadLocalAssociation.localInvokerMDBAssoc().get();
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
      finally
      {
         // cleanup thread local
         setTargetBeanObject(null);
      }
   }

   /** Destroy an instance of the SEI implementation bean if necessary */
   public void destroyServiceEndpoint(ServiceEndpointInfo seInfo, Object seiImpl)
   {
   }
}
