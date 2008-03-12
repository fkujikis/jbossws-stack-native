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
package org.jboss.test.ws.jaxws.fastinfoset;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service21;

import junit.framework.Test;

import org.jboss.ws.feature.FastInfosetFeature;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test FastInfoset functionality
 *
 * @author Thomas.Diesler@jboss.com
 * @since 12-Mar-2008
 */
public class FastInfosetTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(FastInfosetTestCase.class, "jaxws-fastinfoset.war");
   }

   public void testSimple() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-fastinfoset?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/fastinfoset", "FastInfosetEndpointService");
      Service21 service = Service21.create(wsdlURL, serviceName);

      FastInfosetFeature feature = new FastInfosetFeature();
      FastInfoset port = service.getPort(FastInfoset.class, feature);
      String retStr = port.echo("hello world");
      assertEquals("hello world", retStr);
   }
}
