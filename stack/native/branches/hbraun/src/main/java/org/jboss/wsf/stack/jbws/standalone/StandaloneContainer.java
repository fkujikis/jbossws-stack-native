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

import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.Container;
import org.jboss.wsf.spi.transport.TransportManager;
import org.jboss.wsf.spi.transport.HttpSpec;
import org.jboss.wsf.spi.transport.TransportManagerFactory;
import org.jboss.wsf.spi.transport.Protocol;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.DeploymentAspectManager;
import org.jboss.wsf.spi.deployment.DeploymentAspectManagerFactory;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.registry.KernelBus;

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

   private EndpointRegistry registry;
   private TransportManager<HttpSpec> httpTransport;
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
      // Registry
      EndpointRegistryFactory erf = spi.getSPI(EndpointRegistryFactory.class);
      registry = erf.getEndpointRegistry();

      // Http Transport
      TransportManagerFactory tmf = spi.getSPI(TransportManagerFactory.class);
      httpTransport = tmf.createTransportManager(Protocol.HTTP);

      // DeploymentAspcetManager
      DeploymentAspectManagerFactory daf = spi.getSPI(DeploymentAspectManagerFactory.class);
      deploymentManager = daf.getDeploymentAspectManager("WSDeploymentAspectManagerJSE");
   }

   public void publish(Deployment deployment)
   {
      /*
         1. create runtime model (deployment aspects)
         2. add request handler
         3. add invocation handler
         4. register endpoint
         5. create transport listener
       */
       deploymentManager.deploy(deployment);
      
   }

   public void remove(Deployment deployment)
   {
      /*
         1. remove transport listener
         2. remove endpoint from registry
      */
   }


   public void publish(Endpoint endpoint)
   {

   }

   public void remove(Endpoint endpoint)
   {
      
   }
}