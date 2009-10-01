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

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
 * TODO: javadoc
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class RealNettyHttpServer implements Runnable
{
   
   private static final Logger LOG = Logger.getLogger(RealNettyHttpServer.class);
   private static final Lock CLASS_LOCK = new ReentrantLock();
   private static final long WAIT_PERIOD = 100;
   private static Map<String, RealNettyHttpServer> SERVERS = new HashMap<String, RealNettyHttpServer>();
   static final ChannelGroup channelGroup = new DefaultChannelGroup("rmBackPortsServer");

   private final Object instanceLock = new Object();
   private final String scheme;
   private final String host;
   private final int port;
   private boolean started;
   private boolean stopped;
   private boolean terminated;
   private ChannelFactory factory;
   private NettyInvocationHandler handler;
   
   private RealNettyHttpServer(String scheme, String host, int port)
   {
      super();
      this.scheme = scheme;
      this.host = host;
      this.port = port;
      try
      {
         factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

         ServerBootstrap bootstrap = new ServerBootstrap(factory);
         this.handler = new NettyInvocationHandler();
         WSServerPipelineFactory channelPipelineFactory = new WSServerPipelineFactory();
         channelPipelineFactory.setRequestHandler(this.handler);
         bootstrap.setPipelineFactory(channelPipelineFactory);
         bootstrap.setOption("child.tcpNoDelay", true);
         bootstrap.setOption("child.keepAlive", true);
         // Bind and start to accept incoming connections.
         Channel c = bootstrap.bind(new InetSocketAddress(this.port));
         channelGroup.add(c);
         if (LOG.isDebugEnabled())
            LOG.debug("Netty http server started on port: " + this.port);
      }
      catch (Exception e)
      {
         LOG.warn(e.getMessage(), e);
         throw new WebServiceException(e.getMessage(), e);
      }
   }
   
   public final void registerCallback(NettyCallbackHandler callbackHandler)
   {
      this.handler.registerCallback(callbackHandler);
   }
   
   public final void unregisterCallback(NettyCallbackHandler callbackHandler)
   {
      this.handler.unregisterCallback(callbackHandler);
      if (!this.hasMoreCallbacks())
      {
         this.terminate();
      }
   }
   
   public final NettyCallbackHandler getCallback(String requestPath)
   {
      return this.handler.getCallback(requestPath);
   }
   
   public final boolean hasMoreCallbacks()
   {
      return this.handler.hasMoreCallbacks();
   }

   public final String getScheme()
   {
      return this.scheme;
   }
   
   public final String getHost()
   {
      return this.host;
   }
   
   public final int getPort()
   {
      return this.port;
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
               LOG.debug("serving requests");
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
         SERVERS.remove(scheme + "://" + host + ":" + port + "/");
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
      }
   }
   
   /**
    * Starts back ports server on the background if method is called for the first time
    * @param scheme protocol
    * @param host hostname
    * @param port port
    * @return netty http server
    */
   public static RealNettyHttpServer getInstance(String scheme, String host, int port)
   {
      CLASS_LOCK.lock();
      try
      {
         String key = scheme + "://" + host + ":" + port + "/";
         RealNettyHttpServer server = SERVERS.get(key);
         if (server == null)
         {
            server = new RealNettyHttpServer(scheme, host, (port == -1) ? 80 : port); 
            SERVERS.put(key, server);
            // forking back ports server
            Thread t  = new Thread(server, "NettyHttpServer listening on " + key);
            t.setDaemon(true);
            t.start();
            // registering shutdown hook
            final RealNettyHttpServer s = server;
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
               public void run()
               {
                  s.terminate();
               }
            }, "NettyHttpServerShutdownHook(" + key + ")"));
         }
         else
         {
            boolean schemeEquals = server.getScheme().equals(scheme);
            boolean hostEquals = server.getHost().equals(host);
            boolean portEquals = server.getPort() == ((port == -1) ? 80 : port);
            if ((schemeEquals == false) || (hostEquals == false) || (portEquals == false))
               throw new IllegalArgumentException();
         }
         return server;
      }
      finally
      {
         CLASS_LOCK.unlock();
      }
   }
   
}
