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
package org.jboss.test.ws.config;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.ws.metadata.config.WSConfig;
import org.jboss.ws.metadata.config.WSConfigFactory;
import org.jboss.ws.metadata.config.WSEndpointConfig;
import org.jboss.ws.metadata.config.WSHandlerChainConfig;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData;

/**
 * Test parsing of the JBossWS config
 *
 * @author Thomas.Diesler@jboss.org
 * @since 21-Dec-2004
 */
public class WSConfigTestCase extends JBossWSTest
{
   public void testJ2EEWebServicesSchemaBinding() throws Exception
   {
      ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
      URL xsdURL = ctxLoader.getResource("schema/j2ee_web_services_1_1.xsd");

      InputStream xsd = xsdURL.openStream();
      //XsdBinder.bind(xsd, "UTF-8");
   }

   public void testParseWithSchemaBinding() throws Exception
   {
      File confFile = new File("resources/config/jbossws-endpoint-config.xml");
      assertTrue(confFile.exists());

      WSConfigFactory factory = WSConfigFactory.newInstance();
      //WSConfig wsConfig = factory.parseWithSchemaBinding(confFile.toURL());
      //assertConfig(wsConfig);
   }

   public void testParseWithObjectModelFactory() throws Exception
   {
      File confFile = new File("resources/config/jbossws-endpoint-config.xml");
      assertTrue(confFile.exists());

      WSConfigFactory factory = WSConfigFactory.newInstance();
      WSConfig wsConfig = factory.parseWithObjectModelFactory(confFile.toURL());
      assertConfig(wsConfig);
   }

   private void assertConfig(WSConfig wsConfig)
   {
      assertNotNull("Null wsConfig", wsConfig);

      assertEquals(2, wsConfig.getEndpointConfig().size());
      WSEndpointConfig epc1 = (WSEndpointConfig)wsConfig.getEndpointConfig().get(0);
      WSEndpointConfig epc2 = (WSEndpointConfig)wsConfig.getEndpointConfig().get(1);

      assertEquals("Standard Endpoint", epc1.getConfigName());
      assertNull(epc1.getPreHandlerChain());
      assertNull(epc1.getPostHandlerChain());

      assertEquals("WS-Security Endpoint", epc2.getConfigName());
      WSHandlerChainConfig preChain = epc2.getPreHandlerChain();
      assertEquals("PreHandlerChain", preChain.getHandlerChainName());
      assertEquals(1, preChain.getHandlers().size());
      UnifiedHandlerMetaData h1 = (UnifiedHandlerMetaData)preChain.getHandlers().get(0);
      assertEquals("WSSecurityHandlerInbound", h1.getHandlerName());
      assertEquals("org.jboss.ws.wsse.WSSecurityHandlerInbound", h1.getHandlerClass());
      assertNull(epc2.getPostHandlerChain());
   }
}
