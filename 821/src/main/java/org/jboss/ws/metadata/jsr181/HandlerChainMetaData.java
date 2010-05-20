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
package org.jboss.ws.metadata.jsr181;

// $Id$

import java.util.ArrayList;

import org.jboss.logging.Logger;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData;

/**
 * XML Binding element for handler-config/handler-chain elements
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-Oct-2005
 */
public class HandlerChainMetaData
{
   // provide logging
   private static final Logger log = Logger.getLogger(HandlerChainMetaData.class);

   // The parent <handler-config> element
   private HandlerConfigMetaData handlerConfig;

   // The required <handler-chain-name> element
   private String handlerChainName;
   // The required <handler> elements
   private ArrayList<UnifiedHandlerMetaData> handlers = new ArrayList<UnifiedHandlerMetaData>();

   public HandlerChainMetaData(HandlerConfigMetaData handlerConfig)
   {
      this.handlerConfig = handlerConfig;
   }

   public HandlerConfigMetaData getHandlerConfig()
   {
      return handlerConfig;
   }

   public String getHandlerChainName()
   {
      return handlerChainName;
   }

   public void setHandlerChainName(String handlerChainName)
   {
      this.handlerChainName = handlerChainName;
   }

   public void addHandler(UnifiedHandlerMetaData handlerMetaData)
   {
      handlers.add(handlerMetaData);
   }

   public UnifiedHandlerMetaData[] getHandlers()
   {
      UnifiedHandlerMetaData[] array = new UnifiedHandlerMetaData[handlers.size()];
      handlers.toArray(array);
      return array;
   }
}
