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
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingConstants;

import org.jboss.ws.extensions.wsrm.ReliableMessagingException;
import org.jboss.ws.extensions.wsrm.spi.Constants;
import org.jboss.ws.extensions.wsrm.spi.Provider;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse;
import org.jboss.ws.extensions.wsrm.spi.protocol.IncompleteSequenceBehavior;

/**
 * <b>CreateSequenceResponse</b> object de/serializer
 * @author richard.opalka@jboss.com
 */
final class CreateSequenceResponseSerializer
{

   private static final AddressingConstants ADDRESSING_CONSTANTS = 
      AddressingBuilder.getAddressingBuilder().newAddressingConstants();
   
   private CreateSequenceResponseSerializer()
   {
      // no instances
   }
   
   /**
    * Deserialize <b>CreateSequenceResponse</b> using <b>provider</b> from the <b>soapMessage</b>
    * @param object to be deserialized
    * @param provider wsrm provider to be used for deserialization process
    * @param soapMessage soap message from which object will be deserialized
    */
   public static void deserialize(CreateSequenceResponse object, Provider provider, SOAPMessage soapMessage)
   throws ReliableMessagingException
   {
      try
      {
         SOAPBody soapBody = soapMessage.getSOAPPart().getEnvelope().getBody();
         Constants wsrmConstants = provider.getConstants();
         
         // read wsrm:CreateSequenceResponse
         QName createSequenceResponseQName = wsrmConstants.getCreateSequenceResponseQName();
         SOAPElement createSequenceResponseElement = getRequiredElement(soapBody, createSequenceResponseQName, "soap body");

         // read wsrm:identifier
         QName identifierQName = wsrmConstants.getIdentifierQName();
         SOAPElement identifierElement = getRequiredElement(createSequenceResponseElement, identifierQName, createSequenceResponseQName);
         String identifier = getRequiredTextContent(identifierElement, identifierQName);
         object.setIdentifier(identifier);
         
         // read wsrm:Expires
         QName expiresQName = wsrmConstants.getExpiresQName();
         SOAPElement expiresElement = getOptionalElement(createSequenceResponseElement, expiresQName, createSequenceResponseQName);
         if (expiresElement != null)
         {
            String duration = getRequiredTextContent(expiresElement, expiresQName);
            object.setExpires(duration);
         }

         // read wsrm:IncompleteSequenceBehavior
         QName behaviorQName = wsrmConstants.getIncompleteSequenceBehaviorQName();
         SOAPElement behaviorElement = getOptionalElement(createSequenceResponseElement, behaviorQName, createSequenceResponseQName);
         if (behaviorElement != null)
         {
            String behaviorString = getRequiredTextContent(behaviorElement, behaviorQName);
            object.setIncompleteSequenceBehavior(IncompleteSequenceBehavior.getValue(behaviorString));
         }
         
         // read wsrm:Accept
         QName acceptQName = wsrmConstants.getAcceptQName();
         SOAPElement acceptElement = getOptionalElement(createSequenceResponseElement, acceptQName, createSequenceResponseQName);
         if (acceptElement != null)
         {
            CreateSequenceResponse.Accept accept = object.newAccept();
            
            // read wsrm:AcksTo
            QName acksToQName = wsrmConstants.getAcksToQName();
            SOAPElement acksToElement = getRequiredElement(acceptElement, acksToQName, acceptQName);
            QName addressQName = ADDRESSING_CONSTANTS.getAddressQName();
            SOAPElement acksToAddressElement = getRequiredElement(acksToElement, addressQName, acksToQName);
            String acksToAddress = getRequiredTextContent(acksToAddressElement, addressQName);
            accept.setAcksTo(acksToAddress);

            object.setAccept(accept);
         }
      }
      catch (SOAPException se)
      {
         throw new ReliableMessagingException("Unable to deserialize RM message", se);
      }
   }

   /**
    * Serialize <b>CreateSequenceResponse</b> using <b>provider</b> to the <b>soapMessage</b>
    * @param object to be serialized
    * @param provider wsrm provider to be used for serialization process
    * @param soapMessage soap message to which object will be serialized
    */
   public static void serialize(CreateSequenceResponse object, Provider provider, SOAPMessage soapMessage)
   throws ReliableMessagingException
   {
      try 
      {
         SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
         Constants wsrmConstants = provider.getConstants();
         
         // Add xmlns:wsrm declaration
         soapEnvelope.addNamespaceDeclaration(wsrmConstants.getPrefix(), wsrmConstants.getNamespaceURI());

         // write wsrm:CreateSequenceResponse
         QName createSequenceResponseQName = wsrmConstants.getCreateSequenceResponseQName(); 
         SOAPElement createSequenceResponseElement = soapEnvelope.getBody().addChildElement(createSequenceResponseQName);

         // write wsrm:Identifier
         QName identifierQName = wsrmConstants.getIdentifierQName();
         createSequenceResponseElement.addChildElement(identifierQName).setValue(object.getIdentifier());
         
         if (object.getExpires() != null)
         {
            // write wsrm:Expires
            QName expiresQName = wsrmConstants.getExpiresQName();
            createSequenceResponseElement.addChildElement(expiresQName).setValue(object.getExpires());
         }
         
         if (object.getIncompleteSequenceBehavior() != null)
         {
            // write wsrm:IncompleteSequenceBehavior
            IncompleteSequenceBehavior behavior = object.getIncompleteSequenceBehavior();
            QName behaviorQName = wsrmConstants.getIncompleteSequenceBehaviorQName();
            SOAPElement behaviorElement = createSequenceResponseElement.addChildElement(behaviorQName);
            behaviorElement.setValue(behavior.toString());
         }
         
         if (object.getAccept() != null)
         {
            // write wsrm:Accept
            QName acceptQName = wsrmConstants.getAcceptQName();
            SOAPElement acceptElement = createSequenceResponseElement.addChildElement(acceptQName);

            // write wsrm:AcksTo
            QName acksToQName = wsrmConstants.getAcksToQName();
            QName addressQName = ADDRESSING_CONSTANTS.getAddressQName();
            acceptElement.addChildElement(acksToQName)
               .addChildElement(addressQName)
                  .setValue(object.getAccept().getAcksTo());
         }
      }
      catch (SOAPException se)
      {
         throw new ReliableMessagingException("Unable to serialize RM message", se);
      }
   }

}
