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

import org.jboss.rs.MethodHTTP;
import org.jboss.rs.model.ResourceModel;
import org.jboss.rs.util.Convert;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class RuntimeContext
{
   // Request method
   private MethodHTTP requestMethod;

   // Request URI
   private URI uri;

   // Accepted response body mime types
   private List<MimeType> consumeMimeTypes = new ArrayList<MimeType>();

   // Request body content-type, available with POST or PUT requests
   private MimeType provideMimeType;

   // The associated runtime model for a web context
   private List<ResourceModel> rootResources;

   public RuntimeContext(MethodHTTP requestMethod, URI uri, List<ResourceModel> rootResources)
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

   public void parseAcceptHeader(String headerValue)
   {
      consumeMimeTypes.addAll( Convert.mimeStringToMimeTypes(headerValue) );
   }

   public void parseContentTypeHeader(String headerValue)
   {
      try
      {
         this.provideMimeType = new MimeType(headerValue);
      }
      catch (MimeTypeParseException e)
      {
         throw new IllegalArgumentException("Failed to parse 'Content-Type' header", e);
      }
   }

   public List<MimeType> getConsumeMimeTypes()
   {      
      return consumeMimeTypes;
   }


   public MimeType getProvideMimeType()
   {
      if(! (requestMethod == MethodHTTP.POST) )
         throw new IllegalArgumentException(requestMethod + " does not provide a 'Content-Type header'");

      assert provideMimeType !=null;
      
      return provideMimeType;
   }

   /**
    * @return the path info of the associated URI.
    */
   public String getPath()
   {
      String path = uri.getPath();
      if(path.startsWith("/"))
         path = path.substring(1);
      return path;
   }
}
