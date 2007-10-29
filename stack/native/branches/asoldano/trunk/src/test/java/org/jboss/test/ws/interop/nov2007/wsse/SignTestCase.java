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
package org.jboss.test.ws.interop.nov2007.wsse;

import javax.xml.namespace.QName;

import junit.framework.Test;

import org.jboss.ws.core.StubExt;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * WCF Interoperability Plug-fest - November 2007
 * 
 * Scenario 3.2: X509 Mutual Authentication, Sign Only
 * 
 * Client and Server are authenticated and messages integrity are provided by using Asymmetric Binding
 * from Security Policy with server X509 certificate used as Recepient Token and client X509 certificate
 * used as Initiator Token. Only SignedParts assertion is present in the corresponding policy,
 * indicating that the Body of the message must be signed.
 * 
 * SOAP Version:        1.1
 * Addressing:          No
 * Client Certificate:  Alice
 * Server Certificate:  Bob
 * Timestamp:           Yes
 * Signed Parts:        Body and Timestamp.
 * Canonicalization:    XML-EXC-C14N
 * Signature:           SHA1
 * 
 * 
 * @author Alessio Soldano <alessio.soldano@jboss.com>
 * 
 * @version $Id$
 * @since 27-Oct-2007
 */
public class SignTestCase extends AbstractWSSEBase
{

   public static Test suite()
   {
      addClientConfToClasspath("jbossws-interop-nov2007-wsseSign-client.jar");
      return new JBossWSTestSetup(SignTestCase.class, "jbossws-interop-nov2007-wsseSign.war");
   }

   public void testScenario() throws Exception
   {
      String text = "Hello!";
      String result = port.ping(text);
      assertNotNull(result);
      assertEquals(text, result);
   }

   @Override
   protected void scenarioSetup(IPingService port)
   {
      ((StubExt)port).setConfigName("Standard WSSecurity Client");
      
      System.setProperty("org.jboss.ws.wsse.keyStore", "resources/interop/nov2007/wsse/shared/META-INF/alice-sign.jks");
      System.setProperty("org.jboss.ws.wsse.trustStore", "resources/interop/nov2007/wsse/shared/META-INF/wsse10.truststore");
      System.setProperty("org.jboss.ws.wsse.keyStorePassword", "password");
      System.setProperty("org.jboss.ws.wsse.trustStorePassword", "password");
      System.setProperty("org.jboss.ws.wsse.keyStoreType", "jks");
      System.setProperty("org.jboss.ws.wsse.trustStoreType", "jks");
   }

   @Override
   protected QName getScenarioPortQName()
   {
      return new QName("http://tempuri.org/", "MutualCertificate10Sign_IPingService_port");
   }

}
