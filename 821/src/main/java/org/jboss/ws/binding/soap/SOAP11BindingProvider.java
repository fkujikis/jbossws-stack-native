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
package org.jboss.ws.binding.soap;

// $Id$

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.binding.BindingException;
import org.jboss.ws.binding.EndpointInvocation;
import org.jboss.ws.binding.UnboundHeader;
import org.jboss.ws.metadata.OperationMetaData;
import org.jboss.ws.soap.MessageContextAssociation;
import org.jboss.ws.soap.MessageFactoryImpl;
import org.jboss.ws.soap.SOAPMessageContextImpl;

/** A BindingProvider that implements the SOAP-1.1 specifics.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 09-Nov-2004
 */
public class SOAP11BindingProvider extends SOAPBindingProvider
{
   // provide logging
   private static final Logger log = Logger.getLogger(SOAP11BindingProvider.class);

   /** Create the SOAP-1.1 message */
   protected SOAPMessage createMessage(OperationMetaData opMetaData) throws SOAPException
   {
      MessageFactoryImpl factory = new MessageFactoryImpl();
      factory.setEnvelopeURI(Constants.NS_SOAP11_ENV);
      return factory.createMessage();
   }

   /** On the client side, generate the payload from IN parameters. */
   public SOAPMessage bindRequestMessage(OperationMetaData opMetaData, EndpointInvocation epInv, Map<QName, UnboundHeader> unboundHeaders) throws BindingException
   {
      SOAPMessage reqMessage = super.bindRequestMessage(opMetaData, epInv, unboundHeaders);

      // Set the SOAPAction 
      MimeHeaders mimeHeaders = reqMessage.getMimeHeaders();
      String soapAction = opMetaData.getSOAPAction();

      // R2744 A HTTP request MESSAGE MUST contain a SOAPAction HTTP header field
      // with a quoted value equal to the value of the soapAction attribute of
      // soapbind:operation, if present in the corresponding WSDL description.

      // R2745 A HTTP request MESSAGE MUST contain a SOAPAction HTTP header field
      // with a quoted empty string value, if in the corresponding WSDL description,
      // the soapAction attribute of soapbind:operation is either not present, or
      // present with an empty string as its value.

      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
      if (msgContext.getProperty(Call.SOAPACTION_USE_PROPERTY) != null)
         log.info("Ignore Call.SOAPACTION_USE_PROPERTY because of BP-1.0 R2745, R2745");

      String soapActionProperty = (String)msgContext.getProperty(Call.SOAPACTION_URI_PROPERTY);
      if (soapActionProperty != null)
         soapAction = soapActionProperty;

      mimeHeaders.addHeader("SOAPAction", soapAction != null ? soapAction : "");

      return reqMessage;
   }
}
