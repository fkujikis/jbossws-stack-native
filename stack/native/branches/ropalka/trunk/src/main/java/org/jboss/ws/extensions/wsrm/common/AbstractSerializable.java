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
package org.jboss.ws.extensions.wsrm.common;

import javax.xml.soap.SOAPMessage;

import org.jboss.util.NotImplementedException;
import org.jboss.ws.extensions.wsrm.spi.Provider;
import org.jboss.ws.extensions.wsrm.spi.protocol.Serializable;

/**
 * Utility class which should be used by all WS-RM protocol providers.
 * @author richard.opalka@jboss.com
 * @see org.jboss.ws.extensions.wsrm.spi.protocol.Serializable
 */
public abstract class AbstractSerializable implements Serializable
{
   
   protected AbstractSerializable()
   {
      // intended to be overriden
   }
 
   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.Serializable#deserializeFrom(javax.xml.soap.SOAPMessage)
    */
   public final void deserializeFrom(SOAPMessage soapMessage)
   {
      // TODO: implement deserialization using object set methods
      if (true) throw new NotImplementedException();
      validate();
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.Serializable#serializeTo(javax.xml.soap.SOAPMessage)
    */
   public final void serializeTo(SOAPMessage soapMessage)
   {
      validate();
      // TODO implement serialization using object instance fields
      throw new NotImplementedException();
   }
   
   public abstract Provider getProvider();

}
