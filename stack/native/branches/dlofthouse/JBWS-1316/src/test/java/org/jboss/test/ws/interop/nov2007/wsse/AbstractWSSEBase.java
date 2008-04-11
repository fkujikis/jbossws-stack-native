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
package org.jboss.test.ws.interop.nov2007.wsse;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.jboss.test.ws.interop.ClientScenario;
import org.jboss.test.ws.interop.InteropConfigFactory;
import org.jboss.test.ws.interop.nov2007.wsse.EchoDataSet.Request;
import org.jboss.test.ws.interop.nov2007.wsse.EchoDataSetResponse.EchoDataSetResult;
import org.jboss.test.ws.interop.nov2007.wsse.EchoXmlResponse.EchoXmlResult;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;

/**
 * @author Alessio Soldano <alessio.soldano@jboss.com>
 * 
 * @version $Id$
 * @since 26-Oct-2007
 */
public abstract class AbstractWSSEBase extends JBossWSTest
{

   protected IPingService port;

   protected void setUp() throws Exception
   {
      super.setUp();

      if (port == null)
      {
         URL wsdlLocation = new File("resources/interop/nov2007/wsse/shared/WEB-INF/wsdl/WsSecurity10.wsdl").toURL();
         QName serviceName = new QName("http://InteropBaseAddress/interop", "PingService10");
         Service service = Service.create(wsdlLocation, serviceName);
         port = (IPingService)service.getPort(getScenarioPortQName(), IPingService.class);
         configureClient();
      }

      scenarioSetup(port);
   }
   
   protected abstract void scenarioSetup(IPingService port);

   protected abstract QName getScenarioPortQName();

   protected void configureClient()
   {
      
      InteropConfigFactory factory = InteropConfigFactory.newInstance();
      ClientScenario scenario = factory.createClientScenario(System.getProperty("client.scenario"));
      if (scenario != null)
      {
         log.info("Using scenario: " + scenario);
         String targetEndpoint = scenario.getTargetEndpoint().toString();
         if (targetEndpoint.contains("REPLACE_WITH_ACTUAL_HOST"))
         {
            targetEndpoint = targetEndpoint.replace("REPLACE_WITH_ACTUAL_HOST", getServerHost());
         }
         System.out.println("Using target endpoint: " + targetEndpoint);
         ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, targetEndpoint);
      }
      else
      {
         throw new IllegalStateException("Failed to load client scenario");
      }

   }

   protected static void addClientConfToClasspath(String s)
   {
      try
      {
         // wrap the classloader upfront to allow inclusion of the client.jar
         JBossWSTestHelper helper = new JBossWSTestHelper();
         ClassLoader parent = Thread.currentThread().getContextClassLoader();
         URLClassLoader replacement = new URLClassLoader(new URL[] { helper.getArchiveURL(s) }, parent);
         Thread.currentThread().setContextClassLoader(replacement);

      }
      catch (MalformedURLException e)
      {
         throw new IllegalStateException(e);
      }
   }
   
   public void testEcho() throws Exception
   {
      String text = "Hello!";
      String result = port.echo(text);
      assertNotNull(result);
      assertEquals(text, result);
   }
   
   @SuppressWarnings("unchecked")
   public void testEchoDataSet() throws Exception
   {
      String text = "Hello!";
      ObjectFactory factory = new ObjectFactory();
      DataSet dataSet = new DataSet();
      dataSet.setAny(factory.createAnyType(text));
      Request request = new Request();
      request.setAny(factory.createDataSet(dataSet));
      EchoDataSetResult echoDataSetResult = port.echoDataSet(request);
      assertNotNull(echoDataSetResult);
      assertEquals(text, ((JAXBElement)((JAXBElement<DataSet>)echoDataSetResult.getAny()).getValue().getAny()).getValue());
   }
   
   public void testFault() throws Exception
   {
      String text = "Hello!";
      String result = port.fault(text);
      assertNotNull(result);
      assertEquals(text, result);
   }
   
   public void testHeader() throws Exception
   {
      String text = "Hello!";
      String result = port.header(text);
      assertNotNull(result);
      assertEquals(text, result);
   }
   
   @SuppressWarnings("unchecked")
   public void testEchoXml() throws Exception
   {
      String text = "Hello!";
      ObjectFactory factory = new ObjectFactory();
      org.jboss.test.ws.interop.nov2007.wsse.EchoXml.Request request = new org.jboss.test.ws.interop.nov2007.wsse.EchoXml.Request();
      request.setAny(factory.createAnyType(text));
      EchoXmlResult result = port.echoXml(request);
      assertNotNull(result);
      assertEquals(text, ((JAXBElement)result.getAny()).getValue());
   }
   
   public void testPing() throws Exception
   {
      String text = "Hello!";
      String origin = "origin";
      String scenario = "scenario";
      PingRequest parameters = new PingRequest();
      Ping ping = new Ping();
      ping.setOrigin(origin);
      ping.setScenario(scenario);
      ping.setText(text);
      parameters.setPing(ping);
      PingResponse result = port.ping(parameters);
      assertNotNull(result);
      assertNotNull(result.getPingResponse());
      assertEquals(origin, result.getPingResponse().getOrigin());
      assertEquals(scenario, result.getPingResponse().getScenario());
      assertEquals(origin + " : " + text, result.getPingResponse().getText());
   }
}
