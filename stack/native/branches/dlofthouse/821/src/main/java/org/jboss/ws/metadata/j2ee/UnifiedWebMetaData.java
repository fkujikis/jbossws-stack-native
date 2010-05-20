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

//$Id: UnifiedWebMetaData.java 316 2006-05-12 17:09:42Z thomas.diesler@jboss.com $

import java.util.HashMap;
import java.util.Map;

/**
 * The container independent representation of the web.xml and jboss-web.xml deployment descriptors 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class UnifiedWebMetaData
{
   /** The war context root as specified at the jboss-web.xml descriptor level. */
   private String contextRoot;
   /** The web.xml servlet-mapping <String, String> */
   private Map<String, String> servletMappings = new HashMap<String, String>();
   /** The web.xml servlet <String, String> */
   private Map<String, String> servletClassNames = new HashMap<String, String>();
   /** The optional JBossWS config-name */
   private String configName;
   /** The optional JBossWS config-file */
   private String configFile;
   /** The web context class loader, used to create the ws4ee service endpoint */
   private ClassLoader ctxLoader;
   /** The security-domain value assigned to the application */
   private String securityDomain;
   /** A HashMap<String, String> for webservice description publish locations */
   private Map<String, String> wsdlPublishLocationMap = new HashMap<String, String>();

   public String getContextRoot()
   {
      return contextRoot;
   }

   public void setContextRoot(String contextRoot)
   {
      this.contextRoot = contextRoot;
   }

   public Map<String, String> getServletMappings()
   {
      return servletMappings;
   }

   public void setServletMappings(Map<String, String> servletMappings)
   {
      this.servletMappings = servletMappings;
   }

   public Map<String, String> getServletClassMap()
   {
      return servletClassNames;
   }

   public void setServletClassMap(Map<String, String> servletClassNames)
   {
      this.servletClassNames = servletClassNames;
   }

   public String getConfigName()
   {
      return configName;
   }

   public void setConfigName(String configName)
   {
      this.configName = configName;
   }

   public String getConfigFile()
   {
      return configFile;
   }

   public void setConfigFile(String configFile)
   {
      this.configFile = configFile;
   }

   public ClassLoader getContextLoader()
   {
      return ctxLoader;
   }

   public void setContextLoader(ClassLoader ctxLoader)
   {
      this.ctxLoader = ctxLoader;
   }

   public String getSecurityDomain()
   {
      return securityDomain;
   }

   public void setSecurityDomain(String securityDomain)
   {
      this.securityDomain = securityDomain;
   }

   public void setWsdlPublishLocationMap(Map<String, String> wsdlPublishLocationMap)
   {
      this.wsdlPublishLocationMap = wsdlPublishLocationMap;
   }

   public String getWsdlPublishLocationByName(String name)
   {
      // if not found, the we will use default
      return wsdlPublishLocationMap.get(name);
   }
}
