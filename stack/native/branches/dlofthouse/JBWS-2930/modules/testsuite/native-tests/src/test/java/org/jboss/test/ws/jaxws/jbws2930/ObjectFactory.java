
package org.jboss.test.ws.jaxws.jbws2930;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.jboss.test.ws.jaxws.jbws2930 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _LookupResponse_QNAME = new QName("http://test.jboss.org/ws/jbws2930/types", "lookupResponse");
    private final static QName _Nickname_QNAME = new QName("", "Nickname");
    private final static QName _Lookup_QNAME = new QName("http://test.jboss.org/ws/jbws2930/types", "lookup");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.jboss.test.ws.jaxws.jbws2930
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Person }
     * 
     */
    public Person createPerson() {
        return new Person();
    }

    /**
     * Create an instance of {@link Nickname }
     * 
     */
    public Nickname createNickname() {
        return new Nickname();
    }

    /**
     * Create an instance of {@link TelephoneNumber }
     * 
     */
    public TelephoneNumber createTelephoneNumber() {
        return new TelephoneNumber();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TelephoneNumber }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://test.jboss.org/ws/jbws2930/types", name = "lookupResponse")
    public JAXBElement<TelephoneNumber> createLookupResponse(TelephoneNumber value) {
        return new JAXBElement<TelephoneNumber>(_LookupResponse_QNAME, TelephoneNumber.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Nickname }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Nickname")
    public JAXBElement<Nickname> createNickname(Nickname value) {
        return new JAXBElement<Nickname>(_Nickname_QNAME, Nickname.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Person }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://test.jboss.org/ws/jbws2930/types", name = "lookup")
    public JAXBElement<Person> createLookup(Person value) {
        return new JAXBElement<Person>(_Lookup_QNAME, Person.class, null, value);
    }

}
