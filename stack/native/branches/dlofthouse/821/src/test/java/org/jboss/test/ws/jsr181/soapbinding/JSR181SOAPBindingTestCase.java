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
package org.jboss.test.ws.jsr181.soapbinding;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;

import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.jaxrpc.CallImpl;
import org.jboss.ws.jaxrpc.ServiceFactoryImpl;
import org.jboss.ws.jaxrpc.Style;
import org.jboss.ws.jaxrpc.Use;
import org.jboss.ws.metadata.OperationMetaData;
import org.jboss.ws.soap.NameImpl;

/**
 * Test the JSR-181 annotation: javax.jws.SOAPBinding
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @since 17-Oct-2005
 */
public class JSR181SOAPBindingTestCase extends JBossWSTest
{
   private String targetNS = "http://soapbinding.jsr181.ws.test.jboss.org/jaws";

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JSR181SOAPBindingTestCase.class, "jbossws-jsr181-soapbinding.war");
   }

   public void testExampleService() throws Exception
   {
      QName serviceName = new QName(targetNS, "ExampleServiceService");
      QName portName = new QName(targetNS, "ExampleServicePort");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jbossws-jsr181-soapbinding/ExampleService?wsdl");

      ServiceFactoryImpl factory = new ServiceFactoryImpl();
      Service service = factory.createService(wsdlURL, serviceName);
      CallImpl call = (CallImpl)service.createCall(portName, "concat");

      OperationMetaData opMetaData = call.getOperationMetaData();
      assertEquals(Style.RPC, opMetaData.getStyle());
      assertEquals(Use.LITERAL, opMetaData.getUse());
      assertEquals(ParameterStyle.WRAPPED, opMetaData.getParameterStyle());

      Object retObj = call.invoke(new Object[]{"first", "second", "third"});
      assertEquals("first|second|third", retObj);
   }

   public void testDocBareService() throws Exception
   {
      QName serviceName = new QName(targetNS, "DocBareServiceService");
      QName portName = new QName(targetNS, "DocBareServicePort");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jbossws-jsr181-soapbinding/DocBareService?wsdl");

      File mappingFile = new File("resources/jsr181/soapbinding/bare-mapping.xml");
      assertTrue(mappingFile.exists());

      ServiceFactoryImpl factory = new ServiceFactoryImpl();
      Service service = factory.createService(wsdlURL, serviceName, mappingFile.toURL());
      CallImpl call = (CallImpl)service.createCall(portName, "SubmitPO");

      OperationMetaData opMetaData = call.getOperationMetaData();
      assertEquals(Style.DOCUMENT, opMetaData.getStyle());
      assertEquals(Use.LITERAL, opMetaData.getUse());
      assertEquals(ParameterStyle.BARE, opMetaData.getParameterStyle());

      SubmitBareRequest poReq = new SubmitBareRequest("Ferarri");
      SubmitBareResponse poRes = (SubmitBareResponse)call.invoke(new Object[]{poReq});
      assertEquals("Ferarri", poRes.getProduct());
   }

   public void testDocBareServiceMessageAccess() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();

      String reqEnv =
      "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:SubmitPO xmlns:ns1='" + targetNS + "'>" +
      "   <ns1:product>Ferrari</ns1:product>" +
      "  </ns1:SubmitPO>" +
      " </env:Body>" +
      "</env:Envelope>";
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL epURL = new URL("http://" + getServerHost() + ":8080/jbossws-jsr181-soapbinding/DocBareService");
      SOAPMessage resMsg = con.call(reqMsg, epURL);

      NameImpl name = new NameImpl(new QName(targetNS, "SubmitPOResponse"));
      SOAPElement soapElement = (SOAPElement)resMsg.getSOAPBody().getChildElements(name).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new NameImpl(new QName(targetNS, "product"))).next();
      assertEquals("Ferrari", soapElement.getValue());
   }

   public void testNamespacedDocBareServiceMessageAccess() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();

      String requestNamespace = "http://namespace/request";
      String resultNamespace = "http://namespace/result";

      String reqEnv =
      "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:SubmitNamespacedPO xmlns:ns1='" + requestNamespace+ "'>" +
      "   <ns2:product xmlns:ns2='" + targetNS + "'>Ferrari</ns2:product>" +
      "  </ns1:SubmitNamespacedPO>" +
      " </env:Body>" +
      "</env:Envelope>";
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL epURL = new URL("http://" + getServerHost() + ":8080/jbossws-jsr181-soapbinding/DocBareService");
      SOAPMessage resMsg = con.call(reqMsg, epURL);

      NameImpl name = new NameImpl(new QName(resultNamespace, "SubmitBareResponse"));
      SOAPElement soapElement = (SOAPElement)resMsg.getSOAPBody().getChildElements(name).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new NameImpl(new QName(targetNS, "product"))).next();
      assertEquals("Ferrari", soapElement.getValue());
   }

   public void testDocWrappedService() throws Exception
   {
      QName serviceName = new QName(targetNS, "DocWrappedServiceService");
      QName portName = new QName(targetNS, "DocWrappedServicePort");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jbossws-jsr181-soapbinding/DocWrappedService?wsdl");

      File mappingFile = new File("resources/jsr181/soapbinding/wrapped-mapping.xml");
      assertTrue(mappingFile.exists());

      ServiceFactoryImpl factory = new ServiceFactoryImpl();
      Service service = factory.createService(wsdlURL, serviceName, mappingFile.toURL());
      CallImpl call = (CallImpl)service.createCall(portName, "SubmitPO");

      OperationMetaData opMetaData = call.getOperationMetaData();
      assertEquals(Style.DOCUMENT, opMetaData.getStyle());
      assertEquals(Use.LITERAL, opMetaData.getUse());
      assertEquals(ParameterStyle.WRAPPED, opMetaData.getParameterStyle());

      PurchaseOrder poReq = new PurchaseOrder("Ferarri");
      PurchaseOrderAck poRes = (PurchaseOrderAck)call.invoke(new Object[]{poReq});
      assertEquals("Ferarri", poRes.getProduct());
   }

   public void testDocWrappedServiceMessageAccess() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();

      String reqEnv =
      "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:SubmitPO xmlns:ns1='" + targetNS + "'>" +
      "   <ns1:PurchaseOrder>" +
      "     <ns1:product>Ferrari</ns1:product>" +
      "   </ns1:PurchaseOrder>" +
      "  </ns1:SubmitPO>" +
      " </env:Body>" +
      "</env:Envelope>";
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL epURL = new URL("http://" + getServerHost() + ":8080/jbossws-jsr181-soapbinding/DocWrappedService");
      SOAPMessage resMsg = con.call(reqMsg, epURL);

      NameImpl name = new NameImpl(new QName(targetNS, "SubmitPOResponse"));
      SOAPElement soapElement = (SOAPElement)resMsg.getSOAPBody().getChildElements(name).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new NameImpl(new QName(targetNS, "PurchaseOrderAck"))).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new NameImpl(new QName(targetNS, "product"))).next();
      assertEquals("Ferrari", soapElement.getValue());
   }

   public void testNamespacedDocWrappedServiceMessageAccess() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();

      String purchaseNamespace = "http://namespace/purchase";
      String resultNamespace = "http://namespace/result";
      String stringNamespace = "http://namespace/string";

      String reqEnv =
      "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:SubmitNamespacedPO xmlns:ns1='" + targetNS + "'>" +
      "   <ns2:NamespacedPurchaseOrder xmlns:ns2='" + purchaseNamespace + "'>" +
      "     <ns1:product>Ferrari</ns1:product>" +
      "   </ns2:NamespacedPurchaseOrder>" +
      "   <ns3:NamespacedString xmlns:ns3='" + stringNamespace + "'>Ferrari</ns3:NamespacedString>" +
      "  </ns1:SubmitNamespacedPO>" +
      " </env:Body>" +
      "</env:Envelope>";
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL epURL = new URL("http://" + getServerHost() + ":8080/jbossws-jsr181-soapbinding/DocWrappedService");
      SOAPMessage resMsg = con.call(reqMsg, epURL);

      NameImpl name = new NameImpl(new QName(targetNS, "SubmitNamespacedPOResponse"));
      SOAPElement soapElement = (SOAPElement)resMsg.getSOAPBody().getChildElements(name).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new NameImpl(new QName(resultNamespace, "NamespacedPurchaseOrderAck"))).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new NameImpl(new QName(targetNS, "product"))).next();
      assertEquals("Ferrari", soapElement.getValue());
   }
}
