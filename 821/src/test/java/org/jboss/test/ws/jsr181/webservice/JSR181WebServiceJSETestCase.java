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
package org.jboss.test.ws.jsr181.webservice;

import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLDefinitionsFactory;

/**
 * Test the JSR-181 annotation: javax.jws.WebService
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Apr-2005
 */
public class JSR181WebServiceJSETestCase extends JBossWSTest
{
   private static EndpointInterface port;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JSR181WebServiceJSETestCase.class, "jbossws-jsr181-webservice-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      if (port == null)
      {
         InitialContext iniCtx = getInitialContext();
         Service service = (Service)iniCtx.lookup("java:comp/env/service/TestService");
         port = (EndpointInterface)service.getPort(EndpointInterface.class);
      }
   }

   public void testWebService() throws Exception
   {
      deploy("jbossws-jsr181-webservice01.war");
      try
      {
         assertWSDLAccess();
         
         String helloWorld = "Hello world!";
         Object retObj = port.echo(helloWorld);
         assertEquals(helloWorld, retObj);
      }
      finally
      {
         undeploy("jbossws-jsr181-webservice01.war");
      }
   }

   public void testWebServiceWsdlLocation() throws Exception
   {
      deploy("jbossws-jsr181-webservice02.war");
      try
      {
         assertWSDLAccess();
         
         String helloWorld = "Hello world!";
         Object retObj = port.echo(helloWorld);
         assertEquals(helloWorld, retObj);
      }
      finally
      {
         undeploy("jbossws-jsr181-webservice02.war");
      }
   }

   public void testWebServiceEndpointInterface() throws Exception
   {
      deploy("jbossws-jsr181-webservice03.war");
      try
      {
         assertWSDLAccess();
         
         String helloWorld = "Hello world!";
         Object retObj = port.echo(helloWorld);
         assertEquals(helloWorld, retObj);
      }
      finally
      {
         undeploy("jbossws-jsr181-webservice03.war");
      }
   }
   
   private void assertWSDLAccess() throws MalformedURLException
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jsr181?wsdl");
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlURL);
      assertNotNull(wsdlDefinitions);
   }
}
