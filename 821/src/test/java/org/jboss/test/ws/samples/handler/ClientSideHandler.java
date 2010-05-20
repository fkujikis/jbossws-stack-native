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
package org.jboss.test.ws.samples.handler;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import org.jboss.logging.Logger;

public class ClientSideHandler extends GenericHandler
{
   // Provide logging
   private static Logger log = Logger.getLogger(ClientSideHandler.class);

   protected QName[] headers = new QName[]{
      new QName("http://somens", "InHeader"),
      new QName("http://somens", "OutHeader"),
      new QName("http://somens", "InOutHeader")
   };

   public QName[] getHeaders()
   {
      return headers;
   }

   public boolean handleRequest(MessageContext msgContext)
   {
      log.info("handleRequest");

      try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
         SOAPHeader soapHeader = soapMessage.getSOAPHeader();

         SOAPBody soapBody = soapMessage.getSOAPBody();
         SOAPBodyElement soapBodyElement = (SOAPBodyElement)soapBody.getChildElements().next();
         String rpcName = soapBodyElement.getElementName().getLocalName();

         // testInHeader
         if (rpcName.equals("testInHeader"))
         {
            SOAPHeaderElement she = (SOAPHeaderElement)soapHeader.examineAllHeaderElements().next();
            String headerValue = she.getValue();
            if ("IN header message".equals(headerValue) == false)
               throw new JAXRPCException("Unexpected header value: " + headerValue);
         }

         // testOutHeader
         else if (rpcName.equals("testOutHeader"))
         {
            if (soapHeader.examineAllHeaderElements().hasNext())
               throw new JAXRPCException("Unexpected header element");
         }

         // testInOutHeader
         else if (rpcName.equals("testInOutHeader"))
         {
            SOAPHeaderElement she = (SOAPHeaderElement)soapHeader.examineAllHeaderElements().next();
            String headerValue = she.getValue();
            if ("INOUT header message".equals(headerValue) == false)
               throw new JAXRPCException("Unexpected header value: " + headerValue);
         }
         else
         {
            throw new JAXRPCException("Unexpected RPC name: " + rpcName);
         }
      }
      catch (SOAPException e)
      {
         throw  new JAXRPCException(e);
      }

      return true;
   }
}
