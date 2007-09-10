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
import org.jboss.ws.extensions.wsrm.spi.protocol.TerminateSequenceResponse;

/**
 * <b>TerminateSequenceResponse</b> object de/serializer
 * @author richard.opalka@jboss.com
 */
final class TerminateSequenceResponseSerializer
{

   private TerminateSequenceResponseSerializer()
   {
      // no instances
   }
   
   /**
    * Deserialize <b>TerminateSequenceResponse</b> using <b>provider</b> from the <b>soapMessage</b>
    * @param object to be deserialized
    * @param provider wsrm provider to be used for deserialization process
    * @param soapMessage soap message from which object will be deserialized
    */
   public static void deserialize(TerminateSequenceResponse object, Provider provider, SOAPMessage soapMessage)
   throws ReliableMessagingException
   {
      try
      {
         SOAPBody soapBody = soapMessage.getSOAPPart().getEnvelope().getBody();
         Constants wsrmConstants = provider.getConstants();
         
         // read wsrm:TerminateSequenceResponse
         QName terminateSequenceResponseQName = wsrmConstants.getTerminateSequenceResponseQName();
         SOAPElement terminateSequenceResponseElement = getRequiredElement(soapBody, terminateSequenceResponseQName, "soap body");

         // read wsrm:Identifier
         QName identifierQName = wsrmConstants.getIdentifierQName();
         SOAPElement identifierElement = getRequiredElement(terminateSequenceResponseElement, identifierQName, terminateSequenceResponseQName);
         String identifier = getRequiredTextContent(identifierElement, identifierQName);
         object.setIdentifier(identifier);
      }
      catch (SOAPException se)
      {
         throw new ReliableMessagingException("Unable to deserialize RM message", se);
      }
   }

   /**
    * Serialize <b>TerminateSequenceResponse</b> using <b>provider</b> to the <b>soapMessage</b>
    * @param object to be serialized
    * @param provider wsrm provider to be used for serialization process
    * @param soapMessage soap message to which object will be serialized
    */
   public static void serialize(TerminateSequenceResponse object, Provider provider, SOAPMessage soapMessage)
   throws ReliableMessagingException
   {
      try
      {
         SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
         Constants wsrmConstants = provider.getConstants();
         
         // Add xmlns:wsrm declaration
         soapEnvelope.addNamespaceDeclaration(wsrmConstants.getPrefix(), wsrmConstants.getNamespaceURI());

         // write wsrm:TerminateSequenceResponse
         QName terminateSequenceResponseQName = wsrmConstants.getTerminateSequenceResponseQName(); 
         SOAPElement terminateSequenceResponseElement = soapEnvelope.getBody().addChildElement(terminateSequenceResponseQName);

         // write wsrm:Identifier
         QName identifierQName = wsrmConstants.getIdentifierQName();
         terminateSequenceResponseElement.addChildElement(identifierQName).setValue(object.getIdentifier());
      }
      catch (SOAPException se)
      {
         throw new ReliableMessagingException("Unable to serialize RM message", se);
      }
   }

}
