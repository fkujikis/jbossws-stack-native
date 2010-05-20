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
package org.jboss.test.ws.soap;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.ws.jaxrpc.Style;
import org.jboss.ws.soap.MessageFactoryImpl;
import org.jboss.ws.soap.SOAPElementImpl;
import org.jboss.ws.soap.SAAJElementWriter;
import org.jboss.util.xml.DOMWriter;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import java.io.*;

/**
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @version $Id$
 * @since Aug 4, 2006
 */
public class SAAJElementWriterTestCase extends JBossWSTest {

   String envStr =
       "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
           " <env:Body>" +
           "  <businessList generic='2.0' operator='JBOSS' xmlns='urn:uddi-org:api_v2'>" +
           "   <businessInfos>" +
           "    <businessInfo businessKey='892ac280-c16b-11d5-85ad-801eef211111'>" +
           "     <name xml:lang='en'>Demi Credit</name>" +
           "     <description xml:lang='en'>A smaller demo app used for illustrating UDDI inquiry.</description>" +
           "     <serviceInfos>" +
           "      <serviceInfo businessKey='9a26b6e0-c15f-11d5-85a3-801eef208714' serviceKey='860eca90-c16d-11d5-85ad-801eef208714'>" +
           "       <name xml:lang='en'>DCAmail</name>" +
           "      </serviceInfo>" +
           "     </serviceInfos>" +
           "    </businessInfo>" +
           "   </businessInfos>" +
           "  </businessList>" +
           " </env:Body>" +
           "</env:Envelope>";

   public void testWriterDocLit() throws Exception {

      for(int i=1; i<10; i++)
      {
         String fileName = "resources/soap/req" + i + ".xml";
         System.out.println("\nTesting " + fileName);

         File source = new File(fileName);
         InputStream inputStream = new BufferedInputStream(new FileInputStream(source));

         MessageFactoryImpl factory = new MessageFactoryImpl();
         factory.setStyle(Style.DOCUMENT);
         SOAPMessage soapMsg = factory.createMessage(null, inputStream);
         SOAPEnvelope env = soapMsg.getSOAPPart().getEnvelope();

         try
         {
            String saajMarshalled = marshall(env);
            String domMarshalled = verify(saajMarshalled);

            System.out.println(saajMarshalled);
            System.out.println("");
            System.out.println(domMarshalled);

         }
         catch (Exception e)
         {
            System.err.println(fileName + " FAILED:");
            System.err.println(e.getMessage());
            fail(e.getMessage());
         }

      }
   }

   public void testWriterRPC() throws Exception {

      for(int i=1; i<10; i++)
      {
         String fileName = "resources/soap/req" + i + ".xml";
         //System.out.println("Testing " + fileName);

         File source = new File(fileName);
         InputStream inputStream = new BufferedInputStream(new FileInputStream(source));

         MessageFactoryImpl factory = new MessageFactoryImpl();
         factory.setStyle(Style.RPC);
         SOAPMessage soapMsg = factory.createMessage(null, inputStream);
         SOAPEnvelope env = soapMsg.getSOAPPart().getEnvelope();

         String xml = marshall(env);
         verify(xml);
      }
  }

    public void testWriterMessage() throws Exception {

       for(int i=1; i<10; i++)
       {
          String fileName = "resources/soap/req" + i + ".xml";
          //System.out.println("Testing " + fileName);

          File source = new File(fileName);
          InputStream inputStream = new BufferedInputStream(new FileInputStream(source));

          MessageFactoryImpl factory = new MessageFactoryImpl();
          factory.setStyle(null);
          SOAPMessage soapMsg = factory.createMessage(null, inputStream);
          SOAPEnvelope env = soapMsg.getSOAPPart().getEnvelope();

          String xml = marshall(env);
          verify(xml);
       }
   }

   private String marshall(SOAPEnvelope env) throws Exception
   {
      String xml = SAAJElementWriter.printSOAPElement((SOAPElementImpl)env, true);
      //System.out.println(xml);
      //System.out.println("");
      return xml;
   }

   private String verify(String xml) throws Exception {
      ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());

      MessageFactoryImpl factory = new MessageFactoryImpl();
      factory.setStyle(Style.RPC);
      SOAPMessage soapMsg = factory.createMessage(null, inputStream);
      SOAPEnvelope env = soapMsg.getSOAPPart().getEnvelope();

      return DOMWriter.printNode(env, true);
   }
}
