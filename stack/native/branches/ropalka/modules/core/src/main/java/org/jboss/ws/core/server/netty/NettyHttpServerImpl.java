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
package org.jboss.ws.core.server.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import javax.xml.ws.WebServiceException;

import org.jboss.logging.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.ws.core.client.transport.WSServerPipelineFactory;

/**
 * Netty http server implementation.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @author <a href="mailto:asoldano@redhat.com">Alessio Soldano</a>
 */
final class NettyHttpServerImpl implements NettyHttpServer, Runnable
{

   private static final Logger LOG = Logger.getLogger(NettyHttpServerImpl.class);

   private static final long WAIT_PERIOD = 100;

   static final ChannelGroup channelGroup = new DefaultChannelGroup("NettyHttpServer");

   private final Object instanceLock = new Object();

   private final int port;

   private boolean started;

   private boolean stopped;

   private boolean terminated;

   private ChannelFactory factory;

   private AbstractNettyRequestHandler handler;

   NettyHttpServerImpl(int port, NettyRequestHandlerFactory<?> nettyRequestHandlerFactory)
   {
      super();
      this.port = port;
      try
      {
         factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

         ServerBootstrap bootstrap = new ServerBootstrap(factory);
         this.handler = nettyRequestHandlerFactory.newNettyRequestHandler();
         WSServerPipelineFactory channelPipelineFactory = new WSServerPipelineFactory();
         channelPipelineFactory.setRequestHandler(this.handler);
         bootstrap.setPipelineFactory(channelPipelineFactory);
         bootstrap.setOption("child.tcpNoDelay", true);
         bootstrap.setOption("child.keepAlive", true);
         // Bind and start to accept incoming connections.
         Channel c = bootstrap.bind(new InetSocketAddress(this.port));
         channelGroup.add(c);
         // forking Netty server
         Thread t = new Thread(this, "NettyHttpServer listening on port " + port);
         t.setDaemon(true);
         t.start();
         // registering shutdown hook
         Runnable shutdownHook = new NettyHttpServerShutdownHook(this);
         Runtime.getRuntime().addShutdownHook(
               new Thread(shutdownHook, "NettyHttpServerShutdownHook(port=" + port + ")"));
         if (LOG.isDebugEnabled())
            LOG.debug("Netty http server started on port: " + this.port);
      }
      catch (Exception e)
      {
         LOG.warn(e.getMessage(), e);
         throw new WebServiceException(e.getMessage(), e);
      }
   }

   public final void registerCallback(final NettyCallbackHandler callback)
   {
      if (callback == null)
         throw new IllegalArgumentException("Null callback handler");

      this.ensureUpAndRunning();

      this.handler.registerCallback(callback);
   }

   public final void unregisterCallback(final NettyCallbackHandler callback)
   {
      if (callback == null)
         throw new IllegalArgumentException("Null callback handler");

      this.ensureUpAndRunning();

      try
      {
         this.handler.unregisterCallback(callback);
      }
      finally
      {
         if (!this.hasMoreCallbacks())
         {
            this.terminate();
         }
      }
   }

   public final NettyCallbackHandler getCallback(final String requestPath)
   {
      if (requestPath == null)
         throw new IllegalArgumentException("Null request path");

      this.ensureUpAndRunning();

      return this.handler.getCallback(requestPath);
   }

   public final boolean hasMoreCallbacks()
   {
      this.ensureUpAndRunning();

      return this.handler.hasMoreCallbacks();
   }

   public final int getPort()
   {
      this.ensureUpAndRunning();

      return this.port;
   }

   private void ensureUpAndRunning()
   {
      synchronized (this.instanceLock)
      {
         if (this.stopped)
            throw new IllegalStateException("Server is down");
      }
   }

   public final void run()
   {
      synchronized (this.instanceLock)
      {
         if (this.started)
            return;

         this.started = true;

         while (this.stopped == false)
         {
            try
            {
               this.instanceLock.wait(WAIT_PERIOD);
            }
            catch (InterruptedException ie)
            {
               LOG.warn(ie.getMessage(), ie);
            }
         }
         try
         {
            //Close all connections and server sockets.
            channelGroup.close().awaitUninterruptibly();
            //Shutdown the selector loop (boss and worker).
            if (factory != null)
            {
               factory.releaseExternalResources();
            }
         }
         finally
         {
            LOG.debug("terminated");
            this.terminated = true;
         }
      }
   }

   public final void terminate()
   {
      synchronized (this.instanceLock)
      {
         if (this.stopped == true)
            return;

         this.stopped = true;
         LOG.debug("termination forced");
         while (this.terminated == false)
         {
            try
            {
               LOG.debug("waiting for termination");
               this.instanceLock.wait(WAIT_PERIOD);
            }
            catch (InterruptedException ie)
            {
               LOG.warn(ie.getMessage(), ie);
            }
         }
         synchronized (NettyHttpServerFactory.SERVERS)
         {
            NettyHttpServerFactory.SERVERS.remove(port);
         }
      }
   }

   private static final class NettyHttpServerShutdownHook implements Runnable
   {

      private final NettyHttpServerImpl nettyHttpServer;

      private NettyHttpServerShutdownHook(final NettyHttpServerImpl nettyHttpServer)
      {
         super();

         this.nettyHttpServer = nettyHttpServer;
      }

      public void run()
      {
         this.nettyHttpServer.terminate();
      }

   }

}
