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
package org.jboss.test.ws.tools.xsdjava;

import java.io.File;

import org.jboss.test.ws.tools.WSToolsBase;
import org.jboss.test.ws.tools.fixture.JBossSourceComparator;
import org.jboss.ws.core.jaxrpc.LiteralTypeMapping;
import org.jboss.ws.tools.XSDToJava;
import org.jboss.ws.tools.interfaces.XSDToJavaIntf;

/** Testcase that generates XSD -> Java Types
 *  Then uses the JBossSourceComparator to check the
 *  Java Types against the ones generated by wscompile
 *  [JBWS-147] XSDSchema to Java comprehensive test collection
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since  Mar 10, 2005
 */
public class ComplexTypesTestCase extends WSToolsBase
{
   protected String genPath = "tools/xsd-java-checker/jbossws/complextypes";
   
   /**
    * Tests a Complex Type that contains only elements (Xerces Version)
    * @throws Exception
    */
   public void testXSComplexType_ElementsOnlyTestCase() throws Exception
   {
      String filename = "ComplexType_ElementsOnly.xsd";
      String packagename = "org.jboss.ws.types";
      String schemaFile = getResourceFile("tools/xsd/complextypes/ComplexType_ElementsOnly.xsd").getAbsolutePath();

      this.mkdirs(genPath);
      File dir = createResourceFile(genPath);
      generateJavaSource(schemaFile, dir, packagename, true);

      //Compare the generated Java type against the one generated by wscompile
      String fname = "USAddress.java";
      File file2 = createResourceFile(genPath + "/org/jboss/ws/types/" + fname);
      File file1 = getResourceFile("tools/xsd-java-checker/wscompile/complextypes/" + fname);
      JBossSourceComparator sc = new JBossSourceComparator(file1, file2);
      assertTrue("Source Files Mismatch", sc.validate());
      sc.validateImports();

      //Compare the generated Java type against the one generated by wscompile
      fname = "Country.java";
      file2 = createResourceFile(genPath + "/org/jboss/ws/types/" + fname);
      file1 = getResourceFile("tools/xsd-java-checker/wscompile/complextypes/" + fname);
      sc = new JBossSourceComparator(file1, file2);
      assertTrue("Source Files Mismatch", sc.validate());
      sc.validateImports();
   }


   /**
    * Tests a Complex Type can be composed of elements and xsd:attributes
    * @throws Exception
    */
   public void testXSComplexType_ElementsAttribTestCase() throws Exception
   {
      String filename = "ComplexType_ElementsAttrib.xsd";
      String packagename = "org.jboss.ws.types";
      String schemaFile = getResourceFile("tools/xsd/complextypes/ComplexType_ElementsAttrib.xsd").getAbsolutePath();

      this.mkdirs(genPath);
      File dir = createResourceFile(genPath);
      generateJavaSource(schemaFile, dir, packagename, true);

      //Compare the generated Java type against the one generated by wscompile
      String fname = "Address.java";
      File file2 = createResourceFile(genPath + "/org/jboss/ws/types/" + fname);
      File file1 = getResourceFile("tools/xsd-java-checker/wscompile/complextypes/" + fname);
      JBossSourceComparator sc = new JBossSourceComparator(file1, file2);
      assertTrue("Source Files Match:", sc.validate());
      sc.validateImports();
   }


   /**
    * Tests Complex Type derived from Simple Type (with simplecontent)
    * @throws Exception
    */
   public void testXSComplexType_FromSimpleType() throws Exception
   {
      String filename = getResourceFile("tools/xsd/complextypes/ComplexTypeFromSimpleType.xsd").getAbsolutePath();
      String packagename = "org.jboss.ws.types";

      this.mkdirs(genPath);
      File dir = createResourceFile(genPath);
      generateJavaSource(filename, dir, packagename, true);

      //Compare the generated Java type against the one generated by wscompile
      String fname = "GlobalPrice.java";
      File file2 = createResourceFile(genPath + "/org/jboss/ws/types/" + fname);
      File file1 = getResourceFile("tools/xsd-java-checker/wscompile/complextypes/" + fname);
      JBossSourceComparator sc = new JBossSourceComparator(file1, file2);
      assertTrue("Source Files Match:", sc.validate());
      sc.validateImports();
   }


   /**
    * Tests Complex Type with empty content (with simplecontent)
    * @throws Exception
    */
   public void testXSComplexType_EmptyContent() throws Exception
   {
      String filename = getResourceFile("tools/xsd/complextypes/CT_EmptyContent.xsd").getAbsolutePath();
      String packagename = "org.jboss.ws.types";

      this.mkdirs(genPath);
      File dir = createResourceFile(genPath);
      generateJavaSource(filename, dir, packagename, true);

      //Compare the generated Java type against the one generated by wscompile
      String fname = "Price.java";
      File file2 = createResourceFile(genPath + "/org/jboss/ws/types/" + fname);
      File file1 = getResourceFile("tools/xsd-java-checker/wscompile/complextypes/emptycontent/" + fname);
      JBossSourceComparator sc = new JBossSourceComparator(file1, file2);
      assertTrue("Source Files Match:", sc.validate());
      sc.validateImports();
   }

   /**
    * Tests Complex Type with Occurrence Indicator
    * @throws Exception
    */
   public void testXSComplexTypeOccurrence() throws Exception
   {
      String filename = getResourceFile("tools/xsd/complextypes/ComplexTypesOccurence.xsd").getAbsolutePath();
      String packagename = "org.jboss.ws.types";

      this.mkdirs(genPath);
      File dir = createResourceFile(genPath + "/occurrence");
      generateJavaSource(filename, dir, packagename, true);

      //Compare the generated Java type against the one generated by wscompile
      String fname = "President.java";
      File file2 = createResourceFile(genPath + "/occurrence/org/jboss/ws/types/" + fname);
      File file1 = getResourceFile("tools/xsd-java-checker/wscompile/complextypes/occurrence/" + fname);
      JBossSourceComparator sc = new JBossSourceComparator(file1, file2);
      assertTrue("Source Files Match:", sc.validate());
      sc.validateImports();
   }


   /**
    * Tests Complex Type with Inheritance
    * @throws Exception
    */
   public void testXSComplexType_Inheritance() throws Exception
   {
      String filename = getResourceFile("tools/xsd/complextypes/CT_ExtensionRestriction.xsd").getAbsolutePath();
      String packagename = "org.jboss.ws.types";

      this.mkdirs(genPath);
      File dir = createResourceFile(genPath);
      generateJavaSource(filename, dir, packagename, true);

      //Compare the generated Java type against the one generated by wscompile
      String fname = "AwardEmployee.java";
      File file2 = createResourceFile(genPath + "/org/jboss/ws/types/" + fname);
      File file1 = getResourceFile("tools/xsd-java-checker/wscompile/complextypes/inheritance/" + fname);
      JBossSourceComparator sc = new JBossSourceComparator(file1, file2);
      assertTrue("Source Files Match:", sc.validate());
      sc.validateImports();

      //Compare the generated Java type against the one generated by wscompile
      fname = "Employee.java";
      file2 = createResourceFile(genPath + "/org/jboss/ws/types/" + fname);
      file1 = getResourceFile("tools/xsd-java-checker/wscompile/complextypes/inheritance/" + fname);
      sc = new JBossSourceComparator(file1, file2);
      assertTrue("Source Files Match:", sc.validate());
      sc.validateImports();
   }


   /**
    * Tests Complex Type with sequence groups [Element Groups and Attribute Groups]
    * @throws Exception
    */
   public void testXSComplexType_SequenceGroups() throws Exception
   {
      /**
       * wscompile does not support element groups
       */
      String filename = getResourceFile("tools/xsd/complextypes/CT_SequenceGroups.xsd").getAbsolutePath();
      //This testcase will pass because wscompile does not support xsd:group
      String packagename = "org.jboss.ws.types";

      this.mkdirs(genPath + "/groups");
      File dir = createResourceFile(genPath + "/groups");
      generateJavaSource(filename, dir, packagename, true);

      //Compare the generated Java type against the one generated by wscompile
      String fname = "Teacher.java";
      File file2 = createResourceFile(genPath + "/groups" + "/org/jboss/ws/types/" + fname);
      File file1 = getResourceFile("tools/xsd-java-checker/wscompile/complextypes/groups/" + fname);
      JBossSourceComparator sc = new JBossSourceComparator(file1, file2);
      assertTrue("Source Files Match:", sc.validate());
      sc.validateImports();
   }
   
   private void generateJavaSource( String filename, File dir, String packageName,
         boolean createPackageDir) throws Exception
   {
      XSDToJavaIntf xsdJava = new XSDToJava(); 
      xsdJava.setTypeMapping(new LiteralTypeMapping());
      xsdJava.generateJavaSource(filename, dir, packageName, createPackageDir);
   }
}
