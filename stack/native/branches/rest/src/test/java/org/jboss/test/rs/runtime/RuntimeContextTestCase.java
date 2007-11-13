package org.jboss.test.rs.runtime;

import junit.framework.TestCase;
import org.jboss.rs.runtime.RuntimeContext;
import org.jboss.rs.MethodHTTP;
import org.jboss.rs.model.ResourceModel;

import java.net.URI;
import java.util.ArrayList;


public class RuntimeContextTestCase extends TestCase
{
   public void testAcceptHeaderParsing() throws Exception
   {
      // typical firefox accept header
      String headerValue = "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2";
      RuntimeContext ctx = new RuntimeContext(
        MethodHTTP.GET,
        new URI("/jbossrs-deployment/widgets"),
        new ArrayList<ResourceModel>()
      );

      ctx.parseAcceptHeader(headerValue);
   }
}
