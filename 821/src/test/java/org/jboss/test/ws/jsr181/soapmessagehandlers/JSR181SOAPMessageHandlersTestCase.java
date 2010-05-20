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
package org.jboss.test.ws.jsr181.soapmessagehandlers;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.jaxrpc.CallImpl;
import org.jboss.ws.jaxrpc.ServiceFactoryImpl;

/**
 * Test the JSR-181 annotation: javax.jws.SOAPMessageHandlers
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-Oct-2005
 */
public class JSR181SOAPMessageHandlersTestCase extends JBossWSTest
{
   private static final String targetNS = "http://soapmessagehandlers.jsr181.ws.test.jboss.org/jaws";

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JSR181SOAPMessageHandlersTestCase.class, "jbossws-jsr181-soapmessagehandlers.war");
   }

   public void testHandlerChain() throws Exception
   {
      QName serviceName = new QName(targetNS, "MyWebServiceService");
      QName portName = new QName(targetNS, "MyWebServicePort");
      
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jbossws-jsr181-soapmessagehandlers/TestService?wsdl");

      ServiceFactoryImpl factory = new ServiceFactoryImpl();
      Service service = factory.createService(wsdlURL, serviceName);
      CallImpl call = (CallImpl)service.createCall(portName, "echo");
      
      Object retObj = call.invoke(new Object[]{"Kermit"});
      assertEquals("Kermit|LogHandlerRequest|AuthorizationHandlerRequest|RoutingHandlerRequest|endpoint|RoutingHandlerResponse|AuthorizationHandlerResponse|LogHandlerResponse", retObj);
   }
}
