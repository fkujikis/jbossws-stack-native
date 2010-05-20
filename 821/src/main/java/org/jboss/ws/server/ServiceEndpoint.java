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
package org.jboss.ws.server;

// $Id$

import org.jboss.logging.Logger;
import org.jboss.util.xml.DOMWriter;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.binding.BindingException;
import org.jboss.ws.handler.ServerHandlerChain;
import org.jboss.ws.jaxrpc.SOAPFaultExceptionHelper;
import org.jboss.ws.metadata.EndpointMetaData;
import org.jboss.ws.metadata.ServerEndpointMetaData;
import org.jboss.ws.metadata.UnifiedMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData.HandlerType;
import org.jboss.ws.metadata.j2ee.UnifiedInitParamMetaData;
import org.jboss.ws.soap.*;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.server.ServletEndpointContext;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.*;
/**
 * This object registered with the ServiceEndpointManager service.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 16-Jan-2005
 */
public class ServiceEndpoint
{
   // provide logging
   private static Logger log = Logger.getLogger(ServiceEndpoint.class);
   private static Logger msgLog = Logger.getLogger("jbossws.SOAPMessage");

   /** Endpoint type enum */
   public enum State
   {
      CREATED, STARTED, STOPED, DESTROYED
   }

   // The deployment info for this endpoint
   protected ServiceEndpointInfo seInfo;
   // Some metrics for this endpoint
   protected ServiceEndpointMetrics seMetrics;

   public ServiceEndpoint(ServiceEndpointInfo seInfo)
   {
      this.seInfo = seInfo;
      this.seInfo.setState(State.CREATED);
      this.seMetrics = new ServiceEndpointMetrics(seInfo.getServiceEndpointID());
   }

   public ServiceEndpointInfo getServiceEndpointInfo()
   {
      return seInfo;
   }

   public ServiceEndpointMetrics getServiceEndpointMetrics()
   {
      return seMetrics;
   }

   public void create() throws Exception
   {
      seInfo.setState(State.CREATED);
   }

   public void start() throws Exception
   {
      // eagerly initialize the UMDM
      ServerEndpointMetaData epMetaData = seInfo.getServerEndpointMetaData();
      UnifiedMetaData wsMetaData = epMetaData.getServiceMetaData().getUnifiedMetaData();
      wsMetaData.eagerInitialize();

      seMetrics.start();
      seInfo.setState(State.STARTED);
   }

   public void stop()
   {
      seMetrics.stop();
      seInfo.setState(State.STOPED);
      log.debug("Stop Endpoint" + seMetrics);
   }

   public void destroy()
   {
      seInfo.setState(State.DESTROYED);
   }

   /** Handle a WSDL request or a request for an included resource
    */
   public void handleWSDLRequest(OutputStream outStream, URL requestURL, String resourcePath) throws IOException
   {
      ServiceEndpointInfo sepInfo = getServiceEndpointInfo();
      EndpointMetaData epMetaData = sepInfo.getServerEndpointMetaData();

      String urlString = requestURL.toExternalForm();
      String requestURI = requestURL.getPath();
      String hostPath = urlString.substring(0, urlString.indexOf(requestURI));

      WSDLRequestHandler wsdlRequestHandler = new WSDLRequestHandler(epMetaData);
      Document document = wsdlRequestHandler.getDocumentForPath(hostPath, requestURI, resourcePath);

      OutputStreamWriter writer = new OutputStreamWriter(outStream);
      new DOMWriter(writer).setPrettyprint(true).print(document.getDocumentElement());
      outStream.flush();
      outStream.close();
   }

   /**
    * Handle a request to this web service endpoint
    */
   public SOAPMessage handleRequest(HeaderSource headerSource, ServletEndpointContext context, InputStream inputStream) throws BindingException
   {
      boolean popMessageContext = false;

      // Associate a message context with the current thread if the caller has not done so already
      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
      if (msgContext == null)
      {
         msgContext = new SOAPMessageContextImpl();
         MessageContextAssociation.pushMessageContext(msgContext);
         popMessageContext = true;
      }

      ServerEndpointMetaData epMetaData = seInfo.getServerEndpointMetaData();
      msgContext.setEndpointMetaData(epMetaData);

      long beginProcessing = 0;
      ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         State state = seInfo.getState();
         if (state != State.STARTED)
         {
            QName faultCode = Constants.SOAP11_FAULT_CODE_SERVER;
            String faultString = "Endpoint cannot handle requests in state: " + state;
            throw new SOAPFaultException(faultCode, faultString, null, null);
         }

         log.debug("BEGIN handleRequest: " + seInfo.getServiceEndpointID());
         beginProcessing = seMetrics.processRequestMessage();

         // Initialize the handler chain
         if (seInfo.getJaxRpcHandlerChain() == null)
         {
            initHandlerChain(HandlerType.PRE);
            initHandlerChain(HandlerType.JAXRPC);
            initHandlerChain(HandlerType.POST);
         }

         MessageFactoryImpl msgFactory = new MessageFactoryImpl();
         msgFactory.setStyle(epMetaData.getStyle());

         MimeHeaders headers = (headerSource != null ? headerSource.getMimeHeaders() : null);
         SOAPMessageImpl reqMessage = (SOAPMessageImpl) msgFactory.createMessage(headers, inputStream);

         // Associate current message with message context
         msgContext.setMessage(reqMessage);

         // debug the incomming message
         if (msgLog.isDebugEnabled())
         {
            SOAPEnvelope soapEnv = reqMessage.getSOAPPart().getEnvelope();
            String envStr = SAAJElementWriter.printSOAPElement((SOAPElementImpl)soapEnv, true);
            msgLog.debug("Incomming SOAPMessage\n" + envStr);
         }

         // Set the thread context class loader
         ClassLoader classLoader = epMetaData.getClassLoader();
         Thread.currentThread().setContextClassLoader(classLoader);

         // Invoke the service endpoint
         ServiceEndpointInvoker seInvoker = seInfo.getInvoker();
         SOAPMessage resMessage = seInvoker.invoke(seInfo, context);

         postProcessResponse(headerSource, resMessage);

         return resMessage;
      }
      catch (Exception ex)
      {
         SOAPMessage resMessage = msgContext.getMessage();

         // In case we have an exception before the invoker is called
         // we create the fault message here.
         if (resMessage == null || ((SOAPMessageImpl)resMessage).isFaultMessage() == false)
         {
            resMessage = SOAPFaultExceptionHelper.exceptionToFaultMessage(ex);
            msgContext.setMessage(resMessage);
         }

         postProcessResponse(headerSource, resMessage);
         return resMessage;
      }
      finally
      {
         try
         {
            SOAPMessage soapMessage = msgContext.getMessage();
            if (soapMessage != null && soapMessage.getSOAPPart().getEnvelope() != null)
            {
               if (soapMessage.getSOAPPart().getEnvelope().getBody().getFault() != null)
               {
                  seMetrics.processFaultMessage(beginProcessing);
               }
               else
               {
                  seMetrics.processResponseMessage(beginProcessing);
               }
            }
         }
         catch (Exception ex)
         {
            log.error("Cannot process metrics", ex);
         }

         // Reset the message context association
         if (popMessageContext)
            MessageContextAssociation.popMessageContext();

         // Reset the thread context class loader
         Thread.currentThread().setContextClassLoader(ctxClassLoader);
         log.debug("END handleRequest: " + seInfo.getServiceEndpointID());
      }
   }

   /** Set response mime headers
    */
   private void postProcessResponse(HeaderSource headerSource, SOAPMessage resMessage)
   {
      try
      {
         // Set the outbound headers
         if (headerSource != null)
         {
            resMessage.saveChanges();
            headerSource.setMimeHeaders(resMessage.getMimeHeaders());
         }

         // debug the outgoing message
         if (msgLog.isDebugEnabled())
         {
            resMessage.saveChanges();
            SOAPEnvelope soapEnv = resMessage.getSOAPPart().getEnvelope();
            String envStr = SAAJElementWriter.printSOAPElement((SOAPElementImpl)soapEnv, true);
            msgLog.debug("Outgoing SOAPMessage\n" + envStr);
         }
      }
      catch (Exception ex)
      {
         throw new JAXRPCException("Cannot create or send response message", ex);
      }
   }

   /**
    * Init the handler chain
    */
   private void initHandlerChain(HandlerType type)
   {
      Set<String> handlerRoles = new HashSet<String>();
      List<HandlerInfo> infos = new ArrayList<HandlerInfo>();

      ServerEndpointMetaData sepMetaData = seInfo.getServerEndpointMetaData();
      for (UnifiedHandlerMetaData handlerMetaData : sepMetaData.getHandlers(type))
      {
         handlerRoles.addAll(Arrays.asList(handlerMetaData.getSoapRoles()));

         Class hClass;
         String handlerClass = handlerMetaData.getHandlerClass();
         try
         {
            // Load the handler class using the deployments top level CL
            ClassLoader classLoader = sepMetaData.getClassLoader();
            hClass = classLoader.loadClass(handlerClass);
         }
         catch (ClassNotFoundException e)
         {
            throw new WSException("Cannot load handler class: " + handlerClass);
         }

         HashMap<String, Object> hConfig = new HashMap<String, Object>();
         UnifiedInitParamMetaData[] params = handlerMetaData.getInitParams();
         for (int j = 0; j < params.length; j++)
         {
            UnifiedInitParamMetaData param = params[j];
            hConfig.put(param.getParamName(), param.getParamValue());
         }
         QName[] hHeaders = handlerMetaData.getSoapHeaders();
         HandlerInfo info = new HandlerInfo(hClass, hConfig, hHeaders);

         log.debug("Adding server side handler to service '" + sepMetaData.getName() + "': " + info);
         infos.add(info);
      }

      initHandlerChain(infos, handlerRoles, type);
   }

   public void initHandlerChain(List<HandlerInfo> infos, Set<String> handlerRoles, HandlerType type)
   {
      log.debug("Init handler chain with [" + infos.size() + "] handlers");

      ServerHandlerChain handlerChain = new ServerHandlerChain(infos, handlerRoles, type);
      if (type == HandlerType.PRE)
         seInfo.setPreHandlerChain(handlerChain);
      else if (type == HandlerType.JAXRPC)
         seInfo.setJaxRpcHandlerChain(handlerChain);
      else if (type == HandlerType.POST)
         seInfo.setPostHandlerChain(handlerChain);

      if (handlerChain.getState() == ServerHandlerChain.STATE_CREATED)
      {
         // what is the config for a handler chain?
         handlerChain.init(null);
      }
   }

   /**
    * Returns a string representation of the object.
    */
   public String toString()
   {
      StringBuilder buffer = new StringBuilder(seInfo.toString());
      buffer.append("\n state=" + seInfo.getState());
      return buffer.toString();
   }
}
