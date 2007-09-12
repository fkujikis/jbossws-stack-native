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
package org.jboss.ws.extensions.wsrm.common.serialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.ws.extensions.wsrm.ReliableMessagingException;
import org.jboss.wsf.common.DOMUtils;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

/**
 * Serialization helper - TODO: optimize it to minimize usage of org.w3c.dom.Element class
 * @author richard.opalka@jboss.com
 */
final class SerializationHelper
{
   
   private SerializationHelper()
   {
      // no instances
   }
   
   public static String getRequiredTextContent(SOAPElement element, QName elementQName)
   {
      if (!DOMUtils.hasTextChildNodesOnly(element))
         throw new ReliableMessagingException(
            "Only text content is allowed for element " + elementQName.getLocalPart());

      return DOMUtils.getTextContent(element).trim();
   }
   
   private static Element getRequiredElementFromList(List<Element> list, QName requiredQName, QName contextQName)
   {
      return getRequiredElementFromList(list, requiredQName.getLocalPart(), contextQName.getLocalPart());
   }
   
   private static Element getRequiredElementFromList(List<Element> list, QName requiredQName, String context)
   {
      return getRequiredElementFromList(list, requiredQName.getLocalPart(), context);
   }
   
   private static Element getRequiredElement(Element element, QName requiredQName, QName contextQName)
   {
      List<Element> list = DOMUtils.getChildElementsAsList(element, requiredQName);
      return getRequiredElementFromList(list, requiredQName, contextQName);
   }
   
   public static SOAPElement getRequiredElement(SOAPElement element, QName requiredQName, QName contextQName)
   {
      return (SOAPElement)getRequiredElement((Element)element, requiredQName, contextQName);
   }
   
   private static Element getRequiredElement(Element element, QName requiredQName, String context)
   {
      List<Element> list = DOMUtils.getChildElementsAsList(element, requiredQName);
      return getRequiredElementFromList(list, requiredQName, context);
   }
   
   public static SOAPElement getRequiredElement(SOAPElement element, QName requiredQName, String context)
   {
      return (SOAPElement)getRequiredElement((Element)element, requiredQName, context);
   }
   
   private static Element getOptionalElement(Element element, QName optionalQName, QName contextQName)
   {
      List<Element> list = DOMUtils.getChildElementsAsList(element, optionalQName);
      return getOptionalElementFromList(list, optionalQName, contextQName);
   }
   
   public static String getRequiredTextContent(SOAPElement element, QName attributeQName, QName elementQName)
   {
      String attributeValue = element.getAttributeValue(attributeQName);
      
      if (attributeValue == null)
         throw new ReliableMessagingException(
            "Required attribute " + attributeQName.getLocalPart() + " is missing in element " + elementQName.getLocalPart());

      return attributeValue;
   }
   
   public static SOAPElement getOptionalElement(SOAPElement contextElement, QName optionalQName, QName contextQName)
   {
      return (SOAPElement)getOptionalElement((Element)contextElement, optionalQName, contextQName);
   }
   
   public static List<SOAPElement> getOptionalElements(SOAPElement contextElement, QName optionalQName, QName contextQName)
   {
      // TODO: optimize this method - do not create new list
      List<Element> temp = DOMUtils.getChildElementsAsList(contextElement, optionalQName);
      if (temp.size() == 0)
      {
         return Collections.emptyList();
      }
      else
      {
         List<SOAPElement> retVal = new ArrayList<SOAPElement>();
         
         for (Element e : temp)
         {
            retVal.add((SOAPElement)e);
         }
         
         return retVal;
      }
   }
   
   public static long stringToLong(String toEvaluate, String errorMessage) throws ReliableMessagingException
   {
      try
      {
         return Long.valueOf(toEvaluate);
      }
      catch (NumberFormatException nfe)
      {
         throw new ReliableMessagingException(errorMessage, nfe);
      }
   }
   
   private static Element getOptionalElementFromList(List<Element> list, QName requiredQName, QName contextQName)
   {
      return getOptionalElementFromList(list, requiredQName.getLocalPart(), contextQName.getLocalPart());
   }

   private static Element getOptionalElementFromList(List<Element> list, String required, String context)
   {
      if (list.size() > 1)
         throw new ReliableMessagingException(
            "At most one " + required + " element can be present in " + context + " element");
      
      return (list.size() == 1) ? list.get(0) : null;
   }
   
   private static Element getRequiredElementFromList(List<Element> list, String required, String context)
   {
      if (list.size() < 1)
         throw new ReliableMessagingException(
            "Required " + required + " element not found in " + context + " element");
      
      if (list.size() > 1)
         throw new ReliableMessagingException(
            "Only one " + required + " element can be present in " + context + " element");
      
      return list.get(0);
   }
   
}
