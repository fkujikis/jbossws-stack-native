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
package org.jboss.ws.metadata.j2ee;

//$Id: UnifiedServiceRefMetaData.java 314 2006-05-11 10:57:59Z thomas.diesler@jboss.com $

import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;


/**
 * The container independent metdata data from service-ref element in web.xml, ejb-jar.xml, and
 * application-client.xml.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class UnifiedServiceRefMetaData implements Serializable
{
   private static final long serialVersionUID = 122554634182144069L;
   
   // The required <service-ref-name> element
   private String serviceRefName;
   // The required <service-interface> element
   private String serviceInterface;
   // The optional <wsdl-file> element
   private String wsdlFile;
   // The optional <jaxrpc-mapping-file> element
   private String jaxrpcMappingFile;
   // The optional <service-qname> element
   private QName serviceQName;
   // The LinkedHashMap<String, UnifiedPortComponentRefMetaData> for <port-component-ref> elements
   private Map<String, UnifiedPortComponentRefMetaData> portComponentRefs = new LinkedHashMap<String, UnifiedPortComponentRefMetaData>();
   // The optional <handler> elements
   private List<UnifiedHandlerMetaData> handlers = new ArrayList<UnifiedHandlerMetaData>();

   // The optional JBossWS config-name
   private String configName;
   // The optional JBossWS config-file
   private String configFile;
   /** The URL of the actual WSDL to use, <wsdl-override> */
   private URL wsdlOverride;
   /** Arbitrary proxy properties given by <call-property> */
   private Properties callProperties;

   // The wsdl definition, if we have one
   private Object wsdlDefinition;
   // The java/wsdl mapping, if we have one
   private Object javaWsdlMapping;

   //The ClassLoader to load additional resources 
   private transient URLClassLoader resourceCL;

   /** Set the resource classloader that can load the wsdl file
    * On the client side this is set expicitly after unmarshalling.
    */
   public void setResourceCL(URLClassLoader resourceCL)
   {
      if (resourceCL == null)
         throw new IllegalArgumentException("ResourceClassLoader cannot be null");

      this.resourceCL = resourceCL;
   }

   public URLClassLoader getResourceCL()
   {
      if (resourceCL == null)
         resourceCL = new URLClassLoader(new URL[] {}, Thread.currentThread().getContextClassLoader());

      return resourceCL;
   }

   public String getJaxrpcMappingFile()
   {
      return jaxrpcMappingFile;
   }

   public void setJaxrpcMappingFile(String jaxrpcMappingFile)
   {
      this.jaxrpcMappingFile = jaxrpcMappingFile;
   }

   public URL getJavaWsdlMappingURL()
   {
      URL mappingURL = null;
      if (jaxrpcMappingFile != null)
      {
         mappingURL = getResourceCL().findResource(jaxrpcMappingFile);
         if (mappingURL == null)
            throw new IllegalStateException("Cannot find resource: " + jaxrpcMappingFile);
      }
      return mappingURL;
   }

   public Object getJavaWsdlMapping()
   {
      return javaWsdlMapping;
   }

   public void setJavaWsdlMapping(Object javaWsdlMapping)
   {
      this.javaWsdlMapping = javaWsdlMapping;
   }

   public UnifiedPortComponentRefMetaData[] getPortComponentRefs()
   {
      UnifiedPortComponentRefMetaData[] array = new UnifiedPortComponentRefMetaData[portComponentRefs.size()];
      portComponentRefs.values().toArray(array);
      return array;
   }

   public void setPortComponentRefs(LinkedHashMap<String, UnifiedPortComponentRefMetaData> portComponentRefs)
   {
      this.portComponentRefs = portComponentRefs;
   }
   
   public UnifiedHandlerMetaData[] getHandlers()
   {
      UnifiedHandlerMetaData[] array = new UnifiedHandlerMetaData[handlers.size()];
      handlers.toArray(array);
      return array;
   }

   public void setHandlers(ArrayList<UnifiedHandlerMetaData> handlers)
   {
      this.handlers = handlers;
   }
   
   public String getServiceInterface()
   {
      return serviceInterface;
   }

   public void setServiceInterface(String serviceInterface)
   {
      this.serviceInterface = serviceInterface;
   }

   public QName getServiceQName()
   {
      return serviceQName;
   }

   public void setServiceQName(QName serviceQName)
   {
      this.serviceQName = serviceQName;
   }

   public String getServiceRefName()
   {
      return serviceRefName;
   }

   public void setServiceRefName(String serviceRefName)
   {
      this.serviceRefName = serviceRefName;
   }

   public String getWsdlFile()
   {
      return wsdlFile;
   }

   public void setWsdlFile(String wsdlFile)
   {
      this.wsdlFile = wsdlFile;
   }
   
   public String getConfigFile()
   {
      return configFile;
   }

   public void setConfigFile(String configFile)
   {
      this.configFile = configFile;
   }

   public String getConfigName()
   {
      return configName;
   }

   public void setConfigName(String configName)
   {
      this.configName = configName;
   }

   public URL getWsdlOverride()
   {
      return wsdlOverride;
   }


   public void setWsdlOverride(URL wsdlOverride)
   {
      this.wsdlOverride = wsdlOverride;
   }
   
   public URL getWsdlURL()
   {
      URL wsdlURL = wsdlOverride;
      if (wsdlURL == null && wsdlFile != null)
      {
         wsdlURL = getResourceCL().findResource(wsdlFile);
         if (wsdlURL == null)
            throw new IllegalStateException("Cannot find resource: " + wsdlFile);
      }
      return wsdlURL;
   }

   public Properties getCallProperties()
   {
      return callProperties;
   }

   public void setCallProperties(Properties callProperties)
   {
      this.callProperties = callProperties;
   }

   public Object getWsdlDefinition()
   {
      return wsdlDefinition;
   }

   public void setWsdlDefinition(Object wsdlDefinition)
   {
      this.wsdlDefinition = wsdlDefinition;
   }
}
