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
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.addressing.AddressingException;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.addressing.soap.SOAPAddressingBuilder;
import javax.xml.ws.addressing.soap.SOAPAddressingProperties;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;
import org.jboss.ws.extensions.wsrm.spi.Constants;
import org.jboss.ws.extensions.wsrm.spi.Provider;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse;

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
   private static Provider rmProvider = Provider.getInstance("http://docs.oasis-open.org/ws-rx/wsrm/200702");

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

      SOAPAddressingProperties addrProps = (SOAPAddressingProperties)msgContext.get(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
      RMHandlerConstant.Operation operation = (RMHandlerConstant.Operation)msgContext.get(RMHandlerConstant.HANDLER_COMMAND);
      if (addrProps != null)
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
         if (operation == RMHandlerConstant.Operation.CREATE_SEQUENCE)
         {
            String replyTo = addrProps.getReplyTo().getAddress().getURI().toString();
            CreateSequence createSequence = rmProvider.getMessageFactory().newCreateSequence();
            createSequence.setAcksTo(replyTo);
            createSequence.serializeTo(soapMessage);
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
      RMHandlerConstant.Operation operation = (RMHandlerConstant.Operation)msgContext.get(RMHandlerConstant.HANDLER_COMMAND);
      if (operation == RMHandlerConstant.Operation.CREATE_SEQUENCE)
      {
         CreateSequenceResponse createSequenceResponse = rmProvider.getMessageFactory().newCreateSequenceResponse();
         createSequenceResponse.deserializeFrom(soapMessage);
         System.out.println("have wsrm identifier: " + createSequenceResponse.getIdentifier());
      }

      return true;
   }

}
