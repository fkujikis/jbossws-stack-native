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
package org.jboss.test.ws.jbws871;

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Arrays with JSR181 endpoints
 *
 * http://jira.jboss.com/jira/browse/JBWS-871
 *
 * @author Thomas.Diesler@jboss.com
 * @since 30-Apr-2006
 */
public class JBWS871TestCase extends JBossWSTest
{
   private static RpcArrayEndpoint endpoint;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JBWS871TestCase.class, "jbossws-jbws871-rpc.war, jbossws-jbws871-rpc-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (endpoint == null)
      {
         InitialContext iniCtx = getInitialContext();
         RpcArrayEndpointService service = (RpcArrayEndpointService)iniCtx.lookup("java:comp/env/service/RpcArrayEndpointService");
         endpoint = service.getRpcArrayEndpointPort();
      }
   }

   public void testEchoNullArray() throws Exception
   {
      Integer[] outArr = endpoint.intArr("null", null);
      assertNull(outArr);
   }

   public void testEchoEmptyArray() throws Exception
   {
      Integer[] outArr = endpoint.intArr("empty", new Integer[]{});
      assertEquals(0, outArr.length);
   }

   public void testEchoSingleValueArray() throws Exception
   {
      Integer[] outArr = endpoint.intArr("single", new Integer[] {new Integer(1)} );
      assertEquals(1, outArr.length);
      assertEquals(new Integer(1), outArr[0]);
   }

   public void testEchoMultipleValueArray() throws Exception
   {
      Integer[] outArr = endpoint.intArr("multi", new Integer[] { new Integer(1), new Integer(2), new Integer(3) });
      assertEquals(3, outArr.length);
      assertEquals(new Integer(1), outArr[0]);
      assertEquals(new Integer(2), outArr[1]);
      assertEquals(new Integer(3), outArr[2]);
   }
}
