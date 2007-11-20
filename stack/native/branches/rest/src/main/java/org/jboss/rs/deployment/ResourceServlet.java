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
package org.jboss.rs.deployment;

import org.jboss.rs.MethodHTTP;
import org.jboss.rs.ResourceError;
import org.jboss.rs.ResourceRegistry;
import org.jboss.rs.ResourceRegistryFactory;
import org.jboss.rs.model.ResourceMethod;
import org.jboss.rs.model.ResourceModel;
import org.jboss.rs.model.ResourceResolver;
import org.jboss.rs.model.ResourceLocator;
import org.jboss.rs.runtime.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class ResourceServlet extends HttpServlet
{
   
   private List<ResourceModel> rootResources = new ArrayList<ResourceModel>();

   public void init(ServletConfig servletConfig) throws ServletException
   {
      ResourceRegistry reg = ResourceRegistryFactory.newInstance().createResourceRegistry();
      String webContext = servletConfig.getServletContext().getContextPath();
      List<ResourceModel> models = reg.getResourceModelsForContext(webContext);

      if(models.isEmpty())
         throw new IllegalArgumentException("No root resources for context " + webContext);
      else
         rootResources.addAll(models);

   }

   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      invokeFrom(MethodHTTP.GET, req, res);
   }

   protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      invokeFrom(MethodHTTP.POST, req, res);
   }

   protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      invokeFrom(MethodHTTP.PUT, req, res);
   }

   protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      invokeFrom(MethodHTTP.DELETE, req, res);   
   }

   private void invokeFrom(MethodHTTP method, HttpServletRequest req, HttpServletResponse res)
      throws ServletException
   {
      try
      {
         // construct a runtime context
         URI uri = new URI(req.getRequestURI());
         RuntimeContext rt = new RuntimeContext(method, uri, rootResources);         
         parseAcceptHeader(req, rt);

         // locate the resource to be invoked
         ResourceResolver resolver = ResourceResolver.newInstance(rt);
         ResourceMethod resourceMethod = resolver.resolve();

         // evaluate locator stack
         Object subResourceInstance = null;
         Stack<ResourceLocator> visitedLocators = resolver.getVisitedLocator();
         while(!visitedLocators.isEmpty())
         {
            ResourceLocator loc = visitedLocators.pop();            
            rt.setWorkingPath(resolver.getLocatorWorkingPath(loc));

            InvocationBuilder builder = new DefaultInvocationBuilder();
            builder.addInvocationModel(loc.getParameterBinding());
            builder.addInvocationModel(loc.getOperationBinding());
            Invocation locatorInvocation = builder.build(rt);

            InvocationHandler bridgeInvoker = new DefaultInvocationHandler();
            subResourceInstance = bridgeInvoker.invoke(locatorInvocation);

         }

         // create an Invocation instance
         InvocationBuilder builder = new DefaultInvocationBuilder();
         rt.setWorkingPath(resolver.getMethodWorkingPath());
         
         if(subResourceInstance!=null)
            builder.addInvocationModel(new PresetInvocationTarget(subResourceInstance));
                  
         builder.addInvocationModel( resourceMethod.getParameterBinding() );
         builder.addInvocationModel( resourceMethod.getOperationBinding() );
         Invocation invocation = builder.build(rt);

         // invoke it
         InvocationHandler invoker = new DefaultInvocationHandler();
         Object result = invoker.invoke(invocation);

         if(result instanceof String)
         {
            res.setContentType("text/plain");
            PrintWriter writer = res.getWriter();
            writer.write(result.toString());
            writer.flush();
            writer.close();
         }
         else
         {
            serverError(405, "Cannot marshall " + result.getClass(), res);
         }

      }
      catch(ResourceError resourceError)
      {
         serverError(resourceError.status, resourceError.getMessage(), res);
      }
      catch (Throwable e)
      {
         throw new ServletException(e);
      }
   }

   private void parseAcceptHeader(HttpServletRequest req, RuntimeContext rt) throws ServletException {
      String requestAccept = req.getHeader("Accept");
      if(requestAccept!=null)
            rt.parseAcceptHeader("text/plain, text/html");
      else
         throw new ServletException("Accept header is missing");
   }

   private void serverError(int status, String message, HttpServletResponse res)
   {
      try
      {
         res.setStatus(status);
         PrintWriter out = res.getWriter();
         out.write(message);
         out.flush();
         out.close();
      }
      catch (IOException e)
      {
         System.out.println("Failed to write response:" +  e.getMessage());
      }
   }
}
