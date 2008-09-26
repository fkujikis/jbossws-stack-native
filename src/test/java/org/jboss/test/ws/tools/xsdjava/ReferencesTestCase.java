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

import org.jboss.test.ws.tools.WSToolsTest;
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
public class ReferencesTestCase extends WSToolsTest
{ 
   protected String genPath = "tools/xsd-java-checker/jbossws/references";
   
   protected XSDToJavaIntf xsdJava = new XSDToJava();
   /**
    * Tests Use of references in elements and attributes
    * @throws Exception
    */
   public void testXSElemAttribReferences() throws Exception
   {

      String filename = "resources/tools/xsd/references/ElemAttribReferences.xsd";
      String packagename = "org.jboss.ws.types"; 

      this.mkdirs(genPath);
      File dir = new File(genPath);
      xsdJava.setTypeMapping(new LiteralTypeMapping());
      xsdJava.generateJavaSource(filename, dir, packagename, true);

      //Compare the generated Java type against the one generated by wscompile
      String fname = "ElemAttribRef.java";
      String base = "resources/tools/xsd-java-checker";
      File file2 = new File(genPath + "/org/jboss/ws/types/" + fname);
      File file1 = new File(base + "/wscompile/references/" + fname);
      JBossSourceComparator sc = new JBossSourceComparator(file1, file2);
      assertTrue("Source Files Match::ElemAttribRef.java::", sc.validate());
      sc.validateImports();

      //Compare the generated Java type against the one generated by wscompile
      fname = "Address.java";
      base = "resources/tools/xsd-java-checker";
      file2 = new File( genPath + "/org/jboss/ws/types/" + fname);
      file1 = new File(base + "/wscompile/references/" + fname);
      sc = new JBossSourceComparator(file1, file2);
      assertTrue("Source Files Match::Address.java::", sc.validate());
      sc.validateImports();

      //Compare the generated Java type against the one generated by wscompile
      fname = "Employee.java";
      base = "resources/tools/xsd-java-checker";
      file2 = new File(genPath + "/org/jboss/ws/types/" + fname);
      file1 = new File(base + "/wscompile/references/" + fname);
      sc = new JBossSourceComparator(file1, file2);
      assertTrue("Source Files Match:Employee.java::", sc.validate());
      sc.validateImports();
   }
}
