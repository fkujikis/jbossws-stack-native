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
package org.jboss.test.ws.interop.wsse;

// $Id: $

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.jboss.ws.core.StubExt;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;

/**
 * @author Heiko.Braun@jboss.org
 * @since 25.01.2007
 */
public abstract class AbstractWSSEBase extends JBossWSTest
{
   protected IPingService port;
   private String keyStore;
   private String trustStore;
   private String keyStorePassword;
   private String trustStorePassword;
   private String keyStoreType;
   private String trustStoreType;

   protected void setUp() throws Exception
   {
      super.setUp();

      if (port == null)
      {
         URL wsdlLocation = getResourceURL("interop/wsse/shared/WEB-INF/wsdl/WsSecurity10.wsdl");
         QName serviceName = new QName("http://tempuri.org/", "PingService10");
         Service service = Service.create(wsdlLocation, serviceName);
         port = service.getPort(IPingService.class);

         ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpointURL());
         configureClient();
      }

      defaultSetup(port);
   }

   abstract String getEndpointURL();

   protected void defaultSetup(IPingService port)
   {
      ((StubExt)port).setConfigName("Standard WSSecurity Client");

      //Backup values
      keyStore = System.getProperty("org.jboss.ws.wsse.keyStore");
      keyStorePassword = System.getProperty("org.jboss.ws.wsse.keyStorePassword");
      keyStoreType = System.getProperty("org.jboss.ws.wsse.keyStoreType");
      trustStore = System.getProperty("org.jboss.ws.wsse.trustStore");
      trustStorePassword = System.getProperty("org.jboss.ws.wsse.trustStorePassword");
      trustStoreType = System.getProperty("org.jboss.ws.wsse.trustStoreType");
      //Set values
      System.setProperty("org.jboss.ws.wsse.keyStore", getResourceFile("interop/wsse/shared/META-INF/alice.jks").getPath());
      System.setProperty("org.jboss.ws.wsse.trustStore", getResourceFile("interop/wsse/shared/META-INF/wsse10.truststore").getPath());
      System.setProperty("org.jboss.ws.wsse.keyStorePassword", "password");
      System.setProperty("org.jboss.ws.wsse.trustStorePassword", "password");
      System.setProperty("org.jboss.ws.wsse.keyStoreType", "jks");
      System.setProperty("org.jboss.ws.wsse.trustStoreType", "jks");
   }
   
   protected void tearDown() throws Exception
   {
      //Restore environment
      System.setProperty("org.jboss.ws.wsse.keyStore", keyStore);
      System.setProperty("org.jboss.ws.wsse.trustStore", trustStore);
      System.setProperty("org.jboss.ws.wsse.keyStorePassword", keyStorePassword);
      System.setProperty("org.jboss.ws.wsse.trustStorePassword", trustStorePassword);
      System.setProperty("org.jboss.ws.wsse.keyStoreType", keyStoreType);
      System.setProperty("org.jboss.ws.wsse.trustStoreType", trustStoreType);
      super.tearDown();
   }

   protected void configureClient()
   {

      /*InteropConfigFactory factory = InteropConfigFactory.newInstance();
      ClientScenario scenario = factory.createClientScenario(System.getProperty("client.scenario"));
      if(scenario!=null)
      {
         log.info("Using scenario: " + scenario);
         ((Stub)port)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, scenario.getTargetEndpoint().toString());
      }
      else
      {
         throw new IllegalStateException("Failed to load client scenario");
      }
      */
   }

   protected static ClassLoader addClientConfToClasspath(String s)
   {
      try
      {
         // wrap the classloader upfront to allow inclusion of the client.jar
         JBossWSTestHelper helper = new JBossWSTestHelper();
         ClassLoader parent = Thread.currentThread().getContextClassLoader();
         URLClassLoader replacement = new URLClassLoader(new URL[] { helper.getArchiveURL(s) }, parent);
         Thread.currentThread().setContextClassLoader(replacement);
         return parent;
      }
      catch (MalformedURLException e)
      {
         throw new IllegalStateException(e);
      }
   }
}
