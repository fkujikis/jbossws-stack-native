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

import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence;
import org.w3c.dom.Element;

/*
 * @author richard.opalka@jboss.com
 * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence
 */
final class CreateSequenceImpl implements CreateSequence
{

   CreateSequenceImpl()
   {
      // allow inside package use only
   }
   
   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence#getAcksTo()
    */
   public String getAcksTo()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence#getExpires()
    */
   public String getExpires()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence#getOffer()
    */
   public Offer getOffer()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence#newOffer()
    */
   public Offer newOffer()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence#setAcksTo(java.lang.String)
    */
   public void setAcksTo(String address)
   {
      // TODO Auto-generated method stub

   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence#setExpires(java.lang.String)
    */
   public void setExpires(String duration)
   {
      // TODO Auto-generated method stub

   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence#setOffer(org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence.Offer)
    */
   public void setOffer(Offer offer)
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
