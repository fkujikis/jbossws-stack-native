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
package org.jboss.test.ws.xop.doclit;

// $Id: xop.doclitTestCase.java 275 2006-05-04 21:36:29Z jason.greene@jboss.com $

import junit.framework.Test;
import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.xb.binding.SimpleTypeBindings;
import org.jboss.ws.jaxrpc.StubExt;

import javax.activation.DataHandler;
import javax.naming.InitialContext;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;
import java.io.*;

/**
 *
 * @author Heiko.Braun@jboss.org
 * @since 11-Nov-2005
 */
public class XOPElementDoclitTestCase extends JBossWSTest
{
   private static TestService_PortType port;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(XOPElementDoclitTestCase.class, "jbossws-xop-doclit.war, jbossws-xop-doclit-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      if (port == null)
      {
         InitialContext iniCtx = getInitialContext();
         Service service = (Service)iniCtx.lookup("java:comp/env/service/TestService");
         port = (TestService_PortType)service.getPort(TestService_PortType.class);
      }

      //((Stub)port)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:8081/jbossws-xop-doclit");
   }


   public void testPingMsgInlined() throws Exception {

      DataHandler dh = new DataHandler("Another plain text attachment", "text/plain");
      PingMsgResponse value = port.ping(new PingMsg("Some text message", dh));
      assertNotNull("Return value was null", value);
      assertNotNull("Returned xopContent was null", value.getXopContent());
      assertTrue("Wrong java type returned", (value.getXopContent()) instanceof DataHandler);

      // check inline values
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      dh.writeTo(bout);

      byte[] imageBytes = bout.toByteArray();
      String expected = SimpleTypeBindings.marshalBase64(imageBytes);

      String was = (String)((Stub) port)._getProperty("xop.inline.value");
      assertNotNull("base64 value not found", was);
      //assertEquals(expected, was);
   }

   public void testMTOMDisabled() throws Exception {

      byte[] bytes = getBytesFromFile(new File("resources/samples/mtom/attach.jpeg"));
      DataHandler dh = new DataHandler(new ByteArrayInputStream(bytes), "application/octet-stream");

      // force disable MTOM
      ((Stub)port)._setProperty(StubExt.PROPERTY_MTOM_ENABLED, "false");

      PingMsgResponse value = port.ping(new PingMsg("Some text message", dh));
      assertNotNull("Return value was null",value);
      assertNotNull("Return image was null", value.getXopContent());
   } 

   public static byte[] getBytesFromFile(File file) throws IOException {
      InputStream is = new FileInputStream(file);

      long length = file.length();
      byte[] bytes = new byte[(int)length];

      int offset = 0;
      int numRead = 0;
      while (offset < bytes.length
          && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
         offset += numRead;
      }

      is.close();
      return bytes;
   }
}
