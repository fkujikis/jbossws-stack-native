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

import javax.xml.ws.Endpoint;

import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.AbstractExtensible;
import org.jboss.wsf.spi.http.HttpContext;
import org.jboss.wsf.spi.http.HttpContextFactory;
import org.jboss.wsf.spi.http.HttpServer;

/**
 * TODO: javadoc
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
// TODO: review thread safety
final class NettyHttpServer extends AbstractExtensible implements HttpServer
{

   /** JBossWS SPI provider. */
   private static final SPIProvider SPI_PROVIDER = SPIProviderResolver.getInstance().getProvider();
   /** JBossWS Http Context factory. */
   private static final HttpContextFactory HTTP_CONTEXT_FACTORY = NettyHttpServer.SPI_PROVIDER.getSPI(HttpContextFactory.class);

   public HttpContext createContext(final String contextRoot)
   {
      return NettyHttpServer.HTTP_CONTEXT_FACTORY.newHttpContext(this, contextRoot);
   }

   public void destroy(HttpContext context, Endpoint endpoint)
   {
      throw new UnsupportedOperationException();
   }

   public void publish(HttpContext context, Endpoint endpoint)
   {
      throw new UnsupportedOperationException();
   }

   public void start()
   {
      // does nothing
   }

}
