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

import java.util.LinkedList;
import java.util.List;

/**
 * Gathers invocation models (both static and runtime)
 * and builds an {@link org.jboss.rs.runtime.Invocation} instance that can be consumed
 * by an {@link org.jboss.rs.runtime.InvocationHandler}
 * 
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public abstract class InvocationBuilder
{
   private List<InvocationModel> invocationModels = new LinkedList<InvocationModel>();

   /**
    * Add invocaiton model to an ordered list.
    * 
    * @param model
    */
   public void addInvocationModel(InvocationModel model )
   {
      invocationModels.add(model);
   }

   public Invocation build(RuntimeContext context)
   {
      Invocation invocation = new Invocation(context);
      
      for(InvocationModel model : invocationModels)
      {
         model.accept(invocation);
      }

      return invocation;
   }
}
