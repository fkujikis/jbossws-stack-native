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
package org.jboss.test.ws.wsdl11;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.ws.Constants;
import org.jboss.ws.eventing.EventingConstants;
import org.jboss.ws.metadata.wsdl.*;

import javax.xml.namespace.QName;
import java.io.File;

/**
 * Test the unmarshalling of wsdl-1.1 into the unified wsdl structure
 *
 * @author Thomas.Diesler@jboss.org
 * @since 02-Jun-2005
 */
public class WSDL11TestCase extends JBossWSTest
{
   private static final String TARGET_NAMESPACE = "http://org.jboss.ws/jaxrpc/types";

   public void testDocLitSimple() throws Exception
   {
      File wsdlFile = new File("resources/wsdl11/DocLitSimple.wsdl");
      assertTrue(wsdlFile.exists());

      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlFile.toURL());
      WSDLInterface wsdlInterface = wsdlDefinitions.getInterface(new NCName("JaxRpcTestService"));

      // check if the schema has been extracted
      WSDLTypes wsdlTypes = wsdlDefinitions.getWsdlTypes();
      assertNotNull(wsdlTypes.getSchemaModel());

      // check the echoString operation
      WSDLInterfaceOperation wsdlOperation = wsdlInterface.getOperation(new NCName("echoString"));
      assertEquals("document", wsdlOperation.getStyle());

      WSDLInterfaceOperationInput wsdlInput = wsdlOperation.getInput(new QName(TARGET_NAMESPACE, "echoString"));
      assertEquals(new QName(TARGET_NAMESPACE, "echoString"), wsdlInput.getXMLType());
      WSDLInterfaceOperationOutput wsdlOutput = wsdlOperation.getOutput(new QName(TARGET_NAMESPACE, "echoStringResponse"));
      assertEquals(new QName(TARGET_NAMESPACE, "echoStringResponse"), wsdlOutput.getXMLType());

      // check the echoSimpleUserType operation
      wsdlOperation = wsdlInterface.getOperation(new NCName("echoSimpleUserType"));
      assertEquals("document", wsdlOperation.getStyle());

      wsdlInput = wsdlOperation.getInput(new QName(TARGET_NAMESPACE, "echoSimpleUserType"));
      assertEquals(new QName(TARGET_NAMESPACE, "echoSimpleUserType"), wsdlInput.getXMLType());
      wsdlOutput = wsdlOperation.getOutput(new QName(TARGET_NAMESPACE, "echoSimpleUserTypeResponse"));
      assertEquals(new QName(TARGET_NAMESPACE, "echoSimpleUserTypeResponse"), wsdlOutput.getXMLType());

      QName xmlName = new QName(TARGET_NAMESPACE, "echoString");
      QName xmlType = new QName(TARGET_NAMESPACE, "echoString");
      assertEquals(xmlType, wsdlTypes.getXMLType(xmlName));
      xmlName = new QName(TARGET_NAMESPACE, "echoStringResponse");
      xmlType = new QName(TARGET_NAMESPACE, "echoStringResponse");
      assertEquals(xmlType, wsdlTypes.getXMLType(xmlName));

      xmlName = new QName(TARGET_NAMESPACE, "echoSimpleUserType");
      xmlType = new QName(TARGET_NAMESPACE, "echoSimpleUserType");
      assertEquals(xmlType, wsdlTypes.getXMLType(xmlName));
      xmlName = new QName(TARGET_NAMESPACE, "echoSimpleUserTypeResponse");
      xmlType = new QName(TARGET_NAMESPACE, "echoSimpleUserTypeResponse");
      assertEquals(xmlType, wsdlTypes.getXMLType(xmlName));
   }

   public void testRpcLitSimple() throws Exception
   {
      File wsdlFile = new File("resources/wsdl11/RpcLitSimple.wsdl");
      assertTrue(wsdlFile.exists());

      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlFile.toURL());
      WSDLInterface wsdlInterface = wsdlDefinitions.getInterface(new NCName("JaxRpcTestService"));

      // check if the schema has been extracted
      WSDLTypes wsdlTypes = wsdlDefinitions.getWsdlTypes();
      assertNotNull(wsdlTypes.getSchemaModel());

      // check the echoString operation
      WSDLInterfaceOperation wsdlOperation = wsdlInterface.getOperation(new NCName("echoString"));
      assertEquals("rpc", wsdlOperation.getStyle());

      WSDLInterfaceOperationInput wsdlInput = wsdlOperation.getInput(new QName("String_1"));
      assertEquals(Constants.TYPE_LITERAL_STRING, wsdlInput.getXMLType());
      wsdlInput = wsdlOperation.getInput(new QName("String_2"));
      assertEquals(Constants.TYPE_LITERAL_STRING, wsdlInput.getXMLType());
      WSDLInterfaceOperationOutput wsdlOutput = wsdlOperation.getOutput(new QName("result"));
      assertEquals(Constants.TYPE_LITERAL_STRING, wsdlOutput.getXMLType());

      // check the echoSimpleUserType operation
      wsdlOperation = wsdlInterface.getOperation(new NCName("echoSimpleUserType"));
      assertEquals("rpc", wsdlOperation.getStyle());

      wsdlInput = wsdlOperation.getInput(new QName("String_1"));
      assertEquals(Constants.TYPE_LITERAL_STRING, wsdlInput.getXMLType());
      wsdlInput = wsdlOperation.getInput(new QName("SimpleUserType_2"));
      assertEquals(new QName(TARGET_NAMESPACE, "SimpleUserType"), wsdlInput.getXMLType());
      wsdlOutput = wsdlOperation.getOutput(new QName("result"));
      assertEquals(new QName(TARGET_NAMESPACE, "SimpleUserType"), wsdlOutput.getXMLType());
   }

   public void testRpcLitImport() throws Exception
   {
      File wsdlFile = new File("resources/wsdl11/RpcLitImport.wsdl");
      assertTrue(wsdlFile.exists());

      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlFile.toURL());
      WSDLInterface wsdlInterface = wsdlDefinitions.getInterface(new NCName("JaxRpcTestService"));

      // check if the schema has been extracted
      WSDLTypes wsdlTypes = wsdlDefinitions.getWsdlTypes();
      assertNotNull(wsdlTypes.getSchemaModel());

      // check the echoString operation
      WSDLInterfaceOperation wsdlOperation = wsdlInterface.getOperation(new NCName("echoString"));
      assertEquals("rpc", wsdlOperation.getStyle());

      WSDLInterfaceOperationInput wsdlInput = wsdlOperation.getInput(new QName("String_1"));
      assertEquals(Constants.TYPE_LITERAL_STRING, wsdlInput.getXMLType());
      wsdlInput = wsdlOperation.getInput(new QName("String_2"));
      assertEquals(Constants.TYPE_LITERAL_STRING, wsdlInput.getXMLType());
      WSDLInterfaceOperationOutput wsdlOutput = wsdlOperation.getOutput(new QName("result"));
      assertEquals(Constants.TYPE_LITERAL_STRING, wsdlOutput.getXMLType());

      // check the echoSimpleUserType operation
      wsdlOperation = wsdlInterface.getOperation(new NCName("echoSimpleUserType"));
      assertEquals("rpc", wsdlOperation.getStyle());

      wsdlInput = wsdlOperation.getInput(new QName("String_1"));
      assertEquals(Constants.TYPE_LITERAL_STRING, wsdlInput.getXMLType());
      wsdlInput = wsdlOperation.getInput(new QName("SimpleUserType_2"));
      assertEquals(new QName(TARGET_NAMESPACE, "SimpleUserType"), wsdlInput.getXMLType());
      wsdlOutput = wsdlOperation.getOutput(new QName("result"));
      assertEquals(new QName(TARGET_NAMESPACE, "SimpleUserType"), wsdlOutput.getXMLType());
   }

   public void testEventSourceBinding() throws Exception
   {
      File wsdlFile = new File("resources/wsdl11/inherit/wind_inherit.wsdl");
      assertTrue(wsdlFile.exists());

      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlFile.toURL());

      WSDLService service = wsdlDefinitions.getService(new NCName("EventingService"));
      assertNotNull(service);
      WSDLEndpoint[] endpoints = service.getEndpoints();
      for (int i = 0; i < endpoints.length; i++)
      {
         WSDLEndpoint ep = endpoints[i];
         assertEquals(EventingConstants.NS_EVENTING, ep.getQName().getNamespaceURI());
      }

      WSDLInterface warningsInterface = wsdlDefinitions.getInterface(new NCName("Warnings"));
      assertNotNull("Event source port type not parsed", warningsInterface);
      assertEquals(warningsInterface.getQName().getNamespaceURI(), "http://www.example.org/oceanwatch");

      WSDLInterface eventSourceInterface = wsdlDefinitions.getInterface(new NCName("EventSource"));
      assertNotNull(eventSourceInterface);
      assertEquals(eventSourceInterface.getQName().getNamespaceURI(), EventingConstants.NS_EVENTING);
   }
}
