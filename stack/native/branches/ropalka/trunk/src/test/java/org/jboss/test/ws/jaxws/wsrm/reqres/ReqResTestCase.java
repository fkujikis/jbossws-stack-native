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
package org.jboss.test.ws.jaxws.wsrm.reqres;

import org.w3c.dom.Element;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.jboss.wsf.common.DOMUtils;
import org.jboss.wsf.common.DOMWriter;
import org.jboss.test.ws.jaxws.wsrm.ReqResServiceIface;

/**
 * Reliable JBoss WebService client invoking req/res methods
 *
 * @author richard.opalka@jboss.com
 * @since 22-Aug-2007
 */
public class ReqResTestCase extends JBossWSTest
{
   private static final String HELLO_WORLD_MSG = "Hello World";
   private static final String TARGET_NS = "http://org.jboss.ws/jaxws/wsrm";
   private static final String REQ_PAYLOAD = "<ns2:echo xmlns:ns2='" + TARGET_NS + "'><String_1>" + HELLO_WORLD_MSG + "</String_1></ns2:echo>";
   private Exception handlerException;
   private boolean asyncHandlerCalled;
   private ReqResServiceIface proxy;
   private Dispatch<Source> dispatch;

   public static Test suite()
   {
      return new JBossWSTestSetup(ReqResTestCase.class, "jaxws-wsrm.war");
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      QName serviceName = new QName(TARGET_NS, "ReqResService");
      QName portName = new QName(TARGET_NS, "ReqResPort");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-wsrm/ReqResService?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);
      proxy = (ReqResServiceIface)service.getPort(ReqResServiceIface.class);
      handlerException = null;
      asyncHandlerCalled = false;
   }
   
   public void testInvokeSync() throws Exception
   {
      System.out.println("FIXME [JBWS-515] Provide an initial implementation for WS-ReliableMessaging");
      assertEquals(proxy.echo(HELLO_WORLD_MSG), HELLO_WORLD_MSG);
   }

   public void testInvokeAsync() throws Exception
   {
      System.out.println("FIXME [JBWS-515] Provide an initial implementation for WS-ReliableMessaging");
      Response<String> response = proxy.echoAsync(HELLO_WORLD_MSG);
      assertEquals(response.get(), HELLO_WORLD_MSG); // concurrency future pattern
   }

   public void testInvokeAsyncHandler() throws Exception
   {
      System.out.println("FIXME [JBWS-515] Provide an initial implementation for WS-ReliableMessaging");
      AsyncHandler<String> handler = new AsyncHandler<String>()
      {
         public void handleResponse(Response<String> response)
         {
            try
            {
               String retStr = (String) response.get(1000, TimeUnit.MILLISECONDS);
               assertEquals(HELLO_WORLD_MSG, retStr);
               asyncHandlerCalled = true;
            }
            catch (Exception ex)
            {
               handlerException = ex;
            }
         }
      };
      Future<?> future = proxy.echoAsync(HELLO_WORLD_MSG, handler);
      future.get(1000, TimeUnit.MILLISECONDS);
      ensureAsyncStatus();
   }
   
   public void testInvokeAsyncHandlerWithDispach() throws Exception
   {
      System.out.println("FIXME [JBWS-515] Provide an initial implementation for WS-ReliableMessaging");
      AsyncHandler<Source> handler = new AsyncHandler<Source>()
      {
         public void handleResponse(Response<Source> response)
         {
            try
            {
               verifyResponse(response.get());
               asyncHandlerCalled = true;
            }
            catch (Exception ex)
            {
               handlerException = ex;
            }
         }
      };
      StreamSource reqObj = new StreamSource(new StringReader(REQ_PAYLOAD));
      Future<?> future = dispatch.invokeAsync(reqObj, handler);
      future.get(1000, TimeUnit.MILLISECONDS);
      ensureAsyncStatus();
   }
   
   private void ensureAsyncStatus() throws Exception
   {
      if (handlerException != null) throw handlerException;
      assertTrue("Async handler called", asyncHandlerCalled);
   }
   
   private void verifyResponse(Source result) throws IOException
   {
      Element resElement = DOMUtils.sourceToElement(result);
      String resStr = DOMWriter.printNode(resElement, false);
      assertTrue("Unexpected response: " + resStr, resStr.contains("<result>" + HELLO_WORLD_MSG + "</result>"));
   }
}
