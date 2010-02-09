/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ws.extensions.eventing.jaxws;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;


/**
 * <p>Java class for SubscribeResponse element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="SubscribeResponse">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="SubscriptionManagerEndpoint" type="{http://www.w3.org/2005/08/addressing}EndpointReferenceType"/>
 *           &lt;element name="Expires" type="{http://schemas.xmlsoap.org/ws/2004/08/eventing}ExpirationType"/>
 *           &lt;any/>
 *         &lt;/sequence>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
   "subscriptionManager",
   "expires",
   "any"
   })
@XmlRootElement(name = "SubscribeResponse")
public class SubscribeResponse {

   @XmlElement(name = "SubscriptionManager", namespace = "http://schemas.xmlsoap.org/ws/2004/08/eventing")
   protected EndpointReferenceType subscriptionManager;
   @XmlElement(name = "Expires", namespace = "http://schemas.xmlsoap.org/ws/2004/08/eventing")
   protected Date expires;
   @XmlAnyElement(lax = true)
   protected List<Object> any;
   @XmlAnyAttribute
   private Map<QName, String> otherAttributes = new HashMap<QName, String>();

   /**
    * Gets the value of the subscriptionManager property.
    *
    * @return
    *     possible object is
    *     {@link EndpointReferenceType }
    *
    */
   public EndpointReferenceType getSubscriptionManager() {
      return subscriptionManager;
   }

   /**
    * Sets the value of the subscriptionManager property.
    *
    * @param value
    *     allowed object is
    *     {@link EndpointReferenceType }
    *
    */
   public void setSubscriptionManager(EndpointReferenceType value) {
      this.subscriptionManager = value;
   }

   /**
    * Gets the value of the expires property.
    *
    * @return
    *     possible object is
    *     {@link String }
    *
    */
   public Date getExpires() {
      return expires;
   }

   /**
    * Sets the value of the expires property.
    *
    * @param value
    *     allowed object is
    *     {@link String }
    *
    */
   public void setExpires(Date value) {
      this.expires = value;
   }

   /**
    * Gets the value of the any property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the any property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getAny().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link Element }
    * {@link Object }
    *
    *
    */
   public List<Object> getAny() {
      if (any == null) {
         any = new ArrayList<Object>();
      }
      return this.any;
   }

   /**
    * Gets a map that contains attributes that aren't bound to any typed property on this class.
    *
    * <p>
    * the map is keyed by the name of the attribute and
    * the value is the string value of the attribute.
    *
    * the map returned by this method is live, and you can add new attribute
    * by updating the map directly. Because of this design, there's no setter.
    *
    *
    * @return
    *     always non-null
    */
   public Map<QName, String> getOtherAttributes() {
      return otherAttributes;
   }

   public String getSubscriptionId()
   {
      JAXBElement<String> jaxbElement = (JAXBElement<String>)getSubscriptionManager().getReferenceParameters().getAny().get(0);
      return jaxbElement.getValue();
   }

}
