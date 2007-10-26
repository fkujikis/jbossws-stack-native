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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.ws.core.jaxws.client.ClientImpl;
import org.jboss.ws.extensions.wsrm.client_api.RMException;
import org.jboss.ws.extensions.wsrm.client_api.RMSequence;

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
   private boolean closed = false;
   private boolean terminated = false;
   private boolean discarded = false;
   private boolean lastMessage = false;
   private AtomicLong messageNo = new AtomicLong();
   
   public RMSequenceImpl(ClientImpl client, String id)
   {
      super();
      this.client = client;
      this.id = id;
   }

   public final void close() throws RMException
   {
      this.closed = true;
   }
   
   public final long newMessageNumber()
   {
      return this.messageNo.incrementAndGet();
   }
   
   public final long getLastMessageNumber()
   {
      return this.messageNo.get();
   }
   
   public final void discard() throws RMException
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

   public final void terminate() throws RMException
   {
      this.terminated = true;
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

   public final void setLastMessage()
   {
      this.lastMessage = true;
   }
   
   public final boolean isLastMessage()
   {
      return this.lastMessage;
   }

   public final boolean isTerminated()
   {
      return this.terminated;
   }

   public final boolean isClosed()
   {
      return this.closed;
   }
   
   public boolean isDiscarded()
   {
      return this.discarded;
   }
}
