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

import static org.jboss.ws.extensions.wsrm.common.serialization.SerializationHelper.getOptionalElement;
import static org.jboss.ws.extensions.wsrm.common.serialization.SerializationHelper.getRequiredElement;
import static org.jboss.ws.extensions.wsrm.common.serialization.SerializationHelper.getRequiredTextContent;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.jboss.ws.extensions.wsrm.ReliableMessagingException;
import org.jboss.ws.extensions.wsrm.spi.Constants;
import org.jboss.ws.extensions.wsrm.spi.Provider;
import org.jboss.ws.extensions.wsrm.spi.protocol.CloseSequence;

/**
 * <b>CloseSequence</b> object de/serializer
 * @author richard.opalka@jboss.com
 */
final class CloseSequenceSerializer
{

   private CloseSequenceSerializer()
   {
      // no instances
   }
   
   /**
    * Deserialize <b>CloseSequence</b> using <b>provider</b> from the <b>soapMessage</b>
    * @param object to be deserialized
    * @param provider wsrm provider to be used for deserialization process
    * @param soapMessage soap message from which object will be deserialized
    */
   public static void deserialize(CloseSequence object, Provider provider, SOAPMessage soapMessage)
   throws ReliableMessagingException
   {
      try
      {
         SOAPBody soapBody = soapMessage.getSOAPPart().getEnvelope().getBody();
         Constants wsrmConstants = provider.getConstants();
         
         // read wsrm:CloseSequence
         QName closeSequenceQName = wsrmConstants.getCloseSequenceQName();
         SOAPElement closeSequenceElement = getRequiredElement(soapBody, closeSequenceQName, "soap body");

         // read wsrm:Identifier
         QName identifierQName = wsrmConstants.getIdentifierQName();
         SOAPElement identifierElement = getRequiredElement(closeSequenceElement, identifierQName, closeSequenceQName);
         String identifier = getRequiredTextContent(identifierElement, identifierQName);
         object.setIdentifier(identifier);
         
         // read wsrm:LastMsgNumber
         QName lastMsgNumberQName = wsrmConstants.getLastMsgNumberQName();
         SOAPElement lastMsgNumberElement = getOptionalElement(closeSequenceElement, lastMsgNumberQName, closeSequenceQName);
         if (lastMsgNumberElement != null)
         {
            try
            {
               long lastMsgNumber = Long.valueOf(getRequiredTextContent(lastMsgNumberElement, lastMsgNumberQName));
               object.setLastMsgNumber(lastMsgNumber);
            } catch (NumberFormatException nfe)
            {
               throw new ReliableMessagingException("Unable to parse LastMsgNumber element text content", nfe);
            }
         }
      }
      catch (SOAPException se)
      {
         throw new ReliableMessagingException("Unable to deserialize RM message", se);
      }
   }

   /**
    * Serialize <b>CloseSequence</b> using <b>provider</b> to the <b>soapMessage</b>
    * @param object to be serialized
    * @param provider wsrm provider to be used for serialization process
    * @param soapMessage soap message to which object will be serialized
    */
   public static void serialize(CloseSequence object, Provider provider, SOAPMessage soapMessage)
   throws ReliableMessagingException
   {
      try
      {
         SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
         Constants wsrmConstants = provider.getConstants();
         
         // Add xmlns:wsrm declaration
         soapEnvelope.addNamespaceDeclaration(wsrmConstants.getPrefix(), wsrmConstants.getNamespaceURI());

         // write wsrm:CloseSequence
         QName closeSequenceQName = wsrmConstants.getCloseSequenceQName(); 
         SOAPElement closeSequenceElement = soapEnvelope.getBody().addChildElement(closeSequenceQName);

         // write wsrm:Identifier
         QName identifierQName = wsrmConstants.getIdentifierQName();
         closeSequenceElement.addChildElement(identifierQName).setValue(object.getIdentifier());
         
         if (object.getLastMsgNumber() != 0)
         {
            // write wsrm:LastMsgNumber
            QName lastMsgNumberQName = wsrmConstants.getLastMsgNumberQName();
            SOAPElement lastMsgNumberElement = closeSequenceElement.addChildElement(lastMsgNumberQName);
            lastMsgNumberElement.setValue(String.valueOf(object.getLastMsgNumber()));
         }
      }
      catch (SOAPException se)
      {
         throw new ReliableMessagingException("Unable to serialize RM message", se);
      }
   }

}
