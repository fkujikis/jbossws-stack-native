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
package org.jboss.ws.metadata;

// $Id$

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import org.jboss.logging.Logger;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMappingFactory;
import org.jboss.ws.WSException;
import org.jboss.ws.jaxb.SchemaBindingBuilder;
import org.jboss.ws.jaxrpc.TypeMappingImpl;
import org.jboss.ws.jaxrpc.TypeMappingRegistryImpl;
import org.jboss.ws.jaxrpc.Use;
import org.jboss.ws.jaxrpc.encoding.JAXBDeserializerFactory;
import org.jboss.ws.jaxrpc.encoding.JAXBSerializerFactory;
import org.jboss.ws.jaxrpc.encoding.SOAPArrayDeserializerFactory;
import org.jboss.ws.jaxrpc.encoding.SOAPArraySerializerFactory;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLDefinitionsFactory;
import org.jboss.ws.metadata.wsdl.WSDLTypes;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;
import org.jboss.ws.utils.JavaUtils;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;

/**
 * A Service component describes a set of endpoints.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-May-2005
 */
public class ServiceMetaData
{
   // provide logging
   private static final Logger log = Logger.getLogger(ServiceMetaData.class);

   // The parent meta data.
   private UnifiedMetaData wsMetaData;

   // The service endpoints
   private Map<QName, EndpointMetaData> endpoints = new LinkedHashMap<QName, EndpointMetaData>();

   private QName name;
   private String wsdName;
   private String wsdlFile;
   private String jaxrpcMappingFile;
   private String wsdlPublishLocation;

   // The type mapping that is maintained by this service
   private TypesMetaData types;
   private TypeMappingRegistry tmRegistry = new TypeMappingRegistryImpl();
   private SchemaBinding schemaBinding;

   // Arbitrary properties given by <call-property>
   private Properties properties;
   
   // derived cached encoding style
   private Use encStyle;

   // The security configuration
   private WSSecurityConfiguration securityConfiguration;

   public ServiceMetaData(UnifiedMetaData wsMetaData, QName name)
   {
      this.wsMetaData = wsMetaData;
      this.name = name;
      this.types = new TypesMetaData(this);
   }

   public UnifiedMetaData getUnifiedMetaData()
   {
      return wsMetaData;
   }

   public void setName(QName name)
   {
      this.name = name;
   }

   public QName getName()
   {
      return name;
   }

   public String getWebserviceDescriptionName()
   {
      return wsdName;
   }

   public void setWebserviceDescriptionName(String wsdName)
   {
      this.wsdName = wsdName;
   }

   public String getWsdlFile()
   {
      return wsdlFile;
   }

   public void setWsdlFile(String wsdlFile)
   {
      this.wsdlFile = wsdlFile;
   }

   public String getWsdlPublishLocation()
   {
      return wsdlPublishLocation;
   }

   public void setWsdlPublishLocation(String wsdlPublishLocation)
   {
      this.wsdlPublishLocation = wsdlPublishLocation;
   }

   public Properties getProperties()
   {
      return properties;
   }

   public void setProperties(Properties properties)
   {
      this.properties = properties;
   }

   public TypesMetaData getTypesMetaData()
   {
      return types;
   }

   public List<EndpointMetaData> getEndpoints()
   {
      return new ArrayList<EndpointMetaData>(endpoints.values());
   }

   public EndpointMetaData getEndpoint(QName portName)
   {
      return endpoints.get(portName);
   }

   public EndpointMetaData getEndpointByServiceEndpointInterface(String seiName)
   {
      EndpointMetaData epMetaData = null;
      for (EndpointMetaData epmd : endpoints.values())
      {
         if (seiName.equals(epmd.getServiceEndpointInterfaceName()))
         {
            if (epMetaData != null)
            {
               // The CTS uses Service.getPort(Class) with multiple endpoints implementing the same SEI
               log.warn("Multiple possible endpoints implementing SEI: " + seiName);
            }
            epMetaData = epmd;
         }
      }
      return epMetaData;
   }

   public void addEndpoint(EndpointMetaData epMetaData)
   {
      QName portName = epMetaData.getName();

      // This happends when we have multiple port components in sharing the same wsdl port
      // The EndpointMetaData name is the wsdl port, so we cannot have multiple meta data for the same port.
      if (endpoints.get(portName) != null)
         throw new WSException("EndpointMetaData name must be unique: " + portName);

      endpoints.put(portName, epMetaData);
   }

   public String getJaxrpcMappingFile()
   {
      return jaxrpcMappingFile;
   }

   public void setJaxrpcMappingFile(String jaxrpcMappingFile)
   {
      this.jaxrpcMappingFile = jaxrpcMappingFile;
   }

   public JavaWsdlMapping getJavaWsdlMapping()
   {
      JavaWsdlMapping javaWsdlMapping = (JavaWsdlMapping)wsMetaData.getMappingDefinition(jaxrpcMappingFile);
      if (javaWsdlMapping == null && jaxrpcMappingFile != null)
      {
         URL mappingLocation = null;
         try
         {
            mappingLocation = new URL(jaxrpcMappingFile);
         }
         catch (MalformedURLException e)
         {
            // ignore
         }
         if (mappingLocation == null)
         {
            mappingLocation = wsMetaData.getResourceLoader().getResource(jaxrpcMappingFile);
         }

         if (mappingLocation == null)
            throw new IllegalArgumentException("Cannot find jaxrpc-mapping.xml in deployment: " + jaxrpcMappingFile);

         try
         {
            JavaWsdlMappingFactory mappingFactory = JavaWsdlMappingFactory.newInstance();
            javaWsdlMapping = mappingFactory.parse(mappingLocation);
            wsMetaData.addMappingDefinition(jaxrpcMappingFile, javaWsdlMapping);
         }
         catch (IOException e)
         {
            throw new WSException("Cannot parse jaxrpc-mapping.xml", e);
         }
      }
      return javaWsdlMapping;
   }

   /**
    * Get the wsdl definition that corresponds to the wsdl-file element.
    */
   public WSDLDefinitions getWsdlDefinitions()
   {
      WSDLDefinitions wsdlDefinitions = (WSDLDefinitions)wsMetaData.getWSDLDefinition(wsdlFile);
      if (wsdlDefinitions == null && wsdlFile != null)
      {
         URL wsdlLocation = null;
         try
         {
            wsdlLocation = new URL(wsdlFile);
         }
         catch (MalformedURLException e)
         {
            // ignore
         }
         if (wsdlLocation == null)
         {
            wsdlLocation = wsMetaData.getResourceLoader().getResource(wsdlFile);
         }
         if (wsdlLocation == null)
            throw new IllegalArgumentException("Cannot find wsdl in deployment: " + wsdlFile);

         WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
         wsdlDefinitions = factory.parse(wsdlLocation);
         wsMetaData.addWSDLDefinition(wsdlFile, wsdlDefinitions);
      }
      return wsdlDefinitions;
   }

   public TypeMappingImpl getTypeMapping()
   {
      Use encStyle = getEncodingStyle();
      TypeMappingImpl typeMapping = (TypeMappingImpl)tmRegistry.getTypeMapping(encStyle.toURI());
      if (typeMapping == null)
         throw new WSException("No type mapping for encoding style: " + encStyle);

      return typeMapping;
   }

   public WSSecurityConfiguration getSecurityConfiguration()
   {
      return securityConfiguration;
   }

   public void setSecurityConfiguration(WSSecurityConfiguration securityConfiguration)
   {
      this.securityConfiguration = securityConfiguration;
   }

   public Use getEncodingStyle()
   {
      if (encStyle == null)
      {
         if (endpoints.size() > 0)
         {
            for (EndpointMetaData epMetaData : endpoints.values())
            {
               if (encStyle == null)
               {
                  encStyle = epMetaData.getEncodingStyle();
               }
               else if (encStyle.equals(epMetaData.getEncodingStyle()) == false)
               {
                  throw new WSException("Conflicting encoding styles not supported");
               }
            }
         }
         else
         {
            encStyle = Use.getDefaultUse();
         }
      }      
      return encStyle;
   }

   public SchemaBinding getSchemaBinding()
   {
      JavaWsdlMapping wsdlMapping = getJavaWsdlMapping();
      if (schemaBinding == null && getEncodingStyle() == Use.LITERAL && wsdlMapping != null)
      {
         JBossXSModel xsModel = types.getSchemaModel();
         SchemaBindingBuilder bindingBuilder = new SchemaBindingBuilder();
         schemaBinding = bindingBuilder.buildSchemaBinding(xsModel, wsdlMapping);
      }
      return schemaBinding;
   }

   /**
    * @see UnifiedMetaData#eagerInitialize()
    */
   public void eagerInitialize()
   {
      // Initialize all wsdl definitions and schema objects
      WSDLDefinitions definitions = getWsdlDefinitions();
      if (definitions != null)
      {
         WSDLTypes types = definitions.getWsdlTypes();
         if (types != null)
         {
            JBossXSModel model = types.getSchemaModel();
            if (model != null)
               model.eagerInitialize();
         }
      }

      // Initialize jaxrpc-mapping data
      getJavaWsdlMapping();

      TypeMappingImpl typeMapping = getTypeMapping();
      for (TypeMappingMetaData tmMetaData : getTypesMetaData().getTypeMappings())
      {
         String javaTypeName = tmMetaData.getJavaTypeName();
         QName xmlType = tmMetaData.getXmlType();
         if (xmlType != null)
         {
            Class registeredType = typeMapping.getJavaType(xmlType);
            if (registeredType == null || registeredType.getName().equals(javaTypeName) == false)
            {
               ClassLoader classLoader = getUnifiedMetaData().getClassLoader();
               if (classLoader == null)
                  throw new WSException("ClassLoader not available in meta data");

               try
               {
                  Class javaType = JavaUtils.loadJavaType(javaTypeName, classLoader);
                  if (JavaUtils.isPrimitive(javaTypeName))
                     javaType = JavaUtils.getWrapperType(javaType);

                  if (getEncodingStyle() == Use.ENCODED && javaType.isArray())
                  {
                     typeMapping.register(javaType, xmlType, new SOAPArraySerializerFactory(), new SOAPArrayDeserializerFactory());
                  }
                  else
                  {
                     typeMapping.register(javaType, xmlType, new JAXBSerializerFactory(), new JAXBDeserializerFactory());
                  }
               }
               catch (ClassNotFoundException e)
               {
                  log.warn("Cannot load class for type: " + xmlType + "," + javaTypeName);
               }
            }
         }
      }

      // init the endpoints
      for (EndpointMetaData epMetaData : endpoints.values())
         epMetaData.eagerInitialize();

      // init schema binding
      getSchemaBinding();
   }

   /** Assert that the given namespace is the WSDL's target namespace */
   public void assertTargetNamespace(String targetNS)
   {
      if (getName().getNamespaceURI().equals(targetNS) == false)
         throw new WSException("Requested namespace is not WSDL target namespace: " + targetNS);
   }

   public String toString()
   {
      StringBuilder buffer = new StringBuilder("\nServiceMetaData:");
      buffer.append("\n name=" + name);
      buffer.append("\n wsdName=" + wsdName);
      buffer.append("\n wsdlFile=" + wsdlFile);
      buffer.append("\n jaxrpcFile=" + jaxrpcMappingFile);
      buffer.append("\n publishLocation=" + wsdlPublishLocation);
      buffer.append("\n properties=" + properties);
      buffer.append("\n" + types);
      for (EndpointMetaData epMetaData : endpoints.values())
      {
         buffer.append("\n" + epMetaData);
      }
      return buffer.toString();
   }
}
