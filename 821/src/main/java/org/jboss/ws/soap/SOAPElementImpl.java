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

// $Id$

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.Writer;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.Text;

import org.jboss.logging.Logger;
import org.jboss.util.xml.DOMUtils;
import org.jboss.util.xml.DOMWriter;
import org.jboss.ws.Constants;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

/**
 * An object representing an element of a SOAP message that is allowed but not specifically prescribed by a
 * SOAP specification. This interface serves as the base interface for those objects that are specifically
 * prescribed by a SOAP specification.
 *
 * Methods in this interface that are required to return SAAJ specific objects may "silently" replace nodes
 * in the tree as required to successfully return objects of the correct type.
 *
 * @author Thomas.Diesler@jboss.org
 */
public class SOAPElementImpl extends NodeImpl implements SOAPElement
{
   // provide logging
   private static Logger log = Logger.getLogger(SOAPElementImpl.class);

   // The org.w3c.dom.Element
   private Element element;
   // The element's encoding style
   private String encodingStyle = Constants.URI_LITERAL_ENC;

   /** Called by SOAPFactory */
   public SOAPElementImpl(String localPart)
   {

      super(DOMUtils.createElement(localPart, null, null));
      this.element = (Element)domNode;
      log.trace("new SOAPElementImpl: " + getElementName());
   }

   /** Called by SOAPFactory */
   public SOAPElementImpl(String localPart, String prefix, String nsURI)
   {
      super(DOMUtils.createElement(localPart, prefix, nsURI));
      this.element = (Element)domNode;
      log.trace("new SOAPElementImpl: " + getElementName());
   }

   /** Called by SOAPFactory */
   public SOAPElementImpl(Name name)
   {
      this(name.getLocalName(), name.getPrefix(), name.getURI());
   }

   /** Copy constructor for converting SOAPElement types
    */
   protected SOAPElementImpl(SOAPElementImpl element)
   {
      super(element);
      this.element = (Element)domNode;
      log.trace("new SOAPElementImpl: " + getElementName());
   }

   /** Get the SOAPEnvelope for this SOAPElement */
   public SOAPEnvelope getSOAPEnvelope()
   {
      SOAPElement soapElement = this;
      while (soapElement != null && (soapElement instanceof SOAPEnvelope) == false)
         soapElement = soapElement.getParentElement();

      return (SOAPEnvelope)soapElement;
   }

   //  javax.xml.soap.SOAPElement *************************************************************************************

   /**
    * Adds an attribute with the specified name and value to this SOAPElement object.
    *
    * @param name  a Name object with the name of the attribute
    * @param value a String giving the value of the attribute
    * @return the SOAPElement object into which the attribute was inserted
    * @throws javax.xml.soap.SOAPException if there is an error in creating the Attribute
    */
   public SOAPElement addAttribute(Name name, String value) throws SOAPException
   {
      // xml:lang='en'
      if ("xml".equals(name.getPrefix()))
      {
         setAttribute(name.getQualifiedName(), value);
      }
      else
      {
         setAttributeNS(name.getURI(), name.getQualifiedName(), value);
      }
      return this;
   }

   /**
    * Creates a new SOAPElement object initialized with the specified local name and adds the new element to this SOAPElement object.
    *
    * @param name a String giving the local name for the element
    * @return the new SOAPElement object that was created
    * @throws javax.xml.soap.SOAPException if there is an error in creating the SOAPElement object
    */
   public SOAPElement addChildElement(String name) throws SOAPException
   {
      SOAPElement soapElement = new SOAPElementImpl(name);
      soapElement = addChildElement(soapElement);
      return soapElement;
   }

   /**
    * Creates a new SOAPElement object initialized with the specified local name and prefix and adds the new element to this SOAPElement object.
    *
    * @param localName a String giving the local name for the new element
    * @param prefix    a String giving the namespace prefix for the new element
    * @return the new SOAPElement object that was created
    * @throws javax.xml.soap.SOAPException if there is an error in creating the SOAPElement object
    */
   public SOAPElement addChildElement(String localName, String prefix) throws SOAPException
   {
      String nsURI = getNamespaceURI(prefix);
      if (nsURI == null)
         throw new IllegalArgumentException("Cannot obtain namespace URI for prefix: " + prefix);

      SOAPElement soapElement = new SOAPElementImpl(localName, prefix, nsURI);
      soapElement = addChildElement(soapElement);
      return soapElement;
   }

   /**
    * Creates a new SOAPElement object initialized with the specified local name, prefix, and URI and adds the new element to this SOAPElement object.
    *
    * @param localName a String giving the local name for the new element
    * @param prefix    a String giving the namespace prefix for the new element
    * @param uri       a String giving the URI of the namespace to which the new element belongs
    * @return the new SOAPElement object that was created
    * @throws javax.xml.soap.SOAPException if there is an error in creating the SOAPElement object
    */
   public SOAPElement addChildElement(String localName, String prefix, String uri) throws SOAPException
   {
      SOAPElement soapElement = new SOAPElementImpl(localName, prefix, uri);
      soapElement = addChildElement(soapElement);
      return soapElement;
   }

   /**
    * Creates a new SOAPElement object initialized with the given Name object and adds the new element to this SOAPElement object.
    *
    * @param name a Name object with the XML name for the new element
    * @return the new SOAPElement object that was created
    * @throws javax.xml.soap.SOAPException if there is an error in creating the SOAPElement object
    */
   public SOAPElement addChildElement(Name name) throws SOAPException
   {
      SOAPElement soapElement = new SOAPElementImpl(name);
      soapElement = addChildElement(soapElement);
      return soapElement;
   }

   /**
    * Add a SOAPElement as a child of this SOAPElement instance. The SOAPElement is expected to be created by a
    * SOAPElementFactory.
    * <p/>
    * Callers should not rely on the element instance being added as is into the XML tree.
    * Implementations could end up copying the content of the SOAPElement passed into an instance of a different SOAPElement
    * implementation. For instance if addChildElement() is called on a SOAPHeader, element will be copied into an instance
    * of a SOAPHeaderElement.
    * <p/>
    * The fragment rooted in element is either added as a whole or not at all, if there was an error.
    * <p/>
    * The fragment rooted in element cannot contain elements named "Envelope", "Header" or "Body" and in the SOAP namespace.
    * Any namespace prefixes present in the fragment should be fully resolved using appropriate namespace declarations
    * within the fragment itself.
    *
    * @param child the SOAPElement to be added as a new child
    * @return an instance representing the new SOAP element that was actually added to the tree.
    * @throws javax.xml.soap.SOAPException if there was an error in adding this element as a child
    */
   public SOAPElement addChildElement(SOAPElement child) throws SOAPException
   {
      log.trace("addChildElement: " + getElementName() + " -> " + child.getElementName());
      SOAPElementImpl soapElement = (SOAPElementImpl)child;
      soapElement = (SOAPElementImpl)appendChild(soapElement);
      return soapElement.completeNamespaceDeclaration();
   }

   /**
    * Adds a namespace declaration with the specified prefix and URI to this SOAPElement object.
    *
    * @param prefix a String giving the prefix of the namespace
    * @param nsURI    a String giving the uri of the namespace
    * @return the SOAPElement object into which this namespace declaration was inserted.    
    */
   public SOAPElement addNamespaceDeclaration(String prefix, String nsURI)
   {
      if (nsURI == null)
         throw new IllegalArgumentException("Invalid 'null' namespace URI");
      if (nsURI.length() == 0)
         throw new IllegalArgumentException("Invalid empty namespace URI");

      String qualifiedName = "xmlns";
      if (prefix != null && prefix.length() > 0)
         qualifiedName += ":" + prefix;
      
      log.trace("addNamespaceDeclaration: " + qualifiedName + "='" + nsURI + "'");
      element.setAttributeNS("http://www.w3.org/2000/xmlns/", qualifiedName, nsURI);
      return this;
   }

   // Add the namespace declaration if it is not visible yet
   private SOAPElement completeNamespaceDeclaration()
   {
      String prefix = getPrefix();
      String nsURI = getNamespaceURI();
      if (prefix != null && nsURI != null)
      {
         String prevNS = getNamespaceURI(prefix);
         if (nsURI.equals(prevNS) == false)
            addNamespaceDeclaration(prefix, nsURI);
      }
      return this;
   }

   /**
    * Creates a new Text object initialized with the given String and adds it to this SOAPElement object.
    *
    * @param value a String object with the textual content to be added
    * @return the SOAPElement object into which the new Text object was inserted
    * @throws javax.xml.soap.SOAPException if there is an error in creating the new Text object
    */
   public SOAPElement addTextNode(String value) throws SOAPException
   {
      log.trace("addTextNode: " + value);
      org.w3c.dom.Text domText = element.getOwnerDocument().createTextNode(value);
      javax.xml.soap.Text soapText = new TextImpl(domText);
      appendChild(soapText);
      return this;
   }

   /**
    * Returns an Iterator over all of the attribute Name objects in this SOAPElement object.
    * <p/>
    * The iterator can be used to get the attribute names, which can then be passed to the method getAttributeValue to
    * retrieve the value of each attribute.
    *
    * @return an iterator over the names of the attributes
    */
   public Iterator getAllAttributes()
   {
      ArrayList list = new ArrayList();
      NamedNodeMap nnm = getAttributes();
      for (int i = 0; i < nnm.getLength(); i++)
      {
         org.w3c.dom.Node node = (org.w3c.dom.Node)nnm.item(i);
         String local = node.getLocalName();
         String prefix = node.getPrefix();
         String uri = node.getNamespaceURI();
         if ("xmlns".equals(prefix) == false)
         {
            list.add(new NameImpl(local, prefix, uri));
         }         
      }
      return list.iterator();
   }

   /**
    * Returns the value of the attribute with the specified name.
    *
    * @param name a Name object with the name of the attribute
    * @return a String giving the value of the specified attribute
    */
   public String getAttributeValue(Name name)
   {
      Attr attr = getAttributeNode(name);
      return (attr != null ? attr.getValue() : null);
   }

   private Attr getAttributeNode(Name name)
   {
      Attr attr = null;
      String nsURI = name.getURI();
      if (nsURI.length() > 0)
         attr = element.getAttributeNodeNS(nsURI, name.getLocalName());
      else
         attr = element.getAttributeNode(name.getLocalName());

      return attr;
   }

   /**
    * Returns an Iterator over all the immediate child Nodes of this element.
    * <p/>
    * This includes javax.xml.soap.Text objects as well as SOAPElement objects.
    * Calling this method may cause child Element, SOAPElement and org.w3c.dom.Text nodes to be replaced by SOAPElement,
    * SOAPHeaderElement, SOAPBodyElement or javax.xml.soap.Text nodes as appropriate for the type of this parent node.
    * As a result the calling application must treat any existing references to these child nodes that have been obtained
    * through DOM APIs as invalid and either discard them or refresh them with the values returned by this Iterator.
    * This behavior can be avoided by calling the equivalent DOM APIs. See javax.xml.soap for more details.
    *
    * @return an iterator with the content of this SOAPElement object
    */
   public Iterator getChildElements()
   {
      List list = new ArrayList();
      NodeList nodeList = getChildNodes();
      for (int i = 0; i < nodeList.getLength(); i++)
      {
         org.w3c.dom.Node node = nodeList.item(i);
         if (node instanceof SOAPElement)
         {
            list.add(node);
         }
         else if (node instanceof Text)
         {
            String value = node.getNodeValue();
            if (value.trim().length() > 0)
               list.add(node);
         }
      }
      return list.iterator();
   }

   /**
    * Returns an Iterator over all the immediate child Nodes of this element with the specified name.
    * <p/>
    * All of these children will be SOAPElement nodes.
    * Calling this method may cause child Element, SOAPElement and org.w3c.dom.Text nodes to be replaced by SOAPElement,
    * SOAPHeaderElement, SOAPBodyElement or javax.xml.soap.Text nodes as appropriate for the type of this parent node.
    * As a result the calling application must treat any existing references to these child nodes that have been obtained
    * through DOM APIs as invalid and either discard them or refresh them with the values returned by this Iterator.
    * This behavior can be avoided by calling the equivalent DOM APIs. See javax.xml.soap for more details.
    *
    * @param name a Name object with the name of the child elements to be returned
    * @return an Iterator object over all the elements in this SOAPElement object with the specified name
    */
   public Iterator getChildElements(Name name)
   {
      List<SOAPElement> list = new ArrayList<SOAPElement>();
      Iterator it = getChildElements();
      while (it.hasNext())
      {
         Object elementOrTextNode = it.next();
         if (elementOrTextNode instanceof SOAPElement)
         {
            SOAPElement el = (SOAPElement)elementOrTextNode;
            if (el.getElementName().equals(name))
               list.add(el);
         }
      }
      return list.iterator();
   }

   /**
    * Returns the name of this SOAPElement object.
    *
    * @return a Name object with the name of this SOAPElement object
    */
   public Name getElementName()
   {

      String nsURI = element.getNamespaceURI();
      if (nsURI != null && nsURI.length() > 0)
      {
         String prefix = element.getPrefix();
         String localName = element.getLocalName();
         return new NameImpl(localName, prefix, nsURI);
      }
      else
      {
         String nodeName = element.getNodeName();
         return new NameImpl(nodeName);
      }
   }

   /**
    * Returns the encoding style for this SOAPElement object.
    *
    * @return a String giving the encoding style
    */
   public String getEncodingStyle()
   {
      return encodingStyle;
   }

   /**
    * Returns an Iterator over the namespace prefix Strings declared by this element.
    * <p/>
    * The prefixes returned by this iterator can be passed to the method getNamespaceURI to retrieve the URI of each namespace.
    *
    * @return an iterator over the namespace prefixes in this SOAPElement object
    */
   public Iterator getNamespacePrefixes()
   {
      ArrayList list = getNamespacePrefixList();
      return list.iterator();
   }

   private ArrayList getNamespacePrefixList()
   {
      ArrayList list = new ArrayList();
      NamedNodeMap attrMap = element.getAttributes();
      for (int i = 0; i < attrMap.getLength(); i++)
      {
         Attr attr = (Attr)attrMap.item(i);
         String attrName = attr.getNodeName();
         if (attrName.startsWith("xmlns:"))
            list.add(attrName.substring(6));
      }
      return list;
   }

   /**
    * Returns the URI of the namespace that has the given prefix.
    *
    * @param prefix a String giving the prefix of the namespace for which to search
    * @return a String with the uri of the namespace that has the given prefix
    */
   public String getNamespaceURI(String prefix)
   {
      String nsURI = element.getAttribute("xmlns:" + prefix);
      if (nsURI.length() == 0 && getParentElement() != null)
         return getParentElement().getNamespaceURI(prefix);

      return (nsURI.length() > 0 ? nsURI : null);
   }

   /**
    * Returns an Iterator over the namespace prefix Strings visible to this element.
    * <p/>
    * The prefixes returned by this iterator can be passed to the method getNamespaceURI to retrieve the URI of each namespace.
    *
    * @return an iterator over the namespace prefixes are within scope of this SOAPElement object
    */
   public Iterator getVisibleNamespacePrefixes()
   {
      ArrayList list = getNamespacePrefixList();
      SOAPElementImpl parent = (SOAPElementImpl)getParentElement();
      while (parent != null)
      {
         list.addAll(parent.getNamespacePrefixList());
         parent = (SOAPElementImpl)parent.getParentElement();
      }
      return list.iterator();
   }

   /**
    * Removes the attribute with the specified name.
    *
    * @param name the Name object with the name of the attribute to be removed
    * @return true if the attribute was removed successfully; false if it was not
    */
   public boolean removeAttribute(Name name)
   {
      Attr attr = getAttributeNode(name);
      if (attr != null)
      {
         element.removeAttributeNode(attr);
         return true;
      }
      return false;
   }

   /**
    * Detaches all children of this SOAPElement.
    * <p/>
    * This method is useful for rolling back the construction of partially completed SOAPHeaders and SOAPBodys in
    * preparation for sending a fault when an error condition is detected.
    * It is also useful for recycling portions of a document within a SOAP message.
    */
   public void removeContents()
   {
      log.trace("removeContents");
      Iterator it = getChildElements();
      while (it.hasNext())
      {
         SOAPElement el = (SOAPElement)it.next();
         el.detachNode();
      }
   }

   /**
    * Removes the namespace declaration corresponding to the given prefix.
    *
    * @param prefix a String giving the prefix for which to search
    * @return true if the namespace declaration was removed successfully; false if it was not
    */
   public boolean removeNamespaceDeclaration(String prefix)
   {
      boolean ret = getAttributeNode("xmlns:" + prefix) != null;
      removeAttribute("xmlns:" + prefix);
      return ret;
   }

   /**
    * Sets the encoding style for this SOAPElement object to one specified.
    *
    * @param encodingStyle a String giving the encoding style
    * @throws javax.xml.soap.SOAPException if there was a problem in the encoding style being set.
    */
   public void setEncodingStyle(String encodingStyle) throws SOAPException
   {
      if (!Constants.URI_LITERAL_ENC.equals(encodingStyle) && !Constants.URI_SOAP11_ENC.equals(encodingStyle))
         throw new IllegalArgumentException("Unsupported encodingStyle: " + encodingStyle);

      this.encodingStyle = encodingStyle;
   }

   // org.w3c.Element ***********************************************************************************************

   public String getTagName()
   {
      return element.getTagName();
   }

   public void removeAttribute(String name) throws DOMException
   {
      element.removeAttribute(name);
   }

   public boolean hasAttribute(String name)
   {
      return element.hasAttribute(name);
   }

   public String getAttribute(String name)
   {
      return element.getAttribute(name);
   }

   public void removeAttributeNS(String namespaceURI, String localName) throws DOMException
   {
      element.removeAttributeNS(namespaceURI, localName);
   }

   public void setAttribute(String name, String value) throws DOMException
   {
      element.setAttribute(name, value);
   }

   public boolean hasAttributeNS(String namespaceURI, String localName)
   {
      return element.hasAttributeNS(namespaceURI, localName);
   }

   public Attr getAttributeNode(String name)
   {
      Attr attr = element.getAttributeNode(name);

      return (attr == null) ? null : new AttrImpl(this, attr);
   }

   public Attr removeAttributeNode(Attr oldAttr) throws DOMException
   {
      return element.removeAttributeNode(oldAttr);
   }

   public Attr setAttributeNode(Attr newAttr) throws DOMException
   {
      return element.setAttributeNode(newAttr);
   }

   public Attr setAttributeNodeNS(Attr newAttr) throws DOMException
   {
      return element.setAttributeNodeNS(newAttr);
   }

   public NodeList getElementsByTagName(String name)
   {
      return new NodeListImpl(DOMUtils.getChildElements(this, name));
   }

   public String getAttributeNS(String namespaceURI, String localName)
   {
      return element.getAttributeNS(namespaceURI, localName);
   }

   public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException
   {
      element.setAttributeNS(namespaceURI, qualifiedName, value);
   }

   public Attr getAttributeNodeNS(String namespaceURI, String localName)
   {
      /* FIXME We really need to do more than just return an object wrapper.
       * All Attrs should be stored as nodes on our local tree, so that
       * they are discovered during node traversal calls.
       */
      Attr attr = element.getAttributeNodeNS(namespaceURI, localName);

      return (attr == null) ? null : new AttrImpl(this, attr);
   }

   public NodeList getElementsByTagNameNS(String namespaceURI, String localName)
   {
      return new NodeListImpl(DOMUtils.getChildElements(this, new QName(namespaceURI, localName)));
   }

   public TypeInfo getSchemaTypeInfo()
   {
      // FIXME getSchemaTypeInfo
      throw new org.jboss.util.NotImplementedException("getSchemaTypeInfo");
   }

   public void setIdAttribute(String name, boolean isId) throws DOMException
   {
      // FIXME setIdAttribute
      throw new org.jboss.util.NotImplementedException("setIdAttribute");
   }

   public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException
   {
      // FIXME setIdAttributeNode
      throw new org.jboss.util.NotImplementedException("setIdAttributeNode");
   }

   public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException
   {
      // FIXME setIdAttributeNS
      throw new org.jboss.util.NotImplementedException("setIdAttributeNS");
   }
   /**
    * The default implementation uses a DOMWriter.
    * SOAPContentElements overwrite this to optimize DOM callbacks.
    *
    * @param writer
    * @param pretty
    *
    * @return end element tag
    */
   public String write(Writer writer, boolean pretty)
   {
      /*DOMWriter domWriter = new DOMWriter(writer);
      domWriter.setPrettyprint(pretty);
      domWriter.print(this);
      */
      return null;
   }
}
