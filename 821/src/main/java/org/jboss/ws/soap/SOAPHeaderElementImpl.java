/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.ws.soap;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.jboss.ws.Constants;

/**
 * An object representing the contents in the SOAP header part of the SOAP envelope.
 * The immediate children of a SOAPHeader object can be represented only as SOAPHeaderElement objects.
 *
 * A SOAPHeaderElement object can have other SOAPElement objects as its children.
 *
 * @author Thomas.Diesler@jboss.org
 */
public class SOAPHeaderElementImpl extends SOAPContentElement implements SOAPHeaderElement
{
   public SOAPHeaderElementImpl(Name name)
   {
      super(name);
   }
   
   public SOAPHeaderElementImpl(SOAPElementImpl element)
   {
      super(element);
   }

   public String getActor()
   {
      String envURI = Constants.NS_SOAP11_ENV;
      String attr = getAttributeNS(envURI, Constants.SOAP11_ATTR_ACTOR);
      return attr;
   }

   public boolean getMustUnderstand()
   {
      String envURI = Constants.NS_SOAP11_ENV;
      String attr = getAttributeNS(envURI, Constants.SOAP11_ATTR_MUST_UNDERSTAND);
      return "1".equals(attr);
   }

   public void setActor(String actorURI)
   {
      String envURI = Constants.NS_SOAP11_ENV;
      String qualifiedName =  Constants.PREFIX_ENV + ":" + Constants.SOAP11_ATTR_ACTOR;
      setAttributeNS(envURI, qualifiedName, actorURI);
   }

   public void setMustUnderstand(boolean mustUnderstand)
   {
      String envURI = Constants.NS_SOAP11_ENV;
      String qualifiedName =  Constants.PREFIX_ENV + ":" + Constants.SOAP11_ATTR_MUST_UNDERSTAND;
      setAttributeNS(envURI, qualifiedName, mustUnderstand ? "1" : "0");
   }

   public void setParentElement(SOAPElement parent) throws SOAPException
   {
      if (parent == null)
         throw new SOAPException("Invalid null parent element");
      
      if ((parent instanceof SOAPHeader) == false)
         throw new SOAPException("Invalid parent element: " + parent.getElementName());
      
      super.setParentElement(parent);
   }
}
