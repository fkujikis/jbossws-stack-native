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
package org.jboss.test.ws.jaxws.wsrm.emulator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Endpoint emulator
 *
 * @author richard.opalka@jboss.com
 *
 * @since Oct 24, 2007
 */
public class EndpointEmulator extends HttpServlet
{
   private static final String ADDR_NS = "http://www.w3.org/2005/08/addressing";
   private static final Map<String, String> wsrmActions = new HashMap<String, String>();
   private static final String CREATE_SEQUENCE_ACTION = "http://docs.oasis-open.org/ws-rx/wsrm/200702/CreateSequence";
   private static final String CREATE_SEQUENCE_RESPONSE_ACTION = "http://docs.oasis-open.org/ws-rx/wsrm/200702/CreateSequenceResponse";
   private static final String CLOSE_SEQUENCE_ACTION = "http://docs.oasis-open.org/ws-rx/wsrm/200702/CloseSequence";
   private static final String CLOSE_SEQUENCE_RESPONSE_ACTION = "http://docs.oasis-open.org/ws-rx/wsrm/200702/CloseSequenceResponse";
   private static final String TERMINATE_SEQUENCE_ACTION = "http://docs.oasis-open.org/ws-rx/wsrm/200702/TerminateSequence";
   private static final String TERMINATE_SEQUENCE_RESPONSE_ACTION = "http://docs.oasis-open.org/ws-rx/wsrm/200702/TerminateSequenceResponse";
   private static final Random generator = new Random();
   
   static
   {
      wsrmActions.put(CREATE_SEQUENCE_ACTION, CREATE_SEQUENCE_RESPONSE_ACTION);
      wsrmActions.put(CLOSE_SEQUENCE_ACTION, CLOSE_SEQUENCE_RESPONSE_ACTION);
      wsrmActions.put(TERMINATE_SEQUENCE_ACTION, TERMINATE_SEQUENCE_RESPONSE_ACTION);
   }
   
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
   throws ServletException, IOException
   {
      resp.setContentType("text/xml");
      PrintWriter writer = resp.getWriter();
      String wsdlFile = getResource("WEB-INF/resources/ReqResService.wsdl"); 
      writer.print(wsdlFile);
      writer.flush();
      writer.close();
   }
   
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp)
   throws ServletException, IOException
   {
      resp.setContentType("text/xml");
      PrintWriter writer = resp.getWriter();
      Properties properties = getAddressingProperties(getRequestMessage(req));
      String response = getResource("WEB-INF/resources/echoResponse.xml");
      if (properties.get("addressing.action").equals(CREATE_SEQUENCE_ACTION))
         response = getResource("WEB-INF/resources/createSequenceResponse.xml");
      response = modifyResponse(response, properties);
      writer.print(response);
      writer.flush();
      writer.close();
   }
   
   private String modifyResponse(String response, Properties props)
   {
      response = replace("${addressing.to}", props.getProperty("addressing.replyto"), response);
      response = replace("${addressing.relatesto}", props.getProperty("addressing.messageid"), response);
      String action = props.getProperty("addressing.action");
      if (wsrmActions.containsKey(action))
      {
         if (action.equals(CREATE_SEQUENCE_ACTION))
         {
            String sequenceId = "http://wsrm.emulator.jboss/sequence/generated/" + generator.nextInt(Integer.MAX_VALUE);
            response = replace("${messaging.identifier}", sequenceId, response);
         }
         action = wsrmActions.get(action);
      }
      response = replace("${addressing.action}", action, response);
      return response;
   }
   
   private static String replace(String oldString, String newString, String data)
   {
      int fromIndex = 0;
      int index = 0;
      StringBuffer result = new StringBuffer();
      
      while ((index = data.indexOf(oldString, fromIndex)) >= 0)
      {
         result.append(data.substring(fromIndex, index));
         result.append(newString);
         fromIndex = index + oldString.length();
      }
      result.append(data.substring(fromIndex));
      return result.toString();
   }
   
   private Properties getAddressingProperties(String message) throws IOException
   {
      try
      {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document document = builder.parse(new ByteArrayInputStream(message.getBytes()));
         Properties retVal = new Properties();
         String to = document.getElementsByTagNameNS(ADDR_NS, "To").item(0).getTextContent().trim();
         retVal.put("addressing.to", to);
         String messageId = document.getElementsByTagNameNS(ADDR_NS, "MessageID").item(0).getTextContent().trim();
         retVal.put("addressing.messageid", messageId);
         String action = document.getElementsByTagNameNS(ADDR_NS, "Action").item(0).getTextContent().trim();
         retVal.put("addressing.action", action);
         String replyTo = ((Element)document.getElementsByTagNameNS(ADDR_NS, "ReplyTo").item(0))
            .getElementsByTagNameNS(ADDR_NS, "Address").item(0).getTextContent().trim();
         retVal.put("addressing.replyto", replyTo);
         System.out.println("Properties from message: " + retVal);
         return retVal;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new IOException(e.getMessage());
      }
   }
   
   private String getResource(String resource) throws IOException
   {
      return toString(getServletContext().getResourceAsStream(resource));
   }

   private String getRequestMessage(HttpServletRequest req) throws IOException
   {
      BufferedReader reader = req.getReader();
      String line = null;
      StringBuilder sb = new StringBuilder();
      while ((line = reader.readLine()) != null)
      {
         sb.append(line);
      }
      return sb.toString();
   }

   private String toString(InputStream is) throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int offset = -1;
      while ((offset = is.read(buffer, 0, buffer.length)) != -1)
      {
         baos.write(buffer, 0, offset);
      }
      return baos.toString();
   }

}