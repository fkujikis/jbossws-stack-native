package org.jboss.ws.extensions.wsrm.common.serialization;

import java.util.List;
import javax.xml.namespace.QName;
import org.jboss.ws.extensions.wsrm.ReliableMessagingException;
import org.jboss.wsf.common.DOMUtils;
import org.w3c.dom.Element;

final class SerializationHelper
{
   
   private SerializationHelper()
   {
      // no instances
   }
   
   public static String getRequiredTextContent(Element element, QName nodeName)
   {
      if (!DOMUtils.hasTextChildNodesOnly(element))
         throw new ReliableMessagingException(
            "Only text content is allowed for element " + nodeName.getLocalPart());

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
   
   public static Element getRequiredElement(Element element, QName requiredQName, QName contextQName)
   {
      List<Element> list = DOMUtils.getChildElementsAsList(element, requiredQName);
      return getRequiredElementFromList(list, requiredQName, contextQName);
   }
   
   public static Element getRequiredElement(Element element, QName requiredQName, String context)
   {
      List<Element> list = DOMUtils.getChildElementsAsList(element, requiredQName);
      return getRequiredElementFromList(list, requiredQName, context);
   }
   
   public static Element getOptionalElement(Element element, QName optionalQName, QName contextQName)
   {
      List<Element> list = DOMUtils.getChildElementsAsList(element, optionalQName);
      return getOptionalElementFromList(list, optionalQName, contextQName);
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
