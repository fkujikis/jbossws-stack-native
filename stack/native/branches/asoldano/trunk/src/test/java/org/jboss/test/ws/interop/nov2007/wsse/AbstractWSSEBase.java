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

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.jboss.test.ws.interop.ClientScenario;
import org.jboss.test.ws.interop.InteropConfigFactory;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;

/**
 * @author Alessio Soldano <alessio.soldano@jboss.com>
 * 
 * @version $Id:$
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
         URL wsdlLocation = new File("resources/interop/nov2007/wsse/shared/WEB-INF/wsdl/WSSecurty10.wsdl").toURL();
         QName serviceName = new QName("http://tempuri.org/", "PingService10");
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
         System.out.println("SCENARIO: "+scenario);
         log.info("Using scenario: " + scenario);
         ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, scenario.getTargetEndpoint().toString());
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
}
