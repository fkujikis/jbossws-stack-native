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

import javax.activation.MimeType;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Resource mthod meta data.
 * 
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class ResourceMethod extends AbstractResourceOperation {

   private MethodHTTP methodHTTP;
   private List<MimeType> consumeMimeTypes = new ArrayList<MimeType>();
   private List<MimeType> produceMimeTypes = new ArrayList<MimeType>();

   ResourceMethod(ResourceModel parent, MethodHTTP method, String uriTemplate, Method invocationTarget)
   {
      super(uriTemplate, invocationTarget);
      super.parent = parent;
      this.methodHTTP = method;
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
      super.freeze();

      // parse the mime annotations
      initMimeTypes();
   }

   private void initMimeTypes()
   {      
      // ConsumeMime
      ConsumeMime consumeMime = (ConsumeMime)mimeFromMethodOrClass(ConsumeMime.class);
      if(consumeMime != null)
         consumeMimeTypes.addAll(Convert.annotationToMimeType(consumeMime));
      else
         consumeMimeTypes.add( Convert.ANY_MIME );

      // ProduceMime
      ProduceMime produceMime = (ProduceMime)mimeFromMethodOrClass(ProduceMime.class);
      if(produceMime != null)
         produceMimeTypes.addAll(Convert.annotationToMimeType(produceMime));
      else
         produceMimeTypes.add( Convert.ANY_MIME );
   }

   private Annotation mimeFromMethodOrClass( Class type )
   {
      Annotation ann = invocationTarget.isAnnotationPresent(type) ?
        invocationTarget.getAnnotation(type) :
        invocationTarget.getDeclaringClass().getAnnotation(type);

      return ann;
   }

   public String toString()
   {
      return "ResourceMethod {"+methodHTTP+" uri="+uriTemplate+", regex="+regexPattern+"}";
   }
}
