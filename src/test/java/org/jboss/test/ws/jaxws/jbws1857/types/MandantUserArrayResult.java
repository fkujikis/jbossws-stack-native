
package org.jboss.test.ws.jaxws.jbws1857.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for mandantUserArrayResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mandantUserArrayResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mandantUser" type="{http://example.com}mandantUser" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="serviceStatus" type="{http://example.com}serviceStatus" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mandantUserArrayResult", propOrder = {
    "mandantUser",
    "serviceStatus"
})
public class MandantUserArrayResult {

    @XmlElement(nillable = true)
    protected List<MandantUser> mandantUser;
    protected ServiceStatus serviceStatus;

    /**
     * Gets the value of the mandantUser property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mandantUser property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMandantUser().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MandantUser }
     * 
     * 
     */
    public List<MandantUser> getMandantUser() {
        if (mandantUser == null) {
            mandantUser = new ArrayList<MandantUser>();
        }
        return this.mandantUser;
    }

    /**
     * Gets the value of the serviceStatus property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceStatus }
     *     
     */
    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    /**
     * Sets the value of the serviceStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceStatus }
     *     
     */
    public void setServiceStatus(ServiceStatus value) {
        this.serviceStatus = value;
    }

}
