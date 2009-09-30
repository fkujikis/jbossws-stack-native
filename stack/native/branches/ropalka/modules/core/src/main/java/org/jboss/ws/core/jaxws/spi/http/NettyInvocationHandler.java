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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.wsf.common.injection.InjectionHelper;
import org.jboss.wsf.common.injection.PreDestroyHolder;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.spi.invocation.InvocationContext;
import org.jboss.wsf.spi.invocation.RequestHandler;

/**
 * TODO: javadoc
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class NettyInvocationHandler extends SimpleChannelUpstreamHandler
{
   private static final Logger LOG = Logger.getLogger(NettyInvocationHandler.class);
   private final Lock lock = new ReentrantLock();
   protected Endpoint endpoint;
   private List<PreDestroyHolder> preDestroyRegistry = new LinkedList<PreDestroyHolder>();

   public NettyInvocationHandler()
   {
   }
   
   @Override
   public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
   {
      // HERE: Add all accepted channels to the group
      //       so that they are closed properly on shutdown
      //       If the added channel is closed before shutdown,
      //       it will be removed from the group automatically.
      RealNettyHttpServer.channelGroup.add(ctx.getChannel());
   } 

   @Override
   public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
   {
      // TODO: distinguish between GET and POST methods
      HttpRequest request = (HttpRequest)e.getMessage();
      ChannelBuffer content = request.getContent();
      OutputStream baos = new ByteArrayOutputStream();
      OutputStream outputStream = new BufferedOutputStream(baos);

      Map<String, Object> requestHeaders = new HashMap<String, Object>();
      for (String headerName : request.getHeaderNames())
      {
         requestHeaders.put(headerName, request.getHeaders(headerName));
      }
      boolean error = false;
      try
      {
         String requestPath = new URL(request.getUri()).getPath();
         handle(requestPath, getInputStream(content), outputStream);
      }
      catch (Throwable t)
      {
         error = true;
         LOG.error(t);
      }
      finally
      {
         writeResponse(e, request, error, ChannelBuffers.copiedBuffer(baos.toString(), "UTF-8"));
      }
   }
   
   private InputStream getInputStream(ChannelBuffer content)
   {
      return new ChannelBufferInputStream(content);
   }
   
   private void handle(String requestPath, InputStream inputStream, OutputStream outputStream) throws IOException
   {
      this.lock.lock();
      try
      {
         try
         {
            EndpointAssociation.setEndpoint(endpoint);
            RequestHandler requestHandler = endpoint.getRequestHandler();
            requestHandler.handleRequest(endpoint, inputStream, outputStream, new InvocationContext());
         }
         finally
         {
            this.registerForPreDestroy(endpoint);
            EndpointAssociation.removeEndpoint();
         }
      }
      finally
      {
         this.lock.unlock();
      }
   }
   
   protected final void postService()
   {
   }

   public final void destroy() // TODO: provide interface method to be called
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
               LOG.error(exception.getMessage(), exception);
            }
         }
         this.preDestroyRegistry.clear();
         this.preDestroyRegistry = null;
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

   private void writeResponse(MessageEvent e, HttpRequest request, boolean error, ChannelBuffer content)
   {
      // Build the response object.
      HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, error ? HttpResponseStatus.INTERNAL_SERVER_ERROR : HttpResponseStatus.OK);
      response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/xml; charset=UTF-8");
      if (!error)
      {
         response.setContent(content);
      }

      String cookieString = request.getHeader(HttpHeaders.Names.COOKIE);
      if (cookieString != null)
      {
         CookieDecoder cookieDecoder = new CookieDecoder();
         Set<Cookie> cookies = cookieDecoder.decode(cookieString);
         if (!cookies.isEmpty())
         {
            // Reset the cookies if necessary.
            CookieEncoder cookieEncoder = new CookieEncoder(true);
            for (Cookie cookie : cookies)
            {
               cookieEncoder.addCookie(cookie);
            }
            response.addHeader(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode());
         }
      }

      // Write the response.
      ChannelFuture cf = e.getChannel().write(response);
      cf.awaitUninterruptibly();
   }

   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
   {
      e.getCause().printStackTrace();
      e.getChannel().close();
   }

}
