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
package org.jboss.wsf.stack.jbws.standalone;

import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.registry.KernelBus;
import org.jboss.wsf.spi.Container;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspectManager;
import org.jboss.wsf.spi.deployment.DeploymentAspectManagerFactory;

import java.net.URL;

/**
 * A JBossWS container that bootstraps through
 * the {@link org.jboss.wsf.spi.SPIProvider}
 *
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class StandaloneContainer implements Container
{
   private SPIProvider spi;

   private Kernel kernel;
   private KernelController controller;
   private KernelBus bus;

   private DeploymentAspectManager deploymentManager;

   private StandaloneContainer(Kernel kernel, KernelController controller, KernelBus bus)
   {
      this.kernel = kernel;
      this.controller = controller;
      this.bus = bus;     
      this.spi = SPIProviderResolver.getInstance().getProvider();
   }

   public static StandaloneContainer bootstrap(URL jbosswsBeansXml) throws Exception
   {
      EmbeddedBootstrap bootstrap = new EmbeddedBootstrap();
      bootstrap.run();
      bootstrap.deploy(jbosswsBeansXml);

      Kernel kernel = bootstrap.getKernel();
      StandaloneContainer container = new StandaloneContainer(kernel, kernel.getController(), kernel.getBus());
      container.assemble();
      return container;
   }

   private void assemble()
   {     
      // DeploymentAspectManager
      DeploymentAspectManagerFactory daf = spi.getSPI(DeploymentAspectManagerFactory.class);
      deploymentManager = daf.getDeploymentAspectManager("WSDeploymentAspectManagerJSE");
   }

   public void publish(Deployment deployment)
   {
      deploymentManager.deploy(deployment);
   }

   public void remove(Deployment deployment)
   {
      deploymentManager.undeploy(deployment);
   }

}