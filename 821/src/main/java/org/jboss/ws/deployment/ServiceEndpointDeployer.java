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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.metadata.EndpointMetaData;
import org.jboss.ws.metadata.ServerEndpointMetaData;
import org.jboss.ws.metadata.ServiceMetaData;
import org.jboss.ws.metadata.UnifiedMetaData;
import org.jboss.ws.server.ServiceEndpointInfo;
import org.jboss.ws.server.ServiceEndpointManager;
import org.jboss.ws.server.WSDLFilePublisher;

/**
 * The POJO deployer for web service endpoints
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-May-2006
 */
public class ServiceEndpointDeployer
{
   // logging support
   private static Logger log = Logger.getLogger(ServiceEndpointDeployer.class);

   // default bean name
   public static final String BEAN_NAME = "ServiceEndpointDeployer";

   // The servlet init param in web.xml that is the service endpoint class
   public static final String INIT_PARAM_SERVICE_ENDPOINT_IMPL = "ServiceEndpointImpl";

   // The ServiceEndpointManger injected by the kernel
   private ServiceEndpointManager epManager;

   // Maps the deployment url to UMDM
   private Map<String, UnifiedMetaData> metaDataMap = new ConcurrentHashMap<String, UnifiedMetaData>();

   // Injected by the Microkernel
   public void setServiceEndpointManager(ServiceEndpointManager epManager)
   {
      this.epManager = epManager;
   }

   public void create(UnifiedDeploymentInfo udi) throws Throwable
   {
      log.debug("create: " + udi.url);

      UnifiedMetaData wsMetaData;
      if (udi.type == UnifiedDeploymentInfo.Type.JSR109_JSE)
      {
         JSR109ServerMetaDataBuilder builder = new JSR109ServerMetaDataBuilder();
         builder.setClassLoader(null); // the web context loader is not available yet
         builder.setResourceLoader(udi.localCl);
         wsMetaData = builder.buildMetaData((JSR109Deployment)udi);
      }
      else if (udi.type == UnifiedDeploymentInfo.Type.JSR109_EJB21)
      {
         JSR109ServerMetaDataBuilder builder = new JSR109ServerMetaDataBuilder();
         builder.setClassLoader(udi.ucl);
         builder.setResourceLoader(udi.localCl);
         wsMetaData = builder.buildMetaData((JSR109Deployment)udi);
      }
      else if (udi.type == UnifiedDeploymentInfo.Type.JSR181_JSE)
      {
         JSR181MetaDataBuilderJSE builder = new JSR181MetaDataBuilderJSE();
         builder.setClassLoader(udi.annotationsCl);
         builder.setResourceLoader(udi.localCl);
         wsMetaData = builder.buildMetaData(udi);
      }
      else if (udi.type == UnifiedDeploymentInfo.Type.JSR181_EJB21)
      {
         JSR181MetaDataBuilderEJB21 builder = new JSR181MetaDataBuilderEJB21();
         builder.setClassLoader(udi.annotationsCl);
         builder.setResourceLoader(udi.localCl);
         wsMetaData = builder.buildMetaData(udi);
      }
      else if (udi.type == UnifiedDeploymentInfo.Type.JSR181_EJB3)
      {
         JSR181MetaDataBuilderEJB3 builder = new JSR181MetaDataBuilderEJB3();
         builder.setClassLoader(udi.annotationsCl);
         builder.setResourceLoader(udi.localCl);
         wsMetaData = builder.buildMetaData(udi);
      }
      else
      {
         throw new WSException("Invalid type:  " + udi.type);
      }

      metaDataMap.put(udi.url.toExternalForm(), wsMetaData);

      for (ServiceMetaData serviceMetaData : wsMetaData.getServices())
      {
         for (EndpointMetaData epMetaData : serviceMetaData.getEndpoints())
         {
            ServiceEndpointInfo seInfo = new ServiceEndpointInfo(udi, (ServerEndpointMetaData)epMetaData);
            epManager.createServiceEndpoint(seInfo);
         }
      }
   }

   public void start(UnifiedDeploymentInfo udi) throws Throwable
   {
      log.debug("start: " + udi.url);

      UnifiedMetaData wsMetaData = getUnifiedMetaData(udi);
      if (wsMetaData != null)
      {
         // late initialization of the web context loader
         if (wsMetaData.getClassLoader() != udi.ucl)
            wsMetaData.setClassLoader(udi.ucl);
         
         // Publish the WSDL file
         WSDLFilePublisher wsdlfp = new WSDLFilePublisher(udi);
         wsdlfp.publishWsdlFiles(wsMetaData);
         for (ServiceMetaData serviceMetaData : wsMetaData.getServices())
         {
            for (EndpointMetaData epMetaData : serviceMetaData.getEndpoints())
            {
               ObjectName sepID = ((ServerEndpointMetaData)epMetaData).getServiceEndpointID();
               epManager.startServiceEndpoint(sepID);
            }
         }
      }      
   }

   public void stop(UnifiedDeploymentInfo udi) throws Throwable
   {
      log.debug("stop: " + udi.url);

      UnifiedMetaData wsMetaData = getUnifiedMetaData(udi);
      if (wsMetaData != null)
      {
         // Stop the service endpoints
         for (ServiceMetaData serviceMetaData : wsMetaData.getServices())
         {
            for (EndpointMetaData epMetaData : serviceMetaData.getEndpoints())
            {
               ObjectName sepID = ((ServerEndpointMetaData)epMetaData).getServiceEndpointID();
               epManager.stopServiceEndpoint(sepID);
            }
         }
         
         // Unpublish the WSDL file
         WSDLFilePublisher wsdlfp = new WSDLFilePublisher(udi);
         wsdlfp.unpublishWsdlFiles();
      }      
   }

   public void destroy(UnifiedDeploymentInfo udi) throws Throwable
   {
      log.debug("destroy: " + udi.url);

      UnifiedMetaData wsMetaData = getUnifiedMetaData(udi);
      if (wsMetaData != null)
      {
         // Destroy the service endpoints
         for (ServiceMetaData serviceMetaData : wsMetaData.getServices())
         {
            for (EndpointMetaData epMetaData : serviceMetaData.getEndpoints())
            {
               ObjectName sepID = ((ServerEndpointMetaData)epMetaData).getServiceEndpointID();
               epManager.destroyServiceEndpoint(sepID);
            }
         }
      }      
   }

   public UnifiedMetaData getUnifiedMetaData(UnifiedDeploymentInfo udi)
   {
      UnifiedMetaData wsMetaData = metaDataMap.get(udi.url.toExternalForm());
      return wsMetaData;
   }
}