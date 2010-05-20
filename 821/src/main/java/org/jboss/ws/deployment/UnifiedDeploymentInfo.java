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

// $Id$

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

/**
 * The container independent deployment info. 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class UnifiedDeploymentInfo
{
   public enum Type
   {
      JSR109_Client, JSR109_JSE, JSR109_EJB21, JSR181_JSE, JSR181_EJB21, JSR181_EJB3
   };

   public UnifiedDeploymentInfo(Type type)
   {
      this.type = type;
   }

   /** The type of this deployment */
   public Type type;

   /** Sub deployments have a parent */
   public UnifiedDeploymentInfo parent;

   /** The suffix of the deployment url */
   public String shortName;

   /** The URL identifing this SDI **/
   public URL url;

   /** An optional URL to a local copy of the deployment */
   public URL localUrl;

   /** We can hold "typed" metadata */
   public Object metaData;

   /** A CL for preloading annotations */
   public ClassLoader annotationsCl;

   /** Local Cl is a CL that is used for metadata loading */
   public URLClassLoader localCl;

   /** Unified CL is a global scope class loader **/
   public ClassLoader ucl;

   /** The variable deployedObject can contain the MBean that
    * is created through the deployment
    */
   public ObjectName deployedObject;

   /** An arbitrary map of state associated with the deployment */
   public Map<String, Object> context = new HashMap<String, Object>();

   /** The sortName concatenated with the canonical names of all parents. */
   public String getCanonicalName()
   {
      String name = shortName;
      if (parent != null)
         name = parent.getCanonicalName() + "/" + name;
      return name;
   }
}
