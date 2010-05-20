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
package org.jboss.ws.wsse;

// $Id$

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.SOAPException;

import org.jboss.logging.Logger;
import org.jboss.ws.metadata.EndpointMetaData;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;
import org.jboss.ws.soap.SOAPMessageContextImpl;

/**
 * An abstract JAXRPC handler that delegates to the WSSecurityDispatcher
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-Nov-2005
 */
public abstract class WSSecurityHandler extends GenericHandler
{
   protected static String FAULT_THROWN = "org.jboss.ws.wsse.faultThrown";

   // provide logging
   private static Logger log = Logger.getLogger(WSSecurityHandler.class);

   public QName[] getHeaders()
   {
      return null;
   }

   protected boolean thrownByMe(MessageContext msgContext)
   {
      Boolean bool = (Boolean) msgContext.getProperty(FAULT_THROWN);
      return bool != null && bool.booleanValue();
   }

   protected boolean handleInboundSecurity(MessageContext msgContext)
   {
      Exception exception = null;
      try
      {
         if (getSecurityConfiguration(msgContext) != null)
         {
            WSSecurityDispatcher.handleInbound((SOAPMessageContextImpl)msgContext);
         }
      }
      catch (Exception ex)
      {
         exception = ex;
      }

      if (exception != null)
      {
         msgContext.setProperty(FAULT_THROWN, true);
         if (exception instanceof SOAPFaultException)
            throw (SOAPFaultException)exception;

         // Unexpected exception, log it
         log.error("Cannot handle inbound ws-security", exception);
         return false;
      }

      return true;
   }

   protected boolean handleOutboundSecurity(MessageContext msgContext)
   {
      Exception exception = null;
      try
      {
         if (getSecurityConfiguration(msgContext) != null)
         {
            WSSecurityDispatcher.handleOutbound((SOAPMessageContextImpl)msgContext);
         }
      }
      catch (Exception ex)
      {
         exception = ex;
      }

      if (exception != null)
      {
         msgContext.setProperty(FAULT_THROWN, true);
         if (exception instanceof SOAPFaultException)
            throw (SOAPFaultException)exception;

         // Unexpected exception, log it
         log.error("Cannot handle outbound ws-security", exception);
         return false;
      }

      return true;
   }

   private WSSecurityConfiguration getSecurityConfiguration(MessageContext msgContext)
   {
      EndpointMetaData epMetaData = ((SOAPMessageContextImpl)msgContext).getEndpointMetaData();
      WSSecurityConfiguration securityConfiguration = epMetaData.getServiceMetaData().getSecurityConfiguration();
      if (securityConfiguration == null)
         log.warn("Cannot obtain security configuration");

      return securityConfiguration;
   }
}