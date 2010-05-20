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
package org.jboss.ws.integration.jboss;

// $Id: WebServiceDeployer.java 312 2006-05-11 10:49:22Z thomas.diesler@jboss.com $

import java.net.URL;

import javax.management.MBeanServer;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.MainDeployerMBean;
import org.jboss.mx.util.MBeanProxy;
import org.jboss.mx.util.MBeanProxyCreationException;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.ws.deployment.ServiceEndpointPublisher;
import org.jboss.ws.deployment.UnifiedDeploymentInfo;

/**
 * Publish the HTTP service endpoint to JBoss 
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 12-May-2006
 */
public class JBossServiceEndpointPublisher extends ServiceEndpointPublisher
{

   public String publishServiceEndpoint(URL warURL) throws Exception
   {
      rewriteWebXML(warURL);
      getMainDeployer().deploy(warURL);
      return "OK";
   }

   public String destroyServiceEndpoint(URL warURL) throws Exception
   {
      getMainDeployer().undeploy(warURL);
      return "OK";
   }

   public String publishServiceEndpoint(UnifiedDeploymentInfo udi) throws Exception
   {
      URL warURL = udi.localUrl;
      DeploymentInfo di = (DeploymentInfo)udi.context.get(DeploymentInfo.class.getName());
      if (di == null)
         throw new IllegalStateException("Cannot obtain DeploymentInfo from context");

      rewriteWebXML(warURL);

      // Preserve the repository config
      DeploymentInfo auxdi = new DeploymentInfo(warURL, null, MBeanServerLocator.locateJBoss());
      auxdi.repositoryConfig = di.getTopRepositoryConfig();
      getMainDeployer().deploy(auxdi);
      return "OK";
   }

   public String destroyServiceEndpoint(UnifiedDeploymentInfo udi) throws Exception
   {
      return destroyServiceEndpoint(udi.localUrl);
   }

   private MainDeployerMBean getMainDeployer() throws MBeanProxyCreationException
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      MainDeployerMBean mainDeployer = (MainDeployerMBean)MBeanProxy.get(MainDeployerMBean.class, MainDeployerMBean.OBJECT_NAME, server);
      return mainDeployer;
   }
}
