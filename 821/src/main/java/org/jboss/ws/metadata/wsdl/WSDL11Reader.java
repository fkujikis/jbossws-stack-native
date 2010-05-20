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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.mime.MIMEMultipartRelated;
import javax.wsdl.extensions.mime.MIMEPart;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.util.xml.DOMUtils;
import org.jboss.util.xml.DOMWriter;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.jaxrpc.Style;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.jboss.ws.metadata.wsdl.xsd.SchemaUtils;
import org.jboss.ws.tools.JavaToXSD;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * A helper that translates a WSDL-1.1 object graph into a WSDL-2.0 object graph.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Anil.Saldhana@jboss.org
 * @since 10-Oct-2004
 */
public class WSDL11Reader
{
   // provide logging
   private static final Logger log = Logger.getLogger(WSDL11Reader.class);

   private WSDLDefinitions destWsdl;

   // Maps wsdl message parts to their corresponding element names
   private Map<String, QName> messagePartToElementMap = new HashMap<String, QName>();

   // Map of <ns,URL> for schemalocation keyed by namespace
   private Map<String, URL> schemaLocationsMap = new HashMap<String, URL>();

   /**
    * Takes a WSDL11 Definition element and converts into
    * our object graph that has been developed for WSDL20
    *
    * @param srcWsdl The src WSDL11 definition
    * @param wsdlLoc The source location, if null we cannot process imports or includes
    */
   public WSDLDefinitions processDefinition(Definition srcWsdl, URL wsdlLoc) throws IOException
   {
      log.trace("processDefinition: " + wsdlLoc);

      this.destWsdl = new WSDLDefinitions();
      this.destWsdl.setWsdlOneOneDefinition(srcWsdl);
      this.destWsdl.setWsdlNamespace(Constants.NS_WSDL11);

      processNamespaces(srcWsdl);
      processTypes(srcWsdl, wsdlLoc);
      processServices(srcWsdl);

      if (getAllDefinedBindings(srcWsdl).size() != destWsdl.getBindings().length)
         processUnreachableBindings(srcWsdl);

      return destWsdl;
   }

   // process all bindings not within service separetly
   private void processUnreachableBindings(Definition srcWsdl)
   {
      log.trace("processUnreachableBindings");
      
      Iterator it = getAllDefinedBindings(srcWsdl).values().iterator();
      while (it.hasNext())
      {
         Binding srcBinding = (Binding)it.next();
         QName srcQName = srcBinding.getQName();

         WSDLBinding destBinding = destWsdl.getBinding(new NCName(srcQName));
         if (destBinding == null)
         {
            processBinding(srcWsdl, srcBinding);
         }
      }
   }

   private void processNamespaces(Definition srcWsdl)
   {
      String targetNS = srcWsdl.getTargetNamespace();
      destWsdl.setTargetNamespace(targetNS);

      // Copy wsdl namespaces
      Map nsMap = srcWsdl.getNamespaces();
      Iterator iter = nsMap.entrySet().iterator();
      while (iter.hasNext())
      {
         Map.Entry entry = (Map.Entry)iter.next();
         String prefix = (String)entry.getKey();
         String nsURI = (String)entry.getValue();
         destWsdl.registerNamespaceURI(nsURI, prefix);
      }
   }

   private void processTypes(Definition srcWsdl, URL wsdlLoc) throws IOException
   {
      log.trace("BEGIN processTypes: " + wsdlLoc);

      WSDLTypes destTypes = destWsdl.getWsdlTypes();

      Types srcTypes = srcWsdl.getTypes();
      if (srcTypes != null && srcTypes.getExtensibilityElements().size() > 0)
      {
         List extElements = srcTypes.getExtensibilityElements();
         int len = extElements.size();

         for (int i = 0; i < len; i++)
         {
            ExtensibilityElement extElement = (ExtensibilityElement)extElements.get(i);

            Element domElement;
            if (extElement instanceof Schema)
            {
               domElement = ((Schema)extElement).getElement();
            }
            else if (extElement instanceof UnknownExtensibilityElement)
            {
               domElement = ((UnknownExtensibilityElement)extElement).getElement();
            }
            else
            {
               throw new WSException("Unsupported extensibility element: " + extElement);
            }

            Element domElementClone = (Element)domElement.cloneNode(true);
            copyParentNamespaceDeclarations(domElementClone, domElement);

            String localname = domElementClone.getLocalName();
            try
            {
               if ("import".equals(localname))
               {
                  processSchemaImport(destTypes, wsdlLoc, domElementClone);
               }
               else if ("schema".equals(localname))
               {
                  processSchemaInclude(destTypes, wsdlLoc, domElementClone);
               }
               else
               {
                  throw new IllegalArgumentException("Unsuported schema element: " + localname);
               }
            }
            catch (IOException e)
            {
               throw new WSException("Cannot extract schema definition", e);
            }
         }

         if (len > 0)
         {
            Collection<URL> col = schemaLocationsMap.values();
            JavaToXSD jxsd = new JavaToXSD();
            List<String> strlist = new ArrayList<String>();
            for (URL uri : col)
            {
               strlist.add(uri.toExternalForm());
            }
            JBossXSModel xsmodel = jxsd.parseSchema(schemaLocationsMap);
            destTypes.addSchemaModel(destWsdl.getTargetNamespace(), xsmodel);
         }
      }
      else
      {
         log.trace("Empty wsdl types element, processing imports");
         Iterator it = srcWsdl.getImports().values().iterator();
         while (it.hasNext())
         {
            List<Import> srcImports = (List<Import>)it.next();
            for (Import srcImport : srcImports)
            {
               Definition impDefinition = srcImport.getDefinition();
               String impLoc = impDefinition.getDocumentBaseURI();
               processTypes(impDefinition, new URL(impLoc));
            }
         }
      }

      log.trace("END processTypes: " + wsdlLoc + "\n" + destTypes);
   }

   private void copyParentNamespaceDeclarations(Element destElement, Element srcElement)
   {
      Node parent = srcElement.getParentNode();
      while (parent != null)
      {
         if (parent.hasAttributes())
         {
            NamedNodeMap attributes = parent.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++)
            {
               Attr attr = (Attr)attributes.item(i);
               String name = attr.getName();
               String value = attr.getValue();
               if (name.startsWith("xmlns:") && destElement.hasAttribute(name) == false)
                  destElement.setAttribute(name, value);
            }
         }
         parent = parent.getParentNode();
      }
   }

   private void processSchemaImport(WSDLTypes types, URL wsdlLoc, Element importEl) throws IOException
   {
      if (wsdlLoc == null)
         throw new IllegalArgumentException("Cannot process import, parent location not set");

      log.trace("processSchemaImport: " + wsdlLoc);

      String location = importEl.getAttribute("schemaLocation");
      if (location == null || location.length() == 0)
         throw new IllegalArgumentException("schemaLocation is null for xsd:import");

      URL locationURL = getLocationURL(wsdlLoc, location);
      Element rootElement = DOMUtils.parse(locationURL.openStream());
      URL newloc = processSchemaInclude(types, locationURL, rootElement);
      if (newloc != null)
         importEl.setAttribute("schemaLocation", newloc.toExternalForm());
   }

   private URL processSchemaInclude(WSDLTypes types, URL wsdlLoc, Element schemaEl) throws IOException
   {
      if (wsdlLoc == null)
         throw new IllegalArgumentException("Cannot process iclude, parent location not set");

      File tmpFile = null;
      if (wsdlLoc == null)
         throw new IllegalArgumentException("Cannot process include, parent location not set");

      log.trace("processSchemaInclude: " + wsdlLoc);

      String schemaPrefix = schemaEl.getPrefix();

      String importTag = (schemaPrefix == null) ? "import" : schemaPrefix + ":import";
      Element importElement = schemaEl.getOwnerDocument().createElementNS(Constants.NS_SCHEMA_XSD, importTag);
      importElement.setAttribute("namespace", Constants.URI_SOAP11_ENC);
      schemaEl.insertBefore(importElement, DOMUtils.getFirstChildElement(schemaEl));

      // Handle schema includes
      Iterator it = DOMUtils.getChildElements(schemaEl, new QName(Constants.NS_SCHEMA_XSD, "include"));
      while (it.hasNext())
      {
         Element includeEl = (Element)it.next();
         String location = includeEl.getAttribute("schemaLocation");
         if (location == null || location.length() == 0)
            throw new IllegalArgumentException("schemaLocation is null for xsd:include");

         URL locationURL = getLocationURL(wsdlLoc, location);
         Element rootElement = DOMUtils.parse(locationURL.openStream());
         URL newloc = processSchemaInclude(types, locationURL, rootElement);
         if (newloc != null)
            includeEl.setAttribute("schemaLocation", newloc.toExternalForm());
      }

      String targetNS = schemaEl.getAttribute("targetNamespace");
      if (targetNS.length() > 0)
      {
         log.trace("processSchemaInclude: [targetNS=" + targetNS + ",parentURL=" + wsdlLoc + "]");

         tmpFile = SchemaUtils.getSchemaTempFile(targetNS);
         tmpFile.deleteOnExit();

         FileWriter fwrite = new FileWriter(tmpFile);
         new DOMWriter(fwrite).setPrettyprint(true).print(schemaEl);
         fwrite.close();

         schemaLocationsMap.put(targetNS, tmpFile.toURL());
      }

      // schema elements that have no target namespace are skipped
      //
      //  <xsd:schema>
      //    <xsd:import namespace="http://org.jboss.webservice/example/types" schemaLocation="Hello.xsd"/>
      //    <xsd:import namespace="http://org.jboss.webservice/example/types/arrays/org/jboss/test/webservice/admindevel" schemaLocation="subdir/HelloArr.xsd"/>
      //  </xsd:schema>
      if (targetNS.length() == 0)
      {
         log.trace("Schema element without target namespace in: " + wsdlLoc);
      }

      handleSchemaImports(schemaEl, wsdlLoc);

      return tmpFile != null ? tmpFile.toURL() : null;
   }

   private void handleSchemaImports(Element schemaEl, URL wsdlLoc) throws MalformedURLException
   {
      if (wsdlLoc == null)
         throw new IllegalArgumentException("Cannot process import, parent location not set");

      Iterator it = DOMUtils.getChildElements(schemaEl, new QName(Constants.NS_SCHEMA_XSD, "import"));
      while (it.hasNext())
      {
         Element includeEl = (Element)it.next();
         String schemaLocation = includeEl.getAttribute("schemaLocation");
         String namespace = includeEl.getAttribute("namespace");

         log.trace("handleSchemaImport: [namespace=" + namespace + ",schemaLocation=" + schemaLocation + "]");

         // Skip, let the entity resolver resolve these
         if (namespace.length() > 0 && schemaLocation.length() > 0 && !namespace.startsWith("http://www.w3.org/"))
         {
            URL currLoc = getLocationURL(wsdlLoc, schemaLocation);
            schemaLocationsMap.put(namespace, currLoc);
         }
         else
         {
            log.trace("Skip schema import: [namespace=" + namespace + ",schemaLocation=" + schemaLocation + "]");
         }
      }
   }
   
   private URL getLocationURL(URL parentURL, String location) throws MalformedURLException
   {
      log.trace("getLocationURL: [location=" + location + ",parent=" + parentURL + "]");

      URL locationURL = null;
      try
      {
         locationURL = new URL(location);
      }
      catch (MalformedURLException e)
      {
         // ignore malformed URL
      }

      if (locationURL == null)
      {
         String parentProtocol = parentURL.getProtocol();
         if (parentProtocol.equals("file") && !location.startsWith("/"))
         {
            String path = parentURL.toExternalForm();
            path = path.substring(0, path.lastIndexOf("/"));
            locationURL = new URL(path + "/" + location);
         }
         else if (parentProtocol.startsWith("http") && location.startsWith("/"))
         {
            String path = parentProtocol + "://" + parentURL.getHost() + ":" + parentURL.getPort();
            locationURL = new URL(path + location);
         }
         else if (parentProtocol.equals("jar") && !location.startsWith("/"))
         {
            String path = parentURL.toExternalForm();
            path = path.substring(0, path.lastIndexOf("/"));
            locationURL = new URL(path + "/" + location);
         }
         else
         {
            throw new WSException("Unsupported schemaLocation: " + location);
         }
      }

      log.trace("Modified schemaLocation: " + locationURL);
      return locationURL;
   }

   private void processPortType(Definition srcWsdl, PortType srcPortType)
   {
      log.trace("processPortType: " + srcPortType.getQName());
      
      QName qname = srcPortType.getQName();
      NCName ncName = new NCName(qname);
      if (destWsdl.getInterface(ncName) == null)
      {
         WSDLInterface destInterface = new WSDLInterface(destWsdl);
         destInterface.setName(ncName);
         destInterface.setQName(qname);

         // eventing extensions
         QName eventSourceProp = (QName)srcPortType.getExtensionAttribute(Constants.WSDL_ATTRIBUTE_WSE_EVENTSOURCE);
         if (eventSourceProp != null && eventSourceProp.getLocalPart().equals(Boolean.TRUE.toString()))
         {
            destInterface.addProperty(new WSDLProperty(Constants.WSDL_PROPERTY_EVENTSOURCE, eventSourceProp.getLocalPart()));
         }

         destWsdl.addInterface(destInterface);

         processPortTypeOperations(srcWsdl, destInterface, srcPortType);
      }
   }

   private void processPortTypeOperations(Definition srcWsdl, WSDLInterface destInterface, PortType srcPortType)
   {
      Iterator itOperations = srcPortType.getOperations().iterator();
      while (itOperations.hasNext())
      {
         Operation srcOperation = (Operation)itOperations.next();

         WSDLInterfaceOperation destOperation = new WSDLInterfaceOperation(destInterface);
         destOperation.setName(new NCName(srcOperation.getName()));

         processOperationInput(srcWsdl, srcOperation, destOperation, srcPortType);
         processOperationOutput(srcWsdl, srcOperation, destOperation, srcPortType);
         processOperationFaults(srcOperation, destOperation, destInterface);

         destInterface.addOperation(destOperation);
      }
   }

   private void processOperationInput(Definition srcWsdl, Operation srcOperation, WSDLInterfaceOperation destOperation, PortType srcPortType)
   {
      Input srcInput = srcOperation.getInput();
      if (srcInput != null)
      {
         Message srcMessage = srcInput.getMessage();
         log.trace("processOperationInput: " + srcMessage.getQName());
         
         QName wsaAction = (QName)srcInput.getExtensionAttribute(Constants.WSDL_ATTRIBUTE_WSA_ACTION);
         if (wsaAction != null)
            destOperation.addProperty(new WSDLProperty(Constants.WSDL_PROPERTY_ACTION_IN, wsaAction.getLocalPart()));
         
         List paramOrder = srcOperation.getParameterOrdering();
         Iterator itMessageParts = srcMessage.getOrderedParts(paramOrder).iterator();
         while (itMessageParts.hasNext())
         {
            WSDLInterfaceOperationInput destInput = new WSDLInterfaceOperationInput(destOperation);

            Part srcPart = (Part)itMessageParts.next();
            QName elementName = messagePartToElementName(srcWsdl, srcPortType, srcOperation, srcMessage, srcPart);
            destInput.setElement(elementName);

            //Lets remember the Message name
            destInput.addProperty(new WSDLProperty(Constants.WSDL_PROPERTY_MESSAGE_NAME, srcMessage.getQName().getLocalPart()));
            destOperation.addProperty(new WSDLProperty(Constants.WSDL_PROPERTY_MESSAGE_NAME_IN, srcMessage.getQName().getLocalPart()));

            // Remember the original part name
            WSDLProperty wsdlProperty = new WSDLProperty(Constants.WSDL_PROPERTY_PART_NAME, srcPart.getName());
            destInput.addProperty(wsdlProperty);

            // If the Part references a type rather than an element
            // we transport the xmlType as property
            QName xmlType = srcPart.getTypeName();
            if (xmlType != null)
            {
               xmlType = destWsdl.registerQName(xmlType);
               String value = xmlType.getPrefix() + ":" + xmlType.getLocalPart();
               wsdlProperty = new WSDLProperty(Constants.WSDL_PROPERTY_RPC_XMLTYPE, value);
               destInput.addProperty(wsdlProperty);
            }

            destOperation.addInput(destInput);
         }
      }
   }

   private void processOperationOutput(Definition srcWsdl, Operation srcOperation, WSDLInterfaceOperation destOperation, PortType srcPortType)
   {
      Output srcOutput = srcOperation.getOutput();
      if (srcOutput != null)
      {
         Message srcMessage = srcOutput.getMessage();
         log.trace("processOperationOutput: " + srcMessage.getQName());
         
         destOperation.setPattern(Constants.WSDL20_PATTERN_IN_OUT);
         QName wsaAction = (QName)srcOutput.getExtensionAttribute(Constants.WSDL_ATTRIBUTE_WSA_ACTION);
         if (wsaAction != null)
            destOperation.addProperty(new WSDLProperty(Constants.WSDL_PROPERTY_ACTION_OUT, wsaAction.getLocalPart()));

         Iterator itMessageParts = srcMessage.getOrderedParts(null).iterator();
         while (itMessageParts.hasNext())
         {
            WSDLInterfaceOperationOutput destOutput = new WSDLInterfaceOperationOutput(destOperation);

            Part srcPart = (Part)itMessageParts.next();
            QName elementName = messagePartToElementName(srcWsdl, srcPortType, srcOperation, srcMessage, srcPart);
            destOutput.setElement(elementName);

            //Lets remember the Message name
            destOutput.addProperty(new WSDLProperty(Constants.WSDL_PROPERTY_MESSAGE_NAME, srcMessage.getQName().getLocalPart()));
            destOperation.addProperty(new WSDLProperty(Constants.WSDL_PROPERTY_MESSAGE_NAME_OUT, srcMessage.getQName().getLocalPart()));

            // Remember the original part name
            WSDLProperty wsdlProperty = new WSDLProperty(Constants.WSDL_PROPERTY_PART_NAME, srcPart.getName());
            destOutput.addProperty(wsdlProperty);

            // If the Part references a type rather than an element
            // we transport the xmlType as property
            QName xmlType = srcPart.getTypeName();
            if (xmlType != null)
            {
               xmlType = destWsdl.registerQName(xmlType);
               String value = xmlType.getPrefix() + ":" + xmlType.getLocalPart();
               wsdlProperty = new WSDLProperty(Constants.WSDL_PROPERTY_RPC_XMLTYPE, value);
               destOutput.addProperty(wsdlProperty);
            }

            destOperation.addOutput(destOutput);
         }
      }
      else
      {
         destOperation.setPattern(Constants.WSDL20_PATTERN_IN_ONLY);
      }
   }

   private void processOperationFaults(Operation srcOperation, WSDLInterfaceOperation destOperation, WSDLInterface destInterface)
   {

      Map faults = srcOperation.getFaults();
      Iterator itFaults = faults.values().iterator();
      while (itFaults.hasNext())
      {
         Fault srcFault = (Fault)itFaults.next();
         processOperationFault(destOperation, destInterface, srcFault);
      }
   }

   private void processOperationFault(WSDLInterfaceOperation destOperation, WSDLInterface destInterface, Fault srcFault)
   {
      String faultName = srcFault.getName();
      log.trace("processOperationFault: " + faultName);
      
      WSDLInterfaceFault destFault = new WSDLInterfaceFault(destInterface);
      NCName ncName = new NCName(faultName);
      destFault.setName(ncName);

      Message message = srcFault.getMessage();
      QName messageName = message.getQName();

      Map partsMap = message.getParts();
      if (partsMap.size() != 1)
         throw new WSException("Unsupported number of fault parts in message " + messageName);

      Part part = (Part)partsMap.values().iterator().next();
      QName xmlName = part.getElementName();
      QName xmlType = part.getTypeName();

      destFault.setXmlType(xmlType);
      if (xmlName != null)
      {
         destFault.setXmlName(xmlName);
      }
      else
      {
         destFault.setXmlName(messageName);
         log.warn("Unsupported fault message part in message: " + messageName);
      }

      WSDLInterfaceFault prevFault = destInterface.getFault(ncName);
      if (prevFault != null && prevFault.getName().equals(ncName) == false)
         throw new WSException("Fault name must be unique: " + faultName);

      // Add the fault to the interface
      destInterface.addFault(destFault);

      // Add the fault refererence to the operation
      WSDLInterfaceOperationOutfault opOutFault = new WSDLInterfaceOperationOutfault(destOperation);
      opOutFault.setRef(new QName(destWsdl.getTargetNamespace(), faultName));
      destOperation.addOutfault(opOutFault);
   }

   /** Translate the message part name into an XML element name.
    */
   private QName messagePartToElementName(Definition srcWsdl, PortType srcPortType, Operation srcOperation, Message srcMessage, Part srcPart)
   {
      // <part name="param" element="tns:SomeType" />
      QName xmlName = srcPart.getElementName();
      if (xmlName != null)
         xmlName = destWsdl.registerQName(xmlName);

      // <part name="param" type="xsd:string" />
      if (xmlName == null)
      {
         // Use the part name as fallback
         xmlName = new QName(srcPart.getName());

         // The binding may define a different xmlName for this message part (i.e. in case of header parts)
         // The rest of this implementation tries to discover that binding.

         // Find the binding for this portType
         Binding srcBinding = null;
         Iterator itBindings = getAllDefinedBindings(srcWsdl).values().iterator();
         while (srcBinding == null && itBindings.hasNext())
         {
            Binding binding = (Binding)itBindings.next();
            if (binding.getPortType().equals(srcPortType))
               srcBinding = binding;
         }

         if (srcBinding == null)
            throw new WSException("Cannot find binding for: " + srcPortType.getQName());

         String srcOperationName = srcOperation.getName();
         BindingOperation srcBindingOperation = srcBinding.getBindingOperation(srcOperationName, null, null);
         if (srcBindingOperation == null)
            throw new WSException("Cannot find binding operation for: " + srcOperationName);

         // Scan the binding input for a <soap:header> for this message part
         BindingInput srcBindingInput = srcBindingOperation.getBindingInput();
         if (srcBindingInput != null)
         {
            Iterator itExt = srcBindingInput.getExtensibilityElements().iterator();
            while (itExt.hasNext())
            {
               ExtensibilityElement extEl = (ExtensibilityElement)itExt.next();
               if (extEl instanceof SOAPHeader)
               {
                  SOAPHeader header = (SOAPHeader)extEl;
                  QName messageQName = header.getMessage();
                  String partName = header.getPart();
                  if (messageQName.equals(srcMessage.getQName()) && partName.equals(srcPart.getName()))
                  {
                     String namespaceURI = header.getNamespaceURI();
                     if (namespaceURI != null)
                     {
                        xmlName = new QName(namespaceURI, partName);
                        xmlName = destWsdl.registerQName(xmlName);
                     }
                  }
               }
            }
         }

         // Scan the binding output for a <soap:header> for this message part
         BindingOutput srcBindingOutput = srcBindingOperation.getBindingOutput();
         if (srcBindingOutput != null)
         {
            Iterator itExt = srcBindingOutput.getExtensibilityElements().iterator();
            while (itExt.hasNext())
            {
               ExtensibilityElement extEl = (ExtensibilityElement)itExt.next();
               if (extEl instanceof SOAPHeader)
               {
                  SOAPHeader header = (SOAPHeader)extEl;
                  QName messageQName = header.getMessage();
                  String partName = header.getPart();
                  if (messageQName.equals(srcMessage.getQName()) && partName.equals(srcPart.getName()))
                  {
                     String namespaceURI = header.getNamespaceURI();
                     if (namespaceURI != null)
                     {
                        xmlName = new QName(namespaceURI, partName);
                        xmlName = destWsdl.registerQName(xmlName);
                     }
                  }
               }
            }
         }
      }

      // cache the element name for processing of the bindings
      String key = srcMessage.getQName() + "->" + srcPart.getName();
      messagePartToElementMap.put(key, xmlName);

      return xmlName;
   }

   private void processBinding(Definition srcWsdl, Binding srcBinding)
   {
      QName srcBindingQName = srcBinding.getQName();
      log.trace("processBinding: " + srcBindingQName);

      NCName ncName = new NCName(srcBindingQName);
      if (destWsdl.getBinding(ncName) == null)
      {
         PortType srcPortType = srcBinding.getPortType();
         if (srcPortType == null)
            throw new WSException("Cannot find port type for binding: " + ncName);
         
         processPortType(srcWsdl, srcPortType);

         WSDLBinding destBinding = new WSDLBinding(destWsdl);
         destBinding.setQName(srcBindingQName);
         destBinding.setName(ncName);
         destBinding.setInterfaceName(srcPortType.getQName());

         String bindingStyle = Style.getDefaultStyle().toString();
         List extList = srcBinding.getExtensibilityElements();
         for (int i = 0; i < extList.size(); i++)
         {
            Object extElement = extList.get(i);
            if (extElement instanceof SOAPBinding)
            {
               SOAPBinding soapBinding = (SOAPBinding)extElement;
               bindingStyle = soapBinding.getStyle();
            }
         }
         destWsdl.addBinding(destBinding);
         processBindingOperations(destBinding, srcBinding, bindingStyle);
      }
   }

   private Map getAllDefinedBindings(Definition srcWsdl)
   {
      Map<QName, Binding> retMap = new LinkedHashMap<QName, Binding>();
      Map srcBindings = srcWsdl.getBindings();
      Iterator itBinding = srcBindings.values().iterator();
      while (itBinding.hasNext()) 
      {
         Binding srcBinding = (Binding)itBinding.next();
         retMap.put(srcBinding.getQName(), srcBinding);
      }
      
      // Bindings not available when pulled in through <wsdl:import>
      // http://sourceforge.net/tracker/index.php?func=detail&aid=1240323&group_id=128811&atid=712792
      Iterator itService = srcWsdl.getServices().values().iterator();
      while (itService.hasNext())
      {
         Service srcService = (Service)itService.next();
         Iterator itPort = srcService.getPorts().values().iterator();
         while (itPort.hasNext())
         {
            Port srcPort = (Port)itPort.next();
            Binding srcBinding = srcPort.getBinding();
            retMap.put(srcBinding.getQName(), srcBinding);
         }
      }
      return retMap;
   }

   private void processBindingOperations(WSDLBinding destBinding, Binding srcBinding, String bindingStyle)
   {
      Iterator it = srcBinding.getBindingOperations().iterator();
      while (it.hasNext())
      {
         BindingOperation srcBindingOperation = (BindingOperation)it.next();
         processBindingOperation(destBinding, bindingStyle, srcBindingOperation);
      }
   }

   private void processBindingOperation(WSDLBinding destBinding, String bindingStyle, BindingOperation srcBindingOperation)
   {
      String srcBindingName = srcBindingOperation.getName();
      log.trace("processBindingOperation: " + srcBindingName);
      
      WSDLInterface destInterface = destBinding.getInterface();
      String namespaceURI = destInterface.getQName().getNamespaceURI();

      WSDLBindingOperation destBindingOperation = new WSDLBindingOperation(destBinding);
      QName refQName = new QName(namespaceURI, srcBindingName);
      destBindingOperation.setRef(refQName);
      destBinding.addOperation(destBindingOperation);

      String opName = srcBindingName;
      WSDLInterfaceOperation destIntfOperation = destInterface.getOperation(new NCName(opName));

      // Process soap:operation@soapAction, soap:operation@style
      String operationStyle = null;
      Iterator itExt = srcBindingOperation.getExtensibilityElements().iterator();
      while (itExt.hasNext())
      {
         ExtensibilityElement extEl = (ExtensibilityElement)itExt.next();
         if (extEl instanceof SOAPOperation)
         {
            SOAPOperation soapOp = (SOAPOperation)extEl;
            destBindingOperation.setSOAPAction(soapOp.getSoapActionURI());
            operationStyle = soapOp.getStyle();
         }
      }
      destIntfOperation.setStyle(operationStyle != null ? operationStyle : bindingStyle);

      BindingInput srcBindingInput = srcBindingOperation.getBindingInput();
      if (srcBindingInput != null)
      {
         processBindingInput(destBindingOperation, destIntfOperation, srcBindingInput);
      }

      BindingOutput srcBindingOutput = srcBindingOperation.getBindingOutput();
      if (srcBindingOutput != null)
      {
         processBindingOutput(destBindingOperation, destIntfOperation, srcBindingOutput);
      }
   }

   private void processBindingInput(WSDLBindingOperation destBindingOperation, WSDLInterfaceOperation destIntfOperation, BindingInput srcBindingInput)
   {
      log.trace("processBindingInput");
      
      Iterator itExt = srcBindingInput.getExtensibilityElements().iterator();
      while (itExt.hasNext())
      {
         ExtensibilityElement extEl = (ExtensibilityElement)itExt.next();
         if (extEl instanceof SOAPBody)
         {
            SOAPBody body = (SOAPBody)extEl;
            processEncodingStyle(body, destBindingOperation);
         }
         else if (extEl instanceof SOAPHeader)
         {
            SOAPHeader header = (SOAPHeader)extEl;
            QName messageQName = header.getMessage();
            String partName = header.getPart();

            // get cached element name for processing of the bindings
            String key = messageQName + "->" + partName;
            QName elementName = (QName)messagePartToElementMap.get(key);
            if (elementName != null)
            {
               WSDLInterfaceOperationInput destIntfInput = destIntfOperation.getInput(elementName);
               if (destIntfInput == null)
                  throw new WSException("Cannot find interface input for element: " + elementName);

               WSDLProperty wsdlProperty = new WSDLProperty(Constants.WSDL_PROPERTY_APPLICATION_DATA, false, null, null);
               destIntfInput.addProperty(wsdlProperty);
            }
         }
         else if (extEl instanceof MIMEMultipartRelated)
         {
            MIMEMultipartRelated related = (MIMEMultipartRelated)extEl;
            Iterator i = related.getMIMEParts().iterator();
            while (i.hasNext())
            {
               MIMEPart part = (MIMEPart)i.next();
               Iterator j = part.getExtensibilityElements().iterator();
               String name = null;
               String types = null;

               while (j.hasNext())
               {
                  ExtensibilityElement inner = (ExtensibilityElement)j.next();
                  if (inner instanceof MIMEContent)
                  {
                     MIMEContent content = (MIMEContent)inner;
                     name = content.getPart();
                     if (types == null)
                     {
                        types = content.getType();
                     }
                     else
                     {
                        types += "," + content.getType();
                     }
                  }
               }

               // Found content types in this part
               if (name != null)
               {
                  // A mime part must be defined as <part type="">
                  QName elementName = new QName(name);
                  WSDLInterfaceOperationInput destIntfInput = destIntfOperation.getInput(elementName);
                  if (destIntfInput == null)
                     throw new WSException("Cannot find interface input for element: " + elementName);

                  WSDLProperty wsdlProperty = new WSDLProperty(Constants.WSDL_PROPERTY_WSDL11_MIME_TYPE, false, types, null);
                  destIntfInput.addProperty(wsdlProperty);
               }
            }
         }
      }
   }

   private void processBindingOutput(WSDLBindingOperation destBindingOperation, WSDLInterfaceOperation destIntfOperation, BindingOutput srcBindingOutput)
   {
      log.trace("processBindingOutput");
      
      Iterator itExt = srcBindingOutput.getExtensibilityElements().iterator();
      while (itExt.hasNext())
      {
         ExtensibilityElement extEl = (ExtensibilityElement)itExt.next();
         if (extEl instanceof SOAPBody)
         {
            SOAPBody body = (SOAPBody)extEl;
            processEncodingStyle(body, destBindingOperation);
         }
         else if (extEl instanceof SOAPHeader)
         {
            SOAPHeader header = (SOAPHeader)extEl;
            QName messageQName = header.getMessage();
            String partName = header.getPart();

            // get cached element name for processing of the bindings
            String key = messageQName + "->" + partName;
            QName elementName = (QName)messagePartToElementMap.get(key);
            if (elementName != null)
            {
               WSDLInterfaceOperationOutput destIntfOutput = destIntfOperation.getOutput(elementName);
               if (destIntfOutput == null)
                  throw new WSException("Cannot find interface output for element: " + elementName);

               WSDLProperty wsdlProperty = new WSDLProperty(Constants.WSDL_PROPERTY_APPLICATION_DATA, false, null, null);
               destIntfOutput.addProperty(wsdlProperty);
            }
         }
         else if (extEl instanceof MIMEMultipartRelated)
         {
            MIMEMultipartRelated related = (MIMEMultipartRelated)extEl;
            Iterator i = related.getMIMEParts().iterator();
            while (i.hasNext())
            {
               MIMEPart part = (MIMEPart)i.next();
               Iterator j = part.getExtensibilityElements().iterator();
               String name = null;
               String types = null;

               while (j.hasNext())
               {
                  ExtensibilityElement inner = (ExtensibilityElement)j.next();
                  if (inner instanceof MIMEContent)
                  {
                     MIMEContent content = (MIMEContent)inner;
                     name = content.getPart();
                     if (types == null)
                     {
                        types = content.getType();
                     }
                     else
                     {
                        types += "," + content.getType();
                     }
                  }
               }

               // Found content types in this part
               if (name != null)
               {
                  // A mime part must be defined as <part type="">
                  QName elementName = new QName(name);
                  WSDLInterfaceOperationOutput destIntfOutput = destIntfOperation.getOutput(elementName);
                  if (destIntfOutput == null)
                     throw new WSException("Cannot find interface output for element: " + elementName);

                  WSDLProperty wsdlProperty = new WSDLProperty(Constants.WSDL_PROPERTY_WSDL11_MIME_TYPE, false, types, null);
                  destIntfOutput.addProperty(wsdlProperty);
               }
            }
         }
      }
   }

   private void processEncodingStyle(SOAPBody body, WSDLBindingOperation destBindingOperation)
   {
      log.trace("processEncodingStyle");
      
      List encStyleList = body.getEncodingStyles();
      if (encStyleList != null)
      {
         if (encStyleList.size() > 1)
            log.warn("Multiple encoding styles not supported: " + encStyleList);

         if (encStyleList.size() > 0)
         {
            String encStyle = (String)encStyleList.get(0);
            String setStyle = destBindingOperation.getEncodingStyle();

            if (encStyle.equals(setStyle) == false)
               log.warn("Encoding style '" + encStyle + "' not supported for: " + destBindingOperation.getRef());

            destBindingOperation.setEncodingStyle(encStyle);
         }
      }
   }

   private void processServices(Definition srcWsdl)
   {
      log.trace("BEGIN processServices: " + srcWsdl.getDocumentBaseURI());

      if (srcWsdl.getServices().size() > 0)
      {
         Iterator it = srcWsdl.getServices().values().iterator();
         while (it.hasNext())
         {
            Service srcService = (Service)it.next();
            QName qname = srcService.getQName();
            WSDLService destService = new WSDLService(destWsdl);
            destService.setName(new NCName(qname));
            destService.setQName(qname);
            destWsdl.addService(destService);
            processPorts(srcWsdl, destService, srcService);
         }
      }
      else
      {
         log.trace("Empty wsdl services, processing imports");
         Iterator it = srcWsdl.getImports().values().iterator();
         while (it.hasNext())
         {
            List<Import> srcImports = (List<Import>)it.next();
            for (Import srcImport : srcImports)
            {
               Definition importDefinition = srcImport.getDefinition();
               processServices(importDefinition);
            }
         }
      }

      log.trace("END processServices: " + srcWsdl.getDocumentBaseURI());
   }

   private void processPorts(Definition srcWsdl, WSDLService destService, Service srcService)
   {
      Iterator it = srcService.getPorts().values().iterator();
      while (it.hasNext())
      {
         Port srcPort = (Port)it.next();
         processPort(srcWsdl, destService, srcPort);
      }
   }

   private void processPort(Definition srcWsdl, WSDLService destService, Port srcPort)
   {
      log.trace("processPort: " + srcPort.getName());
      
      Binding srcBinding = srcPort.getBinding();

      WSDLEndpoint destEndpoint = new WSDLEndpoint(destService);
      destEndpoint.setName(new NCName(srcPort.getName()));
      destEndpoint.setBinding(srcBinding.getQName());
      destEndpoint.setQName(new QName(srcWsdl.getTargetNamespace(), srcPort.getName()));
      destEndpoint.setAddress(getEndPointAddress(srcPort));
      destService.addEndpoint(destEndpoint);

      processBinding(srcWsdl, srcBinding);
   }

   /** Get the endpoint address from the ports extensible element
    */
   private String getEndPointAddress(Port srcPort)
   {
      String retstr = null;
      Iterator it = srcPort.getExtensibilityElements().iterator();
      while (it.hasNext())
      {
         ExtensibilityElement extElement = (ExtensibilityElement)it.next();
         if (extElement instanceof SOAPAddress)
         {
            SOAPAddress addr = (SOAPAddress)extElement;
            retstr = addr.getLocationURI();
         }
      }
      return retstr;
   }
}