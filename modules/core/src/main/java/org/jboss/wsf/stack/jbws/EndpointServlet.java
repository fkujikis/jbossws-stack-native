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
package org.jboss.wsf.stack.jbws;

import org.jboss.logging.Logger;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.wsf.common.ObjectNameFactory;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;

import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceException;
import java.io.IOException;

/**
 * A servlet that is installed for every web service endpoint.
 * @author richard.opalka@jboss.com
 * @author Thomas.Diesler@jboss.org
 */
public class EndpointServlet extends HttpServlet
{
   // provide logging
   private static final Logger log = Logger.getLogger(EndpointServlet.class);

   protected Endpoint endpoint;
   protected EndpointRegistry epRegistry;

   public void init(ServletConfig servletConfig) throws ServletException
   {
      super.init(servletConfig);
      this.initRegistry();
      this.initDeploymentAspectManager();
      String contextPath = servletConfig.getServletContext().getContextPath();
      this.initServiceEndpoint(contextPath);
   }
   
   protected void initRegistry()
   {
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      epRegistry = spiProvider.getSPI(EndpointRegistryFactory.class).getEndpointRegistry();
   }
   
   public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      try
      {
         EndpointAssociation.setEndpoint(endpoint);
         RequestHandler requestHandler = endpoint.getRequestHandler();
         requestHandler.handleHttpRequest(endpoint, req, res, getServletContext());
      }
      finally
      {
         EndpointAssociation.removeEndpoint();
      }
   }

   /** Initialize the service endpoint
    */
   protected void initServiceEndpoint(String contextPath)
   {
      this.initEndpoint(contextPath, getServletName());
      this.setRuntimeLoader();
      this.callRuntimeAspects();
      this.initEndpointConfig();
   }
   
   private void setRuntimeLoader()
   {
      // Set the runtime classloader for JSE endpoints, this should be the tomcat classloader
      Deployment dep = endpoint.getService().getDeployment();
      if (dep.getType() == Deployment.DeploymentType.JAXRPC_JSE || dep.getType() == Deployment.DeploymentType.JAXWS_JSE)
      {
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         dep.setRuntimeClassLoader(classLoader);
      }
   }

   private void initEndpointConfig()
   {
      // read the config name/file from web.xml
      ServletContext ctx = getServletContext();
      String configName = ctx.getInitParameter("jbossws-config-name");
      String configFile = ctx.getInitParameter("jbossws-config-file");
      if (configName != null || configFile != null)
      {
         ServerEndpointMetaData epMetaData = endpoint.getAttachment(ServerEndpointMetaData.class);
         if (epMetaData == null)
            throw new IllegalStateException("Cannot obtain endpoint meta data");

         log.debug("Updating service endpoint config\n  config-name: " + configName + "\n  config-file: " + configFile);
         epMetaData.setConfigName(configName, configFile);
      }
   }

   private void initEndpoint(String contextPath, String servletName)
   {
      WebAppResolver resolver = new WebAppResolver(contextPath, servletName);
      this.endpoint = epRegistry.resolve(resolver);

      if (this.endpoint == null)
      {
         ObjectName oname = ObjectNameFactory.create(Endpoint.SEPID_DOMAIN + ":" +
           Endpoint.SEPID_PROPERTY_CONTEXT + "=" + contextPath + "," +
           Endpoint.SEPID_PROPERTY_ENDPOINT + "=" + getServletName()
         );
         throw new WebServiceException("Cannot obtain endpoint for: " + oname);
      }

   }

   /**
    * Template method
    */
   protected void initDeploymentAspectManager()
   {
      // does nothing (because of BC)
   }
   
   /**
    * Template method
    */
   protected void callRuntimeAspects()
   {
      // does nothing (because of BC)
   }
   
}
