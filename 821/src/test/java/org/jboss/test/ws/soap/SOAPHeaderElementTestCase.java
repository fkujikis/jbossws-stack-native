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
package org.jboss.test.ws.soap;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.ws.soap.MessageFactoryImpl;
import org.jboss.ws.soap.NameImpl;

import javax.xml.soap.*;
import java.io.ByteArrayInputStream;

/**
 * Test the SOAPHeaderElement
 *
 * @author Thomas.Diesler@jboss.org
 * @since 02-Feb-2005
 */
public class SOAPHeaderElementTestCase extends JBossWSTest
{

   /** Test access to the actor attribute
    */
   public void testAttributeActor() throws Exception
   {
      String envStr =
              "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
              " <env:Header>" +
              "  <ns2:Bar xmlns:ns2='http://org.jboss.ws/header2' env:actor='BradPitt'>SomeOtherValue</ns2:Bar>" +
              " </env:Header>" +
              " <env:Body/>" +
              "</env:Envelope>";

      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage soapMessage = factory.createMessage(null, new ByteArrayInputStream(envStr.getBytes()));
      SOAPEnvelope soapEnv = soapMessage.getSOAPPart().getEnvelope();
      SOAPHeader soapHeader = soapEnv.getHeader();
      SOAPHeaderElement shElement = (SOAPHeaderElement)soapHeader.getChildElements().next();

      Name name = new NameImpl("Bar", "ns2", "http://org.jboss.ws/header2");
      assertEquals(name, shElement.getElementName());

      assertEquals("BradPitt", shElement.getActor());
      assertEquals("SomeOtherValue", shElement.getValue());
   }

   /** Test access to the mustUnderstand attribute
    */
   public void testGetMustUnderstand() throws Exception
   {
      String envStr =
              "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
              " <env:Header>" +
              "  <ns2:Bar xmlns:ns2='http://org.jboss.ws/header2' env:mustUnderstand='1'>SomeOtherValue</ns2:Bar>" +
              " </env:Header>" +
              " <env:Body/>" +
              "</env:Envelope>";

      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage soapMessage = factory.createMessage(null, new ByteArrayInputStream(envStr.getBytes()));
      SOAPEnvelope soapEnv = soapMessage.getSOAPPart().getEnvelope();
      SOAPHeader soapHeader = soapEnv.getHeader();
      SOAPHeaderElement shElement = (SOAPHeaderElement)soapHeader.getChildElements().next();

      Name name = new NameImpl("Bar", "ns2", "http://org.jboss.ws/header2");
      assertEquals(name, shElement.getElementName());

      assertTrue(shElement.getMustUnderstand());
      assertEquals("SomeOtherValue", shElement.getValue());
   }
   
   /** Test access to the mustUnderstand attribute
    */
   public void testSetMustUnderstand() throws Exception
   {
      String envStr =
              "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
              " <env:Header>" +
              "  <ns2:Bar xmlns:ns2='http://org.jboss.ws/header2' env:mustUnderstand='1'>SomeOtherValue</ns2:Bar>" +
              " </env:Header>" +
              " <env:Body/>" +
              "</env:Envelope>";

      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage expSoapMessage = factory.createMessage(null, new ByteArrayInputStream(envStr.getBytes()));
      SOAPEnvelope expSoapEnv = expSoapMessage.getSOAPPart().getEnvelope();

      SOAPMessage soapMessage = factory.createMessage();
      SOAPEnvelope soapEnv = soapMessage.getSOAPPart().getEnvelope();
      SOAPHeader soapHeader = soapEnv.getHeader();

      Name name = new NameImpl("Bar", "ns2", "http://org.jboss.ws/header2");
      SOAPHeaderElement soapHeaderElement = soapHeader.addHeaderElement(name);
      soapHeaderElement.setMustUnderstand(true);
      soapHeaderElement.addTextNode("SomeOtherValue");
      
      assertEquals(expSoapEnv, soapEnv);
   }
}
