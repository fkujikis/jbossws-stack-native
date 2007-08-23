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
import javax.xml.ws.BindingProvider;

public class RMSequenceFactory
{
   private RMSequenceFactory()
   {
      // forbidden constructor
   }
   
   private static final RMSequence DUMMY_SEQUENCE = new RMSequence()
   {
      private int count = 0;
      
      public boolean completed()
      {
         return true;
      }

      public boolean completed(int timeAmount, TimeUnit timeUnit)
      {
         return true;
      }

      public String getId()
      {
         return "DummySequenceId" + count++;
      }

      public void setLastMessage()
      {
      }

      public void terminate()
      {
      }
   };
 
   public static RMSequence newInstance(Object object)
   {
      if (object instanceof BindingProvider) {
         // allowing creation of sequences only for JBossWS client proxies
         return DUMMY_SEQUENCE; // TODO: RIO implement RM SPI provider
      } else {
         throw new IllegalArgumentException(); // TODO: RIO provide clear message to the client
      }
   }
}
