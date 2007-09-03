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
import org.jboss.ws.extensions.wsrm.spi.protocol.SequenceFault;
import org.jboss.ws.extensions.wsrm.spi.protocol.SequenceFaultCode;
import org.w3c.dom.Element;

/*
 * @author richard.opalka@jboss.com
 * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceFault
 */
final class SequenceFaultImpl implements SequenceFault
{
   
   private SequenceFaultCode faultCode;
   private Exception detail;

   SequenceFaultImpl()
   {
      // allow inside package use only
   }
   
   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceFault#getDetail()
    */
   public Exception getDetail()
   {
      return this.detail;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceFault#getFaultCode()
    */
   public SequenceFaultCode getFaultCode()
   {
      return this.faultCode;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceFault#setDetail(java.lang.Exception)
    */
   public void setDetail(Exception detail)
   {
      if (detail == null)
         throw new IllegalArgumentException("Detail cannot be null");
      if (this.detail != null)
         throw new UnsupportedOperationException("Value already set, cannot be overriden");

      this.detail = detail;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.SequenceFault#setFaultCode(org.jboss.ws.extensions.wsrm.spi.protocol.SequenceFaultCode)
    */
   public void setFaultCode(SequenceFaultCode faultCode)
   {
      if (faultCode == null)
         throw new IllegalArgumentException("Fault code cannot be null");
      if (this.faultCode != null)
         throw new UnsupportedOperationException("Value already set, cannot be overriden");

      this.faultCode = faultCode;
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
      if (this.faultCode == null)
         throw new IllegalStateException();
   }

}
