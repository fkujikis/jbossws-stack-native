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

// $Id$

/** 
 * A JBossWS client configuration 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 18-Dec-2005
 */
public abstract class WSCommonConfig
{
   private String configName;
   private WSHandlerChainConfig preHandlerChain;
   private WSHandlerChainConfig postHandlerChain;

   public String getConfigName()
   {
      return configName;
   }

   public void setConfigName(String configName)
   {
      this.configName = configName;
   }

   public WSHandlerChainConfig getPostHandlerChain()
   {
      return postHandlerChain;
   }

   public void setPostHandlerChain(WSHandlerChainConfig postHandlerChain)
   {
      this.postHandlerChain = postHandlerChain;
   }

   public WSHandlerChainConfig getPreHandlerChain()
   {
      return preHandlerChain;
   }

   public void setPreHandlerChain(WSHandlerChainConfig preHandlerChain)
   {
      this.preHandlerChain = preHandlerChain;
   }
}
