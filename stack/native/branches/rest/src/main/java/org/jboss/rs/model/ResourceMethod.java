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
import org.jboss.rs.util.Convert;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.activation.MimeType;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class ResourceMethod extends AbstractRegexResolveable
{   
   private MethodHTTP methodHTTP;
   private String uriTemplate;
   private Method invocationTarget;

   private List<MimeType> consumeMimeTypes = new ArrayList<MimeType>();
   private List<MimeType> produceMimeTypes = new ArrayList<MimeType>();

   ResourceMethod(MethodHTTP method, String uriTemplate, Method invocationTarget)
   {
      this.uriTemplate = uriTemplate;
      this.methodHTTP = method;
      this.invocationTarget = invocationTarget;
   }

   public MethodHTTP getMethodHTTP()
   {
      return methodHTTP;
   }

   public String getUriTemplate()
   {
      return this.uriTemplate;
   }

   boolean hasChildren()
   {
      // always append '(/)?' to the regex
      return false;  
   }

   public Method getInvocationTarget()
   {
      return invocationTarget;
   }


   public List<MimeType> getConsumeMimeTypes()
   {
      return consumeMimeTypes;
   }

   public List<MimeType> getProduceMimeTypes()
   {
      return produceMimeTypes;
   }

   void freeze()
   {
      initFromUriTemplate(this.uriTemplate);
      initMimeTypes();
   }

   private void initMimeTypes()
   {

      // In the absence of either of these annotations,
      // support for any media type (“*/*”) is assumed.

      if(invocationTarget.isAnnotationPresent(ConsumeMime.class))
      {
         ConsumeMime cm = invocationTarget.getAnnotation(ConsumeMime.class);
         consumeMimeTypes.addAll(Convert.annotationToMimeType(cm));
      }
      else
      {
         consumeMimeTypes.add( Convert.ANY_MIME );
      }

      if(invocationTarget.isAnnotationPresent(ProduceMime.class))
      {
         ProduceMime pm = invocationTarget.getAnnotation(ProduceMime.class);
         produceMimeTypes.addAll(Convert.annotationToMimeType(pm));
      }
      else
      {
         produceMimeTypes.add( Convert.ANY_MIME );
      }
   }


   public String toString()
   {
      return "ResourceMethod {"+methodHTTP+" uri="+uriTemplate+", regex="+regexPattern+"}";
   }
}
