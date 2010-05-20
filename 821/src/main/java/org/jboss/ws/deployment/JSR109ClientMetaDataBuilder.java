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
package org.jboss.ws.deployment;

//$Id: JSR109ClientMetaDataBuilder.java 377 2006-05-18 13:57:29Z thomas.diesler@jboss.com $

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.metadata.ClientEndpointMetaData;
import org.jboss.ws.metadata.EndpointMetaData;
import org.jboss.ws.metadata.ServiceMetaData;
import org.jboss.ws.metadata.UnifiedMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedServiceRefMetaData;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMappingFactory;
import org.jboss.ws.metadata.jaxrpcmapping.ServiceEndpointInterfaceMapping;
import org.jboss.ws.metadata.wsdl.NCName;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLEndpoint;
import org.jboss.ws.metadata.wsdl.WSDLService;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;
import org.jboss.ws.metadata.wsse.WSSecurityConfigurationFactory;

/**
 * A client side meta data builder.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-May-2005
 */
public class JSR109ClientMetaDataBuilder extends JSR109MetaDataBuilder
{
   // provide logging
   private final Logger log = Logger.getLogger(JSR109ClientMetaDataBuilder.class);

   /** Build from WSDL and jaxrpc-mapping.xml
    */
   public ServiceMetaData buildMetaData(QName serviceQName, URL wsdlURL, URL mappingURL, URL securityURL, UnifiedServiceRefMetaData serviceRefMetaData)
   {
      try
      {
         JavaWsdlMapping javaWsdlMapping = null;
         if (mappingURL != null)
         {
            JavaWsdlMappingFactory mappingFactory = JavaWsdlMappingFactory.newInstance();
            javaWsdlMapping = mappingFactory.parse(mappingURL);
         }

         WSSecurityConfiguration securityConfig = null;
         if (securityURL != null)
         {
            securityConfig = WSSecurityConfigurationFactory.newInstance().parse(securityURL);
         }

         return buildMetaData(serviceQName, wsdlURL, javaWsdlMapping, securityConfig, serviceRefMetaData);
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WSException("Cannot build meta data: " + ex.getMessage(), ex);
      }
   }

   /** Build from WSDL and jaxrpc-mapping.xml
    */
   public ServiceMetaData buildMetaData(QName serviceQName, URL wsdlURL, JavaWsdlMapping javaWsdlMapping, WSSecurityConfiguration securityConfig,
         UnifiedServiceRefMetaData serviceRefMetaData)
   {
      log.debug("START buildMetaData: [service=" + serviceQName + "]");
      try
      {
         UnifiedMetaData wsMetaData = new UnifiedMetaData();
         wsMetaData.setResourceLoader(resourceLoader);
         wsMetaData.setClassLoader(classLoader);

         ServiceMetaData serviceMetaData = new ServiceMetaData(wsMetaData, serviceQName);
         wsMetaData.addService(serviceMetaData);

         serviceMetaData.setWsdlFile(wsdlURL.toExternalForm());
         WSDLDefinitions wsdlDefinitions = serviceMetaData.getWsdlDefinitions();

         URL mappingURL = null;
         if (javaWsdlMapping != null)
         {
            mappingURL = new URL(Constants.NS_JBOSSWS_URI + "/dummy-mapping-url");
            wsMetaData.addMappingDefinition(mappingURL.toExternalForm(), javaWsdlMapping);
            serviceMetaData.setJaxrpcMappingFile(mappingURL.toExternalForm());
         }

         if (securityConfig != null)
         {
            serviceMetaData.setSecurityConfiguration(securityConfig);
            setupSecurity(securityConfig);
         }

         buildMetaDataInternal(serviceMetaData, wsdlDefinitions, javaWsdlMapping, serviceRefMetaData);

         // eagerly initialize
         wsMetaData.eagerInitialize();

         log.debug("END buildMetaData: " + serviceMetaData);
         return serviceMetaData;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WSException("Cannot build meta data: " + ex.getMessage(), ex);
      }
   }

   private void buildMetaDataInternal(ServiceMetaData serviceMetaData, WSDLDefinitions wsdlDefinitions, JavaWsdlMapping javaWsdlMapping,
         UnifiedServiceRefMetaData serviceRefMetaData) throws IOException
   {
      QName serviceQName = serviceMetaData.getName();

      // Get the WSDL service
      WSDLService wsdlService = null;
      if (serviceQName == null)
      {
         if (wsdlDefinitions.getServices().length != 1)
            throw new IllegalArgumentException("Expected a single service element");

         wsdlService = wsdlDefinitions.getServices()[0];
         serviceMetaData.setName(wsdlService.getQName());
      }
      else
      {
         wsdlService = wsdlDefinitions.getService(new NCName(serviceQName.getLocalPart()));
      }
      if (wsdlService == null)
         throw new IllegalArgumentException("Cannot obtain wsdl service: " + serviceQName);

      // Build type mapping meta data
      setupTypesMetaData(serviceMetaData);

      // Build endpoint meta data
      for (WSDLEndpoint wsdlEndpoint : wsdlService.getEndpoints())
      {
         QName portName = wsdlEndpoint.getQName();
         ClientEndpointMetaData epMetaData = new ClientEndpointMetaData(serviceMetaData, portName);
         epMetaData.setEndpointAddress(wsdlEndpoint.getAddress());
         serviceMetaData.addEndpoint(epMetaData);

         // config-name, config-file
         if (serviceRefMetaData != null)
         {
            String configName = serviceRefMetaData.getConfigName();
            if (configName != null)
               epMetaData.setConfigName(configName);

            String configFile = serviceRefMetaData.getConfigFile();
            if (configFile != null)
               epMetaData.setConfigFile(configFile);
         }

         // Init the service encoding style
         initEndpointEncodingStyle(epMetaData);

         ServiceEndpointInterfaceMapping seiMapping = null;
         if (javaWsdlMapping != null)
         {
            QName portType = wsdlEndpoint.getInterface().getQName();
            seiMapping = javaWsdlMapping.getServiceEndpointInterfaceMappingByPortType(portType);
            if (seiMapping != null)
            {
               epMetaData.setServiceEndpointInterfaceName(seiMapping.getServiceEndpointInterface());
            }
            else
            {
               log.warn("Cannot obtain the SEI mapping for: " + portType);
            }
         }

         processEndpointMetaDataExtensions(epMetaData, wsdlDefinitions);
         setupOperationsFromWSDL(epMetaData, wsdlEndpoint, seiMapping);
         setupHandlers(serviceRefMetaData, portName, epMetaData);
      }
   }

   private void setupHandlers(UnifiedServiceRefMetaData serviceRefMetaData, QName portName, EndpointMetaData epMetaData)
   {
      // Setup the endpoint handlers
      if (serviceRefMetaData != null)
      {
         for (UnifiedHandlerMetaData handlerMetaData : serviceRefMetaData.getHandlers())
         {
            List portNames = Arrays.asList(handlerMetaData.getPortNames());
            if (portNames.size() == 0 || portNames.contains(portName.getLocalPart()))
            {
               epMetaData.addHandler(handlerMetaData);
            }
         }
      }
   }

   private void setupSecurity(WSSecurityConfiguration securityConfig)
   {
      if (securityConfig.getKeyStoreFile() != null)
      {
         URL location = resourceLoader.getResource(securityConfig.getKeyStoreFile());
         if (location != null)
            securityConfig.setKeyStoreURL(location);
      }

      if (securityConfig.getTrustStoreFile() != null)
      {
         URL location = resourceLoader.getResource(securityConfig.getTrustStoreFile());
         if (location != null)
            securityConfig.setTrustStoreURL(location);
      }
   }
}
