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
package org.jboss.test.ws.samples.mtom;

import junit.framework.Test;
import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.jaxrpc.ServiceFactoryImpl;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

/**
 * Test SOAP with XOP through the JAXRPC dynamic proxy layer.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Heiko.Braun@jboss.org
 * @since 18-Jan-2006
 */
public class XOPRpcTestCase extends JBossWSTest
{
   private static XOPTest port;

   /** Deploy the test ear */
   public static Test suite() throws Exception
   {
      return JBossWSTestSetup.newTestSetup(XOPRpcTestCase.class, "jbossws-samples-mtom.war, jbossws-samples-mtom-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (port == null)
      {
         if (isTargetServerJBoss())
         {
            InitialContext iniCtx = getInitialContext();
            Service service = (Service)iniCtx.lookup("java:comp/env/service/XOPTestService");
            port = (XOPTest)service.getPort(XOPTest.class);
         }
         else
         {
            ServiceFactoryImpl factory = new ServiceFactoryImpl();
            URL wsdlURL = new File("resources/samples/mtom/WEB-INF/wsdl/TestService.wsdl").toURL();
            URL mappingURL = new File("resources/samples/mtom/WEB-INF/jaxrpc-mapping.xml").toURL();
            QName qname = new QName("http://org.jboss.ws/samples/mtom", "XOPTest");
            Service service = factory.createService(wsdlURL, qname, mappingURL);
            port = (XOPTest)service.getPort(XOPTest.class);
            ((Stub)port)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, "http://" + getServerHost() + ":8080/jbossws-samples-mtom");
         }

      }

      //((Stub)port)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, "http://" + getServerHost() + ":8081/jbossws-samples-mtom");
   }

   /**
    * Send unknown file as 'application/octet-stream'.
    * Uses a DataHandler both for the endpint parameter and the return type.
    */
   public void testSimpleBinary() throws Exception
   {
      DataHandler value = port.sendOctets("Some text message", new DataHandler(
          new FileDataSource("resources/samples/mtom/disguised_jpeg.xcf")
      ));
      assertNotNull(value);
      assertTrue("Wrong content type", value.getContentType().equals("application/octet-stream"));
   }

   /**
    * Send a multipart message with a 'image/jpeg' attachment part.
    * Uses a DataHandler as endpoint parameter and return type.
    */
   public void testAbstractParameterTypes() throws Exception
   {
      URL url = new File("resources/samples/mtom/attach.jpeg").toURL();
      DataHandler value = port.sendMimeImageJPEG("Some text message", new DataHandler(url));
      assertNotNull(value);
      assertTrue("Wrong return content-type returned", value.getContentType().equals("image/jpeg"));
   }

   /**
    * Send a multipart message with a 'text/plain' attachment part.
    * Uses java.lang.String as endpoint parameter and return type.
    */
   public void testConcreteParameterTypes() throws Exception
   {
      String xoppart = "This is a plain text attachment.";
      String value = port.sendMimeTextPlain("Some text message", xoppart);
      assertNotNull(value);
      assertEquals("Value mismatch", value, xoppart);
   }

   /**
    * Send a multipart message with a 'text/xml' attachment part.
    * Uses a DataHandler as endpoint parameter, but javax.xml.transform.Source as return value.
    */
   public void testParameterConversion() throws Exception
   {
      FileInputStream stream = new FileInputStream("resources/samples/mtom/attach.xml");
      StreamSource source = new StreamSource(stream);

      Source value = port.sendMimeTextXML("Some text message", new DataHandler(source, "text/xml"));
      assertNotNull(value);
      assertTrue("Wrong return value type", value instanceof Source);
   }

   /**
    * Send a multipart message with a application/xml attachment part.
    * Uses a javax.xml.transform.Source as endpoint parameter, but javax.activation.DataHandler as return value.
    */
   public void testParameterConversionReverse() throws Exception
   {
      FileInputStream stream = new FileInputStream("resources/samples/mtom/attach.xml");
      StreamSource source = new StreamSource(stream);

      DataHandler value = port.sendMimeApplicationXML("Some text message", source);
      assertNotNull(value);
      assertTrue("Wrong return value content-type", value.getContentType().equals("application/xml"));
   }

   /**
    * Send a inlined message and expect a multipart response.
    */
   public void testMTOMDisabledClientSide() throws Exception
   {
      /*URL url = new File("resources/samples/mtom/attach.jpeg").toURL();

      // disable MTOM
      ((Stub)port)._setProperty("org.jboss.ws.mtom.enabled", "false");

      DataHandler value = port.sendMimeImageJPEG("MTOM disabled request", new DataHandler(url));
      assertNotNull(value);
      assertTrue("Wrong return content-type returned", value.getContentType().equals("image/jpeg"));
      */

      System.out.println("FIXME: testMTOMDisabledClientSide");
   }
}
