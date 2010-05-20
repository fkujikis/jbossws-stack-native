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
package org.jboss.test.ws.wsse;

import java.rmi.RemoteException;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Test WS-Security with RPC/Literal
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision$
 */
public class SimpleSignEncryptTestCase extends JBossWSTest
{

   /** Construct the test case with a given name
    */

   /** Deploy the test */
   public static Test suite() throws Exception
   {
      return JBossWSTestSetup.newTestSetup(SimpleSignEncryptTestCase.class, "jbossws-wsse-simple-sign-encrypt.war, jbossws-wsse-simple-sign-encrypt-client.jar");
   }

   /**
    * Test JSE endpoint
    */
   public void testEndpoint() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      Service service = (Service)iniCtx.lookup("java:comp/env/service/HelloService");
      Hello hello = (Hello)service.getPort(Hello.class);

      UserType in0 = new UserType("Kermit");
      UserType retObj = hello.echoUserType(in0);
      assertEquals(in0, retObj);
   }

   /**
    * Test JSE endpoint
    */
   public void testEndpointNoProperties() throws Exception
   {
      System.clearProperty("org.jboss.ws.wsse.keyStore");
      System.clearProperty("org.jboss.ws.wsse.trustStore");
      System.clearProperty("org.jboss.ws.wsse.keyStorePassword");
      System.clearProperty("org.jboss.ws.wsse.trustStorePassword");
      System.clearProperty("org.jboss.ws.wsse.keyStoreType");
      System.clearProperty("org.jboss.ws.wsse.trustStoreType");

      InitialContext iniCtx = getInitialContext();
      Service service = (Service)iniCtx.lookup("java:comp/env/service/HelloService");
      Hello hello = (Hello)service.getPort(Hello.class);

      UserType in0 = new UserType("Kermit");

      try
      {
         hello.echoUserType(in0);
         fail("Expected exception not thrown");
      }
      catch (RemoteException e)
      {
      }

   }
}
