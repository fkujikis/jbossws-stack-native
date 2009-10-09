/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.core.jaxws.spi.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.management.ObjectName;
import javax.xml.ws.WebServiceException;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.extensions.wsrm.transport.backchannel.RMCallbackHandlerImpl;
import org.jboss.wsf.common.ObjectNameFactory;
import org.jboss.wsf.common.injection.InjectionHelper;
import org.jboss.wsf.common.injection.PreDestroyHolder;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.spi.invocation.InvocationContext;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.spi.management.EndpointResolver;
import org.jboss.wsf.stack.jbws.WebAppResolver;

/**
 * TODO: javadoc
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class NettyHttpServerCallbackHandler
{
   private static final Logger logger = Logger.getLogger(RMCallbackHandlerImpl.class);
   private final String handledPath;
   private final SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
   private EndpointRegistry epRegistry;
   private Endpoint endpoint;
   private List<PreDestroyHolder> preDestroyRegistry = new LinkedList<PreDestroyHolder>();

   /**
    * Request path to listen for incomming messages
    * @param handledPath
    */
   public NettyHttpServerCallbackHandler(String path, String context, String endpointRegistryPath)
   {
      super();
      this.initRegistry();
      this.initEndpoint(context, endpointRegistryPath);
      this.handledPath = path;
   }

   /**
    * Initializes endpoint registry
    */
   private void initRegistry()
   {
      epRegistry = spiProvider.getSPI(EndpointRegistryFactory.class).getEndpointRegistry();
   }   

   /**
    * Initialize the service endpoint
    * @param contextPath context path
    * @param servletName servlet name
    */
   private void initEndpoint(final String context, final String endpointRegistryPath)
   {
      final EndpointResolver resolver = new WebAppResolver(context, endpointRegistryPath);
      this.endpoint = epRegistry.resolve(resolver);

      if (this.endpoint == null)
      {
         ObjectName oname = ObjectNameFactory.create(Endpoint.SEPID_DOMAIN + ":" +
           Endpoint.SEPID_PROPERTY_CONTEXT + "=" + context + "," +
           Endpoint.SEPID_PROPERTY_ENDPOINT + "=" + endpointRegistryPath
         );
         throw new WebServiceException("Cannot obtain endpoint for: " + oname);
      }
   }
   
   public int handle(String method, InputStream inputStream, OutputStream outputStream, InvocationContext invCtx) throws IOException
   {
      Integer statusCode = null;
      try
      {
         if (method.equals("POST"))
         {
            doPost(inputStream, outputStream, invCtx);
            statusCode = (Integer)invCtx.getProperty(Constants.NETTY_STATUS_CODE);
         }
         else if (method.equals("GET"))
         {
            doGet(inputStream, outputStream, invCtx);
         }
         else
         {
            throw new WSException("Unsupported HTTP method: " + method);
         }
      }
      catch(Exception e)
      {
         logger.error(e.getMessage(), e);
         statusCode = 500;
      }
      
      return statusCode == null ? 200 : statusCode;
   }
   
   public final String getHandledPath()
   {
      return this.handledPath;
   }
   
   public void doGet(InputStream inputStream, OutputStream outputStream, InvocationContext invCtx) throws IOException
   {
      try
      {
         EndpointAssociation.setEndpoint(endpoint);
         RequestHandler requestHandler = endpoint.getRequestHandler();
         requestHandler.handleWSDLRequest(endpoint, outputStream, invCtx);
      }
      finally
      {
         EndpointAssociation.removeEndpoint();
      }
   }

   public void doPost(InputStream inputStream, OutputStream outputStream, InvocationContext invCtx) throws IOException
   {
      try
      {
         EndpointAssociation.setEndpoint(endpoint);
         RequestHandler requestHandler = endpoint.getRequestHandler();
         requestHandler.handleRequest(endpoint, inputStream, outputStream, invCtx);
      }
      finally
      {
         this.registerForPreDestroy(endpoint);
         EndpointAssociation.removeEndpoint();
      }
   }

   private void registerForPreDestroy(Endpoint ep)
   {
      PreDestroyHolder holder = (PreDestroyHolder)ep.getAttachment(PreDestroyHolder.class);
      if (holder != null)
      {
         synchronized(this.preDestroyRegistry)
         {
            if (!this.preDestroyRegistry.contains(holder))
            {
               this.preDestroyRegistry.add(holder);
            }
         }
         ep.removeAttachment(PreDestroyHolder.class);
      }
   }

   public final void destroy()
   {
      synchronized(this.preDestroyRegistry)
      {
         for (final PreDestroyHolder holder : this.preDestroyRegistry)
         {
            try
            {
               final Object targetBean = holder.getObject();
               InjectionHelper.callPreDestroyMethod(targetBean);
            }
            catch (Exception exception)
            {
               logger.error(exception.getMessage(), exception);
            }
         }
         this.preDestroyRegistry.clear();
         this.preDestroyRegistry = null;
      }
   }

}
   