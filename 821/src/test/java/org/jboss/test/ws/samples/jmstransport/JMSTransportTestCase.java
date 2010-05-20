/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.ws.samples.jmstransport;

// $Id$

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.util.xml.DOMUtils;

import EDU.oswego.cs.dl.util.concurrent.CountDown;

/**
 * A web service client that connects to a MDB endpoint.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 26-Apr-2004
 */
public class JMSTransportTestCase extends JBossWSTest
{
   static CountDown done = new CountDown(1);

   public static Test suite() throws Exception
   {
      return JBossWSTestSetup.newTestSetup(JMSTransportTestCase.class, "jbossws-samples-jmstransport.sar");
   }

   /**
    * Send the message to the specified queue
    */
   public void testSOAPMessageToEndpointQueue() throws Exception
   {
      String reqMessage = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" + 
          "<env:Body>" + 
           "<ns1:getContactInfo xmlns:ns1='http://org.jboss.ws/samples/jmstransport'>" + 
            "<String_1>mafia</String_1>" + 
           "</ns1:getContactInfo>" + 
          "</env:Body>" + 
         "</env:Envelope>";

      String resMessage = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
          "<env:Header/>" + 
          "<env:Body>" + 
           "<ns1:getContactInfoResponse xmlns:ns1='http://org.jboss.ws/samples/jmstransport'>" + 
            "<result>The 'mafia' boss is currently out of office, please call again.</result>" + 
           "</ns1:getContactInfoResponse>" + 
          "</env:Body>" + 
         "</env:Envelope>";

      InitialContext context = new InitialContext();
      QueueConnectionFactory connectionFactory = (QueueConnectionFactory)context.lookup("ConnectionFactory");
      Queue reqQueue = (Queue)context.lookup("queue/RequestQueue");
      Queue resQueue = (Queue)context.lookup("queue/ResponseQueue");

      QueueConnection con = connectionFactory.createQueueConnection();
      QueueSession session = con.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      QueueReceiver receiver = session.createReceiver(resQueue);
      ResponseListener responseListener = new ResponseListener();
      receiver.setMessageListener(responseListener);
      con.start();

      TextMessage message = session.createTextMessage(reqMessage);
      message.setJMSReplyTo(resQueue);

      QueueSender sender = session.createSender(reqQueue);
      sender.send(message);
      sender.close();

      done.acquire();
      assertNotNull("Expected response message", responseListener.resMessage);
      assertEquals(DOMUtils.parse(resMessage), DOMUtils.parse(responseListener.resMessage));
      
      con.stop();
      session.close();
      con.close();
   }

   public static class ResponseListener implements MessageListener
   {
      public String resMessage;
      
      public void onMessage(Message msg)
      {
         done.release();
         TextMessage textMessage = (TextMessage)msg;
         try
         {
            resMessage = textMessage.getText();
         }
         catch (Throwable t)
         {
            t.printStackTrace();
         }
      }
   }
}
