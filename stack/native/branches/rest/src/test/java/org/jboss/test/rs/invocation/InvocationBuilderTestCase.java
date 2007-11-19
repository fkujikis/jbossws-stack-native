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
package org.jboss.test.rs.invocation;

import junit.framework.TestCase;
import org.jboss.rs.ResourceRegistry;
import org.jboss.rs.MethodHTTP;
import org.jboss.rs.runtime.RuntimeContext;
import org.jboss.rs.runtime.InvocationBuilder;
import org.jboss.rs.runtime.DefaultInvocationBuilder;
import org.jboss.rs.runtime.Invocation;
import org.jboss.rs.runtime.InvocationHandler;
import org.jboss.rs.model.ResourceModel;
import org.jboss.rs.model.ResourceModelParser;
import org.jboss.rs.model.ResourceResolver;
import org.jboss.rs.model.ResourceMethod;
import org.jboss.test.rs.WidgetList;

import javax.ws.rs.core.HttpHeaders;
import java.util.List;
import java.net.URI;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class InvocationBuilderTestCase extends TestCase
{
   ResourceRegistry registry;
   List<ResourceModel> rootModels;

   protected void setUp() throws Exception
   {
      this.registry = new ResourceRegistry();
      ResourceModel root = ResourceModelParser.newInstance().parse(WidgetList.class);
      registry.addResourceModelForContext("/rest", root);
      rootModels = registry.getResourceModelsForContext("/rest");
   }

   public void testUriParamBinding() throws Exception
   {
      URI uri = new URI("/rest/widgets/Foo/spec/Bar");
      RuntimeContext context = defaultRuntimeContext(MethodHTTP.GET, uri);
      ResourceResolver resolver = ResourceResolver.newInstance(context);

      ResourceMethod method = resolver.resolve();

      assertNotNull(method);
      assertEquals("spec/{name}", method.getUriTemplate());

      // setup a builder
      InvocationBuilder builder = new DefaultInvocationBuilder();
      builder.addInvocationModel(method.getParameterBinding());

      // create an Invocation instance
      Invocation invocation = builder.build(context);
      Object parameterInstance = invocation.getParameterInstances().get(0);
      assertTrue(parameterInstance!=null);
      assertTrue("Wildcard parameter {name} not bound", parameterInstance.equals("Bar"));
           
   }

   public void testHttpContextParamBinding() throws Exception
   {
      URI uri = new URI("/rest/widgets/special");
      RuntimeContext context = defaultRuntimeContext(MethodHTTP.POST, uri);
      context.parseContentTypeHeader("text/xml");
      ResourceResolver resolver = ResourceResolver.newInstance(context);

      ResourceMethod method = resolver.resolve();

      assertNotNull(method);
      assertEquals("special", method.getUriTemplate());

      // setup a builder
      InvocationBuilder builder = new DefaultInvocationBuilder();
      builder.addInvocationModel(method.getParameterBinding());

      // create an Invocation instance
      Invocation invocation = builder.build(context);
      Object parameterInstance = invocation.getParameterInstances().get(0);
      assertTrue(parameterInstance!=null);
      assertTrue("HttpContext parameter not bound", parameterInstance instanceof HttpHeaders);

   }

   private RuntimeContext defaultRuntimeContext(MethodHTTP method, URI uri)
   {
      RuntimeContext context = new RuntimeContext(method, uri, rootModels );
      context.parseAcceptHeader("*/*");
      return context;
   }
}
