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
// $Id: JSR109ServerMetaDataBuilder.java 387 2006-05-20 14:45:47Z thomas.diesler@jboss.com $
package org.jboss.ws.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;
import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.util.xml.DOMUtils;
import org.jboss.ws.WSException;
import org.jboss.ws.metadata.EndpointMetaData;
import org.jboss.ws.metadata.ServerEndpointMetaData;
import org.jboss.ws.metadata.ServiceMetaData;
import org.jboss.ws.metadata.UnifiedMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedApplicationMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedBeanMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedEjbPortComponentMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedWebMetaData;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.ws.metadata.jaxrpcmapping.ServiceEndpointInterfaceMapping;
import org.jboss.ws.metadata.jsr109.PortComponentMetaData;
import org.jboss.ws.metadata.jsr109.WebserviceDescriptionMetaData;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLEndpoint;
import org.jboss.ws.metadata.wsdl.WSDLService;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;
import org.jboss.ws.metadata.wsse.WSSecurityConfigurationFactory;
import org.w3c.dom.Element;

/**
 * A server side meta data builder that is based on webservices.xml.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-May-2005
 */
public class JSR109ServerMetaDataBuilder extends JSR109MetaDataBuilder
{
   // provide logging
   final Logger log = Logger.getLogger(JSR109ServerMetaDataBuilder.class);

   /** Build from webservices.xml
    */
   public UnifiedMetaData buildMetaData(JSR109Deployment udi)
   {
      log.debug("START buildMetaData: [name=" + udi.getCanonicalName() + "]");
      try
      {
         WSSecurityConfiguration securityConfiguration = getWsSecurityConfiguration(udi);

         // For every webservice-description build the ServiceMetaData
         UnifiedMetaData wsMetaData = new UnifiedMetaData();
         wsMetaData.setResourceLoader(resourceLoader);
         wsMetaData.setClassLoader(classLoader);

         WebserviceDescriptionMetaData[] wsDescriptionArr = udi.getWebservicesMetaData().getWebserviceDescriptions();
         for (WebserviceDescriptionMetaData wsdMetaData : wsDescriptionArr)
         {
            ServiceMetaData serviceMetaData = new ServiceMetaData(wsMetaData, null);
            serviceMetaData.setWebserviceDescriptionName(wsdMetaData.getWebserviceDescriptionName());
            wsMetaData.addService(serviceMetaData);

            // Unmarshall the WSDL
            serviceMetaData.setWsdlFile(wsdMetaData.getWsdlFile());
            WSDLDefinitions wsdlDefinitions = serviceMetaData.getWsdlDefinitions();

            // Unmarshall the jaxrpc-mapping.xml
            serviceMetaData.setJaxrpcMappingFile(wsdMetaData.getJaxrpcMappingFile());
            JavaWsdlMapping javaWsdlMapping = serviceMetaData.getJavaWsdlMapping();

            // Build type mapping meta data
            setupTypesMetaData(serviceMetaData);

            // Assign the WS-Security configuration,
            serviceMetaData.setSecurityConfiguration(securityConfiguration);

            // For every port-component build the EndpointMetaData
            PortComponentMetaData[] pcMetaDataArr = wsdMetaData.getPortComponents();
            for (PortComponentMetaData pcMetaData : pcMetaDataArr)
            {
               QName portName = pcMetaData.getWsdlPort();

               // JBWS-722 
               // <wsdl-port> in webservices.xml should be qualified
               if (portName.getNamespaceURI().length() == 0)
               {
                  String nsURI = wsdlDefinitions.getTargetNamespace();
                  portName = new QName(nsURI, portName.getLocalPart());
                  log.warn("Adding wsdl targetNamespace to: " + portName);
                  pcMetaData.setWsdlPort(portName);
               }

               WSDLEndpoint wsdlEndpoint = getWsdlEndpoint(wsdlDefinitions, portName);
               if (wsdlEndpoint == null)
                  throw new WSException("Cannot find port in wsdl: " + portName);

               // set service name
               serviceMetaData.setName(wsdlEndpoint.getWsdlService().getQName());

               ServerEndpointMetaData sepMetaData = new ServerEndpointMetaData(serviceMetaData, portName);
               sepMetaData.setPortComponentName(pcMetaData.getPortComponentName());
               String ejbLink = pcMetaData.getEjbLink();
               String servletLink = pcMetaData.getServletLink();
               sepMetaData.setLinkName(servletLink != null ? servletLink : ejbLink);
               serviceMetaData.addEndpoint(sepMetaData);

               // Init the service encoding style
               initEndpointEncodingStyle(sepMetaData);

               if (udi.metaData instanceof UnifiedApplicationMetaData)
               {
                  UnifiedApplicationMetaData apMetaData = (UnifiedApplicationMetaData)udi.metaData;
                  wsMetaData.setSecurityDomain(apMetaData.getSecurityDomain());

                  // Copy the wsdl publish location from jboss.xml
                  String wsdName = serviceMetaData.getWebserviceDescriptionName();
                  String wsdlPublishLocation = apMetaData.getWsdlPublishLocationByName(wsdName);
                  serviceMetaData.setWsdlPublishLocation(wsdlPublishLocation);

                  // Copy <port-component> meta data
                  UnifiedBeanMetaData beanMetaData = (UnifiedBeanMetaData)apMetaData.getBeanByEjbName(ejbLink);
                  if (beanMetaData == null)
                     throw new WSException("Cannot obtain UnifiedBeanMetaData for: " + ejbLink);

                  String configName = apMetaData.getConfigName();
                  if (configName != null)
                     sepMetaData.setConfigName(configName);

                  String configFile = apMetaData.getConfigFile();
                  if (configFile != null)
                     sepMetaData.setConfigFile(configFile);

                  UnifiedEjbPortComponentMetaData bpcMetaData = beanMetaData.getPortComponent();
                  if (bpcMetaData != null)
                  {
                     if (bpcMetaData.getAuthMethod() != null)
                     {
                        String authMethod = bpcMetaData.getAuthMethod();
                        sepMetaData.setAuthMethod(authMethod);
                     }
                     if (bpcMetaData.getTransportGuarantee() != null)
                     {
                        String transportGuarantee = bpcMetaData.getTransportGuarantee();
                        sepMetaData.setTransportGuarantee(transportGuarantee);
                     }

                     sepMetaData.setURLPattern(bpcMetaData.getURLPattern());
                  }

                  initServicePathEJB(udi, sepMetaData, ejbLink);
               }
               else if (udi.metaData instanceof UnifiedWebMetaData)
               {
                  UnifiedWebMetaData webMetaData = (UnifiedWebMetaData)udi.metaData;
                  wsMetaData.setSecurityDomain(webMetaData.getSecurityDomain());

                  String targetBean = webMetaData.getServletClassMap().get(servletLink);
                  sepMetaData.setServiceEndpointImplName(targetBean);
                  
                  // Copy the wsdl publish location from jboss-web.xml
                  String wsdName = serviceMetaData.getWebserviceDescriptionName();
                  String wsdlPublishLocation = webMetaData.getWsdlPublishLocationByName(wsdName);
                  serviceMetaData.setWsdlPublishLocation(wsdlPublishLocation);

                  String configName = webMetaData.getConfigName();
                  if (configName != null)
                     sepMetaData.setConfigName(configName);

                  String configFile = webMetaData.getConfigFile();
                  if (configFile != null)
                     sepMetaData.setConfigFile(configFile);

                  initServicePathJSE(udi, sepMetaData, servletLink);
                  initTransportGuaranteeJSE(udi, sepMetaData, servletLink);
               }

               // init service endpoint id
               ObjectName sepID = getServiceEndpointID(udi, sepMetaData);
               sepMetaData.setServiceEndpointID(sepID);
               
               replaceAddressLocation(sepMetaData);

               String seiName = pcMetaData.getServiceEndpointInterface();
               sepMetaData.setServiceEndpointInterfaceName(seiName);

               ServiceEndpointInterfaceMapping seiMapping = javaWsdlMapping.getServiceEndpointInterfaceMapping(seiName);
               if (seiMapping == null)
                  log.warn("Cannot obtain SEI mapping for: " + seiName);

               // process endpoint meta extension
               processEndpointMetaDataExtensions(sepMetaData, wsdlDefinitions);

               // Setup the endpoint operations
               setupOperationsFromWSDL(sepMetaData, wsdlEndpoint, seiMapping);

               // Setup the endpoint handlers
               for (UnifiedHandlerMetaData handlerMetaData : pcMetaData.getHandlers())
               {
                  List portNames = Arrays.asList(handlerMetaData.getPortNames());
                  if (portNames.size() == 0 || portNames.contains(portName.getLocalPart()))
                  {
                     sepMetaData.addHandler(handlerMetaData);
                  }
               }
            }
         }

         log.debug("END buildMetaData: " + wsMetaData);
         return wsMetaData;
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

   private WSDLEndpoint getWsdlEndpoint(WSDLDefinitions wsdlDefinitions, QName portName)
   {
      WSDLEndpoint wsdlEndpoint = null;
      for (WSDLService wsdlService : wsdlDefinitions.getServices())
      {
         WSDLEndpoint auxEndpoint = wsdlService.getEndpoint(portName);
         if (auxEndpoint != null)
         {
            wsdlEndpoint = auxEndpoint;
            break;
         }
      }
      return wsdlEndpoint;
   }

   private void initServicePathEJB(UnifiedDeploymentInfo udi, ServerEndpointMetaData epMetaData, String ejbLink)
   {
      UnifiedApplicationMetaData applMetaData = (UnifiedApplicationMetaData)udi.metaData;
      UnifiedBeanMetaData beanMetaData = (UnifiedBeanMetaData)applMetaData.getBeanByEjbName(ejbLink);
      if (beanMetaData == null)
         throw new WSException("Cannot obtain meta data for ejb link: " + ejbLink);

      // Use the webservice context root if we have one
      String contextRoot = applMetaData.getWebServiceContextRoot();

      // If not, derive the context root from the deployment short name
      if (contextRoot == null)
      {
         String shortName = udi.shortName;
         contextRoot = shortName.substring(0, shortName.indexOf('.'));
         contextRoot = "/" + contextRoot;
      }
      epMetaData.setContextRoot(contextRoot);

      String urlPattern;
      UnifiedEjbPortComponentMetaData ejbpcMetaData = beanMetaData.getPortComponent();
      if (ejbpcMetaData != null && ejbpcMetaData.getPortComponentURI() != null)
      {
         urlPattern = ejbpcMetaData.getPortComponentURI();
      }
      else
      {
         urlPattern = "/" + ejbLink;
      }
      epMetaData.setURLPattern(urlPattern);
   }

   private void initServicePathJSE(UnifiedDeploymentInfo udi, ServerEndpointMetaData epMetaData, String servletLink)
   {
      UnifiedWebMetaData webMetaData = (UnifiedWebMetaData)udi.metaData;
      Map<String, String> servletMappings = webMetaData.getServletMappings();

      String contextRoot = webMetaData.getContextRoot();

      // If not, derive the context root from the deployment short name
      if (contextRoot == null)
      {
         String shortName = udi.shortName;
         contextRoot = shortName.substring(0, shortName.indexOf('.'));
         contextRoot = "/" + contextRoot;
      }
      epMetaData.setContextRoot(contextRoot);

      String urlPattern = (String)servletMappings.get(servletLink);
      if (urlPattern == null)
         throw new WSException("Cannot obtain servlet mapping for servlet link: " + servletLink);

      if (urlPattern.startsWith("/") == false)
         urlPattern = "/" + urlPattern;

      epMetaData.setURLPattern(urlPattern);
   }

   /** Read the transport guarantee from web.xml
    */
   protected void initTransportGuaranteeJSE(UnifiedDeploymentInfo udi, EndpointMetaData epMetaData, String servletLink) throws IOException
   {
      File warFile = new File(udi.localUrl.getFile());
      if (warFile.isDirectory() == false)
         throw new WSException("Expected a war directory: " + udi.localUrl);

      File webXML = new File(udi.localUrl.getFile() + "/WEB-INF/web.xml");
      if (webXML.isFile() == false)
         throw new WSException("Cannot find web.xml: " + webXML);

      Element rootElement = DOMUtils.parse(new FileInputStream(webXML));

      Element elServletMapping = null;
      Iterator itServlet = DOMUtils.getChildElements(rootElement, "servlet-mapping");
      while (itServlet.hasNext() && elServletMapping == null)
      {
         Element elAux = (Element)itServlet.next();
         String servletName = DOMUtils.getTextContent(DOMUtils.getFirstChildElement(elAux, "servlet-name"));
         if (servletLink.equals(servletName))
            elServletMapping = elAux;
      }
      if (elServletMapping != null)
      {
         // find servlet-mapping/url-pattern
         String urlPattern = DOMUtils.getTextContent(DOMUtils.getFirstChildElement(elServletMapping, "url-pattern"));
         if (urlPattern == null)
            throw new WSException("Cannot find <url-pattern> for servlet-name: " + servletLink);

         Iterator itSecConstraint = DOMUtils.getChildElements(rootElement, "security-constraint");
         while (itSecConstraint.hasNext())
         {
            Element elSecurityConstraint = (Element)itSecConstraint.next();
            Iterator itWebResourceCollection = DOMUtils.getChildElements(elSecurityConstraint, "web-resource-collection");
            while (itWebResourceCollection.hasNext())
            {
               Element elWebResourceCollection = (Element)itWebResourceCollection.next();
               String wrcurlPattern = DOMUtils.getTextContent(DOMUtils.getFirstChildElement(elWebResourceCollection, "url-pattern"));
               if (urlPattern.equals(wrcurlPattern))
               {
                  Element elUserDataConstraint = DOMUtils.getFirstChildElement(elSecurityConstraint, "user-data-constraint");
                  if (elUserDataConstraint != null)
                  {
                     String transportGuarantee = DOMUtils.getTextContent(DOMUtils.getFirstChildElement(elUserDataConstraint, "transport-guarantee"));
                     epMetaData.setTransportGuarantee(transportGuarantee);
                  }
               }
            }
         }
      }
   }
}
