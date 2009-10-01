/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.test.ws.jaxws.endpoint.jse;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.test.ws.jaxws.endpoint.jse.endpoints.Endpoint1Iface;
import org.jboss.test.ws.jaxws.endpoint.jse.endpoints.Endpoint1Impl;
import org.jboss.ws.Constants;
import org.jboss.wsf.test.JBossWSTest;

/**
 * TODO: javadoc
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class UsecasesTestCase extends JBossWSTest
{
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      System.setProperty(Constants.HTTP_KEEP_ALIVE, Boolean.FALSE.toString());
   }

   @Override
   protected void tearDown() throws Exception
   {
      System.getProperties().remove(Constants.HTTP_KEEP_ALIVE);

      super.tearDown();
   }

   private int port1 = 8878; // 8878
   private int port2 = 8878; // 8878

   public void testTwoPorts() throws Exception
   {
      String publishURL1 = "http://" + getServerHost() + ":" + port1 + "/jaxws-endpoint1";
      Endpoint endpoint1 = publishEndpoint(Endpoint1Impl.class, publishURL1);

      String publishURL2 = "http://" + getServerHost() + ":" + port2 + "/jaxws-endpoint2";
      Endpoint endpoint2 = publishEndpoint(new Endpoint1Impl(), publishURL2);

      invokeEndpoint1(publishURL1);
      invokeEndpoint1(publishURL2);

      endpoint1.stop();
      endpoint2.stop();
   }

   public void testTwoPortsAndLongPaths() throws Exception
   {
      String publishURL1 = "http://" + getServerHost() + ":" + port1 + "/jaxws-endpoint/endpoint/number1";
      Endpoint endpoint1 = publishEndpoint(Endpoint1Impl.class, publishURL1);

      String publishURL2 = "http://" + getServerHost() + ":" + port2 + "/jaxws-endpoint/endpoint/number2";
      Endpoint endpoint2 = publishEndpoint(new Endpoint1Impl(), publishURL2);

      invokeEndpoint1(publishURL1);
      invokeEndpoint1(publishURL2);

      endpoint1.stop();
      endpoint2.stop();
   }

   public void testTwoPortsAndAlmostIdenticalLongPaths() throws Exception
   {
      String publishURL1 = "http://" + getServerHost() + ":" + port1 + "/jaxws-endpoint/endpoint/number1";
      Endpoint endpoint1 = publishEndpoint(Endpoint1Impl.class, publishURL1);

      String publishURL2 = "http://" + getServerHost() + ":" + port1 + "/jaxws-endpoint/endpoint/number11";
      Endpoint endpoint2 = publishEndpoint(new Endpoint1Impl(), publishURL2);

      invokeEndpoint2(publishURL1);
      invokeEndpoint2(publishURL2);

      endpoint1.stop();
      endpoint2.stop();
   }
   
   public void testTwoPortsAndIdenticalPaths() throws Exception
   {
      // TODO: provide test port1/service1 vs. port2/service1
   }

   private Endpoint publishEndpoint(Object epImpl, String publishURL)
   {
      Endpoint endpoint = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, epImpl);
      endpoint.publish(publishURL);
      return endpoint;
   }

   private void invokeEndpoint1(String publishURL) throws Exception
   {
      URL wsdlURL = new URL(publishURL + "?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/endpoint/jse/endpoints/", "Endpoint1Impl");
      Service service = Service.create(wsdlURL, qname);
      Endpoint1Iface port = (Endpoint1Iface)service.getPort(Endpoint1Iface.class);

      String helloWorld = "Hello world!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }
   
   private void invokeEndpoint2(String publishURL) throws Exception
   {
      URL wsdlURL = new URL(publishURL + "?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/endpoint/jse/endpoints/", "Endpoint1Impl");
      Service service = Service.create(wsdlURL, qname);
      Endpoint1Iface port = (Endpoint1Iface)service.getPort(Endpoint1Iface.class);

      // Invoke the endpoint
      String helloWorld = "Hello world!";
      assertEquals(0, port.getCount());
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
      assertEquals(1, port.getCount());
      port.echo(helloWorld);
      assertEquals(2, port.getCount());
   }

}
