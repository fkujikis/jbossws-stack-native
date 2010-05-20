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
package org.jboss.test.ws.jsr181.oneway;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.jaxrpc.CallImpl;
import org.jboss.ws.jaxrpc.ServiceFactoryImpl;
import org.jboss.ws.metadata.OperationMetaData;

/**
 * Test the JSR-181 annotation: javax.jws.Oneway
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Oct-2005
 */
public class JSR181OneWayTestCase extends JBossWSTest
{
   private static final String targetNS = "http://oneway.jsr181.ws.test.jboss.org/jaws";

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JSR181OneWayTestCase.class, "jbossws-jsr181-oneway.war");
   }

   public void testWebService() throws Exception
   {
      QName serviceName = new QName(targetNS, "PingServiceService");
      QName portName = new QName(targetNS, "PingServicePort");

      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jbossws-jsr181-oneway/TestService?wsdl");

      ServiceFactoryImpl factory = new ServiceFactoryImpl();
      Service service = factory.createService(wsdlURL, serviceName);
      CallImpl call = (CallImpl)service.createCall(portName, "ping");

      OperationMetaData opMetaData = call.getOperationMetaData();
      assertTrue("Expected oneway operation", opMetaData.isOneWayOperation());

      Object retObj = call.invoke(null);
      assertNull("Expected null return", retObj);
   }
}
