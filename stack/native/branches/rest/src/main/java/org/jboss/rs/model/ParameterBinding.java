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

import org.jboss.rs.runtime.Invocation;
import org.jboss.rs.runtime.InvocationModel;
import org.jboss.rs.runtime.RuntimeContext;
import org.jboss.rs.runtime.ContextAdapterBuilder;
import org.jboss.rs.MethodHTTP;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.UriParam;
import javax.ws.rs.core.HttpContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Carries mapping information about
 * <ul>
 * <li>MatrixParam
 * <li>QueryParam
 * <li>UriParam
 * <li>HttpContext
 * <li>HeaderParam
 * </ul>
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class ParameterBinding implements InvocationModel
{
   /* Pattenern of the owning ResourceMethod */
   private final Pattern regex;

   /* Pattern to strip prefix from runtime path */
   private static final String PREFIX_PATTERN = "(.*?)";

   /*the total number of parameters */
   private int totalParameters = 0;

   /*positions of the query parameters*/
   Map<String, Integer> queryParam = new HashMap<String, Integer>();

   /*positions of the header parameters*/
   Map<String, Integer> headerParam = new HashMap<String, Integer>();

   /*positions of the header parameters*/
   Map<String, Integer> uriParam = new HashMap<String, Integer>();

   /* maps regex groups to UriTemplate wildcards */
   Map<String, Integer> regexMapping = new HashMap<String, Integer>();

   Map<Class, Integer> contextParamter = new HashMap<Class, Integer>();

   private Class entityBodyType = null;
   private int entityBodyIndex = -1;

   /* Parameter types except for entity body*/
   Map<Integer, Class> parameterTypes = new HashMap<Integer, Class>();

   public void accept(Invocation invocation)
   {
      RuntimeContext ctx = invocation.getContext();
      String path = ctx.getPath();

      Matcher matcher = regex.matcher(path);
      boolean matches = matcher.matches();

      if(!matches)
         throw new RuntimeException("RuntimeContext doesn't match invocation model");

      // @UriParam
      for(String param : uriParam.keySet())
      {
         int paramIndex = uriParam.get(param);
         String paramValue = matcher.group(regexMapping.get(param) + 1);
         invocation.insertParameterInstance(paramIndex, paramValue);
      }

      ContextAdapterBuilder builder = new ContextAdapterBuilder(ctx);
      
      // TODO: @QueryParam and @MatrixParam

      // TODO: @HeaderParam
      
      // @HttpContext
      for(Class paramType : contextParamter.keySet())
      {
         Object paramInstance = builder.buildTypedAdapter(paramType);
         invocation.insertParameterInstance(contextParamter.get(paramType), paramInstance);
      }

      // TODO: Entity body
      if(entityBodyType != null)
      {
         boolean validRequestMethod = MethodHTTP.POST == ctx.getRequestMethod() || MethodHTTP.PUT == ctx.getRequestMethod();
         if(!validRequestMethod)
            throw new RuntimeException("No entity body with request type " + ctx.getRequestMethod());

         // unmarshall body
      }
   }

   ParameterBinding(Pattern rootPattern)
   {
      // Extend pattern to strip root path, results in additional groups
      this.regex = Pattern.compile(PREFIX_PATTERN +rootPattern.toString());
   }

   void registerRegexGroupForParam(int group, String paramName)
   {
      regexMapping.put(paramName, group);
   }

   void registerParameterAnnotations(Method method)
   {      
      for (Annotation[] parameterAnnotations : method.getParameterAnnotations())
      {
         if (parameterAnnotations!=null)
         {
            for (Annotation annotation : parameterAnnotations)
            {
               if(annotation.annotationType() == UriParam.class)
               {
                  UriParam p = (UriParam)annotation;
                  uriParam.put(p.value(), totalParameters);
               }
               else if(annotation.annotationType() == HeaderParam.class)
               {
                  HeaderParam p = (HeaderParam)annotation;
                  headerParam.put(p.value(), totalParameters);
               }
               else if(annotation.annotationType() == HttpContext.class)
               {
                  HttpContext p = (HttpContext)annotation;
                  int paramPosition = totalParameters > 0 ? totalParameters - 1 : 0;
                  Class httpContextType = method.getParameterTypes()[paramPosition];
                  int httpContextIndex = totalParameters;

                  if(null == contextParamter.get(httpContextType))
                     contextParamter.put(httpContextType, httpContextIndex);
                  else
                     throw new RuntimeException("HttpContext on more then one method parameter: " + method.getName()+ "," +httpContextIndex);
               }
            }

         }
         else
         {
            // The value of an non-annotated parameter is mapped from the request entity body
            // Must not be more then one per method
            if(entityBodyType!=null)
               throw new RuntimeException("Method " + method.getName() + " contains more then one possible entity body parameters");

            entityBodyType = method.getParameterTypes()[totalParameters-1]; 
            entityBodyIndex = totalParameters;

         }

         totalParameters++;
      }
   }
   
}
