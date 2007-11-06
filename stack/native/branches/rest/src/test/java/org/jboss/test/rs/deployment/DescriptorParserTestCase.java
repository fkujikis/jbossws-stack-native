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
package org.jboss.test.rs.deployment;

import junit.framework.TestCase;
import org.jboss.rs.model.dd.DeploymentDescriptorParser;
import org.jboss.rs.model.dd.JbossrsType;
import org.jboss.rs.model.dd.ResourceType;

import java.io.ByteArrayInputStream;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class DescriptorParserTestCase extends TestCase
{
   private final static String DD = "<?xml version='1.0' encoding='UTF-8'?>\n"+
     "<jbossrs xmlns='http://org.jboss.rs/'>"+
     "   <resource>"+
     "      <name>SampleEndpoint</name>"+
     "      <implementation>org.jboss.test.rs.WidgetList</implementation>"+
     "   </resource>"+
     "</jbossrs>";

   public void testReadDescriptor() throws Exception
   {
      JbossrsType dd = DeploymentDescriptorParser.read( new ByteArrayInputStream(DD.getBytes()));
      assertNotNull(dd);
      assertTrue(dd.getResource().size()==1);

      ResourceType resource = dd.getResource().get(0);
      assertTrue(resource.getName().equals("SampleEndpoint"));
      assertTrue(resource.getImplementation().equals("org.jboss.test.rs.WidgetList"));
   }

   public void testWriteDescriptor() throws Exception
   {
      JbossrsType dd = DeploymentDescriptorParser.read( new ByteArrayInputStream(DD.getBytes()));
      ResourceType resource = new ResourceType();
      resource.setImplementation("a.b.c.class");
      resource.setName("FooBarResource");

      dd.getResource().add(resource);

      DeploymentDescriptorParser.write(dd, System.out);
   }
}
