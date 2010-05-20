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
package org.jboss.test.ws.tools.jsr181.webservice;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.util.xml.DOMUtils;
import org.jboss.ws.Constants;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.JavaToWSDL;
import org.jboss.ws.utils.IOUtils;
import org.w3c.dom.Element;

/**
 * Test java to wsdl
 *
 * @author Thomas.Diesler@jboss.com
 */
public class JavaToWSDLTestCase extends JBossWSTest
{
   /** Test an ordanary unannotaded SEI */
   public void testSEI() throws Exception
   {
      System.out.println("FIXME: JBWS-550");
      if (true) return;
      JavaToWSDL javaToWSDL = new JavaToWSDL(Constants.NS_WSDL11);
      javaToWSDL.setTargetNamespace("http://www.openuri.org/2004/04/HelloWorld");
      javaToWSDL.setServiceName("TestService");

      WSDLDefinitions wsdlDefs = javaToWSDL.generate(EndpointInterface.class);
      assertNotNull(wsdlDefs);

      String fixFile = "resources/wsdlfixture/jsr181/webservice/TestService.wsdl";
      validate(wsdlDefs, fixFile);
   }

   /** Test an JSR-181 annotated JSE endpoint */
   public void testJSEBean01() throws Exception
   {
      System.out.println("FIXME: JBWS-550");
      if (true) return;
      JavaToWSDL javaToWSDL = new JavaToWSDL(Constants.NS_WSDL11);

      WSDLDefinitions wsdlDefs = javaToWSDL.generate(JSEBean01.class);
      assertNotNull(wsdlDefs);

      String fixFile = "resources/wsdlfixture/jsr181/webservice/TestService.wsdl";
      validate(wsdlDefs, fixFile);
   }

   /** Test an JSR-181 annotated SEI */
   public void testJSEBean03() throws Exception
   {
      System.out.println("FIXME: JBWS-550");
      if (true) return;
      JavaToWSDL javaToWSDL = new JavaToWSDL(Constants.NS_WSDL11);

      WSDLDefinitions wsdlDefs = javaToWSDL.generate(JSEBean03.class);
      assertNotNull(wsdlDefs);

      String fixFile = "resources/wsdlfixture/jsr181/webservice/TestService.wsdl";
      validate(wsdlDefs, fixFile);
   }

   private void validate(WSDLDefinitions wsdlDefs, String fixFile) throws Exception
   {
      File wsdlFile = new File("output/tools/generate/wsdl/" + getName() + ".wsdl");
      Writer writer = IOUtils.getCharsetFileWriter(wsdlFile, Constants.DEFAULT_XML_CHARSET);
      wsdlDefs.write(writer, Constants.DEFAULT_XML_CHARSET);
      writer.close();

      //Validate the generated WSDL
      Element exp = DOMUtils.parse(new File(fixFile).toURL().openStream());
      Element was = DOMUtils.parse(wsdlFile.toURL().openStream());

      assertEquals(exp, was, true);
   }
}
