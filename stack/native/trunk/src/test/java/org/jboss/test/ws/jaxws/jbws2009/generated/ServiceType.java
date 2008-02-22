
package org.jboss.test.ws.jaxws.jbws2009.generated;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.1-b03-
 * Generated source version: 2.0
 * 
 */
@WebService(name = "ServiceType", targetNamespace = "http://jbws2009.jaxws.ws.test.jboss.org/")
public interface ServiceType {


    /**
     * 
     * @return
     *     returns org.jboss.test.ws.jaxws.jbws2009.generated.GetCountryCodesResponse.Response
     */
    @WebMethod(action = "countryCodesAction")
    @WebResult(name = "response", targetNamespace = "http://jbws2009.jaxws.ws.test.jboss.org/")
    @RequestWrapper(localName = "getCountryCodes", targetNamespace = "http://jbws2009.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.jbws2009.generated.GetCountryCodes")
    @ResponseWrapper(localName = "getCountryCodesResponse", targetNamespace = "http://jbws2009.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.jbws2009.generated.GetCountryCodesResponse")
    public org.jboss.test.ws.jaxws.jbws2009.generated.GetCountryCodesResponse.Response getCountryCodes();

    /**
     * 
     * @param parameters
     * @return
     *     returns org.jboss.test.ws.jaxws.jbws2009.generated.CurrencyCodeType
     */
    @WebMethod(action = "currencyAction")
    @WebResult(name = "getCurrencyResponse", targetNamespace = "http://jbws2009.jaxws.ws.test.jboss.org/", partName = "parameters")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public CurrencyCodeType getCurrency(
        @WebParam(name = "getCurrency", targetNamespace = "http://jbws2009.jaxws.ws.test.jboss.org/", partName = "parameters")
        CountryCodeType parameters);

    /**
     * 
     * @return
     *     returns org.jboss.test.ws.jaxws.jbws2009.generated.GetCurrencyCodesResponse.Response
     */
    @WebMethod(action = "currencyCodesAction")
    @WebResult(name = "response", targetNamespace = "http://jbws2009.jaxws.ws.test.jboss.org/")
    @RequestWrapper(localName = "getCurrencyCodes", targetNamespace = "http://jbws2009.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.jbws2009.generated.GetCurrencyCodes")
    @ResponseWrapper(localName = "getCurrencyCodesResponse", targetNamespace = "http://jbws2009.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.jbws2009.generated.GetCurrencyCodesResponse")
    public org.jboss.test.ws.jaxws.jbws2009.generated.GetCurrencyCodesResponse.Response getCurrencyCodes();

}
