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

import org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement;
import org.w3c.dom.Element;

/*
 * @author richard.opalka@jboss.com
 * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement
 */
final class SequenceAcknowledgementImpl implements SequenceAcknowledgement
{
   
   SequenceAcknowledgementImpl()
   {
      // allow inside package use only
   }
   
   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#addAcknowledgementRange(org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement.AcknowledgementRange)
    */
   public void addAcknowledgementRange(AcknowledgementRange acknowledgementRange)
   {
      // TODO Auto-generated method stub

   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#addNack(long)
    */
   public void addNack(long messageNumber)
   {
      // TODO Auto-generated method stub

   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#getAcknowledgementRanges()
    */
   public List<AcknowledgementRange> getAcknowledgementRanges()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#getIdentifier()
    */
   public String getIdentifier()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#getNacks()
    */
   public List<Long> getNacks()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#isFinal()
    */
   public boolean isFinal()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#isNone()
    */
   public boolean isNone()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#newAcknowledgementRange()
    */
   public AcknowledgementRange newAcknowledgementRange()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#setFinal(boolean)
    */
   public void setFinal(boolean isFinal)
   {
      // TODO Auto-generated method stub

   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#setIdentifier(java.lang.String)
    */
   public void setIdentifier(String identifier)
   {
      // TODO Auto-generated method stub

   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement#setNone(boolean)
    */
   public void setNone(boolean isNone)
   {
      // TODO Auto-generated method stub

   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.XMLSerializable#fromXML(org.w3c.dom.Element)
    */
   public void fromXML(Element e)
   {
      // TODO Auto-generated method stub

   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.XMLSerializable#toXML()
    */
   public Element toXML()
   {
      // TODO Auto-generated method stub
      return null;
   }

}
