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
package org.jboss.test.ws.samples.wssecurity;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.HandlerRegistry;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.jaxrpc.ServiceFactoryImpl;
import org.jboss.ws.jaxrpc.ServiceImpl;
import org.jboss.ws.wsse.WSSecurityHandlerOutbound;

/**
 * This test simulates simulates the usage of a jboss-ws-security keystore and truststore use cases
 *
 * @author <a href="mailto:magesh.bojan@jboss.com">Magesh Kumar B</a>
 * @version $Revision$
 */
public class StorePassEncryptTestCase extends JBossWSTest
{
   /** Construct the test case with a given name
    */

   /** Deploy the test */
   public static Test suite() throws Exception
   {
      return JBossWSTestSetup.newTestSetup(StorePassEncryptTestCase.class, "jbossws-samples-store-pass-encrypt.war, jbossws-samples-store-pass-encrypt-client.jar");
   }

   /**
    * Test JSE endpoint
    */
   public void testEndpoint() throws Exception
   {
      Hello hello = getPort();

      UserType in0 = new UserType("Kermit");
      UserType retObj = hello.echoUserType(in0);
      assertEquals(in0, retObj);
   }

   private Hello getPort() throws Exception
   {
      if (isTargetServerJBoss())
      {
         InitialContext iniCtx = getInitialContext();
         Service service = (Service)iniCtx.lookup("java:comp/env/service/HelloService");
         Hello port = (Hello)service.getPort(Hello.class);
         return port;
      }
      else
      {
         try
         {
            ServiceFactoryImpl factory = new ServiceFactoryImpl();
            URL wsdlURL = new File("resources/samples/wssecurity/WEB-INF/wsdl/HelloService.wsdl").toURL();
            URL mappingURL = new File("resources/samples/wssecurity/WEB-INF/jaxrpc-mapping.xml").toURL();
            URL securityURL = new File("resources/samples/wssecurity/store-pass-encrypt/META-INF/jboss-wsse-client.xml").toURL();

            QName serviceName = new QName("http://org.jboss.ws/samples/wssecurity", "HelloService");
            QName portName = new QName("http://org.jboss.ws/samples/wssecurity", "HelloPort");
            ServiceImpl service = (ServiceImpl)factory.createService(wsdlURL, serviceName, mappingURL, securityURL);

            HandlerRegistry registry = service.getDynamicHandlerRegistry();
            List infos = registry.getHandlerChain(portName);
            infos.add(new HandlerInfo(WSSecurityHandlerOutbound.class, new HashMap(), new QName[]{}));
            registry.setHandlerChain(portName, infos);

            Hello port = (Hello)service.getPort(Hello.class);
            ((Stub)port)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, "http://" + getServerHost() + ":8080/jbossws-samples-store-pass-encrypt");
            return port;
         }
         catch (Exception e)
         {
            System.out.println("Exception is : " + e);
            e.printStackTrace();
            throw e;
         }
      }
   }
}
