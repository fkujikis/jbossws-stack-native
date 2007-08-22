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

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.jboss.test.ws.jaxws.wsrm.OneWayServiceIface;

/**
 * Reliable JBoss WebService client invoking one way methods
 *
 * @author richard.opalka@jboss.com
 * @since 22-Aug-2007
 */
public class OneWayTestCase extends JBossWSTest
{
   private String targetNS = "http://wsrm.jaxws.ws.test.jboss.org/";
   private OneWayServiceIface proxy;
   
   public static Test suite()
   {
      return new JBossWSTestSetup(OneWayTestCase.class, "jaxws-wsrm.war");
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      QName serviceName = new QName(targetNS, "OneWayService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-wsrm/OneWayService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      proxy = (OneWayServiceIface)service.getPort(OneWayServiceIface.class);
   }
   
   public void testReliableOneWayInvocations() throws Exception
   {
      System.out.println("FIXME [JBWS-515] Provide an initial implementation for WS-ReliableMessaging");
      proxy.method1();
      proxy.method2("Hello World");
      proxy.method3(new String[] {"Hello","World"});
   }
}
