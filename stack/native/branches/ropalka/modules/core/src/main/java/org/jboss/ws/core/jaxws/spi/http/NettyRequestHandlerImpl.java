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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
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
import org.jboss.netty.channel.Channel;
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
import org.jboss.ws.Constants;
import org.jboss.ws.core.client.transport.NettyTransportOutputStream;
import org.jboss.ws.core.server.netty.NettyCallbackHandler;
import org.jboss.ws.core.server.netty.AbstractNettyRequestHandler;
import org.jboss.wsf.spi.invocation.InvocationContext;

/**
 * Netty request handler for endpoint publish API.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class NettyRequestHandlerImpl extends AbstractNettyRequestHandler
{
   private static final Logger LOG = Logger.getLogger(NettyRequestHandlerImpl.class);

   public NettyRequestHandlerImpl()
   {
      super();
   }

   @Override
   public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
   {
      HttpRequest request = (HttpRequest) e.getMessage();
      ChannelBuffer content = request.getContent();
      OutputStream baos = new ByteArrayOutputStream();
      OutputStream outputStream = new BufferedOutputStream(baos);
      Integer statusCode = null;

      InvocationContext invCtx = new InvocationContext();
      Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
      Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>();
      invCtx.setProperty(Constants.NETTY_REQUEST_HEADERS, requestHeaders);
      invCtx.setProperty(Constants.NETTY_RESPONSE_HEADERS, responseHeaders);
      for (String headerName : request.getHeaderNames())
      {
         requestHeaders.put(headerName, request.getHeaders(headerName));
      }
      try
      {
         String requestPath = request.getUri();
         int paramIndex = requestPath.indexOf('?');
         if (paramIndex != -1)
         {
            requestPath = requestPath.substring(0, paramIndex);
         }
         String httpMethod = request.getMethod().getName();
         statusCode = handle(requestPath, httpMethod, getInputStream(content), outputStream, invCtx);
      }
      catch (Throwable t)
      {
         statusCode = 500;
         LOG.error(t);
      }
      finally
      {
         writeResponse(e, request, baos.toString(), statusCode, responseHeaders, ctx.getChannel());
      }
   }

   private InputStream getInputStream(ChannelBuffer content)
   {
      return new ChannelBufferInputStream(content);
   }

   private int handle(String requestPath, String httpMethod, InputStream inputStream, OutputStream outputStream,
         InvocationContext invCtx) throws IOException
   {
      boolean handlerExists = false;
      requestPath = truncateHostName(requestPath);
      NettyCallbackHandlerImpl handler = (NettyCallbackHandlerImpl) this.getCallback(requestPath);
      if (handler != null)
      {
         handlerExists = true;
         if (LOG.isDebugEnabled())
            LOG.debug("Handling request path: " + requestPath);

         return handler.handle(httpMethod, inputStream, outputStream, invCtx);
      }
      if (handlerExists == false)
         LOG.warn("No callback handler registered for path: " + requestPath);

      return 500;
   }

   private String truncateHostName(String s)
   {
      String retVal = s;
      if (s.startsWith("http"))
      {
         try
         {
            retVal = new URL(s).getPath();
         }
         catch (MalformedURLException mue)
         {
            LOG.error(mue.getMessage(), mue);
         }
      }

      while (retVal.endsWith("/"))
      {
         retVal = retVal.substring(0, retVal.length() - 1);
      }
      return retVal;
   }

   private void writeResponse(MessageEvent e, HttpRequest request, String content, int statusCode,
         Map<String, List<String>> responseHeaders, Channel channel) throws IOException
   {
      // Build the response object.
      HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(statusCode));

      Iterator<String> iterator = responseHeaders.keySet().iterator();
      String key = null;
      List<String> values = null;
      while (iterator.hasNext())
      {
         key = iterator.next();
         values = responseHeaders.get(key);
         values = removeProhibitedCharacters(values);
         response.setHeader(key, values);
      }
      if (!responseHeaders.containsKey(HttpHeaders.Names.CONTENT_TYPE))
      {
         response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/xml; charset=UTF-8");
         response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(content.length()));
         response.setContent(ChannelBuffers.copiedBuffer(content, "UTF-8"));
      }
      else
      {
         response.setHeader(HttpHeaders.Names.TRANSFER_ENCODING, "chunked");
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
      if (responseHeaders.containsKey(HttpHeaders.Names.CONTENT_TYPE))
      {
         NettyTransportOutputStream out = new NettyTransportOutputStream(channel, 1024);
         out.write(content.getBytes("UTF-8"));
         out.close();
         out.getChannelFuture().awaitUninterruptibly();
      }
      else
      {
         cf.awaitUninterruptibly();
      }
   }

   private List<String> removeProhibitedCharacters(List<String> values)
   {
      List<String> retVal = new LinkedList<String>();
      for (int i = 0; i < values.size(); i++)
      {
         retVal.add(i, removeProhibitedCharacters(values.get(i)));
      }

      return retVal;
   }

   private String removeProhibitedCharacters(String s)
   {
      // TODO: https://jira.jboss.org/jira/browse/NETTY-237
      String retVal = s;

      retVal = retVal.replace('\r', ' ');
      retVal = retVal.replace('\n', ' ');

      return retVal;
   }

}
