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
package org.jboss.ws.metadata;

// $Id$

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.metadata.config.WSClientConfig;
import org.jboss.ws.metadata.config.WSConfig;
import org.jboss.ws.metadata.config.WSConfigFactory;
import org.jboss.ws.metadata.config.WSHandlerChainConfig;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData.HandlerType;

/**
 * Client side endpoint meta data.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-May-2005
 */
public class ClientEndpointMetaData extends EndpointMetaData
{
   // provide logging
   private static Logger log = Logger.getLogger(ClientEndpointMetaData.class);

   // The REQUIRED config
   private WSClientConfig clientConfig;

   public ClientEndpointMetaData(ServiceMetaData service, QName qname)
   {
      super(service, qname);
   }

   public void setConfigName(String configName)
   {
      super.setConfigName(configName);
      clientConfig = null;
   }
   
   public void setConfigFile(String configFile)
   {
      super.setConfigFile(configFile);
      clientConfig = null;
   }

   public WSClientConfig getClientConfig()
   {
      if (clientConfig == null)
      {
         String configName = getConfigName();
         if (configName == null)
         {
            configName = "Standard Client";
            setConfigName(configName);
         }

         String configFile = getConfigFile();
         if (configFile == null)
         {
            configFile = "META-INF/standard-jbossws-client-config.xml";
            setConfigFile(configFile);
         }

         log.debug("getClientConfig: [name=" + configName + ",url=" + configFile + "]");

         URL configURL = null;
         try
         {
            configURL = new URL(configFile);
         }
         catch (MalformedURLException ex)
         {
            // ignore
         }

         // Try to get the URL as resource
         if (configURL == null)
         {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            configURL = cl.getResource(configFile);
            if (configURL == null)
               throw new WSException("Cannot get resource: " + configFile);
         }

         WSConfig wsConfig;
         try
         {
            WSConfigFactory factory = WSConfigFactory.newInstance();
            wsConfig = factory.parseWithObjectModelFactory(configURL);
         }
         catch (RuntimeException rte)
         {
            throw rte;
         }
         catch (Exception ex)
         {
            throw new WSException("Cannot parse client config", ex);
         }

         clientConfig = wsConfig.getClientConfigByName(configName);
         if (clientConfig == null)
            throw new WSException("Cannot obtain client config: " + configName);
      }
      return clientConfig;
   }

   public List<UnifiedHandlerMetaData> getHandlers(HandlerType type)
   {
      List<UnifiedHandlerMetaData> handlers = new ArrayList<UnifiedHandlerMetaData>();

      // Add pre handlers
      if (type == HandlerType.PRE || type == HandlerType.ALL)
      {
         WSHandlerChainConfig preHandlerConfig = getClientConfig().getPreHandlerChain();
         if (preHandlerConfig != null)
         {
            handlers.addAll(preHandlerConfig.getHandlers());
         }
      }
      
      // Add endpoint handlers
      if (type == HandlerType.JAXRPC || type == HandlerType.ALL)
      {
         handlers.addAll(super.getHandlers(type));
      }
      
      // Add post handlers
      if (type == HandlerType.POST || type == HandlerType.ALL)
      {
         WSHandlerChainConfig postHandlerConfig = getClientConfig().getPostHandlerChain();
         if (postHandlerConfig != null)
         {
            handlers.addAll(postHandlerConfig.getHandlers());
         }
      }
      
      return handlers;
   }

   public String toString()
   {
      StringBuilder buffer = new StringBuilder("\nClientEndpointMetaData:");
      buffer.append("\n name=" + getName());
      buffer.append("\n address=" + getEndpointAddress());
      buffer.append("\n seiName=" + getServiceEndpointInterfaceName());
      buffer.append("\n configFile=" + getConfigFile());
      buffer.append("\n configName=" + getConfigName());
      buffer.append("\n authMethod=" + getAuthMethod());
      buffer.append("\n transportGuarantee=" + getTransportGuarantee());
      buffer.append("\n properties=" + getProperties());

      for (OperationMetaData opMetaData : getOperations())
      {
         buffer.append("\n" + opMetaData);
      }
      for (UnifiedHandlerMetaData hdlMetaData : getHandlers(HandlerType.ALL))
      {
         buffer.append("\n" + hdlMetaData);
      }
      return buffer.toString();
   }
}
