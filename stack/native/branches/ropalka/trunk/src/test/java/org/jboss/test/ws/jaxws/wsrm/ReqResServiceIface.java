package org.jboss.test.ws.jaxws.wsrm;

import java.util.concurrent.Future;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

@WebService(name = "ReqRes", targetNamespace = "http://org.jboss.ws/jaxws/wsrm")
@SOAPBinding(style = Style.RPC)
public interface ReqResServiceIface
{
   @WebMethod(operationName = "echo")
   public Response<String> echoAsync(@WebParam(name = "String_1") String string1);

   @WebMethod(operationName = "echo")
   public Future<?> echoAsync(@WebParam(name = "String_1") String string1, @WebParam(name = "asyncHandler") AsyncHandler<String> asyncHandler);

   @WebMethod
   @WebResult(name = "result")
   public String echo(@WebParam(name = "String_1") String string1);
}
