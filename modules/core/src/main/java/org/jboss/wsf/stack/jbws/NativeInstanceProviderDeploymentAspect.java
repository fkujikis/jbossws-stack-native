/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;

/**
 * Instance provider DA.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class NativeInstanceProviderDeploymentAspect extends AbstractDeploymentAspect
{

    @Override
    public void start(final Deployment dep)
    {
       final ClassLoader loader = dep.getRuntimeClassLoader();
       final ClassLoader integrationCL = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
       final ClassLoader newCL = new DelegateClassLoader(integrationCL, loader);
       for (final Endpoint ep : dep.getService().getEndpoints())
       {
          ep.setInstanceProvider(new NativeInstanceProvider(newCL));
       }
    }

    @Override
    public void stop(final Deployment dep)
    {
        for (final Endpoint ep : dep.getService().getEndpoints())
        {
           ep.setInstanceProvider(null);
        }
    }

}
