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
import org.jboss.logging.Logger;

import javax.ws.rs.UriTemplate;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class ResourceModelParser
{
   private static Logger log = Logger.getLogger(ResourceModelParser.class);

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

      log.debug("Creating resource model from bean: " + bean);

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

      // freeze root resource
      resource.freeze();

      logResourceTree(resource);
   }

   private void parseMethod(Method method, ResourceModel parentResource)
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
                 parentResource,
                 Convert.annotationToMethodHTTP(a), uri.value(), method
               );

               resourceMethod.freeze();
               parentResource.addSubResourceMethod(resourceMethod);
            }
         }

         // subresource locator
         if(null == resourceMethod)
         {
            ResourceModel subResource = new ResourceModel(parentResource, uri.value(), method.getReturnType());
            ResourceLocator locator = new ResourceLocator(parentResource, method, subResource);

            locator.freeze();
            parentResource.addSubResourceLocator(locator);

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
                 parentResource,
                 Convert.annotationToMethodHTTP(a), "", method
               );

               resourceMethod.freeze();
               parentResource.addResourceMethod(resourceMethod);
            }
         }
      }
   }

   private void logResourceTree(ResourceModel resource)
   {
      log.debug("---");
      log.debug(resource);

      // freeze resource methods
      for(ResourceMethod rm : resource.getResourceMethods())
      {
         log.debug(rm);
      }

      // log locators methods
      for(ResourceLocator loc : resource.getResourceLocator())
      {
         log.debug(loc);
      }

      // freeze sub resource methods
      for(ResourceMethod srm : resource.getSubResourceMethods())
      {
         log.debug(srm);
      }

      log.debug("---");
   }

}
