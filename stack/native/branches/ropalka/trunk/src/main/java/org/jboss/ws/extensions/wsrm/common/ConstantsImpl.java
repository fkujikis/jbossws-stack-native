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

import javax.xml.namespace.QName;

import org.jboss.ws.extensions.wsrm.spi.Constants;

/**
 * Utility class which should be used by all WS-RM protocol providers.
 * @author richard.opalka@jboss.com
 * @see org.jboss.ws.extensions.wsrm.spi.Constants
 */
public final class ConstantsImpl implements Constants
{
   // default namespace prefix
   private static final String WSRM_PREFIX = "wsrm";
   // heavily used constants
   private final QName acceptQName;
   private final QName ackRequestedQName;
   private final QName acknowledgementRangeQName;
   private final QName acksToQName;
   private final QName closeSequenceQName;
   private final QName closeSequenceResponseQName;
   private final QName createSequenceQName;
   private final QName createSequenceResponseQName;
   private final QName detailQName;
   private final QName endpointQName;
   private final QName expiresQName;
   private final QName faultCodeQName;
   private final QName finalQName;
   private final QName identifierQName;
   private final QName incompleteSequenceBehaviorQName;
   private final QName lastMessageNumberQName;
   private final QName lastMessageQName;
   private final QName lastMsgNumberQName;
   private final QName lowerQName;
   private final QName messageNumberQName;
   private final QName nackQName;
   private final QName noneQName;
   private final QName offerQName;
   private final QName sequenceAcknowledgementQName;
   private final QName sequenceFaultQName;
   private final QName equenceQName;
   private final QName terminateSequenceQName;
   private final QName terminateSequenceResponseQName;
   private final QName upperQName;
   
   public ConstantsImpl(String namespace)
   {
      if ((namespace == null) || (namespace.trim().equals("")))
         throw new IllegalArgumentException();
         
      acceptQName = new QName(WSRM_PREFIX, namespace, "Accept");
      ackRequestedQName = new QName(WSRM_PREFIX, namespace, "AckRequested");
      acknowledgementRangeQName = new QName(WSRM_PREFIX, namespace, "AcknowledgementRange");
      acksToQName = new QName(WSRM_PREFIX, namespace, "AcksTo");
      closeSequenceQName = new QName(WSRM_PREFIX, namespace, "CloseSequence");
      closeSequenceResponseQName = new QName(WSRM_PREFIX, namespace, "CloseSequenceResponse");
      createSequenceQName = new QName(WSRM_PREFIX, namespace, "CreateSequence");
      createSequenceResponseQName = new QName(WSRM_PREFIX, namespace, "CreateSequenceResponse");
      detailQName = new QName(WSRM_PREFIX, namespace, "Detail");
      endpointQName = new QName(WSRM_PREFIX, namespace, "Endpoint");
      expiresQName = new QName(WSRM_PREFIX, namespace, "Expires");
      faultCodeQName = new QName(WSRM_PREFIX, namespace, "FaultCode");
      finalQName = new QName(WSRM_PREFIX, namespace, "Final");
      identifierQName = new QName(WSRM_PREFIX, namespace, "Identifier");
      incompleteSequenceBehaviorQName = new QName(WSRM_PREFIX, namespace, "IncompleteSequenceBehavior");
      lastMessageNumberQName = new QName(WSRM_PREFIX, namespace, "LastMessageNumber");
      lastMessageQName = new QName(WSRM_PREFIX, namespace, "LastMessage");
      lastMsgNumberQName = new QName(WSRM_PREFIX, namespace, "LastMsgNumber");
      lowerQName = new QName(WSRM_PREFIX, namespace, "Lower");
      messageNumberQName = new QName(WSRM_PREFIX, namespace, "MessageNumber");
      nackQName = new QName(WSRM_PREFIX, namespace, "Nack");
      noneQName = new QName(WSRM_PREFIX, namespace, "None");
      offerQName = new QName(WSRM_PREFIX, namespace, "Offer");
      sequenceAcknowledgementQName = new QName(WSRM_PREFIX, namespace, "SequenceAcknowledgement");
      sequenceFaultQName = new QName(WSRM_PREFIX, namespace, "SequenceFault");
      equenceQName = new QName(WSRM_PREFIX, namespace, "Sequence");
      terminateSequenceQName = new QName(WSRM_PREFIX, namespace, "TerminateSequence");
      terminateSequenceResponseQName = new QName(WSRM_PREFIX, namespace, "TerminateSequenceResponse");
      upperQName = new QName(WSRM_PREFIX, namespace, "Upper");
   }
   
   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getAcceptQName()
    */
   public final QName getAcceptQName()
   {
      return acceptQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getAckRequestedQName()
    */
   public final QName getAckRequestedQName()
   {
      return ackRequestedQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getAcknowledgementRangeQName()
    */
   public final QName getAcknowledgementRangeQName()
   {
      return acknowledgementRangeQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getAcksToQName()
    */
   public final QName getAcksToQName()
   {
      return acksToQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getCloseSequenceQName()
    */
   public final QName getCloseSequenceQName()
   {
      return closeSequenceQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getCloseSequenceResponseQName()
    */
   public final QName getCloseSequenceResponseQName()
   {
      return closeSequenceResponseQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getCreateSequenceQName()
    */
   public final QName getCreateSequenceQName()
   {
      return createSequenceQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getCreateSequenceResponseQName()
    */
   public final QName getCreateSequenceResponseQName()
   {
      return createSequenceResponseQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getDetailQName()
    */
   public final QName getDetailQName()
   {
      return detailQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getEndpointQName()
    */
   public final QName getEndpointQName()
   {
      return endpointQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getExpiresQName()
    */
   public final QName getExpiresQName()
   {
      return expiresQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getFaultCodeQName()
    */
   public final QName getFaultCodeQName()
   {
      return faultCodeQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getFinalQName()
    */
   public final QName getFinalQName()
   {
      return finalQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getIdentifierQName()
    */
   public final QName getIdentifierQName()
   {
      return identifierQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getIncompleteSequenceBehaviorQName()
    */
   public final QName getIncompleteSequenceBehaviorQName()
   {
      return incompleteSequenceBehaviorQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getLastMessageNumberQName()
    */
   public final QName getLastMessageNumberQName()
   {
      return lastMessageNumberQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getLastMessageQName()
    */
   public final QName getLastMessageQName()
   {
      return lastMessageQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getLastMsgNumberQName()
    */
   public final QName getLastMsgNumberQName()
   {
      return lastMsgNumberQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getLowerQName()
    */
   public final QName getLowerQName()
   {
      return lowerQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getMessageNumberQName()
    */
   public final QName getMessageNumberQName()
   {
      return messageNumberQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getNackQName()
    */
   public final QName getNackQName()
   {
      return nackQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getNoneQName()
    */
   public final QName getNoneQName()
   {
      return noneQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getOfferQName()
    */
   public final QName getOfferQName()
   {
      return offerQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getSequenceAcknowledgementQName()
    */
   public final QName getSequenceAcknowledgementQName()
   {
      return sequenceAcknowledgementQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getSequenceFaultQName()
    */
   public final QName getSequenceFaultQName()
   {
      return sequenceFaultQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getSequenceQName()
    */
   public final QName getSequenceQName()
   {
      return equenceQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getTerminateSequenceQName()
    */
   public final QName getTerminateSequenceQName()
   {
      return terminateSequenceQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getTerminateSequenceResponseQName()
    */
   public final QName getTerminateSequenceResponseQName()
   {
      return terminateSequenceResponseQName;
   }

   /**
    * @see org.jboss.ws.extensions.wsrm.spi.Constants#getUpperQName()
    */
   public final QName getUpperQName()
   {
      return upperQName;
   }

}
