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
package org.jboss.ws.extensions.wsrm.common.serialization;

import org.jboss.ws.extensions.wsrm.spi.Provider;
import org.jboss.ws.extensions.wsrm.spi.protocol.AckRequested;
import org.jboss.ws.extensions.wsrm.spi.protocol.CloseSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.CloseSequenceResponse;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse;
import org.jboss.ws.extensions.wsrm.spi.protocol.Sequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement;
import org.jboss.ws.extensions.wsrm.spi.protocol.SequenceFault;
import org.jboss.ws.extensions.wsrm.spi.protocol.TerminateSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.TerminateSequenceResponse;
import javax.xml.soap.SOAPMessage;

/**
 * Utility class used for de/serialization
 * @author richard.opalka@jboss.com
 */
final class SerializationRepository
{

   private SerializationRepository()
   {
      // forbidden inheritance
   }
   
   public static void serialize(AbstractSerializable object, SOAPMessage soapMessage)
   {
      Provider provider = object.getProvider();
      
      if (object instanceof AckRequested)
         AckRequestedSerializer
            .serialize((AckRequested)object, provider, soapMessage);
      if (object instanceof CloseSequence)
         CloseSequenceSerializer
            .serialize((CloseSequence)object, provider, soapMessage);
      if (object instanceof CloseSequenceResponse)
         CloseSequenceResponseSerializer
            .serialize((CloseSequenceResponse)object, provider, soapMessage);
      if (object instanceof CreateSequence)
         CreateSequenceSerializer
            .serialize((CreateSequence)object, provider, soapMessage);
      if (object instanceof CreateSequenceResponse)
         CreateSequenceResponseSerializer
            .serialize((CreateSequenceResponse)object, provider, soapMessage);
      if (object instanceof SequenceAcknowledgement)
         SequenceAcknowledgementSerializer
            .serialize((SequenceAcknowledgement)object, provider, soapMessage);
      if (object instanceof SequenceFault)
         SequenceFaultSerializer
            .serialize((SequenceFault)object, provider, soapMessage);
      if (object instanceof Sequence)
         SequenceSerializer
            .serialize((Sequence)object, provider, soapMessage);
      if (object instanceof TerminateSequence)
         TerminateSequenceSerializer
            .serialize((TerminateSequence)object, provider, soapMessage);
      if (object instanceof TerminateSequenceResponse)
         TerminateSequenceResponseSerializer
            .serialize((TerminateSequenceResponse)object, provider, soapMessage);
      
      throw new IllegalArgumentException();
   }

   public static void deserialize(AbstractSerializable object, SOAPMessage soapMessage)
   {
      Provider provider = object.getProvider();
      
      if (object instanceof AckRequested)
         AckRequestedSerializer
            .deserialize((AckRequested)object, provider, soapMessage);
      if (object instanceof CloseSequence)
         CloseSequenceSerializer
            .deserialize((CloseSequence)object, provider, soapMessage);
      if (object instanceof CloseSequenceResponse)
         CloseSequenceResponseSerializer
            .deserialize((CloseSequenceResponse)object, provider, soapMessage);
      if (object instanceof CreateSequence)
         CreateSequenceSerializer
            .deserialize((CreateSequence)object, provider, soapMessage);
      if (object instanceof CreateSequenceResponse)
         CreateSequenceResponseSerializer
            .deserialize((CreateSequenceResponse)object, provider, soapMessage);
      if (object instanceof SequenceAcknowledgement)
         SequenceAcknowledgementSerializer
            .deserialize((SequenceAcknowledgement)object, provider, soapMessage);
      if (object instanceof SequenceFault)
         SequenceFaultSerializer
            .deserialize((SequenceFault)object, provider, soapMessage);
      if (object instanceof Sequence)
         SequenceSerializer
            .deserialize((Sequence)object, provider, soapMessage);
      if (object instanceof TerminateSequence)
         TerminateSequenceSerializer
            .deserialize((TerminateSequence)object, provider, soapMessage);
      if (object instanceof TerminateSequenceResponse)
         TerminateSequenceResponseSerializer
            .deserialize((TerminateSequenceResponse)object, provider, soapMessage);
      
      throw new IllegalArgumentException();
   }
   
}
