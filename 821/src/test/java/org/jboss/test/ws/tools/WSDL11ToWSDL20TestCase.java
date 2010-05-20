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

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.tools.sei.PrimitiveTypes;
import org.jboss.test.ws.tools.sei.StandardJavaTypes;
import org.jboss.test.ws.tools.validation.WSDLValidator;
import org.jboss.test.ws.tools.validation.WSDL11Validator;
import org.jboss.ws.jaxrpc.LiteralTypeMapping;
import org.jboss.ws.metadata.wsdl.WSDLDefinitionsFactory;
import org.jboss.ws.metadata.wsdl.WSDL20Writer;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.WSDLToJava;
import org.jboss.ws.tools.interfaces.WSDLToJavaIntf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Test jbossws WSDL11 -> Java -> WSDL20
 *
 * @author anil.saldhana@jboss.org
 * @since January 26,2005
 */
public class WSDL11ToWSDL20TestCase extends JBossWSTest
{
   /** Test a SEI that contains JAXRPC primitive types */
   public void testPrimitiveTypes() throws Exception
   {
      Class seiClass = PrimitiveTypes.class;
      WSDLDefinitions wsdlDefinitions = getWSDLDefinitions(seiClass, "PrimitiveTypesService_RPC_11.wsdl");
      String wsdlString = getWSDLAsString(wsdlDefinitions);
      //System.out.println(wsdlString);
      writeWSDL(wsdlDefinitions, "PrimitiveTypesService_RPC_20.wsdl");
   }

   /** Test a SEI that contains JAXRPC java standard types */
   public void testStandardJavaTypes() throws Exception
   {
      Class seiClass = StandardJavaTypes.class;
      WSDLDefinitions wsdlDefinitions = getWSDLDefinitions(seiClass, "StandardJavaTypesService_RPC_11.wsdl");
      String wsdlString = getWSDLAsString(wsdlDefinitions);
      //System.out.println(wsdlString);
      writeWSDL(wsdlDefinitions, "StandardJavaTypesService_RPC_20.wsdl");
   }

   /** Test a SEI that contains JAXRPC java standard types */
   public void testStandardDocJavaTypes() throws Exception
   {
      Class seiClass = StandardJavaTypes.class;
      WSDLDefinitions wsdlDefinitions = getWSDLDefinitions(seiClass, "StandardJavaTypesService_DOC_11.wsdl");
      String wsdlString = getWSDLAsString(wsdlDefinitions);
      //System.out.println(wsdlString);
      writeWSDL(wsdlDefinitions, "StandardJavaTypesService_DOC_20.wsdl");
   }

   /** Test a SEI that contains JAXRPC java standard types */
   public void testWSDLSpecExample() throws Exception
   {
      WSDLDefinitions wsdl = doWSDLTest("W3CExample_DOC_11.wsdl");
      //System.out.println(wsdlString);
      writeWSDL(wsdl, "W3CExample_DOC_20.wsdl");
   }

   private void writeWSDL(WSDLDefinitions wsdl, String fname) throws Exception
   {
      String wsdlWrite = "tools";

      FileWriter writer = new FileWriter(wsdlWrite + "/" + fname);
      new WSDL20Writer(wsdl).write(writer);
      writer.close();
   }

   private String getWSDLAsString(WSDLDefinitions wsdl) throws IOException
   {
      StringWriter strwr = new StringWriter();
      new WSDL20Writer(wsdl).write(strwr);
      return strwr.toString();
   }

   private WSDLDefinitions getWSDLDefinitions(Class seiClass, String wsdlFileName) throws Exception
   {
      File wsdlFile = new File("resources/wsdlfixture/" + wsdlFileName);
      assertTrue(wsdlFile.exists());

      WSDLDefinitionsFactory wsdlFactory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = wsdlFactory.parse(wsdlFile.toURL());

      WSDLValidator validator = new WSDL11Validator();
      if (validator.validate(seiClass, wsdlDefinitions) == false)
         System.err.println("FIXME: " + validator.getErrorList().toString());

      return wsdlDefinitions;
   }

   /**
    * The idea here is that the WSDLReader reads in the <definition> element
    * and looks at the default namespace. If it is wsdl-1.1, it delegates to
    * wsdl4j which parses the same wsdl again and builds the wsdl4j object graph.
    * TODO:Reading the wsdl4j graph and copying to jbossws/wsdl objects
    * @param wsdlFileName
    * @throws Exception
    */
   private WSDLDefinitions doWSDLTest(String wsdlFileName) throws Exception
   {
      File wsdlFile = new File("resources/wsdlfixture/" + wsdlFileName);
      assertTrue(wsdlFile.exists()); 
      WSDLToJavaIntf wsdljava = new WSDLToJava();
      wsdljava.setTypeMapping(new LiteralTypeMapping());
      return wsdljava.convertWSDL2Java(wsdlFile.toURL());
   }
}
