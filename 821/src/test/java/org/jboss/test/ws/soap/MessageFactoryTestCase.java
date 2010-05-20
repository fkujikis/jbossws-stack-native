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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.soap.*;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.util.xml.DOMUtils;
import org.jboss.ws.Constants;
import org.jboss.ws.jaxrpc.Style;
import org.jboss.ws.soap.*;
import org.w3c.dom.Element;

/**
 * Test the MessageFactory
 *
 * @author Thomas.Diesler@jboss.org
 * @since 21-Mar-2006
 */
public class MessageFactoryTestCase extends JBossWSTest
{
   public void testEnvelopeBuilder() throws Exception
   {
      String envStr = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
         " <env:Body>" +
         "  <businessList generic='2.0' operator='JBOSS' xmlns='urn:uddi-org:api_v2'>" + 
         "   <businessInfos>" + 
         "    <businessInfo businessKey='892ac280-c16b-11d5-85ad-801eef211111'>" + 
         "     <name xml:lang='en'>Demi Credit</name>" + 
         "     <description xml:lang='en'>A smaller demo app used for illustrating UDDI inquiry.</description>" + 
         "     <serviceInfos>" + 
         "      <serviceInfo businessKey='9a26b6e0-c15f-11d5-85a3-801eef208714' serviceKey='860eca90-c16d-11d5-85ad-801eef208714'>" + 
         "       <name xml:lang='en'>DCAmail</name>" + 
         "      </serviceInfo>" + 
         "     </serviceInfos>" + 
         "    </businessInfo>" + 
         "   </businessInfos>" + 
         "  </businessList>" + 
         " </env:Body>" +
         "</env:Envelope>";

      ByteArrayInputStream inputStream = new ByteArrayInputStream(envStr.getBytes());

      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage soapMsg = factory.createMessage(null, inputStream);
      SOAPEnvelope env = soapMsg.getSOAPPart().getEnvelope();

      assertEquals("env:Envelope", env.getNodeName());
      assertEquals(Constants.NS_SOAP11_ENV, env.getNamespaceURI());
      
      SOAPBodyElement soapBodyElement = (SOAPBodyElement)env.getBody().getChildElements().next();
      assertEquals("urn:uddi-org:api_v2", soapBodyElement.getNamespaceURI());
   }

   // http://jira.jboss.org/jira/browse/JBWS-745
   // SAAJ:SOAPBodyElement.addNamespaceDeclaration should allow empty prefix
   public void testAddNamespaceDeclaration() throws Exception
   {
      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage soapMsg = factory.createMessage();
      SOAPEnvelope env = soapMsg.getSOAPPart().getEnvelope();

      assertEquals("env:Envelope", env.getNodeName());
      assertEquals(Constants.NS_SOAP11_ENV, env.getNamespaceURI());
      
      SOAPBody soapBody = env.getBody();
      SOAPBodyElement soapBodyElement = (SOAPBodyElement)soapBody.addChildElement("businessList");
      soapBodyElement.addNamespaceDeclaration("", "urn:uddi-org:api_v2");
      
      String expEnvStr = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" + 
         " <env:Header/>" + 
         " <env:Body>" + 
         "  <businessList xmlns='urn:uddi-org:api_v2'/>" + 
         " </env:Body>" + 
         "</env:Envelope>";
      
      Element expEnv = DOMUtils.parse(expEnvStr);
      assertEquals(expEnv, env);
   }

   // http://jira.jboss.org/jira/browse/JBWS-1138
   public void testJBWS1138() throws Exception
   {
      String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                  +"xmlns:typ=\"http://www.jboss.org/support/phonebook/types\">\n" +
          "   <soapenv:Body>\n" +
          "      <typ:lookup>\n" +
          "         <firstName>Darran</firstName>\n" +
          "         <surname>Lofthouse</surname>\n" +
          "      </typ:lookup>\n" +
          "   </soapenv:Body>\n" +
          "</soapenv:Envelope> ";

      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage soapMsg = factory.createMessage();

      SAAJEnvelopeBuilder envelopeBuilder = SAAJEnvelopeBuilderFactory.newInstance().createSAAJEnvelopeBuilder();
      envelopeBuilder.setIgnoreParseException(false);
      envelopeBuilder.setStyle(Style.DOCUMENT);
      envelopeBuilder.setSOAPMessage(soapMsg);
      envelopeBuilder.build(new ByteArrayInputStream(xml.getBytes()));

      SOAPBody body = soapMsg.getSOAPBody();
      SOAPElement payload = (SOAPElement)body.getChildElements().next();
      assertTrue(payload instanceof SOAPContentElement);

      SOAPContentElement sce = (SOAPContentElement)payload;
      try
      {
         DOMUtils.parse(
             new ByteArrayInputStream(sce.getXMLFragment().getBytes())
         );
      }
      catch (IOException e)
      {
         // fails due to missing NS declaration
         fail(e.getMessage());
      }

   }

}
