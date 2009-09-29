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

import java.io.File;
import java.net.UnknownHostException;

import org.jboss.ws.WSException;
import org.jboss.wsf.spi.management.ServerConfig;

/**
 * TODO: javadoc
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class NettyHttpServerConfig implements ServerConfig
{

   // TODO: count with security manager enabled
   private static File TMP_DIR;
   
   static
   {
      try
      {
         TMP_DIR = new File(System.getProperty("java.io.tmpdir"));
      }
      catch (SecurityException se)
      {
         throw new WSException(se);
      }
   }

   public File getHomeDir()
   {
      return NettyHttpServerConfig.TMP_DIR;
   }

   public File getServerDataDir()
   {
      return NettyHttpServerConfig.TMP_DIR;
   }

   public File getServerTempDir()
   {
      return NettyHttpServerConfig.TMP_DIR;
   }

   public String getImplementationTitle()
   {
      throw new UnsupportedOperationException();
   }

   public String getImplementationVersion()
   {
      throw new UnsupportedOperationException();
   }

   public String getWebServiceHost()
   {
      return "localhost";
   }

   public int getWebServicePort()
   {
      return 8686;
   }

   public int getWebServiceSecurePort()
   {
      throw new UnsupportedOperationException();
   }

   public boolean isModifySOAPAddress()
   {
      throw new UnsupportedOperationException();
   }

   public void setModifySOAPAddress(boolean flag)
   {
      throw new UnsupportedOperationException();
   }

   public void setWebServiceHost(String host) throws UnknownHostException
   {
      throw new UnsupportedOperationException();
   }

   public void setWebServicePort(int port)
   {
      throw new UnsupportedOperationException();
   }

   public void setWebServiceSecurePort(int port)
   {
      throw new UnsupportedOperationException();
   }
   
}
