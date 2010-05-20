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
package org.jboss.test.ws.samples.wsaddressing;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.HandlerRegistry;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.test.ws.samples.dynamichandler.ClientSideHandler;
import org.jboss.ws.addressing.soap.SOAPClientHandler;
import org.jboss.ws.jaxrpc.ServiceFactoryImpl;
import org.jboss.ws.jaxrpc.ServiceImpl;

/**
 * Test stateful endpoint using ws-addressing
 *
 * @author Thomas.Diesler@jboss.org
 * @since 24-Nov-2005
 */
public class AddressingStatefulTestCase extends JBossWSTest
{
   private static StatefulEndpoint port1;
   private static StatefulEndpoint port2;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(AddressingStatefulTestCase.class, "jbossws-samples-wsaddressing.war, jbossws-samples-wsaddressing-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (port1 == null || port2 == null)
      {
         if (isTargetServerJBoss())
         {
            InitialContext iniCtx = getInitialContext();
            Service service1 = (Service)iniCtx.lookup("java:comp/env/service/TestService");
            Service service2 = (Service)iniCtx.lookup("java:comp/env/service/TestService");
            port1 = (StatefulEndpoint)service1.getPort(StatefulEndpoint.class);
            port2 = (StatefulEndpoint)service2.getPort(StatefulEndpoint.class);
         }
         else
         {
            ServiceFactoryImpl factory = new ServiceFactoryImpl();
            URL wsdlURL = new File("resources/samples/wsaddressing/META-INF/wsdl/TestService.wsdl").toURL();
            URL mappingURL = new File("resources/samples/wsaddressing/META-INF/jaxrpc-mapping.xml").toURL();
            
            QName serviceName = new QName("http://org.jboss.ws/samples/wsaddressing", "TestService");
            QName portName = new QName("http://org.jboss.ws/samples/wsaddressing", "StatefulEndpointPort");
            
            ServiceImpl service1 = (ServiceImpl)factory.createService(wsdlURL, serviceName, mappingURL);
            HandlerRegistry registry1 = service1.getDynamicHandlerRegistry();
            List infos1 = registry1.getHandlerChain(portName);
            infos1.add(new HandlerInfo(ClientHandler.class, new HashMap(), new QName[]{}));
            infos1.add(new HandlerInfo(SOAPClientHandler.class, new HashMap(), new QName[]{}));
            registry1.setHandlerChain(portName, infos1);

            
            ServiceImpl service2 = (ServiceImpl)factory.createService(wsdlURL, serviceName, mappingURL);
            HandlerRegistry registry2 = service2.getDynamicHandlerRegistry();
            List infos2 = registry2.getHandlerChain(portName);
            infos2.add(new HandlerInfo(ClientHandler.class, new HashMap(), new QName[]{}));
            infos2.add(new HandlerInfo(SOAPClientHandler.class, new HashMap(), new QName[]{}));
            registry2.setHandlerChain(portName, infos2);
            
            port2 = (StatefulEndpoint)service2.getPort(StatefulEndpoint.class);
            port1 = (StatefulEndpoint)service1.getPort(StatefulEndpoint.class);
         }
      }
   }

   public void testAddItem() throws Exception
   {
      port1.addItem("Ice Cream");
      port1.addItem("Ferrari");

      port2.addItem("Mars Bar");
      port2.addItem("Porsche");
   }

   public void testGetItems() throws Exception
   {
      String items1 = port1.getItems();
      assertEquals("[Ice Cream, Ferrari]", items1);

      String items2 = port2.getItems();
      assertEquals("[Mars Bar, Porsche]", items2);
   }

   public void testCheckout() throws Exception
   {
      port1.checkout();
      assertEquals("[]", port1.getItems());

      port2.checkout();
      assertEquals("[]", port2.getItems());
   }
}
