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

import org.jboss.ws.extensions.wsrm.spi.protocol.Sequence;
import javax.xml.soap.SOAPMessage;
import org.jboss.util.NotImplementedException;

/*
 * @author richard.opalka@jboss.com
 * @see org.jboss.ws.extensions.wsrm.spi.protocol.Sequence
 */
final class SequenceImpl implements Sequence
{
   
   private String identifier;
   private long messageNumber;

   SequenceImpl()
   {
      // allow inside package use only
   }
   
   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.Sequence#getIdentifier()
    */
   public String getIdentifier()
   {
      return this.identifier;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.Sequence#getMessageNumber()
    */
   public long getMessageNumber()
   {
      return messageNumber;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.Sequence#isLastMessage()
    */
   public boolean isLastMessage()
   {
      return false; // always return false for this version of the RM protocol
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.Sequence#setIdentifier(java.lang.String)
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
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.Sequence#setLastMessage(boolean)
    */
   public void setLastMessage(boolean lastMessage)
   {
      // do nothing for this version of the RM protocol
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.Sequence#setMessageNumber(long)
    */
   public void setMessageNumber(long messageNumber)
   {
      if (messageNumber <= 0)
         throw new IllegalArgumentException("Value must be greater than 0");
      if (this.messageNumber > 0)
         throw new UnsupportedOperationException("Value already set, cannot be overriden");
      
      this.messageNumber = messageNumber;
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
      if (this.messageNumber == 0)
         throw new IllegalStateException();
   }

}
