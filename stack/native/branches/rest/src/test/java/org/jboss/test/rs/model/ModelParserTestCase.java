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
import org.jboss.rs.model.ResourceModelParser;
import org.jboss.rs.model.ResourceModel;
import org.jboss.rs.model.ResourceMethod;
import org.jboss.rs.MethodHTTP;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class ModelParserTestCase extends TestCase
{
   public void testParseMetaModel() throws Exception
   {
      ResourceModel root = ResourceModelParser.newInstance().parse(WidgetList.class);

      ResourceMethod match = null;
      for(ResourceMethod m : root.getSubResourceMethods())
      {
         if(m.getMethodHTTP() == MethodHTTP.POST
           && m.getUriTemplate().equals("special"))
         {
            match = m;
            break;
         }
      }

      assertNotNull(match);
   }
}
