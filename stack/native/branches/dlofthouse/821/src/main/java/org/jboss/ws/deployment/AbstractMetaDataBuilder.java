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

// $Id$

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import javax.management.ObjectName;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.metadata.ServerEndpointMetaData;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;
import org.jboss.ws.metadata.wsse.WSSecurityConfigurationFactory;
import org.jboss.ws.metadata.j2ee.UnifiedApplicationMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedBeanMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedMessageDrivenMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedWebMetaData;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLEndpoint;
import org.jboss.ws.metadata.wsdl.WSDLService;
import org.jboss.ws.server.ServiceEndpointManager;
import org.jboss.ws.server.ServiceEndpointManagerFactory;
import org.jboss.ws.utils.ObjectNameFactory;

/** An abstract meta data builder.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-May-2005
 */
public abstract class AbstractMetaDataBuilder
{
   // provide logging
   private final static Logger log = Logger.getLogger(AbstractMetaDataBuilder.class);

   protected ClassLoader classLoader;
   protected URLClassLoader resourceLoader;

   public void setClassLoader(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
   }

   public void setResourceLoader(URLClassLoader resourceLoader)
   {
      this.resourceLoader = resourceLoader;
   }

   protected ObjectName getServiceEndpointID(UnifiedDeploymentInfo udi, ServerEndpointMetaData sepMetaData)
   {
      String endpoint = sepMetaData.getLinkName();
      String context = sepMetaData.getContextRoot();
      if (context.startsWith("/"))
         context = context.substring(1);

      StringBuilder idstr = new StringBuilder(ServerEndpointMetaData.SEPID_DOMAIN + ":");
      idstr.append(ServerEndpointMetaData.SEPID_PROPERTY_CONTEXT + "=" + context);
      idstr.append("," + ServerEndpointMetaData.SEPID_PROPERTY_ENDPOINT + "=" + endpoint);

      // Add JMS destination JNDI name for MDB endpoints
      if (udi.metaData instanceof UnifiedApplicationMetaData)
      {
         String ejbName = sepMetaData.getLinkName();
         if (ejbName == null)
            throw new WSException("Cannot obtain ejb-link from port component");

         UnifiedApplicationMetaData applMetaData = (UnifiedApplicationMetaData)udi.metaData;
         UnifiedBeanMetaData beanMetaData = (UnifiedBeanMetaData)applMetaData.getBeanByEjbName(ejbName);
         if (beanMetaData == null)
            throw new WSException("Cannot obtain ejb meta data for: " + ejbName);

         if (beanMetaData instanceof UnifiedMessageDrivenMetaData)
         {
            UnifiedMessageDrivenMetaData mdMetaData = (UnifiedMessageDrivenMetaData)beanMetaData;
            String jndiName = mdMetaData.getDestinationJndiName();
            idstr.append(",jms=" + jndiName);
         }
      }

      return ObjectNameFactory.create(idstr.toString());
   }

   /** Get the web service address for a given path
    */
   public String getServiceEndpointAddress(String uriScheme, String servicePath)
   {
      if (servicePath == null || servicePath.length() == 0)
         throw new WSException("Service path cannot be null");

      if (servicePath.endsWith("/*"))
         servicePath = servicePath.substring(0, servicePath.length() - 2);

      if (uriScheme == null)
         uriScheme = "http";

      ServiceEndpointManagerFactory factory = ServiceEndpointManagerFactory.getInstance();
      ServiceEndpointManager epManager = factory.getServiceEndpointManager();
      String host = epManager.getWebServiceHost();
      int port = epManager.getWebServicePort();
      if ("https".equals(uriScheme))
         port = epManager.getWebServiceSecurePort();

      String urlStr = uriScheme + "://" + host + ":" + port + servicePath;
      try
      {
         return new URL(urlStr).toExternalForm();
      }
      catch (MalformedURLException e)
      {
         throw new WSException("Malformed URL: " + urlStr);
      }
   }

   /** Replace the address locations for a given port component.
    */
   protected void replaceAddressLocation(ServerEndpointMetaData epMetaData)
   {
      WSDLDefinitions wsdlDefinitions = epMetaData.getServiceMetaData().getWsdlDefinitions();
      QName portName = epMetaData.getName();

      boolean endpointFound = false;
      for (WSDLService wsdlService : wsdlDefinitions.getServices())
      {
         for (WSDLEndpoint wsdlEndpoint : wsdlService.getEndpoints())
         {
            QName wsdlPortName = wsdlEndpoint.getQName();
            if (wsdlPortName.equals(portName))
            {
               endpointFound = true;

               String orgAddress = wsdlEndpoint.getAddress();
               String uriScheme = getUriScheme(orgAddress);

               String transportGuarantee = epMetaData.getTransportGuarantee();
               if ("CONFIDENTIAL".equals(transportGuarantee))
                  uriScheme = "https";

               String servicePath = epMetaData.getContextRoot() + epMetaData.getURLPattern();
               String serviceEndpointURL = getServiceEndpointAddress(uriScheme, servicePath);

               ServiceEndpointManagerFactory factory = ServiceEndpointManagerFactory.getInstance();
               ServiceEndpointManager epManager = factory.getServiceEndpointManager();
               boolean alwaysModify = epManager.isAlwaysModifySOAPAddress();

               if (alwaysModify || uriScheme == null || orgAddress.indexOf("REPLACE_WITH_ACTUAL_URL") >= 0)
               {
                  log.debug("Replace service endpoint address '" + orgAddress + "' with '" + serviceEndpointURL + "'");
                  wsdlEndpoint.setAddress(serviceEndpointURL);
                  epMetaData.setEndpointAddress(serviceEndpointURL);

                  // modify the wsdl-1.1 definition
                  if (wsdlDefinitions.getWsdlOneOneDefinition() != null)
                     replaceWSDL11SOAPAddress(wsdlDefinitions, portName, serviceEndpointURL);
               }
               else
               {
                  log.debug("Don't replace service endpoint address '" + orgAddress + "'");
                  try
                  {
                     epMetaData.setEndpointAddress(new URL(orgAddress).toExternalForm());
                  }
                  catch (MalformedURLException e)
                  {
                     throw new WSException("Malformed URL: " + orgAddress);
                  }
               }
            }
         }
      }

      if (endpointFound == false)
         throw new WSException("Cannot find service endpoint '" + portName + "' in wsdl document");
   }

   private void replaceWSDL11SOAPAddress(WSDLDefinitions wsdlDefinitions, QName portQName, String serviceEndpointURL)
   {
      Definition wsdlOneOneDefinition = wsdlDefinitions.getWsdlOneOneDefinition();
      String tnsURI = wsdlOneOneDefinition.getTargetNamespace();

      // search for matching portElement and replace the address URI
      Port wsdlOneOnePort = modifyPortAddress(tnsURI, portQName, serviceEndpointURL, wsdlOneOneDefinition.getServices());

      // recursivly process imports if none can be found
      if (wsdlOneOnePort == null && !wsdlOneOneDefinition.getImports().isEmpty())
      {

         Iterator imports = wsdlOneOneDefinition.getImports().values().iterator();
         while (imports.hasNext())
         {
            List l = (List)imports.next();
            Iterator importsByNS = l.iterator();
            while (importsByNS.hasNext())
            {
               Import anImport = (Import)importsByNS.next();

               wsdlOneOnePort = modifyPortAddress(anImport.getNamespaceURI(), portQName, serviceEndpointURL, anImport.getDefinition().getServices());
            }
         }
      }

      // if it still doesn't exist something is wrong
      if (wsdlOneOnePort == null)
         throw new IllegalArgumentException("Cannot find port with name '" + portQName + "' in wsdl document");
   }

   private Port modifyPortAddress(String tnsURI, QName portQName, String serviceEndpointURL, Map services)
   {

      Port wsdlOneOnePort = null;
      Iterator itServices = services.values().iterator();
      while (itServices.hasNext())
      {
         Service wsdlOneOneService = (Service)itServices.next();
         Map wsdlOneOnePorts = wsdlOneOneService.getPorts();
         Iterator itPorts = wsdlOneOnePorts.keySet().iterator();
         while (itPorts.hasNext())
         {
            String portLocalName = (String)itPorts.next();
            if (portQName.equals(new QName(tnsURI, portLocalName)))
            {
               wsdlOneOnePort = (Port)wsdlOneOnePorts.get(portLocalName);
               Iterator itElements = wsdlOneOnePort.getExtensibilityElements().iterator();
               while (itElements.hasNext())
               {
                  Object obj = itElements.next();
                  if (obj instanceof SOAPAddress)
                  {
                     SOAPAddress address = (SOAPAddress)obj;
                     address.setLocationURI(serviceEndpointURL);
                  }
               }
            }
         }
      }

      return wsdlOneOnePort;
   }

   private String getUriScheme(String addrStr)
   {
      try
      {
         URI addrURI = new URI(addrStr);
         String scheme = addrURI.getScheme();
         return scheme;
      }
      catch (URISyntaxException e)
      {
         return null;
      }
   }

   protected WSSecurityConfiguration getWsSecurityConfiguration(UnifiedDeploymentInfo udi) throws IOException
   {
      WSSecurityConfiguration config = null;

      String resource = WSSecurityConfigurationFactory.SERVER_RESOURCE_NAME;
      if (udi.metaData instanceof UnifiedWebMetaData)
      {
         resource = "WEB-INF/" + resource;
      }
      else
      {
         resource = "META-INF/" + resource;
      }

      URL location = resourceLoader.getResource(resource);
      if (location != null)
      {
         config = WSSecurityConfigurationFactory.newInstance().parse(location);

         // Get and set deployment path to the keystore file
         if (config.getKeyStoreFile() != null)
         {
            location = resourceLoader.getResource(config.getKeyStoreFile());
            if (location != null)
               config.setKeyStoreURL(location);
         }

         if (config.getTrustStoreFile() != null)
         {
            location = resourceLoader.getResource(config.getTrustStoreFile());
            if (location != null)
               config.setTrustStoreURL(location);
         }
      }

      return config;
   }
}
