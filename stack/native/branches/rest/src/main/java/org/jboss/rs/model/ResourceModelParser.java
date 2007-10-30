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

import org.jboss.rs.util.Convert;

import javax.ws.rs.UriTemplate;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class ResourceModelParser
{
   ResourceModelParser()
   {
   }

   public static ResourceModelParser newInstance()
   {
      return new ResourceModelParser();
   }

   public ResourceModel parse(Class bean)
   {
      assert bean.isAnnotationPresent(UriTemplate.class);

      // the root resource
      UriTemplate rootUri = (UriTemplate)bean.getAnnotation(UriTemplate.class);
      ResourceModel rootResource = new ResourceModel(rootUri.value(), bean);

      System.out.println("Creating resource model from bean: " + bean);

      parseInternal(rootResource);

      return rootResource;
   }

   private void parseInternal(ResourceModel resource)
   {

      for( Method m : resource.getInvocationTarget().getDeclaredMethods() )
      {         
         parseMethod(m, resource);

         // todo: constructors
      }

      // freeze resource
      resource.freeze();

      System.out.println("---");
      System.out.println(resource);

      // freeze resource methods
      for(ResourceMethod rm : resource.getResourceMethods())
      {
         rm.freeze();
         System.out.println(rm);
      }

      // freeze sub resource methods
      for(ResourceMethod srm : resource.getSubResourceMethods())
      {
         srm.freeze();
         System.out.println(srm);
      }

      System.out.println("---");
   }

   private void parseMethod(Method method, ResourceModel resource)
   {      
      if(method.isAnnotationPresent(UriTemplate.class))
      {
         UriTemplate uri = method.getAnnotation(UriTemplate.class);

         ResourceMethod resourceMethod = null;

         // subresource methods
         for(Class requestType : Convert.REQUEST_TYPES)
         {
            if(method.isAnnotationPresent(requestType))
            {
               // sub resource method
               Annotation a = method.getAnnotation(requestType);
               resourceMethod = new ResourceMethod(
                 Convert.annotationToMethodHTTP(a), uri.value(), method
               );
               resource.addSubResourceMethod(resourceMethod);
            }
         }

         // subresource locator
         if(null == resourceMethod)
         {
            // locator
            ResourceModel subResource = new ResourceModel(resource, uri.value(), method.getReturnType());
            ResourceLocator locator = new ResourceLocator(subResource);
            locator.freeze();
            resource.addSubResourceLocator(locator);

            // recursive
            parseInternal(subResource);
         }
      }
      else
      {
         for(Class requestType : Convert.REQUEST_TYPES)
         {
            if(method.isAnnotationPresent(requestType))
            {
               // resource method
               Annotation a = method.getAnnotation(requestType);
               ResourceMethod resourceMethod = new ResourceMethod(
                 Convert.annotationToMethodHTTP(a), "", method
               );
               resource.addResourceMethod(resourceMethod);
            }
         }
      }
   }

}
