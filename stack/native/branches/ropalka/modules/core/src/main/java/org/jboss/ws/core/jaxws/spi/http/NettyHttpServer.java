/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.core.jaxws.spi.http;

import java.util.LinkedList;
import java.util.List;

import javax.xml.ws.Endpoint;

import org.jboss.wsf.common.ResourceLoaderAdapter;
import org.jboss.wsf.framework.deployment.BackwardCompatibleContextRootDeploymentAspect;
import org.jboss.wsf.framework.deployment.DeploymentAspectManagerImpl;
import org.jboss.wsf.framework.deployment.EndpointAddressDeploymentAspect;
import org.jboss.wsf.framework.deployment.EndpointHandlerDeploymentAspect;
import org.jboss.wsf.framework.deployment.EndpointLifecycleDeploymentAspect;
import org.jboss.wsf.framework.deployment.EndpointNameDeploymentAspect;
import org.jboss.wsf.framework.deployment.EndpointRegistryDeploymentAspect;
import org.jboss.wsf.framework.deployment.URLPatternDeploymentAspect;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.AbstractExtensible;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.DeploymentModelFactory;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentType;
import org.jboss.wsf.spi.http.HttpContext;
import org.jboss.wsf.spi.http.HttpContextFactory;
import org.jboss.wsf.spi.http.HttpServer;
import org.jboss.wsf.stack.jbws.EagerInitializeDeploymentAspect;
import org.jboss.wsf.stack.jbws.PublishContractDeploymentAspect;
import org.jboss.wsf.stack.jbws.ServiceEndpointInvokerDeploymentAspect;
import org.jboss.wsf.stack.jbws.UnifiedMetaDataDeploymentAspect;

/**
 * TODO: javadoc
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
// TODO: review thread safety
final class NettyHttpServer extends AbstractExtensible implements HttpServer
{

   /** JBossWS SPI provider. */
   private static final SPIProvider SPI_PROVIDER = SPIProviderResolver.getInstance().getProvider();
   /** JBossWS Http Context factory. */
   private static final HttpContextFactory HTTP_CONTEXT_FACTORY = NettyHttpServer.SPI_PROVIDER.getSPI(HttpContextFactory.class);
   /** Deployment model factory. */
   private final DeploymentModelFactory deploymentModelFactory;
   
   public NettyHttpServer()
   {
      super();

      // deployment factory
      final SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      this.deploymentModelFactory = spiProvider.getSPI(DeploymentModelFactory.class);
   }

   public HttpContext createContext(final String contextRoot)
   {
      return NettyHttpServer.HTTP_CONTEXT_FACTORY.newHttpContext(this, contextRoot);
   }

   public void destroy(HttpContext context, Endpoint endpoint)
   {
      throw new UnsupportedOperationException();
   }

   public void publish(HttpContext context, Endpoint ep)
   {
      Class<?> endpointClass = this.getEndpointClass(ep);
      String contextRoot = context.getContextRoot();
      ClassLoader loader = endpointClass.getClassLoader();
      // TODO: should we use archive deployment - see META-INF/services ???
      final ArchiveDeployment dep = (ArchiveDeployment) this.deploymentModelFactory.newDeployment(contextRoot, loader);
      final org.jboss.wsf.spi.deployment.Endpoint endpoint = this.deploymentModelFactory.newEndpoint(endpointClass.getName());
      endpoint.setShortName("jaxws-dynamic-endpoint");
      dep.getService().addEndpoint(endpoint);
      dep.setRootFile(new ResourceLoaderAdapter(loader));
      dep.setRuntimeClassLoader(loader);
      dep.setType(DeploymentType.JAXWS_JSE);
      dep.getService().setContextRoot(contextRoot);
      
      DeploymentAspectManagerImpl daManager = new DeploymentAspectManagerImpl(); 
      daManager.setDeploymentAspects(getDeploymentAspects());
      daManager.deploy(dep);

      throw new UnsupportedOperationException("TODO: continue in implementation");
   }
   
   private List<DeploymentAspect> getDeploymentAspects()
   {
      List<DeploymentAspect> retVal = new LinkedList<DeploymentAspect>();
      
      retVal.add(new EndpointHandlerDeploymentAspect()); // 13
      retVal.add(new BackwardCompatibleContextRootDeploymentAspect()); // 14
      retVal.add(new URLPatternDeploymentAspect()); // 15
      retVal.add(new EndpointAddressDeploymentAspect()); // 16
      retVal.add(new EndpointNameDeploymentAspect()); // 17
      retVal.add(new UnifiedMetaDataDeploymentAspect()); // 22
      retVal.add(new ServiceEndpointInvokerDeploymentAspect()); // 23
      retVal.add(new PublishContractDeploymentAspect()); // 24
      retVal.add(new EagerInitializeDeploymentAspect()); // 25
      retVal.add(new EndpointRegistryDeploymentAspect()); // 35
      retVal.add(new EndpointLifecycleDeploymentAspect()); // 37
      
      return retVal;
   }

   public void start()
   {
      // does nothing
   }

   /**
    * Returns implementor class associated with endpoint.
    *
    * @param endpoint to get implementor class from
    * @return implementor class
    */
   private Class<?> getEndpointClass(final Endpoint endpoint)
   {
      final Object implementor = endpoint.getImplementor();
      return implementor instanceof Class<?> ? (Class<?>) implementor : implementor.getClass();
   }

}
