
package org.jboss.test.ws.jaxws.jbws2930;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2-12/14/2009 02:16 PM(ramkris)-
 * Generated source version: 2.0
 * 
 */
@WebService(name = "PhoneBook", targetNamespace = "http://test.jboss.org/ws/jbws2930")
public interface PhoneBook {


    /**
     * 
     * @param nickname
     * @param areaCode
     * @param surname
     * @param number
     * @param firstName
     */
    @WebMethod
    @RequestWrapper(localName = "lookup", targetNamespace = "http://test.jboss.org/ws/jbws2930/types", className = "org.jboss.test.ws.jaxws.jbws2930.Person")
    @ResponseWrapper(localName = "lookupResponse", targetNamespace = "http://test.jboss.org/ws/jbws2930/types", className = "org.jboss.test.ws.jaxws.jbws2930.TelephoneNumber")
    public void lookup(
        @WebParam(name = "firstName", targetNamespace = "")
        String firstName,
        @WebParam(name = "surname", targetNamespace = "")
        String surname,
        @WebParam(name = "areaCode", targetNamespace = "", mode = WebParam.Mode.OUT)
        Holder<String> areaCode,
        @WebParam(name = "number", targetNamespace = "", mode = WebParam.Mode.OUT)
        Holder<String> number,
        @WebParam(name = "Nickname", targetNamespace = "", mode = WebParam.Mode.OUT)
        Holder<Nickname> nickname);

}
