/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.core.soap;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

/**
 * A wrapper to delegate calls to an existing SOAPElement without 
 * duplicating the SOAPElement.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 11th June 2009
 */
public class SOAPElementWrapper implements SOAPElementInternal
{

   private final SOAPElementImpl soapElement;

   SOAPElementWrapper(final SOAPElementInternal soapElement)
   {
      this.soapElement = (SOAPElementImpl)soapElement;
   }

   /*
    * Element methods.
    * 
    * @see org.w3c.dom.Element
    */

   public String getAttribute(String name)
   {
      return soapElement.getAttribute(name);
   }

   public Attr getAttributeNode(String name)
   {
      return soapElement.getAttributeNode(name);
   }

   public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException
   {
      return soapElement.getAttributeNodeNS(namespaceURI, localName);
   }

   public String getAttributeNS(String namespaceURI, String localName) throws DOMException
   {
      return soapElement.getAttributeNS(namespaceURI, localName);
   }

   public NodeList getElementsByTagName(String name)
   {
      return soapElement.getElementsByTagName(name);
   }

   public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException
   {
      return soapElement.getElementsByTagNameNS(namespaceURI, localName);
   }

   public TypeInfo getSchemaTypeInfo()
   {
      return soapElement.getSchemaTypeInfo();
   }

   public String getTagName()
   {
      return soapElement.getTagName();
   }

   public boolean hasAttribute(String name)
   {
      return soapElement.hasAttribute(name);
   }

   public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException
   {
      return soapElement.hasAttributeNS(namespaceURI, localName);
   }

   public void removeAttribute(String name) throws DOMException
   {
      soapElement.removeAttribute(name);
   }

   public Attr removeAttributeNode(Attr oldAttr) throws DOMException
   {
      return soapElement.removeAttributeNode(oldAttr);
   }

   public void removeAttributeNS(String namespaceURI, String localName) throws DOMException
   {
      soapElement.removeAttributeNS(namespaceURI, localName);
   }

   public void setAttribute(String name, String value) throws DOMException
   {
      soapElement.setAttribute(name, value);
   }

   public Attr setAttributeNode(Attr newAttr) throws DOMException
   {
      return soapElement.setAttributeNode(newAttr);
   }

   public Attr setAttributeNodeNS(Attr newAttr) throws DOMException
   {
      return soapElement.setAttributeNodeNS(newAttr);
   }

   public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException
   {
      soapElement.setAttributeNS(namespaceURI, qualifiedName, value);
   }

   public void setIdAttribute(String name, boolean isId) throws DOMException
   {
      soapElement.setIdAttribute(name, isId);
   }

   public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException
   {
      soapElement.setIdAttributeNode(idAttr, isId);
   }

   public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException
   {
      soapElement.setIdAttributeNS(namespaceURI, localName, isId);
   }

   /*
    * SAAJVisitable Methods.
    * 
    * @see org.jboss.ws.core.soap.SAAJVisitable
    */

   public void accept(SAAJVisitor visitor)
   {
      soapElement.accept(visitor);
   }

   /*
    * SOAPElement Methods.
    * 
    * @see javax.xml.soap.SOAPElement
    */

   public SOAPElement addAttribute(Name name, String value) throws SOAPException
   {
      return soapElement.addAttribute(name, value);
   }

   public SOAPElement addAttribute(QName qname, String value) throws SOAPException
   {
      return soapElement.addAttribute(qname, value);
   }

   public SOAPElement addChildElement(Name name) throws SOAPException
   {
      return soapElement.addChildElement(name);
   }

   public SOAPElement addChildElement(QName qname) throws SOAPException
   {
      return soapElement.addChildElement(qname);
   }

   public SOAPElement addChildElement(SOAPElement child) throws SOAPException
   {
      return soapElement.addChildElement(child);
   }

   public SOAPElement addChildElement(String localName, String prefix, String uri) throws SOAPException
   {
      return soapElement.addChildElement(localName, prefix, uri);
   }

   public SOAPElement addChildElement(String localName, String prefix) throws SOAPException
   {
      return soapElement.addChildElement(localName, prefix);
   }

   public SOAPElement addChildElement(String name) throws SOAPException
   {
      return soapElement.addChildElement(name);
   }

   public SOAPElement addNamespaceDeclaration(String prefix, String uri)
   {
      return soapElement.addNamespaceDeclaration(prefix, uri);
   }

   public SOAPElement addTextNode(String text) throws SOAPException
   {
      return soapElement.addTextNode(text);
   }

   public QName createQName(String localName, String prefix) throws SOAPException
   {
      return soapElement.createQName(localName, prefix);
   }

   public Iterator getAllAttributes()
   {
      return soapElement.getAllAttributes();
   }

   public Iterator getAllAttributesAsQNames()
   {
      return soapElement.getAllAttributesAsQNames();
   }

   public String getAttributeValue(Name name)
   {
      return soapElement.getAttributeValue(name);
   }

   public String getAttributeValue(QName qname)
   {
      return soapElement.getAttributeValue(qname);
   }

   public Iterator getChildElements()
   {
      return soapElement.getChildElements();
   }

   public Iterator getChildElements(Name name)
   {
      return soapElement.getChildElements(name);
   }

   public Iterator getChildElements(QName qname)
   {
      return soapElement.getChildElements(qname);
   }

   public Name getElementName()
   {
      return soapElement.getElementName();
   }

   public QName getElementQName()
   {
      return soapElement.getElementQName();
   }

   public String getEncodingStyle()
   {
      return soapElement.getEncodingStyle();
   }

   public Iterator getNamespacePrefixes()
   {
      return soapElement.getNamespacePrefixes();
   }

   public String getNamespaceURI(String prefix)
   {
      return soapElement.getNamespaceURI(prefix);
   }

   public Iterator getVisibleNamespacePrefixes()
   {
      return soapElement.getVisibleNamespacePrefixes();
   }

   public boolean removeAttribute(Name name)
   {
      return soapElement.removeAttribute(name);
   }

   public boolean removeAttribute(QName qname)
   {
      return soapElement.removeAttribute(qname);
   }

   public void removeContents()
   {
      soapElement.removeContents();
   }

   public boolean removeNamespaceDeclaration(String prefix)
   {
      return soapElement.removeNamespaceDeclaration(prefix);
   }

   public SOAPElement setElementQName(QName qname) throws SOAPException
   {
      return soapElement.setElementQName(qname);
   }

   public void setEncodingStyle(String encodingStyle) throws SOAPException
   {
      soapElement.setEncodingStyle(encodingStyle);
   }

   /*
    * Node methods.
    * 
    * @see javax.xml.soap.Node
    */

   public void detachNode()
   {
      soapElement.detachNode();
   }

   public SOAPElement getParentElement()
   {
      return soapElement.getParentElement();
   }

   public String getValue()
   {
      return soapElement.getValue();
   }

   public void recycleNode()
   {
      soapElement.recycleNode();
   }

   public void setParentElement(SOAPElement parent) throws SOAPException
   {
      soapElement.setParentElement(parent);
   }

   public void setValue(String value)
   {
      soapElement.setValue(value);
   }

   /*
    * Node methods.
    * 
    * @see org.w3c.dom.Node
    */

   public Node appendChild(Node newChild) throws DOMException
   {
      return soapElement.appendChild(newChild);
   }

   public Node cloneNode(boolean deep)
   {
      return soapElement.cloneNode(deep);
   }

   public short compareDocumentPosition(Node other) throws DOMException
   {
      return soapElement.compareDocumentPosition(other);
   }

   public NamedNodeMap getAttributes()
   {
      return soapElement.getAttributes();
   }

   public String getBaseURI()
   {
      return soapElement.getBaseURI();
   }

   public NodeList getChildNodes()
   {
      return soapElement.getChildNodes();
   }

   public Object getFeature(String feature, String version)
   {
      return soapElement.getFeature(feature, version);
   }

   public Node getFirstChild()
   {
      return soapElement.getFirstChild();
   }

   public Node getLastChild()
   {
      return soapElement.getLastChild();
   }

   public String getLocalName()
   {
      return soapElement.getLocalName();
   }

   public String getNamespaceURI()
   {
      return soapElement.getNamespaceURI();
   }

   public Node getNextSibling()
   {
      return soapElement.getNextSibling();
   }

   public String getNodeName()
   {
      return soapElement.getNodeName();
   }

   public short getNodeType()
   {
      return soapElement.getNodeType();
   }

   public String getNodeValue() throws DOMException
   {
      return soapElement.getNodeValue();
   }

   public Document getOwnerDocument()
   {
      return soapElement.getOwnerDocument();
   }

   public Node getParentNode()
   {
      return soapElement.getParentNode();
   }

   public String getPrefix()
   {
      return soapElement.getPrefix();
   }

   public Node getPreviousSibling()
   {
      return soapElement.getPreviousSibling();
   }

   public String getTextContent() throws DOMException
   {
      return soapElement.getTextContent();
   }

   public Object getUserData(String key)
   {
      return soapElement.getUserData(key);
   }

   public boolean hasAttributes()
   {
      return soapElement.hasAttributes();
   }

   public boolean hasChildNodes()
   {
      return soapElement.hasChildNodes();
   }

   public Node insertBefore(Node newChild, Node refChild) throws DOMException
   {
      return soapElement.insertBefore(newChild, refChild);
   }

   public boolean isDefaultNamespace(String namespaceURI)
   {
      return soapElement.isDefaultNamespace(namespaceURI);
   }

   public boolean isEqualNode(Node arg)
   {
      return soapElement.isEqualNode(arg);
   }

   public boolean isSameNode(Node other)
   {
      return soapElement.isSameNode(other);
   }

   public boolean isSupported(String feature, String version)
   {
      return soapElement.isSupported(feature, version);
   }

   public String lookupNamespaceURI(String prefix)
   {
      return soapElement.lookupNamespaceURI(prefix);
   }

   public String lookupPrefix(String namespaceURI)
   {
      return soapElement.lookupPrefix(namespaceURI);
   }

   public void normalize()
   {
      soapElement.normalize();
   }

   public Node removeChild(Node oldChild) throws DOMException
   {
      return soapElement.removeChild(oldChild);
   }

   public Node replaceChild(Node newChild, Node oldChild) throws DOMException
   {
      return soapElement.replaceChild(newChild, oldChild);
   }

   public void setNodeValue(String nodeValue) throws DOMException
   {
      soapElement.setNodeValue(nodeValue);
   }

   public void setPrefix(String prefix) throws DOMException
   {
      soapElement.setPrefix(prefix);
   }

   public void setTextContent(String textContent) throws DOMException
   {
      soapElement.setTextContent(textContent);
   }

   public Object setUserData(String key, Object data, UserDataHandler handler)
   {
      return soapElement.setUserData(key, data, handler);
   }

   /*
    * SOAPElementInternal methods.
    *  
    * @see org.jboss.ws.core.soap.SOAPElementInternal
    */

   public Node getDomNode()
   {
      return soapElement.getDomNode();
   }

   public SOAPElement setElementQNameInternal(QName qname) throws SOAPException
   {
      return soapElement.setElementQNameInternal(qname);
   }

   public void setSoapParent(SOAPElementImpl soapParent)
   {
      soapElement.setSoapParent(soapParent);
   }   

}
