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
package org.jboss.ws.metadata.wsdl;

// $Id$

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.wsdl.xml.WSDLLocator;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.metadata.wsdl.xsd.SchemaUtils;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

/**
 * A factory that creates a WSDL <code>Definition</code> from an URL.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Anil.Saldhana@jboss.org
 * @since 10-Oct-2004
 */
public class WSDL20Reader implements ObjectModelFactory
{
   // provide logging
   private static final Logger log = Logger.getLogger(WSDL20Reader.class);

   private WSDLLocator locator = null;

   public WSDLDefinitions readWSDL(WSDLLocator wsdlLocator) throws WSDLException, IOException
   {
      locator = wsdlLocator;

      String wsdlURI = wsdlLocator.getBaseURI();
      InputSource inputSource = wsdlLocator.getBaseInputSource();
      if (inputSource == null)
         throw new WSException("Cannot obtain WSDL input source for: " + wsdlURI);

      InputStream is = inputSource.getByteStream();
      try
      {
         Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
         unmarshaller.setNamespaceAware(true);
         unmarshaller.setValidation(true);

         WSDLDefinitions wsdl = new WSDLDefinitions();
         wsdl.setWsdlNamespace(Constants.NS_WSDL20);

         unmarshaller.unmarshal(is, this, wsdl);
         return wsdl;
      }
      catch (RuntimeException re)
      {
         throw re;
      }
      catch (Exception e)
      {
         throw new WSDLException("Cannot unmarshal: " + wsdlURI, e);
      }
      finally
      {
         is.close();
      }
   }

   public void setFeature(String name, boolean value)
   {
   }

   /**
    * This method is called by the object model factory and returns the root of the object graph.
    */
   public Object newRoot(Object root, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      /**
       * WSDL2.0 Assertions (http://www.w3.org/TR/wsdl20-primer)
       * The value of the WSDL target namespace MUST be an absolute URI.
       */
      WSDLDefinitions wsdl = null;

      StringBuilder buf = null;

      if (root instanceof WSDLDefinitions)
      {
         wsdl = (WSDLDefinitions)root;

         String targetNamespace = attrs.getValue("targetNamespace");
         boolean abs = (targetNamespace.startsWith("http"));
         if (targetNamespace == null || abs == false)
            throw new RuntimeException("WSDL 2.0 Assertion: targetNamespace must be an absolute uri");

         wsdl.setWsdlNamespace(Constants.NS_WSDL20);
         wsdl.setTargetNamespace(targetNamespace);

         Iterator uris = navigator.getNamespaceURIs();
         while (uris.hasNext())
         {
            String namespace = (String)uris.next();
            String prefix = navigator.getNamespaceContext().getPrefix(namespace);
            if (prefix.length() > 0)
               wsdl.registerNamespaceURI(namespace, prefix);
         }
         return wsdl;
      }
      else if (root instanceof WSDLTypes)
      {
         wsdl = ((WSDLTypes)root).getWsdlDefinitions();
         if ("import".equalsIgnoreCase(localName))
         {
            importTypes(wsdl, attrs);
            return null;
         }
         if ("schema".equalsIgnoreCase(localName))
         {
            //Included Schema - wsdl 2.0
            //We will start writing whatever the parser has obtained
            buf = new StringBuilder("<schema ");
            Iterator uris = navigator.getNamespaceURIs();
            String targetNS = attrs.getValue("targetNamespace");
            buf.append(" targetNamespace='" + targetNS + "'");
            while (uris.hasNext())
            {
               String namespace = (String)uris.next();
               Iterator iter = navigator.getNamespaceContext().getPrefixes(namespace);
               if (namespace.equals(Constants.NS_WSDL20))
                  continue;
               if (iter == null)
                  buf.append(" xmlns='" + namespace + "'");
               else
               {
                  while (iter.hasNext())
                  {
                     String prefix = (String)iter.next();
                     if (prefix.length() == 0)
                        buf.append(" xmlns='" + namespace + "'");
                     else buf.append(" xmlns:" + prefix + "='" + namespace + "'");
                  }
               }
            }
            buf.append(">");
            return buf;
         }
      }
      else if (root instanceof StringBuilder)
      {
         return newChild((StringBuilder)root, navigator, namespaceURI, localName, attrs);
      }
      else
      {
         throw new IllegalArgumentException("Invalid root for namespace: " + namespaceURI);
      }

      return null;
   }

   /**
    * This method is called by the object model factory and returns the root of the object graph.
    */
   public Object newChild(WSDLTypes types, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      /**
       * WSDL2.0 Assertions (http://www.w3.org/TR/wsdl20-primer)
       * The value of the WSDL target namespace MUST be an absolute URI.
       */
      WSDLDefinitions wsdl = types.getWsdlDefinitions();

      StringBuilder buf = null;
      if ("import".equalsIgnoreCase(localName))
      {
         importTypes(wsdl, attrs);
         return null;
      }
      if ("schema".equalsIgnoreCase(localName))
      {
         //Included Schema - wsdl 2.0
         //We will start writing whatever the parser has obtained
         buf = new StringBuilder("<schema ");
         Iterator uris = navigator.getNamespaceURIs();
         String targetNS = attrs.getValue("targetNamespace");
         buf.append(" targetNamespace='" + targetNS + "'");
         while (uris.hasNext())
         {
            String namespace = (String)uris.next();
            Iterator iter = navigator.getNamespaceContext().getPrefixes(namespace);
            if (namespace.equals(Constants.NS_WSDL20))
               continue;
            if (iter == null)
               buf.append(" xmlns='" + namespace + "'");
            else
            {
               while (iter.hasNext())
               {
                  String prefix = (String)iter.next();
                  if (prefix.length() == 0)
                     buf.append(" xmlns='" + namespace + "'");
                  else buf.append(" xmlns:" + prefix + "='" + namespace + "'");
               }
            }
         }
         buf.append(">");
         return buf;
      }

      return null;
   }

   /**
    * The JBossXB has completed the parsing of the WSDL 2.0 document
    * @param root
    * @param ctx
    * @param uri
    * @param name
    * @return
    */
   public Object completeRoot(Object root, UnmarshallingContext ctx, String uri, String name)
   {
      return root;
   }

   /**
    * Used in the parsing of the included XML Schema in WSDL 2.0
    * @param buf
    * @param navigator
    * @param namespaceURI
    * @param localName
    * @param attrs
    * @return
    */
   public Object newChild(StringBuilder buf, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      int len = attrs.getLength();
      buf.append("<" + localName);
      for (int i = 0; i < len; i++)
      {
         String val = attrs.getValue(i);
         String attrname = attrs.getLocalName(i);
         buf.append(" " + attrname + "='" + val + "'");
      }
      buf.append(">");
      return buf;
   }

   /**
    * Used in the parsing of the included XML Schema in WSDL 2.0
    * We have reached the end of an xml element.
    * @param buf
    * @param navigator
    * @param namespaceURI
    * @param localName
    * @return
    */
   public Object addChild(StringBuilder buf, StringBuilder buf1, UnmarshallingContext navigator, String namespaceURI, String localName) throws WSDLException
   {
      buf.append("</" + localName + ">");
      return buf;

   }

   /**
    * Used in the parsing of the included XML Schema in WSDL 2.0
    * We have parsed the schema completely.  Write it into a file and update WSDLTypes (schemaLocation)
    * @param types
    * @param buf
    * @param navigator
    * @param namespaceURI
    * @param localName
    * @return
    * @throws WSDLException
    */
   public Object addChild(WSDLTypes types, StringBuilder buf, UnmarshallingContext navigator, String namespaceURI, String localName) throws WSDLException
   {
      buf.append("</schema>");
      String targetNS = types.getWsdlDefinitions().getTargetNamespace();
      try
      {
         File file = SchemaUtils.getSchemaTempFile(targetNS);
         file.deleteOnExit();
         FileWriter fwrite = new FileWriter(file);
         fwrite.write(buf.toString());
         fwrite.flush();
         fwrite.close();
      }
      catch (IOException e)
      {
         String msg = "Cannot extract schema definition for target namespace: " + targetNS;
         log.error(msg, e);
         throw new WSException(msg);
      }
      return types;
   }

   /**
    * Parsing the WSDL 2.0 document
    * @param wsdl20
    * @param navigator
    * @param namespaceURI
    * @param localName
    * @param attrs
    * @return
    * @throws WSDLException
    */
   public Object newChild(WSDLDefinitions wsdl20, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs) throws WSDLException
   {
      /**
       * WSDL 20 Assertions
       * Each interface must be given a name that is unique within the set of interfaces defined in this WSDL target NS
       * Interface names are tokens that must not contain a space or colon (":")
       */
      String defaultNamespace = wsdl20.getWsdlNamespace();
      if (Constants.NS_WSDL20.equals(defaultNamespace))
      {
         if ("types".equals(localName))
         {
            WSDLTypes wsdlTypes = new WSDLTypes(wsdl20);
            wsdl20.setWsdlTypes(wsdlTypes);
            return wsdlTypes;
         }
         else if ("interface".equals(localName))
         {
            WSDLInterface wsdlInterface = new WSDLInterface(wsdl20);
            String name = attrs.getValue("name");
            if (name == null)
               throw new RuntimeException("Interface name should not be null");
            if (name.indexOf(" ") > -1 || name.indexOf(":") > -1)
               throw new RuntimeException("WSDL 2.0 Assertion:Interface names are tokens " + "that must not contain a space or colon");

            NCName ncname = new NCName(name);
            if (wsdl20.getInterface(ncname) != null)
               throw new RuntimeException("WSDL 2.0 Assertion:Each interface must be given a name that is "
                     + "unique within the set of interfaces defined in this WSDL target namespace");

            wsdlInterface.setName(ncname);
            wsdl20.addInterface(wsdlInterface);

            return wsdlInterface;
         }
         else if ("binding".equals(localName))
         {
            /**
             * WSDL 2.0 Assertion: Each name must be unique among all bindings in this WSDL target namespace
             */
            String name = attrs.getValue("name");
            NCName ncname = new NCName(name);
            if (wsdl20.getBinding(ncname) != null)
               throw new RuntimeException("WSDL 2.0 Assertion:Each name must be unique among " + "all bindings in this WSDL target namespace");
            WSDLBinding wsdlBinding = new WSDLBinding(wsdl20);
            wsdlBinding.setName(ncname);
            wsdl20.addBinding(wsdlBinding);

            return wsdlBinding;
         }
         else if ("service".equals(localName))
         {
            WSDLService wsdlService = new WSDLService(wsdl20);
            wsdlService.setName(new NCName(attrs.getValue("name")));
            wsdl20.addService(wsdlService);
            wsdlService.setInterfaceName(navigator.resolveQName(attrs.getValue("interface")));
            return wsdlService;
         }
         else log.warn("Unrecogized child element: " + localName);
      }
      else throw new WSDLException("Invalid default namespace: " + defaultNamespace);

      return null;
   }

   /**
    * Parsing the WSDL 2.0 document
    * @param wsdlInterface
    * @param navigator
    * @param namespaceURI
    * @param localName
    * @param attrs
    * @return
    */
   public Object newChild(WSDLInterface wsdlInterface, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      /**
       * WSDL .0 Assertion: Operation names must also be unique within an interface
       */
      if ("operation".equals(localName))
      {
         String name = attrs.getValue("name");
         if (name == null)
            throw new RuntimeException("Operation name must not be null");
         NCName ncname = new NCName(name);
         if (wsdlInterface.containsInterfaceOperation(ncname))
            throw new RuntimeException("WSDL 2.0 Assertion:Each operation must be given a name that is "
                  + "unique within an interface defined in this WSDL target namespace");
         WSDLInterfaceOperation wsdlOperation = new WSDLInterfaceOperation(wsdlInterface);
         wsdlOperation.setName(ncname);
         wsdlInterface.addOperation(wsdlOperation);
         return wsdlOperation;
      }

      log.warn("Unrecogized child element: " + localName);
      return null;
   }

   /**
    * Parsing the WSDL 2.0 document
    * @param wsdlOperation
    * @param navigator
    * @param namespaceURI
    * @param localName
    * @param attrs
    * @return
    */
   public Object newChild(WSDLInterfaceOperation wsdlOperation, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if ("input".equals(localName))
      {
         WSDLInterfaceOperationInput wsdlInterfaceOperationInput = new WSDLInterfaceOperationInput(wsdlOperation);
         wsdlInterfaceOperationInput.setElement(navigator.resolveQName(attrs.getValue("element")));
         wsdlInterfaceOperationInput.setMessageLabel(new NCName(attrs.getValue("messageLabel")));
         wsdlOperation.addInput(wsdlInterfaceOperationInput);
         return wsdlInterfaceOperationInput;
      }
      else if ("output".equals(localName))
      {
         WSDLInterfaceOperationOutput output = new WSDLInterfaceOperationOutput(wsdlOperation);
         output.setElement(navigator.resolveQName(attrs.getValue("element")));
         output.setMessageLabel(new NCName(attrs.getValue("messageLabel")));
         wsdlOperation.addOutput(output);
         return output;
      }
      else if ("outfault".equals(localName))
      {
         WSDLInterfaceOperationOutfault fault = new WSDLInterfaceOperationOutfault(wsdlOperation);
         String messageLabel = attrs.getValue("messageLabel");
         fault.setRef(navigator.resolveQName(attrs.getValue("ref")));
         if (messageLabel != null)
            fault.setMessageLabel(new NCName(messageLabel));
         wsdlOperation.addOutfault(fault);
         return wsdlOperation;
      }

      log.warn("Unrecogized child element: " + localName);
      return null;
   }

   /**
    * Parsing the WSDL 2.0 document
    * @param wsdlBinding
    * @param navigator
    * @param namespaceURI
    * @param localName
    * @param attrs
    * @return
    */
   public Object newChild(WSDLBinding wsdlBinding, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if ("operation".equals(localName))
      {
         WSDLBindingOperation wsdlBindingOperation = new WSDLBindingOperation(wsdlBinding);
         wsdlBindingOperation.setRef(navigator.resolveQName(attrs.getValue("ref")));
         wsdlBinding.addOperation(wsdlBindingOperation);
         return wsdlBindingOperation;
      }

      log.warn("Unrecogized child element: " + localName);
      return null;
   }

   /**
    * Parsing the WSDL 2.0 document
    * @param wsdlBindingOperation
    * @param navigator
    * @param namespaceURI
    * @param localName
    * @param attrs
    * @return
    */
   public Object newChild(WSDLBindingOperation wsdlBindingOperation, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if ("input".equals(localName))
      {
         WSDLBindingOperationInput wsdlBindingOperationInput = new WSDLBindingOperationInput(wsdlBindingOperation);
         wsdlBindingOperationInput.setMessageLabel(new NCName(attrs.getValue("messageLabel")));
         wsdlBindingOperation.addInput(wsdlBindingOperationInput);
         return wsdlBindingOperationInput;
      }
      if ("output".equals(localName))
      {
         WSDLBindingOperationOutput wsdlBindingOperationOutput = new WSDLBindingOperationOutput(wsdlBindingOperation);
         wsdlBindingOperationOutput.setMessageLabel(new NCName(attrs.getValue("messageLabel")));
         wsdlBindingOperation.addOutput(wsdlBindingOperationOutput);
         return wsdlBindingOperationOutput;
      }

      log.warn("Unrecogized child element: " + localName);
      return null;
   }

   /**
    * Parsing the WSDL 2.0 document
    * @param wsdlService
    * @param navigator
    * @param namespaceURI
    * @param localName
    * @param attrs
    * @return
    */
   public Object newChild(WSDLService wsdlService, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if ("endpoint".equals(localName))
      {
         WSDLEndpoint wsdlEndpoint = new WSDLEndpoint(wsdlService);
         wsdlEndpoint.setName(new NCName(attrs.getValue("name")));
         wsdlEndpoint.setBinding(navigator.resolveQName(attrs.getValue("binding")));
         wsdlService.addEndpoint(wsdlEndpoint);
         return wsdlEndpoint;
      }

      log.warn("Unrecogized child element: " + localName);
      return null;
   }

   /**
    * Method that deals with importing schema
    * @param wsdl
    * @param attrs
    */
   private void importTypes(WSDLDefinitions wsdl, Attributes attrs)
   {
      // [TODO]
   }
}