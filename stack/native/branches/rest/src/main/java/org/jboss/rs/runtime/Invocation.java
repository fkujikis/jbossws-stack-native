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

import java.util.List;
import java.util.LinkedList;
import java.lang.reflect.Method;

/**
 * The visitor part of the pattern.
 *
 * @see org.jboss.rs.runtime.InvocationModel
 * 
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class Invocation
{
   private RuntimeContext context;

   // Actual paramter instances
   private List parameterInstances = new LinkedList();

   // invocation metadata about the method
   private Method targetMethod;

   // invocation metadata about the bean
   private Class targetBean;
   
   public Invocation(RuntimeContext context)
   {
      this.context = context;
   }

   public RuntimeContext getContext()
   {
      return context;
   }

   /**
    * Convinience method to work with String parameter types
    * @param index
    * @param param
    */
   public void insertParameterInstance(int index, String param)
   {
      if(param.startsWith("/"))
         param = param.substring(1);
      parameterInstances.add(index, param);
   }

   public void insertParameterInstance(int index, Object param)
   {
      parameterInstances.add(index, param);
   }

   public List getParameterInstances()
   {
      return parameterInstances;
   }

   public void setTargetMethod(Method targetMethod)
   {
      this.targetMethod = targetMethod;
   }

   public void setTargetBean(Class targetBean)
   {
      this.targetBean = targetBean;
   }

   public Method getTargetMethod()
   {
      return targetMethod;
   }

   public Class getTargetBean()
   {
      return targetBean;
   }
}
