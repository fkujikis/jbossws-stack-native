package org.jboss.test.ws.xop;

import junit.framework.TestCase;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSTypeDefinition;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.jboss.ws.metadata.wsdl.xmlschema.WSSchemaUtils;
import org.jboss.ws.metadata.wsdl.xsd.SchemaUtils;
import org.jboss.ws.xop.XOPScanner;
import org.jboss.xb.binding.NamespaceRegistry;

import java.io.File;

/**
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since Jun 9, 2006
 */
public class XOPTypeDefTestCase extends TestCase {

   public XOPTypeDefTestCase(String string) {
      super(string);
   }

   protected void setUp() throws Exception {
      super.setUp();
   }

  public void testCircularReferences() throws Exception{
      SchemaUtils utils = SchemaUtils.getInstance();
      //String prefix = "C:/dev/prj/jbossws/branches/jbossws-1.0_SchemaBindingMarshaller/src/test/";
      File f = new File("resources/xop/circular.xsd");
      assertTrue("Unable to load schema file " + f.getAbsolutePath(), f.exists());

      XSModel xsModel = utils.parseSchema(f.toURL());
      assertNotNull(xsModel);
      WSSchemaUtils wsUtil = WSSchemaUtils.getInstance(new NamespaceRegistry(), "http://complex.jsr181.ws.test.jboss.org/jaws");
      JBossXSModel schemaModel= wsUtil.getJBossXSModel(xsModel);

      XSTypeDefinition xsType = schemaModel.getTypeDefinition("Customer", "http://complex.jsr181.ws.test.jboss.org/jaws");

      assertNotNull("Root type def not found", xsType);
      XOPScanner scanner = new XOPScanner();

      if (xsType instanceof XSComplexTypeDefinition)
      {
         XSComplexTypeDefinition xsComplexType = (XSComplexTypeDefinition)xsType;
         XSTypeDefinition resultType = scanner.findXOPTypeDef(xsComplexType);

         // it fails when getting a stack overflow ;)
      }
   }

   public void testXOPElementScan() throws Exception
   {
      SchemaUtils utils = SchemaUtils.getInstance();
      //String prefix = "C:/dev/prj/jbossws/branches/jbossws-1.0_SchemaBindingMarshaller/src/test/";
      File f = new File("resources/xop/schema.xsd");
      assertTrue("Unable to load schema file " + f.getAbsolutePath(), f.exists());

      XSModel xsModel = utils.parseSchema(f.toURL());
      assertNotNull(xsModel);
      WSSchemaUtils wsUtil = WSSchemaUtils.getInstance(new NamespaceRegistry(), "http://jboss.org/test/ws/xop/doclit");
      JBossXSModel schemaModel= wsUtil.getJBossXSModel(xsModel);

      XSTypeDefinition xsType = schemaModel.getTypeDefinition(">PingMsg", "http://jboss.org/test/ws/xop/doclit");

      assertNotNull("Root type def not found", xsType);
      XOPScanner scanner = new XOPScanner();

      if (xsType instanceof XSComplexTypeDefinition)
      {
         XSComplexTypeDefinition xsComplexType = (XSComplexTypeDefinition)xsType;
         XSTypeDefinition resultType = scanner.findXOPTypeDef(xsComplexType);
         assertNotNull("Unable to find xop typedef in schema", resultType);
      }

      scanner.reset();

      xsType = schemaModel.getTypeDefinition(">PingMsgResponse", "http://jboss.org/test/ws/xop/doclit");
      assertNotNull("Root type def not found", xsType);
      if (xsType instanceof XSComplexTypeDefinition)
      {
         XSComplexTypeDefinition xsComplexType = (XSComplexTypeDefinition)xsType;
         XSTypeDefinition resultType = scanner.findXOPTypeDef(xsComplexType);
         assertNotNull("Unable to find XOP typedef in schema", resultType);
      }

   }
}
