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
package org.jboss.ws.core.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.xml.rpc.Stub;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.addressing.EndpointReference;

import org.jboss.logging.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.security.Base64Encoder;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.MessageAbstraction;
import org.jboss.ws.core.MessageTrace;
import org.jboss.ws.core.StubExt;
import org.jboss.ws.core.WSTimeoutException;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.extensions.wsrm.transport.RMChannel;
import org.jboss.ws.extensions.wsrm.transport.RMTransportHelper;
import org.jboss.ws.feature.FastInfosetFeature;
import org.jboss.ws.metadata.config.CommonConfig;
import org.jboss.ws.metadata.config.EndpointProperty;
import org.jboss.ws.metadata.umdm.EndpointMetaData;

/**
 * SOAPConnection implementation.
 * <p/>
 *
 * Per default HTTP 1.1 chunked encoding is used.
 * This may be ovverriden through {@link org.jboss.ws.metadata.config.EndpointProperty#CHUNKED_ENCODING_SIZE}.
 * A chunksize value of zero disables chunked encoding.
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason@stacksmash.com">Jason T. Greene</a>
 *
 * @since 02-Feb-2005
 */
public abstract class HTTPRemotingConnection implements RemoteConnection
{
   // provide logging
   private static Logger log = Logger.getLogger(HTTPRemotingConnection.class);

   //   private Map<String, Object> clientConfig = new HashMap<String, Object>();

   //   private static Map<String, String> metadataMap = new HashMap<String, String>();
   //   static
   //   {
   //      metadataMap.put(Stub.USERNAME_PROPERTY, "http.basic.username");
   //      metadataMap.put(Stub.PASSWORD_PROPERTY, "http.basic.password");
   //      metadataMap.put(BindingProvider.USERNAME_PROPERTY, "http.basic.username");
   //      metadataMap.put(BindingProvider.PASSWORD_PROPERTY, "http.basic.password");
   //   }
   //   private static Map<String, String> configMap = new HashMap<String, String>();
   //   static
   //   {
   //      configMap.put(StubExt.PROPERTY_KEY_ALIAS, "org.jboss.remoting.keyAlias");
   //      configMap.put(StubExt.PROPERTY_KEY_STORE, "org.jboss.remoting.keyStore");
   //      configMap.put(StubExt.PROPERTY_KEY_STORE_ALGORITHM, "org.jboss.remoting.keyStoreAlgorithm");
   //      configMap.put(StubExt.PROPERTY_KEY_STORE_PASSWORD, "org.jboss.remoting.keyStorePassword");
   //      configMap.put(StubExt.PROPERTY_KEY_STORE_TYPE, "org.jboss.remoting.keyStoreType");
   //      configMap.put(StubExt.PROPERTY_SOCKET_FACTORY, "socketFactoryClassName");
   //      configMap.put(StubExt.PROPERTY_SSL_PROTOCOL, "org.jboss.remoting.sslProtocol");
   //      configMap.put(StubExt.PROPERTY_SSL_PROVIDER_NAME, "org.jboss.remoting.sslProviderName");
   //      configMap.put(StubExt.PROPERTY_TRUST_STORE, "org.jboss.remoting.trustStore");
   //      configMap.put(StubExt.PROPERTY_TRUST_STORE_ALGORITHM, "org.jboss.remoting.truststoreAlgorithm");
   //      configMap.put(StubExt.PROPERTY_TRUST_STORE_PASSWORD, "org.jboss.remoting.trustStorePassword");
   //      configMap.put(StubExt.PROPERTY_TRUST_STORE_TYPE, "org.jboss.remoting.trustStoreType");
   //   }

   private boolean closed;
   private Integer chunkedLength;

   private static final RMChannel RM_CHANNEL = RMChannel.getInstance();

   public HTTPRemotingConnection()
   {
      //      // HTTPClientInvoker connect sends gratuitous POST
      //      // http://jira.jboss.com/jira/browse/JBWS-711
      //      clientConfig.put(Client.ENABLE_LEASE, false);
      //      clientConfig.put(HTTPClientInvoker.UNMARSHAL_NULL_STREAM, "true");
   }

   public boolean isClosed()
   {
      return closed;
   }

   public void setClosed(boolean closed)
   {
      this.closed = closed;
   }

   public Integer getChunkedLength()
   {
      return chunkedLength;
   }

   public void setChunkedLength(Integer chunkedLength)
   {
      this.chunkedLength = chunkedLength;
   }

   public MessageAbstraction invoke(MessageAbstraction reqMessage, Object endpoint, boolean oneway) throws IOException
   {
      return this.invoke(reqMessage, endpoint, oneway, true);
   }

   /** 
    * Sends the given message to the specified endpoint. 
    * 
    * A null reqMessage signifies a HTTP GET request.
    */
   public MessageAbstraction invoke(MessageAbstraction reqMessage, Object endpoint, boolean oneway, boolean maintainSession) throws IOException
   {
//      System.out.println("Entro...");
      if (endpoint == null)
         throw new IllegalArgumentException("Given endpoint cannot be null");

      if (closed)
         throw new IOException("Connection is already closed");

      Long timeout = null;
      String targetAddress;
      Map<String, Object> callProps = new HashMap<String, Object>();

      if (endpoint instanceof EndpointInfo)
      {
         EndpointInfo epInfo = (EndpointInfo)endpoint;
         targetAddress = epInfo.getTargetAddress();
         callProps = epInfo.getProperties();

         if (callProps.containsKey(StubExt.PROPERTY_CLIENT_TIMEOUT))
         {
            timeout = new Long(callProps.get(StubExt.PROPERTY_CLIENT_TIMEOUT).toString());
         }

      }
      else if (endpoint instanceof EndpointReference)
      {
         EndpointReference epr = (EndpointReference)endpoint;
         targetAddress = epr.getAddress().toString();
      }
      else
      {
         targetAddress = endpoint.toString();
      }

      //Netty client
      //      Map<String, Object> metadata = getMetadata(reqMessage, callProps);
      UnMarshaller unmarshaller = getUnmarshaller();

      ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
      //      ChannelFactory factory = new NioClientSocketChannelFactory(clientExecutor, clientExecutor);

      ClientBootstrap bootstrap = new ClientBootstrap(factory);
      WSClientPipelineFactory channelPipelineFactory = new WSClientPipelineFactory();
      WSResponseHandler responseHandler = null;
      if (!oneway || maintainSession)
      {
         responseHandler = new WSResponseHandler(unmarshaller);
         channelPipelineFactory.setResponseHandler(responseHandler);
      }
      bootstrap.setPipelineFactory(channelPipelineFactory);

      if (RMTransportHelper.isRMMessage(callProps))
      {
         //         try
         //         {
         //         RMMetadata rmMetadata = new RMMetadata(null, targetAddress, marshaller, unmarshaller, callProps, metadata, null); //TODO!! remoting version, client config, etc.
         //         return RM_CHANNEL.send(reqMessage, rmMetadata);
         //         }
         //         catch (Throwable t)
         //         {
         //            IOException io = new IOException();
         //            io.initCause(t);
         //            throw io;
         //         }
         return null; //TODO!!!
      }
      else
      {
         Channel channel = null;
         try
         {
//            System.out.println(new Date() + " Inizio connection attempt...");
            //Start the connection attempt
            URL target;
            try
            {
               System.out.println("targetAddress: "+targetAddress);
               target = new URL(targetAddress);
               System.out.println("target.getHost: "+target.getHost());
               System.out.println("target.getPort: "+target.getPort());
            }
            catch (MalformedURLException e)
            {
               throw new RuntimeException("Invalid address: " + targetAddress, e);
            }
            ChannelFuture future = bootstrap.connect(getSocketAddress(target));

            //Wait until the connection attempt succeeds or fails
            awaitUninterruptibly(future, timeout);
            if (!future.isSuccess())
            {
               IOException io = new IOException("Could not connect to " + target.getHost());
               io.initCause(future.getCause());
               factory.releaseExternalResources();
               throw io;
            }
            channel = future.getChannel();

            //Trace the outgoing message
            MessageTrace.traceMessage("Outgoing Request Message", reqMessage);

            //Send the HTTP request
            HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, reqMessage != null ? HttpMethod.POST : HttpMethod.GET, targetAddress);
            request.addHeader(HttpHeaders.Names.HOST, target.getHost());
            request.addHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            Map<String, Object> additionalHeaders = new HashMap<String, Object>();
            populateHeaders(reqMessage, additionalHeaders);
            setAdditionalHeaders(request, additionalHeaders);
            setActualChunkedLength(request);
            setAuthorization(request, callProps);

            writeRequest(channel, request, reqMessage);

//            System.out.println("oneway=" + oneway + " maintainSession=" + maintainSession);
            if (oneway && !maintainSession)
            {
               //No need to wait for the connection to be closed
               return null;
            }
            //Wait for the server to close the connection
            ChannelFuture closeFuture = channel.getCloseFuture();
            awaitUninterruptibly(closeFuture, timeout);
            if (responseHandler.getError() != null)
            {
               throw responseHandler.getError();
            }
            MessageAbstraction resMessage = null;
            Map<String, Object> resHeaders = null;
            if (!oneway)
            {
               //Get the response
               resMessage = responseHandler.getResponseMessage();
               resHeaders = responseHandler.getResponseHeaders();
            }
            //Update props with response headers (required to maintain session using cookies)
            callProps.clear();
            if (resHeaders != null)
            {
               callProps.putAll(resHeaders);
            }

            //Trace the incoming response message
            MessageTrace.traceMessage("Incoming Response Message", resMessage);
//            System.out.println(new Date() + " Fatto.");
            return resMessage;
         }
         catch (IOException ioe)
         {
            ioe.printStackTrace();
            throw ioe;
         }
         catch (WSTimeoutException toe)
         {
            throw toe;
         }
         catch (Throwable t)
         {
            t.printStackTrace();
            IOException io = new IOException("Could not transmit message");
            io.initCause(t);
            throw io;
         }
         finally
         {
//            System.out.println("Mi preparo a rilasciare...");
            if (channel != null)
            {
               channel.close();
            }
            //Shut down executor threads to exit
            factory.releaseExternalResources();
//            System.out.println("Rilasciato");
         }
      }
   }
   
   private InetSocketAddress getSocketAddress(URL target)
   {
      int port = target.getPort();
      if (port < 0)
      {
         //use default port
         String protocol = target.getProtocol();
         if ("http".equalsIgnoreCase(protocol))
         {
            port = 80;
         }
         else if ("https".equalsIgnoreCase(protocol))
         {
            port = 443;
         }
      }
      return new InetSocketAddress(target.getHost(), port);
   }
   
   private void writeRequest(Channel channel, HttpRequest request, MessageAbstraction reqMessage) throws IOException
   {
      if (reqMessage == null)
      {
         channel.write(request);
      }
      else
      {
         ChannelBuffer content = ChannelBuffers.dynamicBuffer();
         OutputStream os = new ChannelBufferOutputStream(content);
         getMarshaller().write(reqMessage, os);
         if (request.isChunked())
         {
            //TODO!! handle chunks here...
            channel.write(request);
      
            HttpChunk chunk = new DefaultHttpChunk(content);
            channel.write(chunk);
      
            channel.write(HttpChunk.LAST_CHUNK);
         }
         else
         {
            request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(content.writerIndex()));
            request.setContent(content);
            channel.write(request);
         }
      }
   }

   /**
    * Utility method for awaiting with or without timeout (timeout == null or <=0 implies not timeout)
    * 
    * @param future
    * @param timeout
    * @throws WSTimeoutException
    */
   private static void awaitUninterruptibly(ChannelFuture future, Long timeout) throws WSTimeoutException
   {
//      System.out.println(new Date() + " Inizio attesa...");
      if (timeout != null && timeout.longValue() > 0)
      {
         boolean bool = future.awaitUninterruptibly(timeout);
         if (!bool)
         {
            throw new WSTimeoutException("Timeout after: " + timeout + "ms", timeout);
         }
      }
      else
      {
         future.awaitUninterruptibly();
      }
//      System.out.println(new Date() + " Fine attesa.");
   }

   protected void setActualChunkedLength(HttpRequest message)
   {
      if (HttpMethod.POST.equals(message.getMethod()))
      {
         CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
         //We always use chunked transfer encoding 
         int chunkSizeValue = (chunkedLength != null ? chunkedLength : 1024);
         // Overwrite, through endpoint config
         if (msgContext != null)
         {
            EndpointMetaData epMetaData = msgContext.getEndpointMetaData();
            CommonConfig config = epMetaData.getConfig();
   
            String sizeValue = config.getProperty(EndpointProperty.CHUNKED_ENCODING_SIZE);
            if (sizeValue != null)
               chunkSizeValue = Integer.valueOf(sizeValue);
            if (epMetaData.isFeatureEnabled(FastInfosetFeature.class))
               chunkSizeValue = 0;
         }
         if (chunkSizeValue > 0)
         {
            message.addHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
         }
      }
   }

   protected void setAuthorization(HttpMessage message, Map callProps) throws IOException
   {
      //Get authentication type, default to BASIC authetication
      String authType = (String)callProps.get(StubExt.PROPERTY_AUTH_TYPE);
      if (authType == null)
         authType = StubExt.PROPERTY_AUTH_TYPE_BASIC;
      String username = (String)callProps.get(Stub.USERNAME_PROPERTY);
      String password = (String)callProps.get(Stub.PASSWORD_PROPERTY);
      if (username == null || password == null)
      {
         username = (String)callProps.get(BindingProvider.USERNAME_PROPERTY);
         password = (String)callProps.get(BindingProvider.PASSWORD_PROPERTY);
      }
      if (username != null && password != null)
      {
         if (authType.equals(StubExt.PROPERTY_AUTH_TYPE_BASIC))
         {
            message.addHeader(HttpHeaders.Names.AUTHORIZATION, getBasicAuthHeader(username, password));
         }
      }
   }

   private static String getBasicAuthHeader(String username, String password) throws IOException
   {
      return "Basic " + Base64Encoder.encode(username + ":" + password);
   }

   protected void setAdditionalHeaders(HttpMessage message, Map<String, Object> headers)
   {
      for (String key : headers.keySet())
      {
         try
         {
            String header = (String)headers.get(key);
            message.addHeader(key, header.replaceAll("[\r\n\f]", " "));
         }
         catch (Exception e)
         {
            e.printStackTrace();
            throw new RuntimeException(e);
         }
      }
   }

   //   private Map<String, Object> createRemotingMetaData(MessageAbstraction reqMessage, Map callProps)
   //   {
   //      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
   //
   //      Map<String, Object> metadata = new HashMap<String, Object>();
   //
   //      // We need to unmarshall faults (HTTP 500)
   //      // metadata.put(HTTPMetadataConstants.NO_THROW_ON_ERROR, "true"); // since 2.0.0.GA
   //      metadata.put("NoThrowOnError", "true");
   //
   //      if (reqMessage != null)
   //      {
   //         populateHeaders(reqMessage, metadata);
   //
   //         // Enable chunked encoding. This is the default size. 
   //         int chunkSizeValue = (chunkedLength != null ? chunkedLength : 1024);
   //
   //         // Overwrite, through endpoint config
   //         if (msgContext != null)
   //         {
   //            EndpointMetaData epMetaData = msgContext.getEndpointMetaData();
   //            CommonConfig config = epMetaData.getConfig();
   //
   //            String sizeValue = config.getProperty(EndpointProperty.CHUNKED_ENCODING_SIZE);
   //            if (sizeValue != null)
   //               chunkSizeValue = Integer.valueOf(sizeValue);
   //
   //            if (epMetaData.isFeatureEnabled(FastInfosetFeature.class))
   //               chunkSizeValue = 0;
   //         }
   //
   //         if (chunkSizeValue > 0)
   //         {
   //            clientConfig.put("chunkedLength", String.valueOf(chunkSizeValue));
   //         }
   //         else
   //         {
   //            clientConfig.remove("chunkedLength");
   //         }
   //      }
   //      else
   //      {
   //         metadata.put("TYPE", "GET");
   //      }
   //
   //      if (callProps != null)
   //      {
   //         Iterator it = callProps.entrySet().iterator();
   //
   //         // Get authentication type, default to BASIC authetication
   //         String authType = (String)callProps.get(StubExt.PROPERTY_AUTH_TYPE);
   //         if (authType == null)
   //            authType = StubExt.PROPERTY_AUTH_TYPE_BASIC;
   //
   //         while (it.hasNext())
   //         {
   //            Map.Entry entry = (Map.Entry)it.next();
   //            String key = (String)entry.getKey();
   //            Object val = entry.getValue();
   //
   //            // pass properties to remoting meta data
   //            if (metadataMap.containsKey(key))
   //            {
   //               String remotingKey = metadataMap.get(key);
   //               if ("http.basic.username".equals(remotingKey) || "http.basic.password".equals(remotingKey))
   //               {
   //                  if (authType.equals(StubExt.PROPERTY_AUTH_TYPE_BASIC))
   //                  {
   //                     metadata.put(remotingKey, val);
   //                  }
   //                  else
   //                  {
   //                     log.warn("Ignore '" + key + "' with auth typy: " + authType);
   //                  }
   //               }
   //               else
   //               {
   //                  metadata.put(remotingKey, val);
   //               }
   //            }
   //
   //            // pass properties to remoting client config
   //            if (configMap.containsKey(key))
   //            {
   //               String remotingKey = configMap.get(key);
   //               clientConfig.put(remotingKey, val);
   //            }
   //         }
   //      }
   //
   //      return metadata;
   //   }

   protected void populateHeaders(MessageAbstraction reqMessage, Map<String, Object> metadata)
   {
      if (reqMessage != null)
      {
         MimeHeaders mimeHeaders = reqMessage.getMimeHeaders();

         Iterator i = mimeHeaders.getAllHeaders();
         while (i.hasNext())
         {
            MimeHeader header = (MimeHeader)i.next();
            Object currentValue = metadata.get(header.getName());

            /*
             * Coalesce multiple headers into one
             *
             * From HTTP/1.1 RFC 2616:
             *
             * Multiple message-header fields with the same field-name MAY be
             * present in a message if and only if the entire field-value for that
             * header field is defined as a comma-separated list [i.e., #(values)].
             * It MUST be possible to combine the multiple header fields into one
             * "field-name: field-value" pair, without changing the semantics of
             * the message, by appending each subsequent field-value to the first,
             * each separated by a comma.
             */
            if (currentValue != null)
            {
               metadata.put(header.getName(), currentValue + "," + header.getValue());
            }
            else
            {
               metadata.put(header.getName(), header.getValue());
            }
         }
      }
   }
}
