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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class ResourceModel extends AbstractRegexResolveable
{
   private ResourceModel parent = null;

   private String uriTemplate;
   private Class invocationTarget;

   /*TODO : parse resource methods => no UriTemplate but HttpMethod annotation */
   private List<ResourceMethod> resourceMethods = new ArrayList<ResourceMethod>();
   private List<ResourceMethod> subResourceMethods = new ArrayList<ResourceMethod>();
   private List<ResourceLocator> resourceLocators = new ArrayList<ResourceLocator>();

   ResourceModel(String uriTemplate, Class implementation)
   {
      this(null, uriTemplate, implementation);
   }

   public ResourceModel(ResourceModel parent, String uriTemplate, Class invocationTarget)
   {
      this.parent = parent;
      this.uriTemplate = uriTemplate;
      this.invocationTarget = invocationTarget;
   }

   /**
    * Locks the resource model for further modifications    
    */
   void freeze()
   {
      initFromUriTemplate(this.uriTemplate);     
   }

   public void addResourceMethod(ResourceMethod srm)
   {
      resourceMethods.add(srm);
   }

   public void addSubResourceMethod(ResourceMethod srm)
   {
      subResourceMethods.add(srm);
   }

   public void addSubResourceLocator(ResourceLocator srl)
   {
      resourceLocators.add(srl);
   }

   public boolean hasSubResources()
   {
      return !subResourceMethods.isEmpty() || !resourceLocators.isEmpty();
   }

   ResourceModel getParent()
   {
      return parent;
   }

   public Class getInvocationTarget()
   {
      return invocationTarget;
   }

   public String getUriTemplate()
   {
      return uriTemplate;
   }

   public List<ResourceMethod> getResourceMethods()
   {
      return resourceMethods;
   }

   public List<ResourceMethod> getSubResourceMethods()
   {
      return subResourceMethods;
   }

   public List<ResourceLocator> getResourceLocator()
   {
      return resourceLocators;
   }

   boolean hasChildren()
   {
      return hasSubResources(); 
   }

   public String toString()
   {
      return "ResourceModel {uri=" + uriTemplate + ", regex=" + regexPattern + ", impl=" + invocationTarget + "}";
   }
}
