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

import javax.ws.rs.GET;
import javax.ws.rs.UriTemplate;
import java.lang.reflect.Method;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class ResourceModelFactory
{
   public static ResourceModel createModel(Class bean)
   {
      assert bean.isAnnotationPresent(UriTemplate.class);

      // the root resource
      UriTemplate rootUri = (UriTemplate)bean.getAnnotation(UriTemplate.class);
      ResourceModel rootResource = new ResourceModel(rootUri.value(), bean);

      System.out.println("Creating resource model from bean: " + bean);

      parseMetaData(rootResource);

      return rootResource;
   }

   private static void parseMetaData(ResourceModel resource)
   {
      String parent = resource.getParent()!=null ? resource.getParent().getImplementation().getName() : "";
      
      for( Method m : resource.getImplementation().getDeclaredMethods() )
      {
         // handle subresources
         if(m.isAnnotationPresent(UriTemplate.class))
         {
            UriTemplate uri = m.getAnnotation(UriTemplate.class);
            Class<?> returnType = m.getReturnType();

            // TODO: extend to other http methods
            if(m.isAnnotationPresent(GET.class))
            {
               // sub resource method
               GET http = (GET)m.getAnnotation(GET.class);
               ResourceMethod resourceMethod = new ResourceMethod(
                 MethodHTTP.valueOf("GET"), uri.value(), new EntityModel(returnType)
               );
               resource.addSubResourceMethod(resourceMethod);
            }
            else
            {
               // locator
               ResourceModel subResource = new ResourceModel(resource, uri.value(), returnType);
               ResourceLocator locator = new ResourceLocator(subResource);
               locator.freeze();
               resource.addSubResourceLocator(locator);

               // recursive
               parseMetaData(subResource);
            }
         }

      }

      // freeze resource
      resource.freeze();

      System.out.println("---");
      System.out.println(resource);

      // freeze sub resource methods
      for(ResourceMethod srm : resource.getSubResourceMethods())
      {
         srm.freeze();
         System.out.println(srm);
      }
      
      System.out.println("---");
   }
}
