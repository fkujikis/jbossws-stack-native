
package org.jboss.test.ws.jaxws.jbws1857.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for glaeubigerArrayResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="glaeubigerArrayResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="glaeubigerArray" type="{http://example.com}glaeubiger" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "glaeubigerArrayResult", propOrder = {
    "glaeubigerArray",
    "serviceStatus"
})
public class GlaeubigerArrayResult {

    @XmlElement(nillable = true)
    protected List<Glaeubiger> glaeubigerArray;
    protected ServiceStatus serviceStatus;

    /**
     * Gets the value of the glaeubigerArray property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the glaeubigerArray property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGlaeubigerArray().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Glaeubiger }
     * 
     * 
     */
    public List<Glaeubiger> getGlaeubigerArray() {
        if (glaeubigerArray == null) {
            glaeubigerArray = new ArrayList<Glaeubiger>();
        }
        return this.glaeubigerArray;
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
