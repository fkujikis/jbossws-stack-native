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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.addressing.soap.SOAPAddressingProperties;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;
import org.jboss.ws.extensions.wsrm.spi.Constants;
import org.jboss.ws.extensions.wsrm.spi.Provider;
import org.jboss.ws.extensions.wsrm.spi.protocol.AckRequested;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse;
import org.jboss.ws.extensions.wsrm.spi.protocol.Sequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.Serializable;
import org.jboss.ws.extensions.wsrm.spi.protocol.TerminateSequence;

/**
 * TODO: add comment
 *
 * @author richard.opalka@jboss.com
 *
 * @since Oct 23, 2007
 */
public class RMClientHandler extends GenericSOAPHandler
{
   // Provide logging
   private static Logger log = Logger.getLogger(RMClientHandler.class);

   private static Set<QName> HEADERS = new HashSet<QName>();
   private static Provider rmProvider = Provider.get();

   static
   {
      Constants constants = rmProvider.getConstants();
      HEADERS.add(constants.getCreateSequenceQName());
      HEADERS.add(constants.getCloseSequenceQName());
      HEADERS.add(constants.getTerminateSequenceQName());
      HEADERS.add(constants.getCreateSequenceResponseQName());
      HEADERS.add(constants.getCloseSequenceResponseQName());
      HEADERS.add(constants.getTerminateSequenceResponseQName());
   }

   public Set getHeaders()
   {
      return Collections.unmodifiableSet(HEADERS);
   }

   protected boolean handleOutbound(MessageContext msgContext)
   {
      if(log.isDebugEnabled()) log.debug("handleOutbound");

      CommonMessageContext commonMsgContext = (CommonMessageContext)msgContext;
      SOAPAddressingProperties addrProps = (SOAPAddressingProperties)commonMsgContext.get(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
      Map<String, Object> rmRequestContext = (Map<String, Object>)commonMsgContext.get(RMConstant.REQUEST_CONTEXT);
      String operation = (String)rmRequestContext.get(RMConstant.OPERATION_TYPE);
      if (addrProps != null)
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)commonMsgContext).getMessage();
         if (RMConstant.CREATE_SEQUENCE.equals(operation))
         {
            String replyTo = addrProps.getReplyTo().getAddress().getURI().toString();
            CreateSequence createSequence = rmProvider.getMessageFactory().newCreateSequence();
            createSequence.setAcksTo(replyTo);
            createSequence.serializeTo(soapMessage);
            List<Serializable> data = new LinkedList<Serializable>();
            data.add(createSequence);
            rmRequestContext.put(RMConstant.DATA, data);
            
            return true;
         }
         
         if (RMConstant.SEQUENCE.equals(operation))
         {
            RMSequenceImpl sequenceImpl = (RMSequenceImpl)rmRequestContext.get(RMConstant.SEQUENCE_REFERENCE);
            Sequence sequence = rmProvider.getMessageFactory().newSequence();
            sequence.setIdentifier(sequenceImpl.getId());
            sequence.setMessageNumber(sequenceImpl.newMessageNumber());
            sequence.serializeTo(soapMessage);
            
            List<Serializable> data = new LinkedList<Serializable>();
            data.add(sequence);
            
            if (commonMsgContext.getOperationMetaData().isOneWay() == false)
            {
               // TODO: ask msgStore if there are other sequences related to the same
               // endpoint that requires ack and serialize it here
               AckRequested ackRequested = rmProvider.getMessageFactory().newAckRequested();
               ackRequested.setIdentifier(sequenceImpl.getId());
               ackRequested.setMessageNumber(sequenceImpl.getLastMessageNumber());
               ackRequested.serializeTo(soapMessage);
               data.add(ackRequested);
            }
            rmRequestContext.put(RMConstant.DATA, data);
            
            return true;
         }
         
         if (RMConstant.TERMINATE_SEQUENCE.equals(operation))
         {
            RMSequenceImpl sequenceImpl = (RMSequenceImpl)rmRequestContext.get(RMConstant.SEQUENCE_REFERENCE);
            TerminateSequence terminateSequence = rmProvider.getMessageFactory().newTerminateSequence();
            terminateSequence.setIdentifier(sequenceImpl.getId());
            terminateSequence.setLastMsgNumber(sequenceImpl.getLastMessageNumber());
            terminateSequence.serializeTo(soapMessage);
            
            List<Serializable> data = new LinkedList<Serializable>();
            data.add(terminateSequence);
            rmRequestContext.put(RMConstant.DATA, data);
            
            return true;
         }
      }
      else
      {
         throw new IllegalStateException();
      }

      return true;
   }

   protected boolean handleInbound(MessageContext msgContext)
   {
      if(log.isDebugEnabled()) log.debug("handleInbound");

      SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
      // TODO: inspect operation type different way - don't forget on piggy-backing
      Map<String, Object> rmRequestContext = (Map<String, Object>)msgContext.get(RMConstant.REQUEST_CONTEXT);
      String operation = (String)rmRequestContext.get(RMConstant.OPERATION_TYPE);
      if (RMConstant.CREATE_SEQUENCE.equals(operation))
      {
         CreateSequenceResponse createSequenceResponse = rmProvider.getMessageFactory().newCreateSequenceResponse();
         createSequenceResponse.deserializeFrom(soapMessage);
         List<Serializable> data = new LinkedList<Serializable>();
         data.add(createSequenceResponse);
         Map<String, Object> rmResponseContext = new HashMap<String, Object>();
         rmResponseContext.put(RMConstant.OPERATION_TYPE, RMConstant.CREATE_SEQUENCE_RESPONSE);
         rmResponseContext.put(RMConstant.DATA, data);
         msgContext.put(RMConstant.RESPONSE_CONTEXT, rmResponseContext);
         msgContext.setScope(RMConstant.RESPONSE_CONTEXT, Scope.APPLICATION);
      }

      return true;
   }

}
