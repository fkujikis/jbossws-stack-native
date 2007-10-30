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
package org.jboss.rs.model;

import org.jboss.rs.MethodHTTP;

import java.util.List;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class RuntimeContext
{
   private MethodHTTP requestMethod;
   private List<ResourceModel> rootResources;
   private String uri;

   private Object requestMediaType;
   private Object responseMediaType;
   
   public RuntimeContext(MethodHTTP requestMethod, String uri, List<ResourceModel> rootResources)
   {
      this.requestMethod = requestMethod;
      this.rootResources = rootResources;
      this.uri = uri;
   }

   public MethodHTTP getRequestMethod()
   {
      return requestMethod;
   }


   public List<ResourceModel> getRootResources()
   {
      return rootResources;
   }

   public String getUri()
   {
      return uri;
   }
}
