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
package org.jboss.ws.metadata.config;

import java.util.ArrayList;
import java.util.List;

/** 
 * A JBossWS configuration 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 18-Dec-2005
 */
public class WSConfig
{
   private List<WSClientConfig> clientConfigList = new ArrayList<WSClientConfig>();
   private List<WSEndpointConfig> endpointConfigList = new ArrayList<WSEndpointConfig>();

   public List<WSClientConfig> getClientConfig()
   {
      return clientConfigList;
   }

   public void setClientConfig(List<WSClientConfig> clientConfig)
   {
      this.clientConfigList = clientConfig;
   }

   public List<WSEndpointConfig> getEndpointConfig()
   {
      return endpointConfigList;
   }

   public void setEndpointConfig(List<WSEndpointConfig> endpointConfig)
   {
      this.endpointConfigList = endpointConfig;
   }
   
   public WSClientConfig getClientConfigByName(String configName)
   {
      WSClientConfig config = null;
      for(WSClientConfig aux : clientConfigList)
      {
         if (aux.getConfigName().equals(configName))
         {
            config = aux;
            break;
         }
      }
      
      if (config == null && clientConfigList.size() == 1)
         config = clientConfigList.get(0);
      
      return config;
   }
   
   public WSEndpointConfig getEndpointConfigByName(String configName)
   {
      WSEndpointConfig config = null;
      for(WSEndpointConfig aux : endpointConfigList)
      {
         if (aux.getConfigName().equals(configName))
         {
            config = aux;
            break;
         }
      }
      
      if (config == null && endpointConfigList.size() == 1)
         config = endpointConfigList.get(0);
      
      return config;
   }
}
