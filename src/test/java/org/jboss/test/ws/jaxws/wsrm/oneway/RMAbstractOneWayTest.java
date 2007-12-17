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
package org.jboss.test.ws.jaxws.wsrm.oneway;

import static org.jboss.test.ws.jaxws.wsrm.Helper.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.ws.extensions.wsrm.api.RMProvider;
import org.jboss.ws.extensions.wsrm.api.RMSequence;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.test.ws.jaxws.wsrm.services.OneWayServiceIface;

/**
 * Reliable JBoss WebService client invoking one way methods
 *
 * @author richard.opalka@jboss.com
 * @since 22-Aug-2007
 */
public abstract class RMAbstractOneWayTest extends JBossWSTest
{
   private static final Properties props = new Properties();
   private final String serviceURL = "http://" + getServerHost() + ":" + props.getProperty("port") + props.getProperty("path");
   private String targetNS = "http://org.jboss.ws/jaxws/wsrm";
   private OneWayServiceIface proxy;
   
   static
   {
      try 
      {
         props.load(
            new FileInputStream(
               new File("resources/jaxws/wsrm/properties/RMAbstractOneWayTest.properties")));
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
      }
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      QName serviceName = new QName(targetNS, "OneWayService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      proxy = (OneWayServiceIface)service.getPort(OneWayServiceIface.class);
   }
   
   public void testOneWayMethods() throws Exception
   {
      RMSequence sequence = ((RMProvider)proxy).createSequence(isClientAddressable());
      setAddrProps(proxy, "http://useless/action1", serviceURL);
      proxy.method1();
      setAddrProps(proxy, "http://useless/action2", serviceURL);
      proxy.method2("Hello World");
      setAddrProps(proxy, "http://useless/action3", serviceURL);
      proxy.method3(new String[] {"Hello","World"});
      sequence.close();
   }

   public static String getClasspath()
   {
      return props.getProperty("archives");
   }
   
   protected abstract boolean isClientAddressable();
   
}
