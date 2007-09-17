package org.jboss.test.ws.jaxws.wsrm;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import org.jboss.logging.Logger;
import org.jboss.ws.extensions.policy.PolicyScopeLevel;
import org.jboss.ws.extensions.policy.annotation.Policy;
import org.jboss.ws.extensions.policy.annotation.PolicyAttachment;

@WebService
(
   name = "ReqRes",
   serviceName = "ReqResService",
   targetNamespace = "http://org.jboss.ws/jaxws/wsrm"
)
@SOAPBinding
(
   style = Style.RPC
)
@PolicyAttachment
(
   @Policy
   (
      policyFileLocation = "WEB-INF/wsrm11_exactly_one_in_order.xml",
      scope = PolicyScopeLevel.WSDL_BINDING
   )
)
public class ReqResServiceImpl
{
   private static Logger log = Logger.getLogger(ReqResServiceImpl.class);

   @WebMethod
   @WebResult(name = "result")
   public String echo(@WebParam(name = "String_1") String msg)
   {
      log.info("echo: " + msg);
      return msg;
   }
}
