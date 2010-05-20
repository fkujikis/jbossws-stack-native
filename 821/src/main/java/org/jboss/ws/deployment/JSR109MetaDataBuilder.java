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
package org.jboss.ws.deployment;

//$Id: JSR109MetaDataBuilder.java 377 2006-05-18 13:57:29Z thomas.diesler@jboss.com $

import java.util.ArrayList;
import java.util.List;

import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.encoding.TypeMappingRegistry;
import javax.xml.ws.addressing.AddressingConstants;

import org.apache.xerces.xs.XSTypeDefinition;
import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.addressing.AddressingConstantsImpl;
import org.jboss.ws.addressing.metadata.AddressingOpMetaExt;
import org.jboss.ws.eventing.EventingConstants;
import org.jboss.ws.eventing.deployment.EventingEndpoint;
import org.jboss.ws.eventing.metadata.EventingEpMetaExt;
import org.jboss.ws.jaxrpc.LiteralTypeMapping;
import org.jboss.ws.jaxrpc.Style;
import org.jboss.ws.jaxrpc.TypeMappingImpl;
import org.jboss.ws.jaxrpc.TypeMappingRegistryImpl;
import org.jboss.ws.jaxrpc.UnqualifiedFaultException;
import org.jboss.ws.jaxrpc.Use;
import org.jboss.ws.metadata.EndpointMetaData;
import org.jboss.ws.metadata.FaultMetaData;
import org.jboss.ws.metadata.OperationMetaData;
import org.jboss.ws.metadata.ParameterMetaData;
import org.jboss.ws.metadata.ServerEndpointMetaData;
import org.jboss.ws.metadata.ServiceMetaData;
import org.jboss.ws.metadata.TypeMappingMetaData;
import org.jboss.ws.metadata.TypesMetaData;
import org.jboss.ws.metadata.jaxrpcmapping.ExceptionMapping;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.ws.metadata.jaxrpcmapping.JavaXmlTypeMapping;
import org.jboss.ws.metadata.jaxrpcmapping.MethodParamPartsMapping;
import org.jboss.ws.metadata.jaxrpcmapping.ServiceEndpointInterfaceMapping;
import org.jboss.ws.metadata.jaxrpcmapping.ServiceEndpointMethodMapping;
import org.jboss.ws.metadata.jaxrpcmapping.VariableMapping;
import org.jboss.ws.metadata.jaxrpcmapping.WsdlMessageMapping;
import org.jboss.ws.metadata.jaxrpcmapping.WsdlReturnValueMapping;
import org.jboss.ws.metadata.wsdl.NCName;
import org.jboss.ws.metadata.wsdl.WSDLBinding;
import org.jboss.ws.metadata.wsdl.WSDLBindingOperation;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLEndpoint;
import org.jboss.ws.metadata.wsdl.WSDLInterface;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceFault;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperation;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationInput;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationOutfault;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationOutput;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationPart;
import org.jboss.ws.metadata.wsdl.WSDLProperty;
import org.jboss.ws.metadata.wsdl.WSDLService;
import org.jboss.ws.metadata.wsdl.WSDLTypes;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.jboss.ws.utils.JavaUtils;
import org.jboss.ws.xop.XOPScanner;

/**
 * A meta data builder that is based on webservices.xml.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-Oct-2005
 */
public abstract class JSR109MetaDataBuilder extends AbstractMetaDataBuilder
{
   // provide logging
   final Logger log = Logger.getLogger(JSR109MetaDataBuilder.class);

   private AddressingConstants ADDR = new AddressingConstantsImpl();

   protected void setupTypesMetaData(ServiceMetaData serviceMetaData)
   {
      WSDLDefinitions wsdlDefinitions = serviceMetaData.getWsdlDefinitions();
      JavaWsdlMapping javaWsdlMapping = serviceMetaData.getJavaWsdlMapping();
      TypesMetaData typesMetaData = serviceMetaData.getTypesMetaData();

      // Copy the schema locations to the types meta data
      if (wsdlDefinitions != null)
      {
         WSDLTypes wsdlTypes = wsdlDefinitions.getWsdlTypes();
         typesMetaData.setSchemaModel(wsdlTypes.getSchemaModel());
      }

      // Copy the type mappings to the types meta data
      if (javaWsdlMapping != null)
      {
         for (JavaXmlTypeMapping xmlTypeMapping : javaWsdlMapping.getJavaXmlTypeMappings())
         {
            String javaTypeName = xmlTypeMapping.getJavaType();
            String qnameScope = xmlTypeMapping.getQnameScope();

            QName xmlType = xmlTypeMapping.getRootTypeQName();
            QName anonymousXMLType = xmlTypeMapping.getAnonymousTypeQName();
            if (xmlType == null && anonymousXMLType != null)
               xmlType = anonymousXMLType;

            String nsURI = xmlType.getNamespaceURI();
            if (Constants.NS_SCHEMA_XSD.equals(nsURI) == false && Constants.URI_SOAP11_ENC.equals(nsURI) == false)
            {
               TypeMappingMetaData tmMetaData = new TypeMappingMetaData(typesMetaData, xmlType, javaTypeName);
               tmMetaData.setQNameScope(qnameScope);
               typesMetaData.addTypeMapping(tmMetaData);
            }
         }

         for (ExceptionMapping exceptionMapping : javaWsdlMapping.getExceptionMappings())
         {
            QName xmlType = exceptionMapping.getWsdlMessage();
            String javaTypeName = exceptionMapping.getExceptionType();
            TypeMappingMetaData tmMetaData = new TypeMappingMetaData(typesMetaData, xmlType, javaTypeName);
            typesMetaData.addTypeMapping(tmMetaData);
         }
      }
   }

   protected void setupOperationsFromWSDL(EndpointMetaData epMetaData, WSDLEndpoint wsdlEndpoint, ServiceEndpointInterfaceMapping seiMapping)
   {
      WSDLDefinitions wsdlDefinitions = wsdlEndpoint.getInterface().getWsdlDefinitions();

      // For every WSDL interface operation build the OperationMetaData
      WSDLInterface wsdlInterface = wsdlEndpoint.getInterface();
      for (WSDLInterfaceOperation wsdlOperation : wsdlInterface.getOperations())
      {
         String opName = wsdlOperation.getName().toString();
         QName opQName = wsdlOperation.getQName();

         // Set java method name
         String javaName = opName.substring(0, 1).toLowerCase() + opName.substring(1);
         ServiceEndpointMethodMapping seiMethodMapping = null;
         if (seiMapping != null)
         {
            epMetaData.setServiceEndpointInterfaceName(seiMapping.getServiceEndpointInterface());

            seiMethodMapping = seiMapping.getServiceEndpointMethodMappingByWsdlOperation(opName);
            if (seiMethodMapping == null)
               throw new WSException("Cannot obtain method maping for: " + opName);

            javaName = seiMethodMapping.getJavaMethodName();
         }

         OperationMetaData opMetaData = new OperationMetaData(epMetaData, opQName, javaName);
         epMetaData.addOperation(opMetaData);

         // Set the operation style
         String style = wsdlOperation.getStyle();
         epMetaData.setStyle(Style.valueOf(style));

         // Set the operation MEP
         if (Constants.WSDL20_PATTERN_IN_ONLY.equals(wsdlOperation.getPattern()))
            opMetaData.setOneWayOperation(true);

         // Set the operation SOAPAction
         WSDLBinding wsdlBinding = wsdlDefinitions.getBindingByInterfaceName(wsdlInterface.getQName());
         WSDLBindingOperation wsdlBindingOperation = wsdlBinding.getOperationByRef(opQName);
         if (wsdlBindingOperation != null)
            opMetaData.setSOAPAction(wsdlBindingOperation.getSOAPAction());

         // Get the type mapping for the encoding style
         String encStyle = epMetaData.getEncodingStyle().toURI();
         TypeMappingRegistry tmRegistry = new TypeMappingRegistryImpl();
         TypeMappingImpl typeMapping = (TypeMappingImpl)tmRegistry.getTypeMapping(encStyle);

         // Build the parameter meta data
         if (opMetaData.getStyle() == Style.RPC)
         {
            buildParameterMetaDataRpc(opMetaData, wsdlOperation, seiMethodMapping, typeMapping);
         }
         else
         {
            buildParameterMetaDataDoc(opMetaData, wsdlOperation, seiMethodMapping, typeMapping);
         }

         // Build operation faults
         buildFaultMetaData(opMetaData, wsdlOperation, seiMapping);

         // process further operation extensions
         processOpMetaExtensions(opMetaData, wsdlOperation);
      }
   }

   private void buildParameterMetaDataRpc(OperationMetaData opMetaData, WSDLInterfaceOperation wsdlOperation, ServiceEndpointMethodMapping seiMethodMapping,
         TypeMappingImpl typeMapping)
   {
      log.trace("buildParameterMetaDataRpc: " + opMetaData.getXmlName());

      TypesMetaData typesMetaData = opMetaData.getEndpointMetaData().getServiceMetaData().getTypesMetaData();

      for (WSDLInterfaceOperationInput opInput : wsdlOperation.getInputs())
      {
         QName xmlName = opInput.getElement();
         QName xmlType = opInput.getXMLType();
         String partName = opInput.getProperty(Constants.WSDL_PROPERTY_PART_NAME).getValue();
         String javaTypeName = typeMapping.getJavaTypeName(xmlType);

         if (seiMethodMapping != null)
         {
            MethodParamPartsMapping paramMapping = seiMethodMapping.getMethodParamPartsMappingByPartName(partName);
            if (paramMapping == null)
               throw new WSException("Cannot obtain method parameter mapping for message part '" + partName + "' in wsdl operation: "
                     + seiMethodMapping.getWsdlOperation());

            javaTypeName = paramMapping.getParamType();
         }

         JavaWsdlMapping javaWsdlMapping = opMetaData.getEndpointMetaData().getServiceMetaData().getJavaWsdlMapping();
         if (javaTypeName == null && javaWsdlMapping != null)
         {
            String packageName = javaWsdlMapping.getPackageNameForNamespaceURI(xmlType.getNamespaceURI());
            if (packageName != null)
            {
               javaTypeName = packageName + "." + xmlType.getLocalPart();
               log.warn("Guess java type from package mapping: [xmlType=" + xmlType + ",javaType=" + javaTypeName + "]");
            }
         }

         if (javaTypeName == null)
            throw new WSException("Cannot obtain java type mapping for: " + xmlType);

         ParameterMetaData inMetaData = new ParameterMetaData(opMetaData, xmlName, xmlType, javaTypeName);
         opMetaData.addParameter(inMetaData);

         // In arrays of user types, wscompile does not generate a mapping in jaxrpc-mapping.xml
         if (typesMetaData.getTypeMappingByXMLType(xmlType) == null)
         {
            String nsURI = xmlType.getNamespaceURI();
            if (Constants.NS_SCHEMA_XSD.equals(nsURI) == false && Constants.URI_SOAP11_ENC.equals(nsURI) == false)
            {
               TypeMappingMetaData tmMetaData = new TypeMappingMetaData(typesMetaData, xmlType, javaTypeName);
               typesMetaData.addTypeMapping(tmMetaData);
            }
         }

         setupAttachmentParameter(opInput, inMetaData);

         boolean inHeader = opInput.getProperty(Constants.WSDL_PROPERTY_APPLICATION_DATA) != null;
         inMetaData.setInHeader(inHeader);

         // SOAPENC:Array
         setupSOAPArrayParameter(inMetaData, javaTypeName);
      }

      for (WSDLInterfaceOperationOutput opOutput : wsdlOperation.getOutputs())
      {
         String partName = opOutput.getProperty(Constants.WSDL_PROPERTY_PART_NAME).getValue();
         QName xmlName = opOutput.getElement();

         ParameterMetaData outMetaData = opMetaData.getParameter(xmlName);
         if (outMetaData != null && wsdlOperation.getInputByPartName(partName) != null)
         {
            outMetaData.setMode(ParameterMode.INOUT);
         }
         else
         {
            QName xmlType = opOutput.getXMLType();
            String javaTypeName = typeMapping.getJavaTypeName(xmlType);

            boolean inHeader = opOutput.getProperty(Constants.WSDL_PROPERTY_APPLICATION_DATA) != null;
            boolean hasReturnMapping = (inHeader == false);

            if (seiMethodMapping != null)
            {
               WsdlReturnValueMapping returnMapping = seiMethodMapping.getWsdlReturnValueMapping();
               if (returnMapping != null)
               {
                  javaTypeName = returnMapping.getMethodReturnValue();
                  hasReturnMapping = true;
               }
               else
               {
                  MethodParamPartsMapping paramMapping = seiMethodMapping.getMethodParamPartsMappingByPartName(partName);
                  if (paramMapping != null)
                  {
                     javaTypeName = paramMapping.getParamType();
                     hasReturnMapping = false;
                  }
               }
            }

            JavaWsdlMapping javaWsdlMapping = opMetaData.getEndpointMetaData().getServiceMetaData().getJavaWsdlMapping();
            if (javaTypeName == null && javaWsdlMapping != null)
            {
               String packageName = javaWsdlMapping.getPackageNameForNamespaceURI(xmlType.getNamespaceURI());
               if (packageName != null)
               {
                  javaTypeName = packageName + "." + xmlType.getLocalPart();
                  log.warn("Guess java type from package mapping: [xmlType=" + xmlType + ",javaType=" + javaTypeName + "]");
               }
            }

            if (javaTypeName == null)
               throw new WSException("Cannot obtain java type mapping for: " + xmlType);

            if (hasReturnMapping)
            {
               outMetaData = new ParameterMetaData(opMetaData, xmlName, xmlType, javaTypeName);
               opMetaData.setReturnParameter(outMetaData);

               // In arrays of user types, wscompile does not generate a mapping in jaxrpc-mapping.xml
               if (typesMetaData.getTypeMappingByXMLType(xmlType) == null)
               {
                  String nsURI = xmlType.getNamespaceURI();
                  if (Constants.NS_SCHEMA_XSD.equals(nsURI) == false && Constants.URI_SOAP11_ENC.equals(nsURI) == false)
                  {
                     TypeMappingMetaData tmMetaData = new TypeMappingMetaData(typesMetaData, xmlType, javaTypeName);
                     typesMetaData.addTypeMapping(tmMetaData);
                  }
               }

               setupAttachmentParameter(opOutput, outMetaData);
            }
            else
            {
               outMetaData = new ParameterMetaData(opMetaData, xmlName, xmlType, javaTypeName);
               outMetaData.setMode(ParameterMode.OUT);
               opMetaData.addParameter(outMetaData);

               setupAttachmentParameter(opOutput, outMetaData);

               outMetaData.setInHeader(inHeader);
            }

            // SOAPENC:Array
            setupSOAPArrayParameter(outMetaData, javaTypeName);
         }
      }
   }

   private void setupAttachmentParameter(WSDLInterfaceOperationPart wsdlOperationPart, ParameterMetaData paramMetaData)
   {
      QName xmlType = paramMetaData.getXmlType();
      QName xmlName = paramMetaData.getXmlName();

      WSDLProperty mimeTypeProp = wsdlOperationPart.getProperty(Constants.WSDL_PROPERTY_WSDL11_MIME_TYPE);
      if (mimeTypeProp != null)
      {
         String mimeTypes = mimeTypeProp.getValue();
         paramMetaData.setMimeTypes(mimeTypes);
         paramMetaData.setSwA(true);
      }

      // An XOP parameter is detected if it is a complex type that derives from xsd:base64Binary
      WSDLInterfaceOperation wsdlOperation = wsdlOperationPart.getWsdlOperation();
      WSDLTypes wsdlTypes = wsdlOperation.getWsdlInterface().getWsdlDefinitions().getWsdlTypes();
      JBossXSModel schemaModel = wsdlTypes.getSchemaModel();
      XSTypeDefinition xsType = schemaModel.getTypeDefinition(xmlType.getLocalPart(), xmlType.getNamespaceURI());
      XOPScanner scanner = new XOPScanner();
      if(scanner.findXOPTypeDef(xsType)!=null)
      {
         // FIXME: read the xmime:contentType from the element declaration
         // See SchemaUtils#findXOPTypeDef(XSTypeDefinition typeDef) for details

         /*
         FIXME: the classloader is not set yet
         paramMetaData.setXopContentType(
             MimeUtils.resolveMimeType(paramMetaData.getJavaType())
         );
         */

         paramMetaData.setXOP(true);

      }
   }

   /* SOAP-ENC:Array
    *
    * FIXME: This hack should be removed as soon as we can reliably get the
    * soapenc:arrayType from wsdl + schema.
    */
   private void setupSOAPArrayParameter(ParameterMetaData paramMetaData, String javaTypeName)
   {
      Use use = paramMetaData.getOperationMetaData().getUse();
      String xmlTypeLocalPart = paramMetaData.getXmlType().getLocalPart();
      if (use == Use.ENCODED && xmlTypeLocalPart.indexOf("ArrayOf") >= 0)
      {
         paramMetaData.setSOAPArrayParam(true);
         try
         {
            // This approach determins the array component type from the javaTypeName.
            // It will not work for user defined types, nor will the array dimension be
            // initialized properly. Ideally the array parameter meta data should be initialized
            // from the XSModel or wherever it is defined in WSDL.
            Class javaType = JavaUtils.loadJavaType(javaTypeName);
            Class compJavaType = javaType.getComponentType();

            if (xmlTypeLocalPart.indexOf("ArrayOfArrayOf") >= 0)
               compJavaType = compJavaType.getComponentType();

            QName compXMLType = new LiteralTypeMapping().getXMLType(compJavaType);
            paramMetaData.setSOAPArrayCompType(compXMLType);
         }
         catch (ClassNotFoundException e)
         {
            // ignore that user defined types cannot be loaded yet
         }
      }
   }

   private void buildParameterMetaDataDoc(OperationMetaData opMetaData, WSDLInterfaceOperation wsdlOperation, ServiceEndpointMethodMapping seiMethodMapping,
         TypeMappingImpl typeMapping)
   {
      log.trace("buildParameterMetaDataDoc: " + opMetaData.getXmlName());

      EndpointMetaData epMetaData = opMetaData.getEndpointMetaData();
      ServiceMetaData serviceMetaData = epMetaData.getServiceMetaData();
      TypesMetaData typesMetaData = serviceMetaData.getTypesMetaData();

      WSDLDefinitions wsdlDefinitions = wsdlOperation.getWsdlInterface().getWsdlDefinitions();
      WSDLTypes wsdlTypes = wsdlDefinitions.getWsdlTypes();

      for (WSDLInterfaceOperationInput opInput : wsdlOperation.getInputs())
      {
         QName xmlName = opInput.getElement();
         QName xmlType = opInput.getXMLType();
         String javaTypeName = typeMapping.getJavaTypeName(xmlType);

         TypeMappingMetaData typeMetaData = typesMetaData.getTypeMappingByXMLType(xmlType);
         if (typeMetaData != null)
            javaTypeName = typeMetaData.getJavaTypeName();

         if (javaTypeName == null)
            throw new WSException("Cannot obtain java type mapping for: " + xmlType);

         // Check if we need to wrap the parameters
         boolean isWrapParameters = (seiMethodMapping != null ? seiMethodMapping.isWrappedElement() : false);
         log.trace("isWrapParameters based on wrapped-element: " + isWrapParameters);
         if (isWrapParameters == false && seiMethodMapping != null)
         {
            MethodParamPartsMapping[] partsMappings = seiMethodMapping.getMethodParamPartsMappings();
            if (partsMappings.length > 0)
            {
               boolean matchingPartFound = false;
               for (MethodParamPartsMapping partsMapping : partsMappings)
               {
                  String paramTypeName = partsMapping.getParamType();
                  if (paramTypeName.equals(javaTypeName))
                  {
                     matchingPartFound = true;
                     break;
                  }
                  else
                  {
                     // Check assignability, JavaUtils.isAssignableFrom("org.w3c.dom.Element", "javax.xml.soap.SOAPElement")
                     try
                     {
                        Class paramType = JavaUtils.loadJavaType(paramTypeName);
                        Class javaType = JavaUtils.loadJavaType(javaTypeName);

                        // If it is assignable the explict mapping takes precedence and we don't wrap
                        if (JavaUtils.isAssignableFrom(javaType, paramType))
                        {
                           javaTypeName = paramTypeName;
                           matchingPartFound = true;
                           break;
                        }
                     }
                     catch (ClassNotFoundException e)
                     {
                        // Ignore. For simple types this should work, others should be lexically equal
                        // if it is not wrapped.
                     }
                  }
               }
               isWrapParameters = (matchingPartFound == false);
               log.trace("isWrapParameters based on matching parts: " + isWrapParameters);
            }
         }

         ParameterMetaData inMetaData = new ParameterMetaData(opMetaData, xmlName, xmlType, javaTypeName);
         setupAttachmentParameter(opInput, inMetaData);
         epMetaData.setParameterStyle(isWrapParameters ? ParameterStyle.WRAPPED : ParameterStyle.BARE);
         inMetaData.setInHeader(opInput.getProperty(Constants.WSDL_PROPERTY_APPLICATION_DATA) != null);
         opMetaData.addParameter(inMetaData);

         // Set the variable names
         if (opMetaData.isDocumentWrapped())
         {
            if (seiMethodMapping == null)
               throw new IllegalArgumentException("Cannot wrap parameters without SEI method mapping");

            ServiceEndpointInterfaceMapping seiMapping = seiMethodMapping.getServiceEndpointInterfaceMapping();
            JavaXmlTypeMapping javaXmlTypeMapping = seiMapping.getJavaWsdlMapping().getTypeMappingForQName(xmlType);
            if (javaXmlTypeMapping == null)
               throw new WSException("Cannot obtain java/xml type mapping for: " + xmlType);

            List<String> variableNames = new ArrayList<String>();
            for (VariableMapping varMapping : javaXmlTypeMapping.getVariableMappings())
            {
               variableNames.add(varMapping.getJavaVariableName());
            }
            inMetaData.setWrappedVariables(variableNames);
         }
      }

      for (WSDLInterfaceOperationOutput opOutput : wsdlOperation.getOutputs())
      {
         String partName = opOutput.getProperty(Constants.WSDL_PROPERTY_PART_NAME).getValue();
         QName xmlName = opOutput.getElement();

         ParameterMetaData paramMetaData = opMetaData.getParameter(xmlName);
         if (paramMetaData != null && wsdlOperation.getInputByPartName(partName) != null)
         {
            paramMetaData.setMode(ParameterMode.INOUT);
         }
         else
         {
            QName xmlType = opOutput.getXMLType();
            String javaTypeName = typeMapping.getJavaTypeName(xmlType);

            boolean inHeader = opOutput.getProperty(Constants.WSDL_PROPERTY_APPLICATION_DATA) != null;
            boolean hasReturnMapping = (inHeader == false);

            if (typesMetaData.getTypeMappingByXMLType(xmlType) != null)
               javaTypeName = typesMetaData.getTypeMappingByXMLType(xmlType).getJavaTypeName();

            if (javaTypeName == null)
               throw new WSException("Cannot obtain java/xml type mapping for: " + xmlType);

            if (hasReturnMapping)
            {
               if (seiMethodMapping != null)
               {
                  WsdlReturnValueMapping returnValueMapping = seiMethodMapping.getWsdlReturnValueMapping();
                  if ((returnValueMapping != null && returnValueMapping.getMethodReturnValue().equals(javaTypeName)) == false)
                  {
                     if (xmlType.equals(Constants.TYPE_LITERAL_ANYTYPE))
                     {
                        javaTypeName = returnValueMapping.getMethodReturnValue();
                     }
                  }
               }

               ParameterMetaData retMetaData = new ParameterMetaData(opMetaData, xmlName, xmlType, javaTypeName);
               opMetaData.setReturnParameter(retMetaData);

               setupAttachmentParameter(opOutput, retMetaData);

               // Set the variable names
               if (opMetaData.getParameterStyle() == ParameterStyle.WRAPPED)
               {
                  if (seiMethodMapping == null)
                     throw new IllegalArgumentException("Cannot wrap parameters without SEI method mapping");

                  ServiceEndpointInterfaceMapping seiMapping = seiMethodMapping.getServiceEndpointInterfaceMapping();
                  JavaWsdlMapping javaWsdlMapping = seiMapping.getJavaWsdlMapping();
                  JavaXmlTypeMapping javaXmlTypeMapping = javaWsdlMapping.getTypeMappingForQName(xmlType);
                  if (typeMapping == null)
                     throw new WSException("Cannot obtain java/xml type mapping for: " + xmlType);

                  List<String> variableNames = new ArrayList<String>();
                  for (VariableMapping varMapping : javaXmlTypeMapping.getVariableMappings())
                  {
                     variableNames.add(varMapping.getJavaVariableName());
                  }
                  retMetaData.setWrappedVariables(variableNames);
               }
            }
            else
            {
               ParameterMetaData outMetaData = new ParameterMetaData(opMetaData, xmlName, xmlType, javaTypeName);
               opMetaData.addParameter(outMetaData);
               outMetaData.setMode(ParameterMode.OUT);

               setupAttachmentParameter(opOutput, outMetaData);

               outMetaData.setInHeader(inHeader);
            }
         }
      }

      // Add header parameters that are defined in jaxrpc-mapping but are not part of the wsdl message parts
      // http://jira.jboss.org/jira/browse/JBWS-663
      if (seiMethodMapping != null && wsdlDefinitions.getWsdlOneOneDefinition() != null)
      {
         MethodParamPartsMapping[] mppMappings = seiMethodMapping.getMethodParamPartsMappings();
         for (int i = 0; i < mppMappings.length; i++)
         {
            MethodParamPartsMapping mppMapping = mppMappings[i];
            String javaTypeName = mppMapping.getParamType();

            WsdlMessageMapping wmMapping = mppMapping.getWsdlMessageMapping();
            if (wmMapping.isSoapHeader())
            {
               QName wsdlMessageName = wmMapping.getWsdlMessage();
               String partName = wmMapping.getWsdlMessagePartName();
               Message wsdl11Message = wsdlDefinitions.getWsdlOneOneDefinition().getMessage(wsdlMessageName);
               Part wsdl11Part = wsdl11Message.getPart(partName);
               QName xmlName = wsdl11Part.getElementName();
               if (opMetaData.getParameter(xmlName) == null)
               {
                  String mode = wmMapping.getParameterMode();
                  QName xmlType = wsdlTypes.getXMLType(xmlName);

                  ParameterMetaData headerMetaData = new ParameterMetaData(opMetaData, xmlName, xmlType, javaTypeName);
                  opMetaData.addParameter(headerMetaData);
                  headerMetaData.setInHeader(true);
                  headerMetaData.setMode(mode);
               }
            }
         }
      }
   }

   private void buildFaultMetaData(OperationMetaData opMetaData, WSDLInterfaceOperation wsdlOperation, ServiceEndpointInterfaceMapping seiMapping)
   {
      TypesMetaData typesMetaData = opMetaData.getEndpointMetaData().getServiceMetaData().getTypesMetaData();

      WSDLInterface wsdlInterface = wsdlOperation.getWsdlInterface();
      for (WSDLInterfaceOperationOutfault outFault : wsdlOperation.getOutfaults())
      {
         QName ref = outFault.getRef();

         WSDLInterfaceFault wsdlFault = wsdlInterface.getFault(new NCName(ref.getLocalPart()));
         QName xmlName = wsdlFault.getXmlName();
         QName xmlType = wsdlFault.getXmlType();
         String javaTypeName = null;

         if (typesMetaData.getTypeMappingByXMLType(xmlType) != null)
            javaTypeName = typesMetaData.getTypeMappingByXMLType(xmlType).getJavaTypeName();

         if (javaTypeName == null)
         {
            log.warn("Cannot obtain java type mapping for: " + xmlType);
            javaTypeName = new UnqualifiedFaultException(xmlType).getClass().getName();
         }

         FaultMetaData faultMetaData = new FaultMetaData(opMetaData, xmlName, xmlType, javaTypeName);
         opMetaData.addFault(faultMetaData);
      }
   }

   /** Initialize the endpoint encoding style from the binding operations
    */
   protected void initEndpointEncodingStyle(EndpointMetaData epMetaData)
   {
      WSDLDefinitions wsdlDefinitions = epMetaData.getServiceMetaData().getWsdlDefinitions();
      for (WSDLService wsdlService : wsdlDefinitions.getServices())
      {
         for (WSDLEndpoint wsdlEndpoint : wsdlService.getEndpoints())
         {
            if (epMetaData.getName().equals(wsdlEndpoint.getQName()))
            {
               QName bindQName = wsdlEndpoint.getBinding();
               NCName ncName = new NCName(bindQName.getLocalPart());
               WSDLBinding wsdlBinding = wsdlDefinitions.getBinding(ncName);
               if (wsdlBinding == null)
                  throw new WSException("Cannot obtain binding: " + ncName);

               for (WSDLBindingOperation wsdlBindingOperation : wsdlBinding.getOperations())
               {
                  String encStyle = wsdlBindingOperation.getEncodingStyle();
                  epMetaData.setEncodingStyle(Use.valueOf(encStyle));
               }
            }
         }
      }
   }

   protected void processEndpointMetaDataExtensions(EndpointMetaData epMetaData, WSDLDefinitions wsdlDefinitions)
   {
      for (WSDLInterface wsdlInterface : wsdlDefinitions.getInterfaces())
      {
         WSDLProperty eventSourceProp = wsdlInterface.getProperty(Constants.WSDL_PROPERTY_EVENTSOURCE);
         if (eventSourceProp != null && epMetaData instanceof ServerEndpointMetaData)
         {
            ServerEndpointMetaData sepMetaData = (ServerEndpointMetaData)epMetaData;
            String eventSourceNS = wsdlInterface.getQName().getNamespaceURI() + "/" + wsdlInterface.getQName().getLocalPart();
            Object notificationSchema = null; // todo: resolve schema from operation message

            EventingEpMetaExt ext = new EventingEpMetaExt(EventingConstants.NS_EVENTING);
            ext.setEventSourceNS(eventSourceNS);
            ext.setNotificationSchema(notificationSchema);

            sepMetaData.addExtension(ext);
            sepMetaData.setManagedEndpointBean(EventingEndpoint.class.getName());
         }
      }
   }

   /**
    * Process operation meta data extensions.
    *
    * @param opMetaData
    * @param wsdlOperation
    */
   protected void processOpMetaExtensions(OperationMetaData opMetaData, WSDLInterfaceOperation wsdlOperation)
   {

      String tns = wsdlOperation.getQName().getNamespaceURI();
      String portTypeName = wsdlOperation.getQName().getLocalPart();

      AddressingOpMetaExt addrExt = new AddressingOpMetaExt(ADDR.getNamespaceURI());

      // inbound action
      WSDLProperty wsaInAction = wsdlOperation.getProperty(Constants.WSDL_PROPERTY_ACTION_IN);
      if (wsaInAction != null)
      {
         addrExt.setInboundAction(wsaInAction.getValue());
      }
      else
      {
         WSDLProperty messageName = wsdlOperation.getProperty(Constants.WSDL_PROPERTY_MESSAGE_NAME_IN);
         addrExt.setInboundAction(tns + "/" + portTypeName + "/" + messageName);
      }

      // outbound action
      WSDLProperty wsaOutAction = wsdlOperation.getProperty(Constants.WSDL_PROPERTY_ACTION_OUT);
      if (wsaOutAction != null)
      {
         addrExt.setOutboundAction(wsaOutAction.getValue());
      }
      else
      {
         WSDLProperty messageName = wsdlOperation.getProperty(Constants.WSDL_PROPERTY_MESSAGE_NAME_OUT);
         addrExt.setOutboundAction(tns + "/" + portTypeName + "/" + messageName);
      }

      opMetaData.addExtension(addrExt);
   }

   /**
    * Build default action according to the pattern described in
    * http://www.w3.org/Submission/2004/SUBM-ws-addressing-20040810/
    * Section 3.3.2 'Default Action Pattern'<br>
    * [target namespace]/[port type name]/[input|output name]
    *
    * @param wsdlOperation
    * @return action value
    */
   private String buildWsaActionValue(WSDLInterfaceOperation wsdlOperation)
   {
      WSDLProperty wsaAction = wsdlOperation.getProperty(Constants.WSDL_ATTRIBUTE_WSA_ACTION.toString());
      String actionValue = null;

      if (null == wsaAction)
      {

         String tns = wsdlOperation.getQName().getNamespaceURI();
         String portTypeName = wsdlOperation.getQName().getLocalPart();
         WSDLProperty messageName = wsdlOperation.getProperty("http://www.jboss.org/jbossws/messagename/in");

         actionValue = new String(tns + "/" + portTypeName + "/" + messageName.getValue());
      }
      else
      {
         actionValue = wsaAction.getValue();
      }

      return actionValue;
   }

}
