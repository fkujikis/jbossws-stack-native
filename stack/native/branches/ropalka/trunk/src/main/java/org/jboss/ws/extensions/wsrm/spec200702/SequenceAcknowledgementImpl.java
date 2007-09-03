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

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

import org.jboss.util.NotImplementedException;
import org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement;
import org.w3c.dom.Element;

/*
 * @author richard.opalka@jboss.com
 * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement
 */
final class SequenceAcknowledgementImpl implements SequenceAcknowledgement
{
   
   private final List<Long> nacks = new LinkedList<Long>();
   private final List<AcknowledgementRange> acknowledgementRanges = new LinkedList<AcknowledgementRange>(); 
   private String identifier;
   private boolean isFinal;
   private boolean isNone;
   
   SequenceAcknowledgementImpl()
   {
      // allow inside package use only
   }
   
   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#addAcknowledgementRange(org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement.AcknowledgementRange)
    */
   public void addAcknowledgementRange(AcknowledgementRange newAcknowledgementRange)
   {
      if ((newAcknowledgementRange == null) || (!(newAcknowledgementRange instanceof AcknowledgementRangeImpl)))
         throw new IllegalArgumentException();
      if (this.nacks.size() != 0)
         throw new IllegalStateException("There are already some nacks specified");
      if (this.isNone)
         throw new IllegalStateException("There is already none specified");
      if ((newAcknowledgementRange.getLower() == 0) || (newAcknowledgementRange.getUpper() == 0))
         throw new IllegalArgumentException("Both, lower and upper values must be specified");
      for (AcknowledgementRange alreadyAccepted : acknowledgementRanges)
         checkOverlap(alreadyAccepted, newAcknowledgementRange);
      
      this.acknowledgementRanges.add(newAcknowledgementRange);
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#addNack(long)
    */
   public void addNack(long messageNumber)
   {
      if (this.isFinal)
         throw new IllegalStateException("There is already final specified");
      if (this.isNone)
         throw new IllegalStateException("There is already none specified");
      if (this.acknowledgementRanges.size() != 0)
         throw new IllegalStateException("There are already some acknowledgement ranges specified");
      if (this.nacks.contains(messageNumber))
         throw new IllegalArgumentException("There is already nack with value " + messageNumber + " specified");

      this.nacks.add(messageNumber);
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#getAcknowledgementRanges()
    */
   public List<AcknowledgementRange> getAcknowledgementRanges()
   {
      return Collections.unmodifiableList(acknowledgementRanges);
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#getIdentifier()
    */
   public String getIdentifier()
   {
      return this.identifier;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#getNacks()
    */
   public List<Long> getNacks()
   {
      return Collections.unmodifiableList(nacks);
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#isFinal()
    */
   public boolean isFinal()
   {
      return this.isFinal;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#isNone()
    */
   public boolean isNone()
   {
      return this.isNone;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#newAcknowledgementRange()
    */
   public AcknowledgementRange newAcknowledgementRange()
   {
      return new AcknowledgementRangeImpl();
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#setFinal(boolean)
    */
   public void setFinal()
   {
      if (this.nacks.size() != 0)
         throw new IllegalStateException("There are already some nacks specified");

      this.isFinal = true;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#setIdentifier(java.lang.String)
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
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#setNone(boolean)
    */
   public void setNone()
   {
      if (this.acknowledgementRanges.size() != 0)
         throw new IllegalStateException("There are already some acknowledgement ranges specified");
      if (this.nacks.size() != 0)
         throw new IllegalStateException("There are already some nacks specified");
      
      this.isNone = true;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.XMLSerializable#fromXML(org.w3c.dom.Element)
    */
   public void fromXML(Element e)
   {
      // TODO: implement deserialization using object set methods
      if (true) throw new NotImplementedException();
      ensureLegalState();
   }
   
   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.XMLSerializable#toXML()
    */
   public Element toXML()
   {
      ensureLegalState();
      // TODO implement serialization using object instance fields
      throw new NotImplementedException();
   }
   
   private void ensureLegalState()
   {
      if ((this.acknowledgementRanges.size() == 0) && (this.nacks.size() == 0) && (!this.isNone))
         throw new IllegalStateException();
   }

   private static void checkOverlap(AcknowledgementRange currentRange, AcknowledgementRange newRange)
   {
      if ((currentRange.getLower() <= newRange.getLower()) && (newRange.getLower() <= currentRange.getUpper()))
         throw new IllegalArgumentException(
            "Overlap detected: " + currentRange + " vs. " + newRange);
      if ((currentRange.getLower() <= newRange.getUpper()) && (newRange.getUpper() <= currentRange.getUpper()))
         throw new IllegalArgumentException(
            "Overlap detected: " + currentRange + " vs. " + newRange);
   }
   
   private static final class AcknowledgementRangeImpl implements SequenceAcknowledgement.AcknowledgementRange
   {
      
      private long lower;
      private long upper;

      /*
       * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement.AcknowledgementRange#getLower()
       */
      public long getLower()
      {
         return this.lower;
      }

      /*
       * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement.AcknowledgementRange#getUpper()
       */
      public long getUpper()
      {
         return this.upper;
      }

      /*
       * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement.AcknowledgementRange#setLower(long)
       */
      public void setLower(long lower)
      {
         if (lower <= 0)
            throw new IllegalArgumentException("Value must be greater than 0");
         if (this.lower > 0)
            throw new UnsupportedOperationException("Value already set, cannot be overriden");
         if ((this.upper > 0) && (lower > this.upper))
            throw new IllegalArgumentException("Value must be lower or equal to " + this.upper);
         
         this.lower = lower;
      }

      /*
       * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement.AcknowledgementRange#setUpper(long)
       */
      public void setUpper(long upper)
      {
         if (upper <= 0)
            throw new IllegalArgumentException("Value must be greater than 0");
         if (this.upper > 0)
            throw new UnsupportedOperationException("Value already set, cannot be overriden");
         if ((this.lower > 0) && (this.lower > upper))
            throw new IllegalArgumentException("Value must be greater or equal to " + this.lower);

         this.upper = upper;
      }
      
      /*
       * @see org.jboss.ws.extensions.wsrm.spi.protocol.XMLSerializable#fromXML(org.w3c.dom.Element)
       */
      public void fromXML(Element e)
      {
         // TODO: implement deserialization using object set methods
         if (true) throw new NotImplementedException();
         ensureLegalState();
      }
      
      /*
       * @see org.jboss.ws.extensions.wsrm.spi.protocol.XMLSerializable#toXML()
       */
      public Element toXML()
      {
         ensureLegalState();
         // TODO implement serialization using object instance fields
         throw new NotImplementedException();
      }

      public String toString()
      {
         return "<" + lower + "; " + upper + ">";
      }

      private void ensureLegalState()
      {
         if (this.lower == 0)
            throw new IllegalStateException();
         if (this.upper == 0)
            throw new IllegalStateException();
      }

   }

}
