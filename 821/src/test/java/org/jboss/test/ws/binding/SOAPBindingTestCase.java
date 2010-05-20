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
package org.jboss.test.ws.binding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.util.xml.DOMUtils;
import org.jboss.ws.Constants;
import org.jboss.ws.binding.BindingProvider;
import org.jboss.ws.binding.EndpointInvocation;
import org.jboss.ws.binding.UnboundHeader;
import org.jboss.ws.binding.soap.SOAP12BindingProvider;
import org.jboss.ws.jaxrpc.CallImpl;
import org.jboss.ws.metadata.OperationMetaData;
import org.jboss.ws.metadata.ParameterMetaData;
import org.jboss.ws.soap.MessageContextAssociation;
import org.jboss.ws.soap.MessageFactoryImpl;
import org.jboss.ws.soap.SOAPMessageContextImpl;

/**
 * Test the SOAPBindingProvider
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-Oct-2004
 */
public class SOAPBindingTestCase extends JBossWSTest
{
   String reqEnvelope =
      "<env:Envelope xmlns:env='http://www.w3.org/2003/05/soap-envelope'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:echoStringOne xmlns:ns1='http://org.jboss.ws/2004'>" +
      "   <String_1>Hello World!</String_1>" +
      "  </ns1:echoStringOne>" +
      " </env:Body>" +
      "</env:Envelope>";

   String reqEnvelopeWithBoundHeader =
      "<env:Envelope xmlns:env='http://www.w3.org/2003/05/soap-envelope'>" +
      " <env:Header>" +
      "  <ns2:String_2 xmlns:ns2='http://somens'>IN header message</ns2:String_2>" +
      " </env:Header>" +
      " <env:Body>" +
      "  <ns1:echoStringTwo xmlns:ns1='http://org.jboss.ws/2004'>" +
      "   <String_1>Hello World!</String_1>" +
      "  </ns1:echoStringTwo>" +
      " </env:Body>" +
      "</env:Envelope>";

   String reqEnvelopeWithUnboundHeader =
      "<env:Envelope xmlns:env='http://www.w3.org/2003/05/soap-envelope'>" +
      " <env:Header>" +
      "  <ns2:String_2 xmlns:ns2='http://somens'>IN header message</ns2:String_2>" +
      " </env:Header>" +
      " <env:Body>" +
      "  <ns1:echoStringOne xmlns:ns1='http://org.jboss.ws/2004'>" +
      "   <String_1>Hello World!</String_1>" +
      "  </ns1:echoStringOne>" +
      " </env:Body>" +
      "</env:Envelope>";

   String resEnvelope =
      "<env:Envelope xmlns:env='http://www.w3.org/2003/05/soap-envelope'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:echoStringOneResponse xmlns:ns1='http://org.jboss.ws/2004'>" +
      "   <result>Hello World!</result>" +
      "  </ns1:echoStringOneResponse>" +
      " </env:Body>" +
      "</env:Envelope>";

   String resEnvelopeWithBoundHeader =
      "<env:Envelope xmlns:env='http://www.w3.org/2003/05/soap-envelope'>" +
      " <env:Header>" +
      "  <ns2:OutHeader xmlns:ns2='http://somens'>OUT header message</ns2:OutHeader>" +
      " </env:Header>" +
      " <env:Body>" +
      "  <ns1:echoStringTwoResponse xmlns:ns1='http://org.jboss.ws/2004'>" +
      "   <result>Hello World!</result>" +
      "  </ns1:echoStringTwoResponse>" +
      " </env:Body>" +
      "</env:Envelope>";

   String resEnvelopeWithUnboundHeader =
      "<env:Envelope xmlns:env='http://www.w3.org/2003/05/soap-envelope'>" +
      " <env:Header>" +
      "  <ns2:OutHeader xmlns:ns2='http://somens'>OUT header message</ns2:OutHeader>" +
      " </env:Header>" +
      " <env:Body>" +
      "  <ns1:echoStringOneResponse xmlns:ns1='http://org.jboss.ws/2004'>" +
      "   <result>Hello World!</result>" +
      "  </ns1:echoStringOneResponse>" +
      " </env:Body>" +
      "</env:Envelope>";

   String resEnvelopeWithFault =
      "<env:Envelope xmlns:env='http://www.w3.org/2003/05/soap-envelope'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <env:Fault>" +
      "   <faultcode>env:Client</faultcode>" +
      "   <faultstring>Some fault message</faultstring>" +
      "  </env:Fault>" +
      " </env:Body>" +
      "</env:Envelope>";

   private OperationMetaData opMetaData;

   protected void setUp() throws Exception
   {
      super.setUp();

      // Setup the operation description
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));
      CallImpl call = (CallImpl)service.createCall();

      // Tests that involve a header value use SEI method: echoStringTwo
      String opName = (getName().endsWith("BoundHeader") ? "echoStringTwo" : "echoStringOne");
      call.setOperationName(new QName("http://org.jboss.ws/2004", opName));

      call.addParameter("String_1", Constants.TYPE_LITERAL_STRING, String.class, ParameterMode.IN);
      call.setReturnType(Constants.TYPE_LITERAL_STRING, String.class);

      opMetaData = call.getOperationMetaData();
      opMetaData.getEndpointMetaData().setServiceEndpointInterfaceName(SOAPBindingTestService.class.getName());

      // Associate a message context with the current thread
      SOAPMessageContextImpl messageContext = new SOAPMessageContextImpl();
      MessageContextAssociation.pushMessageContext(messageContext);
      messageContext.setOperationMetaData(opMetaData);
   }

   /** Test binding of the request message
    */
   public void testBindRequestMessage() throws Exception
   {
      BindingProvider bp = new SOAP12BindingProvider();

      EndpointInvocation epInv = new EndpointInvocation(opMetaData);
      epInv.initInputParams(new Object[]{"Hello World!"});
      
      SOAPMessage reqMessage = bp.bindRequestMessage(opMetaData, epInv, null);

      ByteArrayOutputStream outs = new ByteArrayOutputStream();
      reqMessage.writeTo(outs);

      String retString = new String(outs.toByteArray());
      assertEquals(DOMUtils.parse(reqEnvelope), DOMUtils.parse(retString));
   }

   /** Test binding of the request message with bound header
    */
   public void testBindRequestMessageWithBoundHeader() throws Exception
   {
      BindingProvider bp = new SOAP12BindingProvider();

      // Add bound header
      QName xmlName = new QName("http://somens", "String_2");
      ParameterMetaData paramMetaData = new ParameterMetaData(opMetaData, xmlName, Constants.TYPE_LITERAL_STRING, "java.lang.String");
      opMetaData.addParameter(paramMetaData);
      paramMetaData.setInHeader(true);

      EndpointInvocation epInv = new EndpointInvocation(opMetaData);
      epInv.initInputParams(new Object[]{"Hello World!", "IN header message"});
      
      SOAPMessage reqMessage = bp.bindRequestMessage(opMetaData, epInv, null);

      ByteArrayOutputStream outs = new ByteArrayOutputStream();
      reqMessage.writeTo(outs);

      String retString = new String(outs.toByteArray());
      assertEquals(DOMUtils.parse(reqEnvelopeWithBoundHeader), DOMUtils.parse(retString));
   }

   /** Test binding of the request message with header
    */
   public void testBindRequestMessageWithUnboundHeader() throws Exception
   {
      BindingProvider bp = new SOAP12BindingProvider();

      // Add unbound header
      QName xmlName = new QName("http://somens", "String_2");
      UnboundHeader header = new UnboundHeader(xmlName, Constants.TYPE_LITERAL_STRING, String.class, ParameterMode.IN);
      header.setHeaderValue("IN header message");

      Map headers = new HashMap();
      headers.put(xmlName, header);

      EndpointInvocation epInv = new EndpointInvocation(opMetaData);
      epInv.initInputParams(new Object[]{"Hello World!"});
      
      SOAPMessage reqMessage = bp.bindRequestMessage(opMetaData, epInv, headers);

      ByteArrayOutputStream outs = new ByteArrayOutputStream();
      reqMessage.writeTo(outs);

      String retString = new String(outs.toByteArray());
      assertEquals(DOMUtils.parse(reqEnvelopeWithUnboundHeader), DOMUtils.parse(retString));
   }

   /** Test unbinding of the request message
    */
   public void testUnbindRequestMessage() throws Exception
   {
      BindingProvider bp = new SOAP12BindingProvider();

      ByteArrayInputStream inputStream = new ByteArrayInputStream(reqEnvelope.getBytes());

      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage reqMessage = factory.createMessage(null, inputStream);

      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
      msgContext.setMessage(reqMessage);

      EndpointInvocation epInv = bp.unbindRequestMessage(opMetaData, reqMessage);
      assertNotNull(epInv);

      Object[] args = epInv.getRequestPayload();
      assertEquals(1, args.length);
      assertEquals("Hello World!", args[0]);
   }

   /** Test unbinding of the request message
    */
   public void testUnbindRequestMessageWithBoundHeader() throws Exception
   {
      // Add bound header
      QName xmlName = new QName("http://somens", "String_2");
      ParameterMetaData paramMetaData = new ParameterMetaData(opMetaData, xmlName, Constants.TYPE_LITERAL_STRING, "java.lang.String");
      opMetaData.addParameter(paramMetaData);
      paramMetaData.setInHeader(true);

      ByteArrayInputStream inputStream = new ByteArrayInputStream(reqEnvelopeWithBoundHeader.getBytes());

      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage reqMessage = factory.createMessage(null, inputStream);

      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
      msgContext.setMessage(reqMessage);

      BindingProvider bp = new SOAP12BindingProvider();
      EndpointInvocation epInv = bp.unbindRequestMessage(opMetaData, reqMessage);
      assertNotNull(epInv);

      Object[] args = epInv.getRequestPayload();
      assertEquals(2, args.length);
      assertEquals("Hello World!", args[0]);
      assertEquals("IN header message", args[1]);
   }

   /** Test unbinding of the request message
    */
   public void testUnbindRequestMessageWithUnboundHeader() throws Exception
   {
      ByteArrayInputStream inputStream = new ByteArrayInputStream(reqEnvelopeWithUnboundHeader.getBytes());

      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage reqMessage = factory.createMessage(null, inputStream);

      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
      msgContext.setMessage(reqMessage);

      BindingProvider bp = new SOAP12BindingProvider();
      EndpointInvocation epInv = bp.unbindRequestMessage(opMetaData, reqMessage);
      assertNotNull(epInv);

      Object[] args = epInv.getRequestPayload();
      assertEquals(1, args.length);
      assertEquals("Hello World!", args[0]);
   }

   /** Test binding of the response message
    */
   public void testBindResponseMessage() throws Exception
   {
      BindingProvider bp = new SOAP12BindingProvider();

      EndpointInvocation epInv = new EndpointInvocation(opMetaData);
      epInv.setReturnValue("Hello World!");

      SOAPMessage resMessage = bp.bindResponseMessage(opMetaData, epInv);

      ByteArrayOutputStream outs = new ByteArrayOutputStream();
      resMessage.writeTo(outs);

      String retString = new String(outs.toByteArray());
      assertEquals(DOMUtils.parse(resEnvelope), DOMUtils.parse(retString));
   }

   /** Test unbinding of the response message
    */
   public void testUnbindResponseMessage() throws Exception
   {
      BindingProvider bp = new SOAP12BindingProvider();

      ByteArrayInputStream inputStream = new ByteArrayInputStream(resEnvelope.getBytes());

      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage resMessage = factory.createMessage(null, inputStream);

      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
      msgContext.setMessage(resMessage);

      EndpointInvocation epInv = new EndpointInvocation(opMetaData);
      bp.unbindResponseMessage(opMetaData, resMessage, epInv, null);
      assertEquals("Hello World!", epInv.getReturnValue());
   }

   /** Test unbinding of the response message with bound header
    */
   public void testUnbindResponseMessageWithBoundHeader() throws Exception
   {
      BindingProvider bp = new SOAP12BindingProvider();

      ByteArrayInputStream inputStream = new ByteArrayInputStream(resEnvelopeWithBoundHeader.getBytes());

      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage resMessage = factory.createMessage(null, inputStream);

      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
      msgContext.setMessage(resMessage);

      // Add bound header
      QName xmlName = new QName("http://somens", "OutHeader");
      ParameterMetaData paramMetaData = new ParameterMetaData(opMetaData, xmlName, Constants.TYPE_LITERAL_STRING, "java.lang.String");
      paramMetaData.setMode(ParameterMode.OUT);
      paramMetaData.setInHeader(true);
      opMetaData.addParameter(paramMetaData);

      EndpointInvocation epInv = new EndpointInvocation(opMetaData);
      bp.unbindResponseMessage(opMetaData, resMessage, epInv, null);
      assertEquals("Hello World!", epInv.getReturnValue());

      Object headerValue = epInv.getResponseParamValue(xmlName);
      assertEquals("OUT header message", headerValue);
   }

   /** Test unbinding of the response message with unbound header
    */
   public void testUnbindResponseMessageWithUnboundHeader() throws Exception
   {
      BindingProvider bp = new SOAP12BindingProvider();

      ByteArrayInputStream inputStream = new ByteArrayInputStream(resEnvelopeWithUnboundHeader.getBytes());

      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage resMessage = factory.createMessage(null, inputStream);

      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
      msgContext.setMessage(resMessage);

      QName xmlName = new QName("http://somens", "OutHeader");
      UnboundHeader header = new UnboundHeader(xmlName, Constants.TYPE_LITERAL_STRING, String.class, ParameterMode.OUT);

      Map headers = new HashMap();
      headers.put(xmlName, header);

      EndpointInvocation epInv = new EndpointInvocation(opMetaData);
      bp.unbindResponseMessage(opMetaData, resMessage, epInv, headers);
      assertEquals("Hello World!", epInv.getReturnValue());

      assertEquals("OUT header message", header.getHeaderValue());
   }

   /** Test unbinding of the response message with unbound header
    */
   public void testUnbindFaultResponse() throws Exception
   {
      BindingProvider bp = new SOAP12BindingProvider();

      ByteArrayInputStream inputStream = new ByteArrayInputStream(resEnvelopeWithFault.getBytes());

      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage resMessage = factory.createMessage(null, inputStream);

      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
      msgContext.setMessage(resMessage);

      try
      {
         EndpointInvocation epInv = new EndpointInvocation(opMetaData);
         bp.unbindResponseMessage(opMetaData, resMessage, epInv, null);
         fail("SOAPFaultException expected");
      }
      catch (SOAPFaultException faultEx)
      {
         assertEquals("Client", faultEx.getFaultCode().getLocalPart());
         assertEquals("Some fault message", faultEx.getFaultString());
      }
   }
}
