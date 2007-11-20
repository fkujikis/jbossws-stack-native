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
package org.jboss.rs.util;

import org.jboss.rs.MethodHTTP;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class Convert
{
   
   public static MimeType ANY_MIME;

   static
   {
      try
      {
         ANY_MIME = new MimeType("*/*");
      }
      catch (MimeTypeParseException e)
      {
         //
      }
   }

   public static MethodHTTP prefixToMethodHTTP(Method m)
   {
      MethodHTTP result = null;

      String methodName = m.getName();
      if(methodName.startsWith("get"))
         result = MethodHTTP.GET;
      else if(methodName.startsWith("post"))
         result = MethodHTTP.POST;
      else if(methodName.startsWith("put"))
         result = MethodHTTP.PUT;
      else if(methodName.startsWith("delete"))
         result = MethodHTTP.DELETE;

      if(null==result)
         throw new IllegalArgumentException("Failed to match method by prefix: " + methodName);
      
      return result;
   }

   public static MethodHTTP annotationToMethodHTTP(HttpMethod a)
   {    
      HttpMethod hm = (HttpMethod)a;
      MethodHTTP result = null;

      if(hm.value() == HttpMethod.GET)
         result = MethodHTTP.GET;
      else if(hm.value() == HttpMethod.POST)
         result = MethodHTTP.POST;
      else if(hm.value() == HttpMethod.PUT)
         result = MethodHTTP.PUT;
      else if(hm.value() == HttpMethod.DELETE)
         result = MethodHTTP.DELETE;

      if(null==result)
         throw new IllegalArgumentException("Failed to match method by value: " + hm.value());

      return result;

   }

   public static List<MimeType> annotationToMimeType(ConsumeMime consumeMime)
   {
      return mimeStringsToMimeTypes(consumeMime.value());
   }

   public static List<MimeType> annotationToMimeType(ProduceMime produceMime)
   {
      return mimeStringsToMimeTypes(produceMime.value());
   }

   public static List<MimeType> mimeStringToMimeTypes(String mime)
   {
      List<MimeType> mimes = new ArrayList<MimeType>();

      try
      {
         StringTokenizer tokenizer = new StringTokenizer(mime, ",");
         while(tokenizer.hasMoreTokens())
         {
            String tok = tokenizer.nextToken().trim();
            if(tok.indexOf("/") != -1)  // Ignore mimes without subtype, i.e '*; q=.2'
               mimes.add( new MimeType(tok) );
         }
      }
      catch (MimeTypeParseException e)
      {
         throw new IllegalArgumentException("Failed to parse mime string '"+mime+"'", e);
      }

      return mimes;
   }

   public static List<MimeType> mimeStringsToMimeTypes(String[] mimeStrings)
   {
      List<MimeType> mimes = new ArrayList<MimeType>();

      try
      {
         for(String s : mimeStrings)
         {
            mimes.add( new MimeType(s) );
         }
      }
      catch (MimeTypeParseException e)
      {
         throw new IllegalArgumentException("Failed to parse mime string '"+mimeStrings+"'", e);
      }

      return mimes;
   }
}
