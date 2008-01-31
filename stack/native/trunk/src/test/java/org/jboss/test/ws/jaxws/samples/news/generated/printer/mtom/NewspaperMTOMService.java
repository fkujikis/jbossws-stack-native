
package org.jboss.test.ws.jaxws.samples.news.generated.printer.mtom;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.1-b03-
 * Generated source version: 2.0
 * 
 */
@WebServiceClient(name = "NewspaperMTOMService", targetNamespace = "http://org.jboss.ws/samples/news", wsdlLocation = "http://localhost.localdomain:8080/news/newspaper/mtom?wsdl")
public class NewspaperMTOMService
    extends Service
{

    private final static URL NEWSPAPERMTOMSERVICE_WSDL_LOCATION;

    static {
        URL url = null;
        try {
            url = new URL("http://localhost.localdomain:8080/news/newspaper/mtom?wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        NEWSPAPERMTOMSERVICE_WSDL_LOCATION = url;
    }

    public NewspaperMTOMService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public NewspaperMTOMService() {
        super(NEWSPAPERMTOMSERVICE_WSDL_LOCATION, new QName("http://org.jboss.ws/samples/news", "NewspaperMTOMService"));
    }

    /**
     * 
     * @return
     *     returns NewspaperMTOMEndpoint
     */
    @WebEndpoint(name = "NewspaperMTOMEndpointPort")
    public NewspaperMTOMEndpoint getNewspaperMTOMEndpointPort() {
        return (NewspaperMTOMEndpoint)super.getPort(new QName("http://org.jboss.ws/samples/news", "NewspaperMTOMEndpointPort"), NewspaperMTOMEndpoint.class);
    }

}
