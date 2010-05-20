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
package org.jboss.ws.server;

//$Id: WebServiceDeployer.java 312 2006-05-11 10:49:22Z thomas.diesler@jboss.com $

import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.kernel.spi.dependency.KernelControllerContextAware;

/**
 * Locate the single instance of the kernel 
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 12-May-2006
 */
public class KernelLocator implements KernelControllerContextAware
{
   private static Kernel kernel;

   public static Kernel getKernel()
   {
      return kernel;
   }

   public void setKernelControllerContext(KernelControllerContext context) throws Exception
   {
      kernel = context.getKernel();
   }

   public void unsetKernelControllerContext(KernelControllerContext arg0) throws Exception
   {
      kernel = null;
   }
}
