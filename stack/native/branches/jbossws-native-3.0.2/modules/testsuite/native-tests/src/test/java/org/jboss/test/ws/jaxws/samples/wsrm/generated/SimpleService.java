
package org.jboss.test.ws.jaxws.samples.wsrm.generated;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.1-b03-
 * Generated source version: 2.0
 * 
 */
@WebService(name = "SimpleService", targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/wsrm")
public interface SimpleService {


    /**
     * 
     * @param arg0
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "echo", targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/wsrm", className = "org.jboss.test.ws.jaxws.samples.wsrm.generated.Echo")
    @ResponseWrapper(localName = "echoResponse", targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/wsrm", className = "org.jboss.test.ws.jaxws.samples.wsrm.generated.EchoResponse")
    public String echo(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0);

    /**
     * 
     */
    @WebMethod
    @Oneway
    @RequestWrapper(localName = "ping", targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/wsrm", className = "org.jboss.test.ws.jaxws.samples.wsrm.generated.Ping")
    public void ping();

}
