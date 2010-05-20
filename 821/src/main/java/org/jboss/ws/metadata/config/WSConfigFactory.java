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
package org.jboss.ws.metadata.config;

//$Id$

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedInitParamMetaData;
import org.jboss.xb.binding.JBossXBException;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.binding.sunday.unmarshalling.XsdBinder;
import org.xml.sax.Attributes;

/** 
 * A factory for the JBossWS endpoint/client configuration 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 18-Dec-2005
 */
public class WSConfigFactory implements ObjectModelFactory
{
   // provide logging
   private final Logger log = Logger.getLogger(WSConfigFactory.class);

   // Hide constructor
   private WSConfigFactory()
   {
   }

   /** Create a new instance of the factory
    */
   public static WSConfigFactory newInstance()
   {
      return new WSConfigFactory();
   }

   public WSConfig parseWithSchemaBinding(URL configURL) throws IOException, JBossXBException
   {
      log.debug("parse: " + configURL);

      String configSchema = "schema/jbossws_config_1_0.xsd";
      URL xsdURL = Thread.currentThread().getContextClassLoader().getResource(configSchema);
      if (xsdURL == null)
         throw new WSException("Cannot find: " + configSchema);

      InputStream xsd = xsdURL.openStream();
      SchemaBinding schemaBinding = XsdBinder.bind(xsd, "UTF-8");

      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      WSConfig wsConfig = (WSConfig)unmarshaller.unmarshal(configURL.openStream(), schemaBinding);
      return wsConfig;
   }

   // Below is ObjectModelFactory stuff, which should be removed when parseWithSchemaBinding works. 
   
   public WSConfig parseWithObjectModelFactory(URL configURL) throws IOException, JBossXBException
   {
      log.debug("parse: " + configURL);

      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      WSConfig wsConfig = (WSConfig)unmarshaller.unmarshal(configURL.openStream(), this, null);
      return wsConfig;
   }

   public Object newRoot(Object root, UnmarshallingContext ctx, String namespaceURI, String localName, Attributes attrs)
   {
      return new WSConfig();
   }

   public Object completeRoot(Object root, UnmarshallingContext ctx, String namespaceURI, String localName)
   {
      return root;
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(WSConfig wsConfig, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      log.trace("WSConfig newChild: " + localName);
      if ("endpoint-config".equals(localName))
      {
         WSEndpointConfig wsEndpointConfig = new WSEndpointConfig();
         wsConfig.getEndpointConfig().add(wsEndpointConfig);
         return wsEndpointConfig;
      }
      if ("client-config".equals(localName))
      {
         WSClientConfig wsClientConfig = new WSClientConfig();
         wsConfig.getClientConfig().add(wsClientConfig);
         return wsClientConfig;
      }
      return null;
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(WSCommonConfig wsCommonConfig, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (log.isTraceEnabled())
         log.trace("WSCommonConfig setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);

      if (localName.equals("config-name"))
         wsCommonConfig.setConfigName(value);
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(WSCommonConfig wsCommonConfig, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      log.trace("WSCommonConfig newChild: " + localName);
      
      if ("pre-handler-chain".equals(localName))
      {
         WSHandlerChainConfig preHandlerChain = new WSHandlerChainConfig();
         wsCommonConfig.setPreHandlerChain(preHandlerChain);
         return preHandlerChain;
      }
      if ("post-handler-chain".equals(localName))
      {
         WSHandlerChainConfig postHandlerChain = new WSHandlerChainConfig();
         wsCommonConfig.setPostHandlerChain(postHandlerChain);
         return postHandlerChain;
      }
      return null;
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(WSHandlerChainConfig wsHandlerChainConfig, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (log.isTraceEnabled())
         log.trace("WSHandlerChainConfig setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);

      if (localName.equals("handler-chain-name"))
         wsHandlerChainConfig.setHandlerChainName(value);
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(WSHandlerChainConfig wsHandlerChainConfig, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      log.trace("WSHandlerChainConfig newChild: " + localName);
      if ("handler".equals(localName))
      {
         UnifiedHandlerMetaData handler = new UnifiedHandlerMetaData();
         List<UnifiedHandlerMetaData> handlers = wsHandlerChainConfig.getHandlers();
         handlers.add(handler);
         return handler;
      }
      return null;
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(UnifiedHandlerMetaData handler, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (log.isTraceEnabled())
         log.trace("UnifiedHandlerMetaData setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);

      if (localName.equals("handler-name"))
         handler.setHandlerName(value);
      else if (localName.equals("handler-class"))
         handler.setHandlerClass(value);
      else if (localName.equals("soap-header"))
         handler.addSoapHeader(navigator.resolveQName(value));
      else if (localName.equals("soap-role"))
         handler.addSoapRole(value);
      else if(localName.equals("port-name"))
         handler.addPortName(value);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(UnifiedInitParamMetaData param, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (log.isTraceEnabled())
         log.trace("UnifiedInitParamMetaData setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);

      if (localName.equals("param-name"))
         param.setParamName(value);
      else if (localName.equals("param-value"))
         param.setParamValue(value);
   }
}
