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
package org.jboss.ws.tools;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ResourceBundle;

import org.jboss.ws.api.util.BundleUtils;

/**
 * Security actions for this package
 * 
 * @author alessio.soldano@jboss.com
 * @since 19-Jun-2009
 *
 */
class SecurityActions
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(SecurityActions.class);
   /**
    * Get context classloader.
    * 
    * @return the current context classloader
    */
   static ClassLoader getContextClassLoader()
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return Thread.currentThread().getContextClassLoader();
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         });
      }
   }
   
   /**
    * Set context classloader.
    *
    * @param cl the classloader
    * @return previous context classloader
    * @throws Throwable for any error
    */
   static ClassLoader setContextClassLoader(final ClassLoader cl)
   {
      if (System.getSecurityManager() == null)
      {
         ClassLoader result = Thread.currentThread().getContextClassLoader();
         if (cl != null)
            Thread.currentThread().setContextClassLoader(cl);
         return result;
      }
      else
      {
         try
         {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<ClassLoader>() {
               public ClassLoader run() throws Exception
               {
                  try
                  {
                     ClassLoader result = Thread.currentThread().getContextClassLoader();
                     if (cl != null)
                        Thread.currentThread().setContextClassLoader(cl);
                     return result;
                  }
                  catch (Exception e)
                  {
                     throw e;
                  }
                  catch (Error e)
                  {
                     throw e;
                  }
                  catch (Throwable e)
                  {
                     throw new RuntimeException(BundleUtils.getMessage(bundle, "ERROR_SETTING_CONTEXT_CLASSLOADER"),  e);
                  }
               }
            });
         }
         catch (PrivilegedActionException e)
         {
            throw new RuntimeException(BundleUtils.getMessage(bundle, "ERROR_RUNNING_PRIVILEGED_ACTION"), e);
         }
      }
   }
   
   static URL getResource(final ClassLoader cl, final String filename)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return cl.getResource(filename);
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<URL>() {
            public URL run()
            {
               return cl.getResource(filename);
            }
         });
      }
   }
}
