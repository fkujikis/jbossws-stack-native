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
package org.jboss.rs.media;

import org.jboss.rs.model.ResourceMethod;
import org.jboss.rs.model.ResourceMatch;
import org.jboss.rs.model.NoMethodException;
import org.jboss.rs.runtime.RuntimeContext;
import org.jboss.rs.MethodHTTP;
import org.jboss.logging.Logger;

import javax.activation.MimeType;
import java.util.List;
import java.util.ArrayList;

/**
 * Matches resource methods in the following order:
 * <ol>
 * <li>Request Method
 * <li>Input mime-type
 * <li>Output mime-type
 * </ol>
 *
 * Failure in any of the above steps can result in a {@link org.jboss.rs.model.NoMethodException}:
 * <ol>
 * <li>HTTP 405
 * <li>HTTP 415
 * <li>HTTP 406
 * </ol>
 *
 * @see org.jboss.rs.model.ResourceResolver
 * 
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class DefaultContentNegotiation implements ContentNegotiation
{

   private static Logger log = Logger.getLogger(DefaultContentNegotiation.class);

   public ResourceMethod match(RuntimeContext context, List<ResourceMatch<ResourceMethod>> candidates)
     throws NoMethodException
   {
      ResourceMethod match = null;

      for(ResourceMatch<ResourceMethod> candiate : candidates)
      {

         List<ResourceMethod> matches = matchByHTTPMethod(context, candidates);
         matches = matchByInputMime(context, matches);
         matches = matchByOutputMime(context, matches);

         // TODO: sort results, requires abstracting of RegexQualifier to a more general Qualifier
         log.warn("Conneg sort algo is missing");
         match = matches.get(0);
      }

      return match;
   }

   /**
    * Match by request method -> HTTP 405
    * @param context
    * @param candidates
    * @return
    * @throws NoMethodException
    */
   private List<ResourceMethod> matchByHTTPMethod(RuntimeContext context, List<ResourceMatch<ResourceMethod>> candidates)
     throws NoMethodException
   {
      List<ResourceMethod> matches = new ArrayList<ResourceMethod>();

      for(ResourceMatch<ResourceMethod> candiate : candidates)
      {
         if(context.getRequestMethod() == candiate.model.getMethodHTTP())
         {
            matches.add(candiate.model);
         }
      }

      if(matches.isEmpty())
         throw new NoMethodException(405, "The request method is not supported");

      return matches;
   }

   /**
    * Match by supported input data format -> HTTP 415
    * @param context
    * @param candidates
    * @return
    * @throws NoMethodException
    */
   private List<ResourceMethod> matchByInputMime(RuntimeContext context, List<ResourceMethod> candidates)
     throws NoMethodException
   {
      List<ResourceMethod> matches = new ArrayList<ResourceMethod>();
      MethodHTTP requestMethod = context.getRequestMethod();

      // matching by input mime only works on POST and PUT
      if(MethodHTTP.POST == requestMethod || MethodHTTP.PUT == requestMethod)
      {
         MimeType produceMime = context.getProvideMimeType();
         
         for(ResourceMethod candiate : candidates)
         {
            for(MimeType consumeMime : candiate.getConsumeMimeTypes())
            {
               if(consumeMime.match(produceMime))
                  matches.add(candiate);
            }
         }

         if(matches.isEmpty())
            throw new NoMethodException(415, "No consumer for " + produceMime);
      }
      else
      {
         // Not matching by input mime
         matches = candidates;
      }

      return matches;
   }


   /**
    * Match by supported output data format -> HTTP 406
    *
    * @param context
    * @param candidates
    * @return
    * @throws NoMethodException
    */
   private List<ResourceMethod> matchByOutputMime(RuntimeContext context, List<ResourceMethod> candidates)
     throws NoMethodException
   {
      List<ResourceMethod> matches = new ArrayList<ResourceMethod>();
      List<MimeType> requestConsumeMimes = context.getConsumeMimeTypes();

      for(ResourceMethod candiate : candidates)
      {
         for(MimeType methodProduceMime : candiate.getProduceMimeTypes())
         {
            for(MimeType requestConsumeMime : requestConsumeMimes)
            {
               if(methodProduceMime.match(requestConsumeMime))
                  matches.add(candiate);
            }
         }
      }

      if(matches.isEmpty())
         throw new NoMethodException(415, "No producer for " + requestConsumeMimes);

      return matches;
   }



}
