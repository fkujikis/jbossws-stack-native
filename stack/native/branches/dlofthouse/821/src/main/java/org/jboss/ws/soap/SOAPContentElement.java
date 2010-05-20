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

import org.jboss.logging.Logger;
import org.jboss.util.xml.DOMUtils;
import org.jboss.util.xml.DOMWriter;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.binding.BindingException;
import org.jboss.ws.jaxrpc.TypeMappingImpl;
import org.jboss.ws.jaxrpc.encoding.*;
import org.jboss.ws.metadata.ParameterMetaData;
import org.jboss.ws.utils.JavaUtils;
import org.jboss.ws.utils.ThreadLocalAssociation;
import org.jboss.ws.xop.XOPContext;
import org.w3c.dom.*;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * A SOAPElement that gives access to its content as XML fragment or Java object.
 *
 * The SOAPContentElement has three content representations, which may exist in parallel.
 * The getter and setter of the content properties perform the conversions.
 * It is the responsibility of this objects to keep the representations in sync.
 *
 * +---------+         +-------------+          +-------------+
 * | Object  | <-----> | XMLFragment |  <-----> | DOMTree     |
 * +---------+         +-------------+          +-------------+
 *
 * The idea is, that jaxrpc handlers can work with both the object and the dom view of this SOAPElement.
 * Note, that state transitions may be expensive.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 13-Dec-2004
 */
public class SOAPContentElement extends SOAPElementImpl
{
   // provide logging
   private static Logger log = Logger.getLogger(SOAPContentElement.class);

   // The well formed XML content of this element.
   private String xmlFragment;
   // The java object content of this element.
   private Object objectValue;
   // True if the current DOM tree is valid
   private boolean isDOMValid;
   // True if the current content object is valid
   private boolean isObjectValid;
   // True while expanding to DOM
   private boolean expandingToDOM;

   // The associated parameter
   private ParameterMetaData paramMetaData;

   /** Construct a SOAPContentElement
    */
   public SOAPContentElement(Name name)
   {
      super(name);
   }

   public SOAPContentElement(SOAPElementImpl element)
   {
      super(element);
      isDOMValid = true;
   }

   public ParameterMetaData getParamMetaData()
   {
      return paramMetaData;
   }

   public void setParamMetaData(ParameterMetaData paramMetaData)
   {
      this.paramMetaData = paramMetaData;
   }

   public QName getXmlType()
   {
      return (paramMetaData != null ? paramMetaData.getXmlType() : null);
   }

   public Class getJavaType()
   {
      return (paramMetaData != null ? paramMetaData.getJavaType() : null);
   }

   public String getXMLFragment() throws SOAPException
   {
      // Serialize the valueContent
      if (xmlFragment == null && isObjectValid)
      {
         assertContentMapping();

         QName xmlType = getXmlType();
         Class javaType = getJavaType();
         log.debug("getXMLFragment from Object [xmlType=" + xmlType + ",javaType=" + javaType + "]");

         SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
         if (msgContext == null)
            throw new WSException("MessageContext not available");

         SerializationContextImpl serContext = msgContext.getSerializationContext();
         serContext.setProperty(SerializationContextImpl.PROPERTY_PARAMETER_META_DATA, paramMetaData);

         TypeMappingImpl typeMapping = serContext.getTypeMapping();

         QName xmlName = getQName();
         Boolean domExpansionState = ThreadLocalAssociation.localDomExpansion().get();
         try
         {
            ThreadLocalAssociation.localDomExpansion().set(Boolean.FALSE);
            SerializerSupport ser;
            if (objectValue != null)
            {               
               SerializerFactoryBase serializerFactory = getSerializerFactory(typeMapping, javaType, xmlType);
               ser = (SerializerSupport)serializerFactory.getSerializer();
            }
            else
            {
               ser = new NullValueSerializer();
               if (getNamespaceURI(Constants.PREFIX_XSI) == null)
                  addNamespaceDeclaration(Constants.PREFIX_XSI, Constants.NS_SCHEMA_XSI);
            }

            xmlFragment = ser.serialize(xmlName, xmlType, getObjectValue(), serContext, null);

            // Add the arrayType namespace declaration
            QName compXmlType = paramMetaData.getSOAPArrayCompType();
            if (compXmlType != null)
            {
               String nsURI = getNamespaceURI(compXmlType.getPrefix());
               if (nsURI == null)
                  addNamespaceDeclaration(compXmlType.getPrefix(), compXmlType.getNamespaceURI());
            }

            log.debug("xmlFragment: " + xmlFragment);
         }
         catch (BindingException e)
         {
            throw new JAXRPCException(e);
         }
         finally {
            ThreadLocalAssociation.localDomExpansion().set(domExpansionState);
         }
      }

      // Generate the xmlFragment from the DOM tree
      else if (xmlFragment == null && isDOMValid)
      {
         log.debug("getXMLFragment from DOM");
         xmlFragment = DOMWriter.printNode(this, false);
         log.debug("xmlFragment: " + xmlFragment);
      }

      if (xmlFragment == null || xmlFragment.startsWith("<") == false)
         throw new WSException("Invalid XMLFragment: " + xmlFragment);

      return xmlFragment;
   }

   public void setXMLFragment(String xmlFragment)
   {
      log.debug("setXMLFragment: " + xmlFragment);

      if (xmlFragment == null || xmlFragment.startsWith("<") == false)
         throw new WSException("Invalid XMLFragment: " + xmlFragment);

      removeContentsAsIs();
      removeAttributesAsIs();
      resetElementContent();

      this.xmlFragment = xmlFragment;
      invalidateDOMContent();
      invalidateObjectContent();
   }

   public Object getObjectValue() throws SOAPException
   {
      if (isObjectValid == false)
      {
         QName xmlType = getXmlType();
         Class javaType = getJavaType();

         log.debug("getObjectValue [xmlType=" + xmlType + ",javaType=" + javaType + "]");
         assertContentMapping();

         SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
         if (msgContext == null)
            throw new WSException("MessageContext not available");

         SerializationContextImpl serContext = msgContext.getSerializationContext();
         serContext.setProperty(SerializationContextImpl.PROPERTY_PARAMETER_META_DATA, paramMetaData);

         try
         {
            // Get the deserializer from the type mapping
            TypeMappingImpl typeMapping = serContext.getTypeMapping();
            DeserializerFactoryBase deserializerFactory = getDeserializerFactory(typeMapping, javaType, xmlType);
            DeserializerSupport des = (DeserializerSupport)deserializerFactory.getDeserializer();

            String strContent = getXMLFragment();

            Object obj = des.deserialize(getQName(), xmlType, strContent, serContext);
            if (obj != null)
            {
               Class objType = obj.getClass();
               boolean isAssignable = JavaUtils.isAssignableFrom(javaType, objType);
               if (isAssignable == false && javaType.isArray())
               {
                  try
                  {
                     Method toArrayMethod = objType.getMethod("toArray", new Class[] {});
                     Class returnType = toArrayMethod.getReturnType();
                     if (JavaUtils.isAssignableFrom(javaType, returnType))
                     {
                        Method getValueMethod = objType.getMethod("getValue", new Class[] {});
                        Object value = getValueMethod.invoke(obj, new Object[] {});
                        if (value != null)
                        {
                           // Do not invoke toArray if getValue returns null
                           obj = toArrayMethod.invoke(obj, new Object[] {});
                        }
                        else
                        {
                           // if the fragment did not indicate a null return
                           // by an xsi:nil we return an empty array
                           Class componentType = javaType.getComponentType();
                           obj = Array.newInstance(componentType, 0);
                        }
                        isAssignable = true;
                     }
                  }
                  catch (Exception e)
                  {
                     // ignore
                  }
               }

               if (isAssignable == false)
               {
                  Object convertedObj = null;

                  if(obj instanceof DataHandler)
                  {
                     try
                     {
                        convertedObj = ((DataHandler)obj).getContent();
                     }
                     catch (IOException e)
                     {
                        throw new WSException("Failed to convert unassignable endpoint parameter", e);
                     }
                  }

                  if(null == convertedObj || !JavaUtils.isAssignableFrom(javaType, convertedObj.getClass()) ) // conversion failed
                  {
                     throw new WSException("Java type '" + javaType + "' is not assignable from: " + objType.getName());
                  }
                  else
                  {
                     obj = convertedObj;
                  }
               }
            }

            this.objectValue = obj;
            this.isObjectValid = true;
         }
         catch (BindingException e)
         {
            throw new JAXRPCException(e);
         }

         log.debug("objectValue: " + (objectValue != null ? objectValue.getClass().getName() : null));
      }

      return objectValue;
   }

   public void setObjectValue(Object objValue)
   {
      log.debug("setObjectValue: " + objValue);
      removeContentsAsIs();
      resetElementContent();
      this.objectValue = objValue;
      this.isObjectValid = true;
   }


   private void removeContentsAsIs()
   {
      log.trace("removeContentsAsIs");
      boolean cachedFlag = isDOMValid;
      try
      {
         this.isDOMValid = true;
         super.removeContents();
      }
      finally
      {
         this.isDOMValid = cachedFlag;
      }
   }

   /** Remove the attributes that represent bean properties.
    */
   private void removeAttributesAsIs()
   {
      log.trace("removeAttributesAsIs");
      boolean cachedFlag = isDOMValid;
      try
      {
         this.isDOMValid = true;
         Iterator it = super.getAllAttributes();
         while (it.hasNext())
         {
            Name attrName = (Name)it.next();
            if ("xmlns".equals(attrName.getPrefix()) == false)
               removeAttribute(attrName);
         }
      }
      finally
      {
         this.isDOMValid = cachedFlag;
      }
   }

   // Get the serializer factory for a given javaType and xmlType
   private SerializerFactoryBase getSerializerFactory(TypeMappingImpl typeMapping, Class javaType, QName xmlType)
   {
      SerializerFactoryBase serializerFactory = (SerializerFactoryBase)typeMapping.getSerializer(javaType, xmlType);

      // The type mapping might contain a mapping for the array wrapper bean
      if (serializerFactory == null && javaType.isArray())
      {
         Class arrayWrapperType = typeMapping.getJavaType(xmlType);
         if (arrayWrapperType != null)
         {
            try
            {
               Method toArrayMethod = arrayWrapperType.getMethod("toArray", new Class[] {});
               Class returnType = toArrayMethod.getReturnType();
               if (JavaUtils.isAssignableFrom(javaType, returnType))
               {
                  serializerFactory = (SerializerFactoryBase)typeMapping.getSerializer(arrayWrapperType, xmlType);
               }
            }
            catch (NoSuchMethodException e)
            {
               // ignore
            }
         }
      }

      if (serializerFactory == null)
         throw new JAXRPCException("Cannot obtain serializer factory for: [xmlType=" + xmlType + ",javaType=" + javaType + "]");

      return serializerFactory;
   }

   // Get the deserializer factory for a given javaType and xmlType
   private DeserializerFactoryBase getDeserializerFactory(TypeMappingImpl typeMapping, Class javaType, QName xmlType)
   {
      DeserializerFactoryBase deserializerFactory = (DeserializerFactoryBase)typeMapping.getDeserializer(javaType, xmlType);

      // The type mapping might contain a mapping for the array wrapper bean
      if (deserializerFactory == null && javaType.isArray())
      {
         Class arrayWrapperType = typeMapping.getJavaType(xmlType);
         if (arrayWrapperType != null)
         {
            try
            {
               Method toArrayMethod = arrayWrapperType.getMethod("toArray", new Class[] {});
               Class returnType = toArrayMethod.getReturnType();
               if (JavaUtils.isAssignableFrom(javaType, returnType))
               {
                  deserializerFactory = (DeserializerFactoryBase)typeMapping.getDeserializer(arrayWrapperType, xmlType);
               }
            }
            catch (NoSuchMethodException e)
            {
               // ignore
            }
         }
      }

      if (deserializerFactory == null)
         throw new JAXRPCException("Cannot obtain deserializer factory for: [xmlType=" + xmlType + ",javaType=" + javaType + "]");

      return deserializerFactory;
   }

   /** Assert the notNull state of the xmlType and javaType
    */
   private void assertContentMapping()
   {
      if (getJavaType() == null)
         throw new WSException("javaType cannot be null");
      if (getXmlType() == null)
         throw new WSException("xmlType cannot be null");
   }

   // SOAPElement interface ********************************************************************************************

   public SOAPElement addChildElement(SOAPElement child) throws SOAPException
   {
      expandToDOM();
      SOAPElement soapElement = super.addChildElement(child);
      invalidateObjectContent();
      invalidateXMLContent();
      return soapElement;
   }

   public SOAPElement addChildElement(String localName, String prefix) throws SOAPException
   {
      expandToDOM();
      SOAPElement soapElement = super.addChildElement(localName, prefix);
      invalidateObjectContent();
      invalidateXMLContent();
      return soapElement;
   }

   public SOAPElement addChildElement(String localName, String prefix, String uri) throws SOAPException
   {
      expandToDOM();
      SOAPElement soapElement = super.addChildElement(localName, prefix, uri);
      invalidateObjectContent();
      invalidateXMLContent();
      return soapElement;
   }

   public SOAPElement addChildElement(Name name) throws SOAPException
   {
      expandToDOM();
      SOAPElement soapElement = super.addChildElement(name);
      invalidateObjectContent();
      invalidateXMLContent();
      return soapElement;
   }

   public SOAPElement addChildElement(String name) throws SOAPException
   {
      expandToDOM();
      SOAPElement soapElement = super.addChildElement(name);
      invalidateObjectContent();
      invalidateXMLContent();
      return soapElement;
   }

   public SOAPElement addTextNode(String value) throws SOAPException
   {
      expandToDOM();
      SOAPElement soapElement = super.addTextNode(value);
      invalidateObjectContent();
      invalidateXMLContent();
      return soapElement;
   }

   public Iterator getChildElements()
   {
      expandToDOM();
      return super.getChildElements();
   }

   public Iterator getChildElements(Name name)
   {
      expandToDOM();
      return super.getChildElements(name);
   }

   public void removeContents()
   {
      expandToDOM();
      super.removeContents();
      invalidateObjectContent();
      invalidateXMLContent();
   }

   public Iterator getAllAttributes()
   {
      expandToDOM();
      return super.getAllAttributes();
   }

   public String getAttribute(String name)
   {
      expandToDOM();
      return super.getAttribute(name);
   }

   public Attr getAttributeNode(String name)
   {
      expandToDOM();
      return super.getAttributeNode(name);
   }

   public Attr getAttributeNodeNS(String namespaceURI, String localName)
   {
      expandToDOM();
      return super.getAttributeNodeNS(namespaceURI, localName);
   }

   public String getAttributeNS(String namespaceURI, String localName)
   {
      expandToDOM();
      return super.getAttributeNS(namespaceURI, localName);
   }

   public String getAttributeValue(Name name)
   {
      expandToDOM();
      return super.getAttributeValue(name);
   }

   public SOAPElement addAttribute(Name name, String value) throws SOAPException
   {
      expandToDOM();
      return super.addAttribute(name, value);
   }

   public SOAPElement addNamespaceDeclaration(String prefix, String nsURI)
   {
      expandToDOM();
      return super.addNamespaceDeclaration(prefix, nsURI);
   }

   public Name getElementName()
   {
      return super.getElementName();
   }

   public NodeList getElementsByTagName(String name)
   {
      expandToDOM();
      return super.getElementsByTagName(name);
   }

   public NodeList getElementsByTagNameNS(String namespaceURI, String localName)
   {
      expandToDOM();
      return super.getElementsByTagNameNS(namespaceURI, localName);
   }

   public String getEncodingStyle()
   {
      expandToDOM();
      return super.getEncodingStyle();
   }

   public Iterator getNamespacePrefixes()
   {
      expandToDOM();
      return super.getNamespacePrefixes();
   }

   public String getNamespaceURI(String prefix)
   {
      expandToDOM();
      return super.getNamespaceURI(prefix);
   }

   public TypeInfo getSchemaTypeInfo()
   {
      expandToDOM();
      return super.getSchemaTypeInfo();
   }

   public String getTagName()
   {
      expandToDOM();
      return super.getTagName();
   }

   public Iterator getVisibleNamespacePrefixes()
   {
      expandToDOM();
      return super.getVisibleNamespacePrefixes();
   }

   public boolean hasAttribute(String name)
   {
      expandToDOM();
      return super.hasAttribute(name);
   }

   public boolean hasAttributeNS(String namespaceURI, String localName)
   {
      expandToDOM();
      return super.hasAttributeNS(namespaceURI, localName);
   }

   public boolean removeAttribute(Name name)
   {
      expandToDOM();
      log.trace("removeAttribute: " + name.getQualifiedName());
      return super.removeAttribute(name);
   }

   public void removeAttribute(String name) throws DOMException
   {
      expandToDOM();
      log.trace("removeAttribute: " + name);
      super.removeAttribute(name);
   }

   public Attr removeAttributeNode(Attr oldAttr) throws DOMException
   {
      expandToDOM();
      log.trace("removeAttribute: " + oldAttr.getNodeName());
      return super.removeAttributeNode(oldAttr);
   }

   public void removeAttributeNS(String namespaceURI, String localName) throws DOMException
   {
      expandToDOM();
      log.trace("removeAttributeNS: {" + namespaceURI + "}" + localName);
      super.removeAttributeNS(namespaceURI, localName);
   }

   public boolean removeNamespaceDeclaration(String prefix)
   {
      expandToDOM();
      log.trace("removeNamespaceDeclaration: " + prefix);
      return super.removeNamespaceDeclaration(prefix);
   }

   public void setAttribute(String name, String value) throws DOMException
   {
      expandToDOM();
      log.trace("setAttribute: [name=" + name + ",value=" + value + "]");
      super.setAttribute(name, value);
   }

   public Attr setAttributeNode(Attr newAttr) throws DOMException
   {
      expandToDOM();
      return super.setAttributeNode(newAttr);
   }

   public Attr setAttributeNodeNS(Attr newAttr) throws DOMException
   {
      expandToDOM();
      return super.setAttributeNodeNS(newAttr);
   }

   public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException
   {
      expandToDOM();
      super.setAttributeNS(namespaceURI, qualifiedName, value);
   }

   public void setEncodingStyle(String encodingStyle) throws SOAPException
   {
      expandToDOM();
      super.setEncodingStyle(encodingStyle);
   }

   public void setIdAttribute(String name, boolean isId) throws DOMException
   {
      expandToDOM();
      super.setIdAttribute(name, isId);
   }

   public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException
   {
      expandToDOM();
      super.setIdAttributeNode(idAttr, isId);
   }

   public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException
   {
      expandToDOM();
      super.setIdAttributeNS(namespaceURI, localName, isId);
   }

   // Node interface **************************************************************************************************

   public Node appendChild(Node newChild) throws DOMException
   {
      log.trace("appendChild: " + newChild);
      expandToDOM();
      Node node = super.appendChild(newChild);
      invalidateObjectContent();
      invalidateXMLContent();
      return node;
   }

   public Node cloneNode(boolean deep)
   {
      log.trace("cloneNode: deep=" + deep);
      expandToDOM();
      return super.cloneNode(deep);
   }

   public NodeList getChildNodes()
   {
      expandToDOM();
      return super.getChildNodes();
   }

   public Node getFirstChild()
   {
      expandToDOM();
      return super.getFirstChild();
   }

   public Node getLastChild()
   {
      expandToDOM();
      return super.getLastChild();
   }

   public String getValue()
   {
      expandToDOM();
      return super.getValue();
   }

   public boolean hasChildNodes()
   {
      expandToDOM();
      return super.hasChildNodes();
   }

   public Node removeChild(Node oldChild) throws DOMException
   {
      log.trace("removeChild: " + oldChild);
      expandToDOM();
      Node node = super.removeChild(oldChild);
      invalidateObjectContent();
      invalidateXMLContent();
      return node;
   }

   public Node replaceChild(Node newChild, Node oldChild) throws DOMException
   {
      log.trace("replaceChild: [new=" + newChild + ",old=" + oldChild + "]");
      expandToDOM();
      Node node = super.replaceChild(newChild, oldChild);
      invalidateObjectContent();
      invalidateXMLContent();
      return node;
   }

   public void setValue(String value)
   {
      log.trace("setValue: " + value);
      expandToDOM();
      super.setValue(value);
      invalidateObjectContent();
      invalidateXMLContent();
   }

   public NamedNodeMap getAttributes()
   {
      expandToDOM();
      return super.getAttributes();
   }

   public boolean hasAttributes()
   {
      expandToDOM();
      return super.hasAttributes();
   }

   // END Node interface ***********************************************************************************************

   /** Expand the content, generating appropriate child nodes
    */
   private void expandToDOM()
   {
      // If JBossWS itself uses the SAAJ API, we can safely disable
      // DOM expansion, assuming it does everything right.
      // SOAPContentElements should only be expanded when jaxxrpc handlers do require it.
      boolean domExpansionEnabled = ThreadLocalAssociation.localDomExpansion().get().booleanValue();
      if (isDOMValid == false && expandingToDOM == false && domExpansionEnabled)
      {
         log.trace("BEGIN: expandToDOM");
         expandingToDOM = true;

         try
         {
            if (xmlFragment == null && isObjectValid)
               xmlFragment = getXMLFragment();

            if (xmlFragment != null)
            {
               String wrappedXMLFragment = insertNamespaceDeclarations("<wrapper>" + xmlFragment + "</wrapper>");
               Element contentRoot = DOMUtils.parse(wrappedXMLFragment);
               contentRoot = DOMUtils.getFirstChildElement(contentRoot);

               String rootLocalName = contentRoot.getLocalName();
               String rootPrefix = contentRoot.getPrefix();
               String rootNS = contentRoot.getNamespaceURI();
               Name contentRootName = new NameImpl(rootLocalName, rootPrefix, rootNS);

               // Make sure the content root element name matches this element name
               Name elementName = getElementName();
               if (contentRootName.equals(elementName) == false)
                  throw new WSException("Content root name does not match element name: " + contentRootName + " != " + elementName);

               // Copy attributes
               DOMUtils.copyAttributes(this, contentRoot);

               SOAPFactoryImpl soapFactory = new SOAPFactoryImpl();

               NodeList nlist = contentRoot.getChildNodes();
               for (int i = 0; i < nlist.getLength(); i++)
               {
                  Node child = nlist.item(i);
                  short childType = child.getNodeType();
                  if (childType == Node.ELEMENT_NODE)
                  {
                     SOAPElementImpl soapElement = soapFactory.createElement((Element)child, true);
                     super.addChildElement(soapElement);
                     if(isXOPParameter())                     
                        XOPContext.inlineXOPData(soapElement);
                  }
                  else if (childType == Node.TEXT_NODE)
                  {
                     String nodeValue = child.getNodeValue();
                     super.addTextNode(nodeValue);
                  }
                  else if (childType == Node.CDATA_SECTION_NODE)
                  {
                     String nodeValue = child.getNodeValue();
                     super.addTextNode(nodeValue);
                  }
                  else
                  {
                     log.trace("Ignore child type: " + childType);
                  }
               }
            }

            isDOMValid = true;
         }
         catch (RuntimeException e)
         {
            invalidateDOMContent();
            throw e;
         }
         catch (Exception e)
         {
            invalidateDOMContent();
            throw new JAXRPCException(e);
         }
         finally
         {
            expandingToDOM = false;
            log.trace("END: expandToDOM");
         }

         // Either the dom-valid state, or the xml-valid state can be true
         // Therefore we invalidate the xml content.
         invalidateXMLContent();
      }
   }

   public String insertNamespaceDeclarations(String xmlfragment)
   {
      StringBuilder xmlBuffer = new StringBuilder(xmlfragment);

      int endIndex = xmlfragment.indexOf(">");
      int insIndex = endIndex;
      if (xmlfragment.charAt(insIndex - 1) == '/')
         insIndex = insIndex - 1;

      SOAPElement soapElement = this;
      while (soapElement != null)
      {
         Iterator it = soapElement.getNamespacePrefixes();
         while (it.hasNext())
         {
            String prefix = (String)it.next();
            String nsURI = soapElement.getNamespaceURI(prefix);
            String nsDecl = " xmlns:" + prefix + "='" + nsURI + "'";

            // Make sure there is not a duplicate on just the wrapper tag
            int nsIndex = xmlBuffer.indexOf("xmlns:" + prefix);
            if (nsIndex < 0 || nsIndex > endIndex)
            {
               xmlBuffer.insert(insIndex, nsDecl);
               endIndex += nsDecl.length();
            }
         }
         soapElement = soapElement.getParentElement();
      }

      log.trace("insertNamespaceDeclarations: " + xmlBuffer);
      return xmlBuffer.toString();
   }

   private void invalidateDOMContent()
   {
      if (expandingToDOM == false)
      {
         log.trace("invalidateDOMContent");
         this.isDOMValid = false;
      }
   }

   private void invalidateObjectContent()
   {
      if (expandingToDOM == false)
      {
         log.trace("invalidateObjectContent");
         this.isObjectValid = false;
         this.objectValue = null;
      }
   }

   private void invalidateXMLContent()
   {
      if (expandingToDOM == false)
      {
         log.trace("invalidateXMLContent");
         this.xmlFragment = null;
      }
   }

   private void resetElementContent()
   {
      if (expandingToDOM == false)
      {
         log.trace("resetElementContent");
         invalidateDOMContent();
         invalidateObjectContent();
         invalidateXMLContent();
      }
   }
   public String write(Writer writer, boolean pretty) {
      try
      {
         handleMTOMTransitions();

         if(isDOMValid)
         {
            DOMWriter dw = new DOMWriter(writer);
            dw.setPrettyprint(pretty);
            dw.print(this);
         }
         else
         {
            writer.write( getXMLFragment() );
            if(pretty)
               writer.write("\n");
         }

      }
      catch (Exception e)
      {
         log.error("Failed to write SOAPContentElement ", e);
      }

      return null;
   }
   /**
    * When a SOAPContentElement transitions between dom-valid and xml-valid
    * the XOP elements need to transition from XOP optimized to base64 and reverse.<p>
    *
    * If MTOM is disabled through a message context property we always enforce the
    * base64 representation by expanding to DOM, the same happens when a JAXRPC handler
    * accesses the SOAPContentElement.<p>
    *
    * If the element is in dom-valid state (because a handlers accessed it), upon marshalling
    * it's needs to be decided wether or not the <code>xop:Include</code> should be restored.
    * This as well depends upon the message context property.
    */
   private void handleMTOMTransitions() {

      boolean mtomEnabled = isXOPParameter() && XOPContext.isMTOMEnabled();

      if( paramMetaData != null && mtomEnabled == false )
      {
         // If MTOM is disabled, we force dom expansion.
         // This will inline any XOP include element
         // and remove the attachment part when transitioning.
         // See SOAPFactoryImpl for details.

         log.debug("Transitioning to dom-valid state, MTOM disabled");
         expandToDOM();
      }
      else if(isDOMValid && mtomEnabled )
      {
         // When the DOM representation is valid,
         // but MTOM is enabled we need to convert the inlined
         // element back to an xop:Include element and create the attachment part

         log.debug("Transitioning to xml-valid state, MTOM enabled");
         XOPContext.restoreXOPData(this);
      }
   }

   private boolean isXOPParameter() {
      return (paramMetaData != null && paramMetaData.isXOP());
   }
}
