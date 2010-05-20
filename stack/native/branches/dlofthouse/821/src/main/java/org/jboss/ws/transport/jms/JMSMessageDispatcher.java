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
package org.jboss.ws.transport.jms;

// $Id: JMSMessageDispatcher.java 356 2006-05-16 17:26:40Z thomas.diesler@jboss.com $

import java.io.InputStream;
import java.rmi.RemoteException;

import javax.management.ObjectName;
import javax.xml.soap.SOAPMessage;

import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;
import org.jboss.webservice.transport.jms.MessageDispatcher;
import org.jboss.ws.WSException;
import org.jboss.ws.binding.BindingException;
import org.jboss.ws.integration.jboss.ServiceEndpointInvokerMDB;
import org.jboss.ws.server.ServiceEndpoint;
import org.jboss.ws.server.ServiceEndpointInvoker;
import org.jboss.ws.server.ServiceEndpointManager;
import org.jboss.ws.server.ServiceEndpointManagerFactory;

/**
 * A dispatcher for SOAPMessages
 *  
 * @author Thomas.Diesler@jboss.org
 */
public class JMSMessageDispatcher implements MessageDispatcher
{
   // logging support
   protected Logger log = Logger.getLogger(JMSMessageDispatcher.class);

   /** Dispatch the message to the underlying SOAP engine
    */
   public SOAPMessage dipatchMessage(String fromName, Object targetBean, InputStream reqMessage) throws RemoteException
   {
      try
      {
         ServiceEndpointManagerFactory factory = ServiceEndpointManagerFactory.getInstance();
         ServiceEndpointManager epManager = factory.getServiceEndpointManager();
         ObjectName sepID = getServiceEndpointForDestination(epManager, fromName);

         if (sepID == null)
            throw new WSException("Cannot find serviceID for: " + fromName);

         log.debug("dipatchMessage: " + sepID);

         // Setup the MDB invoker
         ServiceEndpoint sep = epManager.getServiceEndpointByID(sepID);
         ServiceEndpointInvoker invoker = sep.getServiceEndpointInfo().getInvoker();
         if (invoker instanceof ServiceEndpointInvokerMDB)
         {
            ServiceEndpointInvokerMDB mdbInvoker = (ServiceEndpointInvokerMDB)invoker;
            mdbInvoker.setTargetBeanObject(targetBean);
         }

         return sep.handleRequest(null, null, reqMessage);
      }
      catch (BindingException ex)
      {
         throw new WSException("Cannot bind incomming soap message", ex);
      }
   }

   /** Dispatch the message to the underlying SOAP engine
    */
   public SOAPMessage delegateMessage(String serviceID, InputStream soapMessage) throws RemoteException
   {
      throw new NotImplementedException();
   }

   // The destination jndiName is encoded in the service object name under key 'jms'
   private ObjectName getServiceEndpointForDestination(ServiceEndpointManager epManager, String fromName)
   {
      ObjectName sepID = null;
      for (ObjectName aux : epManager.getServiceEndpoints())
      {
         String jmsProp = aux.getKeyProperty("jms");
         if (jmsProp != null && jmsProp.equals(fromName))
         {
            sepID = aux;
            break;
         }
      }
      return sepID;
   }
}
