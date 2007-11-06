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
package org.jboss.rs.runtime;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Cookie;
import java.util.List;
import java.net.URI;

/**
 * Adopts 311 runtime interfaces to an internal runtime context.<br>
 * This way typed subsets of runtime information can be exposed, i.e:
 *
 * <ul>
 * <li>{@link javax.ws.rs.core.HttpHeaders}
 * <li>{@link javax.ws.rs.core.UriInfo}
 * </ul>
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class ContextAdapterBuilder
{
   private RuntimeContext context;   

   public ContextAdapterBuilder(RuntimeContext context)
   {
      this.context = context;
   }

   public Object buildTypedAdapter(Class type)
   {
      Object returnType = null;

      if(HttpHeaders.class == type)
      {
         returnType = new HttpHeadersAdapter();
      }
      else if (UriInfo.class == type)
      {
         returnType = new UriInfoAdapter();
      }

      if(null==returnType)
         throw new RuntimeException("Unknown type " + type);
      
      return returnType;
   }

   public class HttpHeadersAdapter implements HttpHeaders
   {

      public MultivaluedMap<String, String> getRequestHeaders()
      {
         return null;
      }

      public List<MediaType> getAcceptableMediaTypes()
      {
         return null;
      }

      public MediaType getMediaType()
      {
         return null;
      }

      public String getLanguage()
      {
         return null;
      }

      public List<Cookie> getCookies()
      {
         return null;
      }
   }

   public class UriInfoAdapter implements UriInfo
   {

      public String getPath()
      {
         return null;
      }

      public String getPath(boolean b)
      {
         return null;
      }

      public List<PathSegment> getPathSegments()
      {
         return null;
      }

      public List<PathSegment> getPathSegments(boolean b)
      {
         return null;
      }

      public URI getAbsolute()
      {
         return null;
      }

      public UriBuilder getBuilder()
      {
         return null;
      }

      public URI getBase()
      {
         return null;
      }

      public UriBuilder getBaseBuilder()
      {
         return null;
      }

      public MultivaluedMap<String, String> getTemplateParameters()
      {
         return null;
      }

      public MultivaluedMap<String, String> getTemplateParameters(boolean b)
      {
         return null;
      }

      public MultivaluedMap<String, String> getQueryParameters()
      {
         return null;
      }

      public MultivaluedMap<String, String> getQueryParameters(boolean b)
      {
         return null;
      }
   }
}
