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
package org.jboss.ws.extensions.wsrm.spec200702;

import org.jboss.util.NotImplementedException;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse;
import org.jboss.ws.extensions.wsrm.spi.protocol.IncompleteSequenceBehavior;
import javax.xml.soap.SOAPMessage;

/*
 * @author richard.opalka@jboss.com
 * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse
 */
final class CreateSequenceResponseImpl implements CreateSequenceResponse
{
   
   private String identifier;
   private String expires;
   private Accept accept;
   private IncompleteSequenceBehavior incompleteSequenceBehavior;

   CreateSequenceResponseImpl()
   {
      // allow inside package use only
   }
   
   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse#getAccept()
    */
   public Accept getAccept()
   {
      return this.accept;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse#getDuration()
    */
   public String getExpires()
   {
      return this.expires;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse#getIdentifier()
    */
   public String getIdentifier()
   {
      return this.identifier;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse#getIncompleteSequenceBehavior()
    */
   public IncompleteSequenceBehavior getIncompleteSequenceBehavior()
   {
      return this.incompleteSequenceBehavior;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse#newAccept()
    */
   public Accept newAccept()
   {
      return new AcceptImpl();
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse#setAccept(org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse.Accept)
    */
   public void setAccept(Accept accept)
   {
      if (accept == null)
         throw new IllegalArgumentException("Accept cannot be null");
      if (!(accept instanceof AcceptImpl))
         throw new IllegalArgumentException();
      if (accept.getAcksTo() == null)
         throw new IllegalArgumentException("Accept acksTo must be specified");
      if (this.accept != null)
         throw new UnsupportedOperationException("Value already set, cannot be overriden");
      
      this.accept = accept;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse#setExpires(java.lang.String)
    */
   public void setExpires(String duration)
   {
      if ((duration == null) || (duration.trim().equals("")))
         throw new IllegalArgumentException("Duration cannot be null nor empty string");
      if (this.expires != null)
         throw new UnsupportedOperationException("Value already set, cannot be overriden");
      
      this.expires = duration;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse#setIdentifier(java.lang.String)
    */
   public void setIdentifier(String identifier)
   {
      if ((identifier == null) || (identifier.trim().equals("")))
         throw new IllegalArgumentException("Identifier cannot be null nor empty string");
      if (this.identifier != null)
         throw new UnsupportedOperationException("Value already set, cannot be overriden");
      
      this.identifier = identifier;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse#setIncompleteSequenceBehavior(org.jboss.ws.extensions.wsrm.spi.protocol.IncompleteSequenceBehavior)
    */
   public void setIncompleteSequenceBehavior(IncompleteSequenceBehavior incompleteSequenceBehavior)
   {
      if (incompleteSequenceBehavior == null)
         throw new IllegalArgumentException("Sequence behavior type cannot be null");
      if (this.incompleteSequenceBehavior != null)
         throw new UnsupportedOperationException("Value already set, cannot be overriden");
      
      this.incompleteSequenceBehavior = incompleteSequenceBehavior;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.Serializable#deserializeFrom(javax.xml.soap.SOAPMessage)
    */
   public void deserializeFrom(SOAPMessage soapMessage)
   {
      // TODO: implement deserialization using object set methods
      if (true) throw new NotImplementedException();
      ensureLegalState();
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.Serializable#serializeTo(javax.xml.soap.SOAPMessage)
    */
   public void serializeTo(SOAPMessage soapMessage)
   {
      ensureLegalState();
      // TODO implement serialization using object instance fields
      throw new NotImplementedException();
   }
   
   private void ensureLegalState()
   {
      if (this.identifier == null)
         throw new IllegalStateException();
   }
   
   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse.Accept
    */
   private static final class AcceptImpl implements CreateSequenceResponse.Accept
   {

      private String acksTo;
      
      /*
       * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse.Accept#getAcksTo()
       */
      public String getAcksTo()
      {
         return this.acksTo;
      }

      /*
       * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse.Accept#setAcksTo(java.lang.String)
       */
      public void setAcksTo(String address)
      {
         if ((address == null) || (address.trim().equals("")))
            throw new IllegalArgumentException("Address cannot be null nor empty string");
         if (this.acksTo != null)
            throw new UnsupportedOperationException("Value already set, cannot be overriden");
         
         this.acksTo = address;
      }

      /*
       * @see org.jboss.ws.extensions.wsrm.spi.protocol.Serializable#deserializeFrom(javax.xml.soap.SOAPMessage)
       */
      public void deserializeFrom(SOAPMessage soapMessage)
      {
         // TODO: implement deserialization using object set methods
         if (true) throw new NotImplementedException();
         ensureLegalState();
      }

      /*
       * @see org.jboss.ws.extensions.wsrm.spi.protocol.Serializable#serializeTo(javax.xml.soap.SOAPMessage)
       */
      public void serializeTo(SOAPMessage soapMessage)
      {
         ensureLegalState();
         // TODO implement serialization using object instance fields
         throw new NotImplementedException();
      }

      private void ensureLegalState()
      {
         if (this.acksTo == null)
            throw new IllegalStateException();
      }
      
   }

}
