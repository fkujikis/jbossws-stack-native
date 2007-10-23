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
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * RM endpoint emulator
 *
 * @author richard.opalka@jboss.com
 *
 * @since Oct 23, 2007
 */
public class RMEndpointEmulator extends HttpServlet
{
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
   throws ServletException, IOException
   {
      doPost(req, resp);
   }

   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp)
   throws ServletException, IOException
   {
      String requestMessage = getRequestMessage(req);
      String responseMessage = getResponseMessage(requestMessage);
      resp.setContentType("text/xml");
      PrintWriter out = resp.getWriter(); 
      out.write(responseMessage);
      out.flush();
      out.close();
   }
   
   private String getResponseMessage(String requestMessage)
   {
      return "";
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
}
