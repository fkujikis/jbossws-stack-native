package org.jboss.test.ws.jaxws.wsrm;

import javax.jws.WebService;
import org.jboss.logging.Logger;
import org.jboss.ws.extensions.policy.PolicyScopeLevel;
import org.jboss.ws.extensions.policy.annotation.Policy;
import org.jboss.ws.extensions.policy.annotation.PolicyAttachment;

import java.util.Arrays;

@WebService
(
   name = "OneWay",
   serviceName = "OneWayService",
   endpointInterface = "org.jboss.test.ws.jaxws.wsrm.OneWayServiceIface"
)
@PolicyAttachment
(
   @Policy
   (
      policyFileLocation = "WEB-INF/wsrm-exactly-once-in-order-policy.xml",
      scope = PolicyScopeLevel.WSDL_BINDING
   )
)
public class OneWayServiceImpl implements OneWayServiceIface
{
   private Logger log = Logger.getLogger(OneWayServiceImpl.class);

   public void method1()
   {
      log.info("method1()");
   }

   public void method2(String s)
   {
      log.info("method2(" + s + ")");
   }

   public void method3(String[] sa)
   {
      log.info("method3(" + Arrays.asList(sa) + ")");
   }
}
