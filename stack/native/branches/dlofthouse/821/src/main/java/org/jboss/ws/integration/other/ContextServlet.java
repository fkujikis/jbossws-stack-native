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
package org.jboss.ws.integration.other;

// $Id: ContextServlet.java 293 2006-05-08 16:31:50Z thomas.diesler@jboss.com $

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.server.ServiceEndpointManager;
import org.jboss.ws.server.ServiceEndpointManagerFactory;

/**
 * The servlet that that is associated with context /jbossws
 *
 * @author Thomas.Diesler@jboss.org
 * @since 21-Mar-2005
 */
public class ContextServlet extends HttpServlet
{
   // provide logging
   protected final Logger log = Logger.getLogger(ContextServlet.class);

   protected ServiceEndpointManager epManager;

   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      initServiceEndpointManager();
   }

   private void initServiceEndpointManager()
   {
      try
      {
         URL beansXML = new File(getServletContext().getRealPath("/META-INF/jboss-beans.xml")).toURL();
         if (beansXML == null)
            throw new IllegalStateException("Invalid null kernel deployment");

         new KernelBootstrap().bootstrap(beansXML);
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WSException("Cannot bootstrap kernel", ex);
      }

      // Initialize the ServiceEndpointManager
      ServiceEndpointManagerFactory factory = ServiceEndpointManagerFactory.getInstance();
      epManager = factory.getServiceEndpointManager();
   }

   /** Process GET requests.
    */
   public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      PrintWriter writer = res.getWriter();
      res.setContentType("text/html");

      writer.print("<html>");
      setupHTMLResponseHeader(writer);

      writer.print("<body>");
      writer.print(epManager.showServiceEndpointTable());
      writer.print("</body>");
      writer.print("</html>");
      writer.close();
   }

   private void setupHTMLResponseHeader(PrintWriter writer)
   {
      writer.println("<head>");
      writer.println("<meta http-equiv='Content-Type content='text/html; charset=iso-8859-1'>");
      writer.println("<title>JBossWS</title>");
      writer.println("<link rel='stylesheet' href='./styles.css'>");
      writer.println("</head>");
   }
}
