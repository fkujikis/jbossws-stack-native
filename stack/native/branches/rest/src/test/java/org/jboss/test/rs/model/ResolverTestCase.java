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
package org.jboss.test.rs.model;

import junit.framework.TestCase;
import org.jboss.rs.ResourceRegistry;
import org.jboss.rs.MethodHTTP;
import org.jboss.rs.runtime.RuntimeContext;
import org.jboss.rs.model.ResourceModel;
import org.jboss.rs.model.ResourceModelParser;
import org.jboss.rs.model.StatefulResourceResolver;
import org.jboss.rs.model.ResourceMethod;
import org.jboss.test.rs.WidgetList;

import java.util.List;
import java.lang.reflect.Method;
import java.net.URI;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class ResolverTestCase extends TestCase
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

   public void testRegexResolver1() throws Exception
   {
      URI uri = new URI("/rest/widgets/Id/spec");
      RuntimeContext context = defaultRuntimeContext(MethodHTTP.GET, uri);
      StatefulResourceResolver resolver = StatefulResourceResolver.newInstance(context);
      ResourceMethod method = resolver.resolve();

      assertNotNull(method);
      assertEquals("spec", method.getUriTemplate());
   }

   public void testRegexResolver2() throws Exception
   {
      URI uri = new URI("/rest/widgets/special");
      RuntimeContext context = defaultRuntimeContext(MethodHTTP.PUT, uri);
      context.parseContentTypeHeader("text/xml");
      
      StatefulResourceResolver resolver = StatefulResourceResolver.newInstance(context);

      ResourceMethod method = resolver.resolve();

      assertNotNull(method);
      assertEquals("special", method.getUriTemplate());
   }

   public void testRegexResolver3() throws Exception
   {
      URI uri = new URI("/rest/widgets/offers");
      RuntimeContext context = defaultRuntimeContext(MethodHTTP.GET, uri);
      StatefulResourceResolver resolver = StatefulResourceResolver.newInstance(context);

      ResourceMethod method = resolver.resolve();

      assertNotNull(method);
      assertEquals("offers", method.getUriTemplate());
   }

   public void testRegexResolver4() throws Exception
   {
      URI uri = new URI("/rest/widgets/Id/spec/SpecName");
      RuntimeContext context = defaultRuntimeContext(MethodHTTP.GET, uri);
      StatefulResourceResolver resolver = StatefulResourceResolver.newInstance(context);

      ResourceMethod method = resolver.resolve();

      assertNotNull(method);
      assertEquals("spec/{name}", method.getUriTemplate());
   }

   public void testRegexResolver5() throws Exception
   {
      URI uri = new URI("/rest/widgets");
      RuntimeContext context = defaultRuntimeContext(MethodHTTP.GET, uri);
      context.parseAcceptHeader("text/plain");

      StatefulResourceResolver resolver = StatefulResourceResolver.newInstance(context);
      ResourceMethod method = resolver.resolve();

      assertNotNull(method);
      Method target = method.getInvocationTarget();
      String result = (String)target.invoke( target.getDeclaringClass().newInstance());
      assertEquals("A widgetlist", result);
   }

   private RuntimeContext defaultRuntimeContext(MethodHTTP method, URI uri)
   {
      RuntimeContext context = new RuntimeContext(method, uri, rootModels );
      context.parseAcceptHeader("text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
      return context;
   }

}
