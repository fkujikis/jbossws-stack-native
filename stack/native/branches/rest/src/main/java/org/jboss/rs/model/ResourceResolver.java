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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class ResourceResolver
{
   public ResourceMethod resolve(List<ResourceModel> rootResources, String uri)
     throws NoResourceException, NoMethodException
   {
      ResourceMethod resourceMethod = null;

      // Filter the set of resource classes by rejecting those whose
      // regular expression does not match uri
      List<ResourceMatch> includedResources = new ArrayList<ResourceMatch>();
      Iterator<ResourceModel> it1 = rootResources.iterator();
      while(it1.hasNext())
      {
         ResourceModel model = it1.next();
         RegexQualifier qualifier = model.resolve(uri);
         if(qualifier!=null)
            includedResources.add( new ResourceMatch(model, qualifier) );
      }

      if(includedResources.isEmpty())
         throw new NoResourceException("No resource matches URI '"+uri+"'");
            
      Collections.sort(includedResources);

      // DFS by locator, should result in a resource match
      Iterator<ResourceMatch> it2 = includedResources.iterator();
      while(it2.hasNext() && null==resourceMethod)
      {
         ResourceMatch rootResource = it2.next();

         // a root resource may be a final resource match already         
         resourceMethod = resolveResourceMethod(rootResource, rootResource.qualifier.nextUriToken);

         // root didn't match, so recurse locators to find a resource
         if(null == resourceMethod)
         {
            ResourceMatch subResource = resolveByLocator(rootResource);
            if(subResource!=null)
               resourceMethod = resolveResourceMethod(subResource, rootResource.qualifier.nextUriToken);
         }
      }

      if(null == resourceMethod)
         throw new NoMethodException("No method for URI '"+uri);

      // gotcha
      return resourceMethod;
   }

   private ResourceMatch resolveByLocator(ResourceMatch<ResourceModel> resourceMatch)
   {
      ResourceMatch match = null;

      List<ResourceMatch> weightedResults = new ArrayList<ResourceMatch>();
      Iterator<ResourceLocator> locators = resourceMatch.model.getResourceLocator().iterator();
      while(locators.hasNext())
      {
         ResourceLocator bridge = locators.next();
         RegexQualifier qualifier = bridge.resolve(resourceMatch.qualifier.nextUriToken);
         if(qualifier!=null)
            weightedResults.add( new ResourceMatch( bridge.field(), qualifier) );
      }

      Collections.sort(weightedResults);
      ResourceMatch next = weightedResults.get(0);
      String nextUriToken = next.qualifier.nextUriToken;

      if("".equals(nextUriToken) || "/".equals(nextUriToken))
         match = next;
      else
         match = resolveByLocator(next);

      return match;
   }

   private ResourceMethod resolveResourceMethod(ResourceMatch<ResourceModel> methodTarget, String uriToken)
   {
      ResourceMethod match = null;

      List<ResourceMatch<ResourceMethod>> matches = new ArrayList<ResourceMatch<ResourceMethod>>();
      List<ResourceMethod> methods = methodTarget.model.getSubResourceMethods();
      Iterator<ResourceMethod> it = methods.iterator();
      while(it.hasNext())
      {
         ResourceMethod method = it.next();
         RegexQualifier qualifier = method.resolve(uriToken);
         if(qualifier!=null && ("".equals(qualifier.nextUriToken) || "/".equals(qualifier.nextUriToken)))
         {
            matches.add(
              new ResourceMatch<ResourceMethod>(method, qualifier)
            );            
         }
      }

      if(!matches.isEmpty())
      {
         Collections.sort(matches);
         match = matches.get(0).model;
      }
      
      return match;
   }

   class ResourceMatch<T> implements Comparable
   {
      final T model;
      final RegexQualifier qualifier;

      public ResourceMatch(T model, RegexQualifier weight)
      {
         this.model = model;
         this.qualifier = weight;
      }

      public int compareTo(Object o)
      {
         return qualifier.compareTo(((ResourceMatch<T>)o).qualifier);
      }

      public String toString()
      {
         return "ResourceMatch {model="+model+", weight="+ qualifier +"}";
      }
   }

}
