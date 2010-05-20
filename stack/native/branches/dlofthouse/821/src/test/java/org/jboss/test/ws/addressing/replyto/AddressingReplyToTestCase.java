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
package org.jboss.test.ws.addressing.replyto;

import java.io.ByteArrayInputStream;
import java.net.URL;

import javax.xml.rpc.Service;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Test endpoint using ws-addressing
 *
 * @author Thomas.Diesler@jboss.org
 * @since 24-Nov-2005
 */
public class AddressingReplyToTestCase extends JBossWSTest
{
   private static InitialEndpoint initial;
   private static ReplyToEndpoint replyto;
   private static FaultToEndpoint faultto;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(AddressingReplyToTestCase.class,
            "jbossws-addressing-initial.war,jbossws-addressing-initial-client.jar," +
            "jbossws-addressing-replyto.war,jbossws-addressing-replyto-client.jar," +
            "jbossws-addressing-faultto.war,jbossws-addressing-faultto-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (initial == null)
      {
         Service initialService = (Service)getInitialContext("initial-client").lookup("java:comp/env/service/InitialService");
         initial = (InitialEndpoint)initialService.getPort(InitialEndpoint.class);
         Service replytoService = (Service)getInitialContext("replyto-client").lookup("java:comp/env/service/ReplyToService");
         replyto = (ReplyToEndpoint)replytoService.getPort(ReplyToEndpoint.class);
         Service faulttoService = (Service)getInitialContext("faultto-client").lookup("java:comp/env/service/FaultToService");
         faultto = (FaultToEndpoint)faulttoService.getPort(FaultToEndpoint.class);
      }
   }

   /** This sends a valid message to the ReplyTo endpoint and verfies whether we can read it of again.
    */
   public void testReplyToMessage() throws Exception
   {
      String reqEnv =
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
         "  <env:Header/>" +
         "  <env:Body>" +
         "    <ns1:addItemResponse xmlns:ns1='http://org.jboss.ws/addressing/replyto'>" +
         "      <ns1:result>Mars Bar</ns1:result>" +
         "    </ns1:addItemResponse>" +
         "  </env:Body>" +
         "</env:Envelope>";

      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL epURL = new URL("http://" + getServerHost() + ":8080/jbossws-addressing-replyto/ReplyToService");
      con.call(reqMsg, epURL);

      assertEquals("Mars Bar", replyto.getLastItem());
   }

   /** This sends a fault message to the FaultTo endpoint and verfies whether we can read it of again.
    */
   public void testFaultToMessage() throws Exception
   {
      String reqEnv =
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
         "  <env:Header/>" +
         "  <env:Body>" +
         "    <env:Fault>" +
         "      <faultcode>env:Client</faultcode>" +
         "      <faultstring>java.lang.IllegalArgumentException: Mars Bar</faultstring>" +
         "    </env:Fault>" +
         "  </env:Body>" +
         "</env:Envelope>";

      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL epURL = new URL("http://" + getServerHost() + ":8080/jbossws-addressing-faultto/FaultToService");
      con.call(reqMsg, epURL);

      assertEquals("java.lang.IllegalArgumentException: Mars Bar", faultto.getLastFault());
   }

   public void testInital() throws Exception
   {
      String item = initial.addItem("Ice Cream");
      assertNull("Expected null, but was: " + item, item);

      item = initial.addItem("Invalid Value");
      assertNull("Expected null, but was: " + item, item);
   }

   public void testReplyTo() throws Exception
   {
      String item = replyto.getLastItem();
      assertEquals("Ice Cream", item);
   }

   public void testFaultTo() throws Exception
   {
      String lastFault = faultto.getLastFault();
      assertEquals("java.lang.IllegalArgumentException: Invalid Value", lastFault);
   }
}
