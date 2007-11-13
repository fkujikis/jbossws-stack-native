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
package org.jboss.rs.deployment;

import org.jboss.rs.model.dd.DeploymentDescriptorParser;
import org.jboss.rs.model.dd.JbossrsType;
import org.jboss.rs.model.dd.ResourceType;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.*;

import java.io.IOException;

/**
 * Parses the jbossrs descriptor and turns it into a
 * SPI structure for further consumption.
 *
 * @see org.jboss.rs.model.dd.DeploymentDescriptorParser
 *
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class DescriptorDeploymentAspect extends DeploymentAspect
{

   public final static String RSDD_POINTER = "jbossrs.dd.pointer";

   public void create(Deployment deployment)
   {
      try
      {         
         UnifiedVirtualFile vf = getJBossRSDescriptor(deployment);
         JbossrsType dd = DeploymentDescriptorParser.read(vf.toURL().openStream());

         // keep the DD model
         deployment.addAttachment(JbossrsType.class, dd);

         Service service = deployment.getService();

         for(ResourceType resourceDesc : dd.getResource())
         {
            String name = resourceDesc.getName() != null ? resourceDesc.getName() : "";
            String impl = resourceDesc.getImplementation();

            Endpoint ep = newEndpoint(impl);
            ep.setShortName(name);
            service.addEndpoint(ep);
         }
      }
      catch (IOException e)
      {
         throw new RuntimeException("Failed to parse JBossRS descriptor", e);
      }

   }

   private UnifiedVirtualFile getJBossRSDescriptor(Deployment deployment)
   {
      Object vfs = deployment.getProperty(RSDD_POINTER);

      if(null==vfs)
         throw new IllegalArgumentException("JBossRS deployment descripto not found");
      
      return  (UnifiedVirtualFile) vfs;
   }

   private Endpoint newEndpoint(String impl)
   {
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      DeploymentModelFactory deploymentModelFactory = spiProvider.getSPI(DeploymentModelFactory.class);
      return deploymentModelFactory.newEndpoint(impl);
   }
}
