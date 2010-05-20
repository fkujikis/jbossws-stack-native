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

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;

/**
 * The top level meta data.
 *
 * <h4>Thread safety</h4>
 * <p>A <code>UnifiedMetaData</code> instance may be shared accross threads provided that the following conditions are met:
 * <ol>
 * <li>{@link #eagerInitialize() eagerInitialize()} is called from a single thread on startup</li>
 * <li>Multi-thread access is limited to read-only calls</li>
 * </ol></p>
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @since 12-May-2005
 */
public class UnifiedMetaData
{
   // The modules resource class loader
   private URLClassLoader resourceLoader;
   // The modules class loader
   private ClassLoader classLoader;
   // The optional security domain
   private String securityDomain;
   // Map<String, WSDLDefinitions> the wsdl-file to the wsdl Document
   // Note the same wsdl can be used in multiple webservice descriptions
   Map<String, WSDLDefinitions> wsdlMap = new HashMap<String, WSDLDefinitions>();
   // Maps the jaxrpc-mapping-file to {@link JavaWsdlMapping} object
   // Note the same jaxrpc-mapping.xml can be used in multiple webservice descriptions
   Map<String, JavaWsdlMapping> jaxrpcMap = new HashMap<String, JavaWsdlMapping>();
   // The list of service meta data
   private List<ServiceMetaData> services = new ArrayList<ServiceMetaData>();
   // Used by eager initialization
   private boolean initialized = false;

   public UnifiedMetaData()
   {
      this.classLoader = Thread.currentThread().getContextClassLoader();
   }

   public URLClassLoader getResourceLoader()
   {
      return resourceLoader;
   }

   public void setResourceLoader(URLClassLoader resourceLoader)
   {
      this.resourceLoader = resourceLoader;
   }

   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   public void setClassLoader(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
   }

   public String getSecurityDomain()
   {
      return securityDomain;
   }

   public void setSecurityDomain(String domain)
   {
      String prefix = "java:/jaas/";
      if (domain != null && domain.startsWith(prefix))
         domain = domain.substring(prefix.length());
      
      this.securityDomain = domain;
   }

   public List<ServiceMetaData> getServices()
   {
      return new ArrayList<ServiceMetaData>(services);
   }

   public void addService(ServiceMetaData serviceMetaData)
   {
      services.add(serviceMetaData);
   }

   public void addWSDLDefinition(String wsdlFile, WSDLDefinitions wsdlDefinitions)
   {
      wsdlMap.put(wsdlFile, wsdlDefinitions);
   }

   public WSDLDefinitions getWSDLDefinition(String wsdlFile)
   {
      return wsdlMap.get(wsdlFile);
   }

   public void addMappingDefinition(String jaxrpcFile, JavaWsdlMapping javaWsdlMapping)
   {
      jaxrpcMap.put(jaxrpcFile, javaWsdlMapping);
   }

   public JavaWsdlMapping getMappingDefinition(String jaxrpcFile)
   {
      return jaxrpcMap.get(jaxrpcFile);
   }

   /**
    * Eagerly initialize all cache values that are normally lazy-loaded. This
    * allows for concurrent read-only access to a <code>UnifiedMetaData</code>
    * instance. This method, however, must be called from a single thread.
    */
   public void eagerInitialize()
   {
      if (initialized == false)
      {
         for (ServiceMetaData service : services)
            service.eagerInitialize();

         initialized = true;
      }
   }

   public String toString()
   {
      StringBuilder buffer = new StringBuilder("\nUnifiedMetaData: ");
      buffer.append("\n securityDomain: " + securityDomain);
      //buffer.append("\n resourceLoader: " + resourceLoader);
      //buffer.append("\n classLoader: " + classLoader);
      buffer.append("\n");
      
      for (ServiceMetaData serviceMetaData : services)
      {
         buffer.append("\n" + serviceMetaData);
      }
      return buffer.toString();
   }
}