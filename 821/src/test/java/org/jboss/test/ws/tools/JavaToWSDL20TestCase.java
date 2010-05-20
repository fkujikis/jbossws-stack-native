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
package org.jboss.test.ws.tools;

import java.io.File;
import java.io.FileWriter;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.jaxb.complex.ComplexTypes;
import org.jboss.test.ws.tools.sei.ArrayInterface;
import org.jboss.test.ws.tools.sei.CustomInterface;
import org.jboss.test.ws.tools.sei.PrimitiveTypes;
import org.jboss.test.ws.tools.sei.ServiceException;
import org.jboss.test.ws.tools.sei.StandardJavaTypes;
import org.jboss.ws.Constants;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLUtils;
import org.jboss.ws.tools.JavaToWSDL;
import org.jboss.ws.tools.WSToolsConstants;

/**
 * Test jbossws Java -> WSDL20
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-Oct-2004
 */
public class JavaToWSDL20TestCase extends JBossWSTest
{
   public void testShowJBWS()
   {
      System.out.println("FIXME: JBWS-509");
   }
//   /** Test a SEI that contains JAXRPC primitive types */
//   public void testPrimitiveTypes() throws Exception
//   {
//      Class seiClass = PrimitiveTypes.class;
//      doWSDLTest(seiClass);
//   }
//
//   /** Test a SEI that contains JAXRPC java standard types */
//   public void testStandardJavaTypes() throws Exception
//   {
//      Class seiClass = StandardJavaTypes.class;
//      doWSDLTest(seiClass);
//   }
//
//   /** Test a SEI that contains custom exceptions */
//   public void testCustomTypes() throws Exception
//   {
//      Class seiClass = CustomInterface.class;
//      doWSDLTest(seiClass);
//   }
//
//   /** Test a SEI that contains custom exceptions */
//   public void testExceptionTypes() throws Exception
//   {
//      Class seiClass = ServiceException.class;
//      doWSDLTest(seiClass);
//   }
//
//   /** Test a SEI that contains complex types */
//   public void testComplexTypes() throws Exception
//   {
//      Class seiClass = ComplexTypes.class;
//      doWSDLTest(seiClass);
//   }
//
//   /** Test a SEI that contains array types */
//   public void testArrayTypes() throws Exception
//   {
//      Class seiClass = ArrayInterface.class;
//      doWSDLTest(seiClass);
//   }
//
//   private void doWSDLTest(Class seiClass) throws Exception
//   {
//      this.setSystemProperties();
//      String wsdlDir = "tools/";
//      String sname = WSDLUtils.getInstance().getJustClassName(seiClass) + "Service";
//      String wsdlPath = wsdlDir+ "/" + sname + ".wsdl";
//      JavaToWSDL jwsdl = new JavaToWSDL(Constants.NS_WSDL20);
//      jwsdl.setServiceName(sname);
//      jwsdl.setTargetNamespace("http://org.jboss.ws/types");
//      jwsdl.addFeature(WSToolsConstants.WSTOOLS_FEATURE_RESTRICT_TO_TARGET_NS, true); //generate types to targetns
//      WSDLDefinitions wsdl = jwsdl.generate(seiClass);
//
//      FileWriter fw = new FileWriter(new File(wsdlPath));
//      wsdl.write(fw);
//      fw.close();
//
//      System.out.println("FIXME: JBWS-212");
//      //TODO:Have fixture files for wsdl 2.0
//      //Validate the generated WSDL
//      /*File wsdlfix = new File(fixturefile);
//      Element exp = DOMUtils.parse(wsdlfix.toURL().openStream());
//      File wsdlFile = new File(wsdlPath);
//      assertNotNull("Generated WSDL File exists?", wsdlFile);
//      Element was = DOMUtils.parse(wsdlFile.toURL().openStream());
//
//      assertEquals(exp, was);*/
//      /*
//      File wsdlFile = new File(config.getWsdlOutFile());
//      WSDLDefinitionsFactory wsdlFactory = WSDLDefinitionsFactory.newInstance();
//      WSDLDefinitions wsdl = wsdlFactory.parse(wsdlFile.toURL());
//
//      WSDLValidator validator = new WSDLValidator();
//      if (validator.validate(seiClass, wsdl) == false)
//         fail(validator.getErrorList().toString());
//         */
//   }
//   private void setSystemProperties()
//   {
//      //Set the XSDWriter to be used
//      System.setProperty("jbossws.xsdwriter","org.jboss.ws.wsdl.xmlschema.WSXSDWriter");
//      System.setProperty("jbossws.primitiveNillableFlag","false");
//   }
}
