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
import org.jboss.rs.model.ResourceModel;
import org.jboss.rs.model.ResourceModelFactory;
import org.jboss.rs.model.ResourceResolver;
import org.jboss.rs.model.ResourceMethod;

import java.util.List;

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
      ResourceModel root = ResourceModelFactory.createModel(WidgetList.class);
      registry.addResourceModelForContext("/rest", root);
      rootModels = registry.getResourceModelsForContext("/rest");
   }

   public void testRegexResolver1() throws Exception
   {
      ResourceResolver resolver = new ResourceResolver();
      ResourceMethod method = resolver.resolve(rootModels, "widgets/Id/spec");

      assertNotNull(method);
      assertEquals(method.getUriTemplate(), "spec");
   }

   public void testRegexResolver2() throws Exception
   {
      ResourceResolver resolver = new ResourceResolver();

      ResourceMethod method = resolver.resolve(rootModels, "widgets/special");

      assertNotNull(method);
      assertEquals(method.getUriTemplate(), "special");
   }

   public void testRegexResolver3() throws Exception
   {
      ResourceResolver resolver = new ResourceResolver();

      ResourceMethod method = resolver.resolve(rootModels, "widgets/offers");

      assertNotNull(method);
      assertEquals(method.getUriTemplate(), "offers");
      assertTrue(method.getEntityModel().getImplementation().equals(WidgetList.class));
   }

   public void testRegexResolver4() throws Exception
   {
      ResourceResolver resolver = new ResourceResolver();

      ResourceMethod method = resolver.resolve(rootModels, "widgets/Id/spec/SpecName");

      assertNotNull(method);                 
      assertEquals(method.getUriTemplate(), "spec/{name}");
      assertTrue(method.getEntityModel().getImplementation().equals(Specification.class));
   }

}
