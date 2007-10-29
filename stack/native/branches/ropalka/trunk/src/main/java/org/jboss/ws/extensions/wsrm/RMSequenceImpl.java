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
package org.jboss.ws.extensions.wsrm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.namespace.QName;

import org.jboss.ws.core.jaxws.client.ClientImpl;
import org.jboss.ws.extensions.wsrm.client_api.RMException;
import org.jboss.ws.extensions.wsrm.client_api.RMSequence;
import org.jboss.ws.extensions.wsrm.spi.Provider;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse;
import org.jboss.ws.extensions.wsrm.spi.protocol.Serializable;

/**
 * Reliable messaging sequence implementation
 *
 * @author richard.opalka@jboss.com
 *
 * @since Oct 25, 2007
 */
public final class RMSequenceImpl implements RMSequence
{
   private final String id;
   private final ClientImpl client;
   // object states variables
   private boolean terminated = false;
   private boolean discarded = false;
   private AtomicLong messageNumber = new AtomicLong();
   private final Lock objectLock = new ReentrantLock();
   
   public RMSequenceImpl(ClientImpl client, String id)
   {
      super();
      this.client = client;
      this.id = id;
   }

   public final long newMessageNumber()
   {
      this.objectLock.lock();
      try
      {
         return this.messageNumber.incrementAndGet();
      }
      finally 
      {
         this.objectLock.unlock();
      }
   }
   
   public final long getLastMessageNumber()
   {
      this.objectLock.lock();
      try
      {
         return this.messageNumber.get();
      }
      finally
      {
         this.objectLock.unlock();
      }
   }
   
   public final void discard() throws RMException
   {
      this.objectLock.lock();
      try
      {
         this.client.getWSRMLock().lock();
         try
         {
            this.client.setWSRMSequence(null);
            this.discarded = true;
         }
         finally
         {
            this.client.getWSRMLock().unlock();
         }
      }
      finally
      {
         this.objectLock.unlock();
      }
   }

   public final void terminate() throws RMException
   {
      this.objectLock.lock();
      try
      {
         if (this.terminated)
            return; 
         
         this.terminated = true;
         
         client.getWSRMLock().lock();
         try 
         {
            try
            {
               QName terminateSequenceQN = Provider.get().getConstants().getTerminateSequenceQName();
               Map<String, Object> rmRequestContext = new HashMap<String, Object>();
               rmRequestContext.put(RMConstant.OPERATION_TYPE, RMConstant.TERMINATE_SEQUENCE);
               rmRequestContext.put(RMConstant.SEQUENCE_REFERENCE, client.getWSRMSequence());
               this.client.getBindingProvider().getRequestContext().put(RMConstant.REQUEST_CONTEXT, rmRequestContext);
               this.client.invoke(terminateSequenceQN, new Object[] {}, this.client.getBindingProvider().getResponseContext());
            }
            catch (Exception e) {
               throw new RMException("Unable to create WSRM sequence", e);
            }
         }
         finally
         {
            this.client.getWSRMLock().unlock();
         }
      }
      finally
      {
         this.objectLock.unlock();
      }
   }

   public final boolean isCompleted()
   {
      return true;
   }

   public final boolean isCompleted(int timeAmount, TimeUnit timeUnit)
   {
      return true;
   }

   public final String getId()
   {
      return id;
   }

   public final boolean isTerminated()
   {
      return this.terminated;
   }

   public final boolean isDiscarded()
   {
      return this.discarded;
   }
}
