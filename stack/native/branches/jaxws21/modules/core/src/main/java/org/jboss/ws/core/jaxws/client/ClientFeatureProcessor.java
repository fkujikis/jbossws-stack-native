/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.ws.core.jaxws.client;

import java.util.List;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.ws.core.jaxws.binding.BindingExt;
import org.jboss.ws.extensions.addressing.jaxws.WSAddressingClientHandler;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData.HandlerType;

/**
 * Process WebServiceFeature provided on client side
 * 
 * @author alessio.soldano@jboss.com
 * @since 14-Jan-2009
 *
 */
public class ClientFeatureProcessor
{
   public static <T> void processFeature(WebServiceFeature feature, EndpointMetaData epMetaData, T stub)
   {
      epMetaData.addFeature(feature);
      processAddressingFeature(feature, epMetaData, stub);
      processMTOMFeature(feature, epMetaData, stub);
   }
   
   @SuppressWarnings("unchecked")
   private static <T> void processAddressingFeature(WebServiceFeature feature, EndpointMetaData epMetaData, T stub)
   {
      if (feature instanceof AddressingFeature && feature.isEnabled())
      {
         BindingExt bindingExt = (BindingExt)((BindingProvider)stub).getBinding();
         List<Handler> handlers = bindingExt.getHandlerChain(HandlerType.POST);
         handlers.add(new WSAddressingClientHandler());
         bindingExt.setHandlerChain(handlers, HandlerType.POST);
      }
   }
   
   private static <T> void processMTOMFeature(WebServiceFeature feature, EndpointMetaData epMetaData, T stub)
   {
      if (feature instanceof MTOMFeature)
      {
         SOAPBinding binding = (SOAPBinding)((BindingProvider)stub).getBinding();
         binding.setMTOMEnabled(feature.isEnabled());
      }
   }

}
