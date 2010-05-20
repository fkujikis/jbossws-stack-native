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
package org.jboss.ws.tools.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.encoding.TypeMapping;
import javax.xml.rpc.holders.ByteArrayHolder;
import javax.xml.rpc.holders.Holder;

import org.apache.xerces.xs.XSTypeDefinition;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.jaxrpc.LiteralTypeMapping;
import org.jboss.ws.jaxrpc.Style;
import org.jboss.ws.metadata.EndpointMetaData;
import org.jboss.ws.metadata.FaultMetaData;
import org.jboss.ws.metadata.OperationMetaData;
import org.jboss.ws.metadata.ParameterMetaData;
import org.jboss.ws.metadata.jaxrpcmapping.ExceptionMapping;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.ws.metadata.jaxrpcmapping.JavaXmlTypeMapping;
import org.jboss.ws.metadata.jaxrpcmapping.MethodParamPartsMapping;
import org.jboss.ws.metadata.jaxrpcmapping.PackageMapping;
import org.jboss.ws.metadata.jaxrpcmapping.PortMapping;
import org.jboss.ws.metadata.jaxrpcmapping.ServiceEndpointInterfaceMapping;
import org.jboss.ws.metadata.jaxrpcmapping.ServiceEndpointMethodMapping;
import org.jboss.ws.metadata.jaxrpcmapping.ServiceInterfaceMapping;
import org.jboss.ws.metadata.jaxrpcmapping.VariableMapping;
import org.jboss.ws.metadata.jaxrpcmapping.WsdlMessageMapping;
import org.jboss.ws.metadata.jaxrpcmapping.WsdlReturnValueMapping;
import org.jboss.ws.metadata.wsdl.NCName;
import org.jboss.ws.metadata.wsdl.WSDLBinding;
import org.jboss.ws.metadata.wsdl.WSDLBindingFault;
import org.jboss.ws.metadata.wsdl.WSDLBindingOperation;
import org.jboss.ws.metadata.wsdl.WSDLBindingOperationInput;
import org.jboss.ws.metadata.wsdl.WSDLBindingOperationOutput;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLEndpoint;
import org.jboss.ws.metadata.wsdl.WSDLInterface;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceFault;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperation;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationInput;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationOutfault;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationOutput;
import org.jboss.ws.metadata.wsdl.WSDLProperty;
import org.jboss.ws.metadata.wsdl.WSDLService;
import org.jboss.ws.metadata.wsdl.WSDLTypes;
import org.jboss.ws.metadata.wsdl.WSDLUtils;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSElementDeclaration;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.jboss.ws.metadata.wsdl.xmlschema.WSSchemaUtils;
import org.jboss.ws.metadata.wsdl.xsd.SchemaUtils;
import org.jboss.ws.tools.JavaToXSD;
import org.jboss.ws.tools.interfaces.JavaToXSDIntf;
import org.jboss.ws.tools.interfaces.SchemaCreatorIntf;
import org.jboss.ws.utils.JavaUtils;

/**
 *  Java To WSDL Helper which uses UnifiedMetaData
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since  Oct 7, 2005
 */
public class JavaToWSDLHelper
{
   protected WSDLDefinitions wsdl = null;

   protected String wsdlNamespace =  Constants.NS_WSDL11; //Default - wsdl11;

   protected JavaToXSDIntf javaToXSD = new JavaToXSD();

   protected Class seiClass = null;

   private JavaWsdlMapping javaWsdlMapping = new JavaWsdlMapping();

   private Map<QName, JavaXmlTypeMapping> mappedTypes = new HashMap<QName, JavaXmlTypeMapping>();

   private Set<String> mappedPackages = new HashSet<String>();

   protected Map<String,String> packageNamespaceMap = new HashMap<String,String>();

   private Set<String> mappedExceptions = new HashSet<String>();

   // Features as represented by Constants
   protected Map<String, Boolean> features = new HashMap<String, Boolean>();

   /**
    * Default ctr
    */
   public JavaToWSDLHelper(WSDLDefinitions wsdl, String wsdlNS)
   {
      this.wsdl = wsdl;
      this.wsdlNamespace = wsdlNS;
   }

   public void appendDefinitions(String targetNamespace)
   {
      wsdl.setTargetNamespace(targetNamespace);
      wsdl.registerNamespaceURI(targetNamespace, Constants.PREFIX_TNS);
      wsdl.registerNamespaceURI(Constants.NS_SCHEMA_XSD, Constants.PREFIX_XSD);
      if (wsdlNamespace.equals(Constants.NS_WSDL11))
         wsdl.registerNamespaceURI(Constants.NS_SOAP11, Constants.PREFIX_SOAP);
   }


   public void generateTypesForXSD(ParameterMetaData pmd) throws IOException
   {
      //Types always deals with TypeNamespace
      SchemaCreatorIntf sc = javaToXSD.getSchemaCreator();
      QName xmlType = pmd.getXmlType();
      if(xmlType.getNamespaceURI().equals(Constants.NS_SCHEMA_XSD) == false)
        generateType(xmlType, pmd.getJavaType(), buildElementNameMap(pmd));

      if (pmd.getOperationMetaData().getStyle() == Style.DOCUMENT || pmd.isInHeader())
         generateElement(pmd.getXmlName(), xmlType);

      //Attachment type
      if(pmd.isSwA())
         wsdl.registerNamespaceURI(Constants.NS_SWA_MIME, "mime");
   }

   private Map<String, QName> buildElementNameMap(ParameterMetaData pmd)
   {
      List<QName> elements = pmd.getWrappedElementNames();
      List<String> variables = pmd.getWrappedVariables();

      if (elements == null || variables == null)
         return null;

      if (elements.size() != variables.size())
         return null;

      Map<String, QName> map = new LinkedHashMap<String, QName>(elements.size());

      int i = 0;
      for (String variable : variables)
         map.put(variable, elements.get(i++));

      return map;
   }

   public void generateTypesForXSD(FaultMetaData fmd) throws IOException
   {
      //Types always deals with TypeNamespace
      SchemaCreatorIntf sc = javaToXSD.getSchemaCreator();
      //Look at the features
      QName xmlType = fmd.getXmlType();
      if(xmlType.getNamespaceURI().equals(Constants.NS_SCHEMA_XSD) == false)
        generateType(xmlType ,fmd.getJavaType(), null);
   }

   public void generateInterfaces(OperationMetaData op , String intfName)
   {
      WSDLUtils utils = WSDLUtils.getInstance();
      if(op == null)
         throw new IllegalArgumentException("Illegal Null Argument: op");

      WSDLInterface wsdlInterface = wsdl.getInterface(new NCName(intfName));
      if(wsdlInterface  == null)
      {
         wsdlInterface = new WSDLInterface(wsdl);
         wsdlInterface.setName(new NCName(intfName));
         wsdl.addInterface(wsdlInterface);
      }

      QName xmlName = op.getXmlName();
      String opname = xmlName.getLocalPart();
      if(opname == null || opname.length() == 0)
         throw new WSException("opname is null or blank");
      //Operation Level
      WSDLInterfaceOperation wsdlInterfaceOperation = new WSDLInterfaceOperation(wsdlInterface);
      wsdlInterfaceOperation.setName(new NCName( opname ));
      wsdlInterface.addOperation(wsdlInterfaceOperation);
      if (op.getStyle() == Style.DOCUMENT)
         wsdlInterfaceOperation.setStyle(Constants.URI_STYLE_IRI);
      else
         wsdlInterfaceOperation.setStyle(Constants.URI_STYLE_RPC);

      if (op.isOneWayOperation())
         wsdlInterfaceOperation.setPattern(Constants.WSDL20_PATTERN_IN_ONLY);
      else
         wsdlInterfaceOperation.setPattern(Constants.WSDL20_PATTERN_IN_OUT);

      List<WSDLInterfaceOperationOutput> holderOuts = new ArrayList<WSDLInterfaceOperationOutput>();

      //Parameter Level
      List<ParameterMetaData> pmds = op.getParameters();
      for(ParameterMetaData pmd : pmds)
      {
         if (pmd.getMode() != ParameterMode.IN)
         {
            if(op.isOneWayOperation())
               throw new WSException(opname + " is a oneway operation and" +
               " defines a holder");

            holderOuts.add(getWSDLInterfaceOperationOutput(wsdlInterfaceOperation, pmd));
            if (pmd.getMode() == ParameterMode.INOUT)
               wsdlInterfaceOperation.addInput(getWSDLInterfaceOperationInput(wsdlInterfaceOperation, pmd));
         }
         else
         {
            wsdlInterfaceOperation.addInput(getWSDLInterfaceOperationInput(wsdlInterfaceOperation, pmd));
         }
      }

      //Deal with Return Type
      ParameterMetaData retpmd = op.getReturnParameter();
      Class returnType = retpmd != null ? retpmd.getClass() : void.class;

      if (returnType != null && !(void.class == returnType) )
      {
         WSDLInterfaceOperationOutput returnOutput = getWSDLInterfaceOperationOutput(wsdlInterfaceOperation, retpmd);
         returnOutput.addProperty(new WSDLProperty(Constants.WSDL_PROPERTY_RETURN_PART, null));
         wsdlInterfaceOperation.addOutput(returnOutput);
      }
      //Deal with the temporary arraylist for holders
      for (WSDLInterfaceOperationOutput wo : holderOuts)
         wsdlInterfaceOperation.addOutput(wo);

      List<FaultMetaData> fmds = op.getFaults();
      for(FaultMetaData fmd: fmds)
      {
         generateWSDLFaults(wsdlInterfaceOperation, fmd.getXmlName());
      }
   }

   public void generateBindings(OperationMetaData op , String bindName)
   {
      WSDLUtils utils = WSDLUtils.getInstance();
      String intfName = utils.chop(bindName, "Binding");

      WSDLBinding wsdlBinding = wsdl.getBinding(new NCName(bindName));
      if(wsdlBinding == null)
      {
         wsdlBinding = new WSDLBinding(wsdl);
         wsdlBinding.setName(new NCName(bindName));
         wsdlBinding.setInterfaceName(new QName(wsdl.getTargetNamespace(),intfName));
         wsdl.addBinding(wsdlBinding);
      }

      String opname = op.getXmlName().getLocalPart();
      //Operation Level
      WSDLBindingOperation wsdlBindingOperation = new WSDLBindingOperation(wsdlBinding);
      wsdlBindingOperation.setRef(new QName(wsdl.getTargetNamespace(), opname));
      wsdlBindingOperation.setSOAPAction(op.getSOAPAction());
      wsdlBinding.addOperation(wsdlBindingOperation);

      //TODO:FIXME JBWS-269
      //Parameter Level

      List<ParameterMetaData> pmds = op.getInputParameters();
      for(ParameterMetaData pmd : pmds)
      {
         WSDLBindingOperationInput wsdlBindingOperationInput = new WSDLBindingOperationInput(wsdlBindingOperation);
         wsdlBindingOperationInput.setMessageLabel(new NCName(pmd.getXmlName().getLocalPart()) );
         wsdlBindingOperation.addInput(wsdlBindingOperationInput);
      }

      //Deal with Return Type
      ParameterMetaData retpmd = op.getReturnParameter();
      Class returnType = retpmd != null ? retpmd.getClass() : void.class;

      if (returnType != null && !(void.class == returnType) )
      {
         WSDLBindingOperationOutput wsdlBindingOperationOutput = new WSDLBindingOperationOutput(wsdlBindingOperation);
         wsdlBindingOperationOutput.setMessageLabel(new NCName(retpmd.getXmlName().getLocalPart()));
         wsdlBindingOperation.addOutput(wsdlBindingOperationOutput);
      }
// What was this for?
//      if(void.class == returnType)
//      {
//         WSDLBindingOperationOutput wsdlBindingOperationOutput = new WSDLBindingOperationOutput(wsdlBindingOperation);
//         wsdlBindingOperation.addOutput(wsdlBindingOperationOutput);
//      }

      pmds = op.getOutputParameters();
      for(ParameterMetaData pmd : pmds)
      {
         WSDLBindingOperationOutput wsdlBindingOperationOutput = new WSDLBindingOperationOutput(wsdlBindingOperation);
         wsdlBindingOperationOutput.setMessageLabel(new NCName(pmd.getXmlName().getLocalPart()) );
         wsdlBindingOperation.addOutput(wsdlBindingOperationOutput);
      }

      List<FaultMetaData> fmds = op.getFaults();
      for(FaultMetaData fmd: fmds)
      {
         WSDLBindingFault f = new WSDLBindingFault(wsdlBinding);
         String faultName = fmd.getXmlName().getLocalPart();
         WSDLInterfaceFault intFault = wsdl.getInterface(new NCName(intfName)).getFault(new NCName(faultName));
         if(intFault == null)
            throw new WSException("Fault in WSDLInterface for name=" + faultName + " not found");
         QName fqname = new QName(wsdl.getTargetNamespace(), faultName );
         f.setRef(fqname);
      }
   }

   public void generateServices(EndpointMetaData endpoint, String intfName)
   {
      if(endpoint == null)
         throw new WSException("Illegal Null Argument: endpt");
      String serviceName = endpoint.getServiceMetaData().getName().getLocalPart();

      //Create a WSDLService
      WSDLService wsdlService = new WSDLService(wsdl);
      wsdlService.setName( new NCName(serviceName) );
      wsdl.addService(wsdlService);

      String portName = endpoint.getName().getLocalPart();

      WSDLInterface wsdlInterface = wsdl.getInterface(new NCName(intfName));
      if(wsdlInterface == null)
         throw new WSException("WSDL Interface for name = " + intfName + " is null");

      QName seiQName = wsdlInterface.getQName();
      WSDLBinding wsdlBinding = wsdl.getBindings()[0];
      NCName bindingName = wsdlBinding.getName();
      QName bindingQName = new QName(wsdl.getTargetNamespace(), bindingName.toString());

      wsdlService.setInterfaceName(seiQName);

      WSDLEndpoint wsdlEndpoint = new WSDLEndpoint(wsdlService);
      wsdlEndpoint.setName(new NCName(portName));
      wsdlEndpoint.setBinding(bindingQName);
      wsdlEndpoint.setAddress("REPLACE_WITH_ACTUAL_URL");
      wsdlService.addEndpoint(wsdlEndpoint);

      buildServiceMapping(endpoint, intfName);
   }

   /*
    * Currently we only handle 1 endpoint on 1 service, this is the way everything
    * else is handled anyway.
    */
   private void buildServiceMapping(EndpointMetaData endpoint, String interfaceName)
   {
      QName origQName = endpoint.getServiceMetaData().getName();
      String serviceInterfaceName = endpoint.getServiceEndpointInterface().getPackage().getName() + "." + origQName.getLocalPart();
      QName serviceQName = new QName(origQName.getNamespaceURI(), origQName.getLocalPart(), "serviceNS");

      ServiceInterfaceMapping serviceMapping = new ServiceInterfaceMapping(javaWsdlMapping);
      serviceMapping.setServiceInterface(serviceInterfaceName);
      serviceMapping.setWsdlServiceName(serviceQName);

      String endpointName = endpoint.getName().getLocalPart();
      PortMapping portMapping = new PortMapping(serviceMapping);
      portMapping.setJavaPortName(endpointName);
      portMapping.setPortName(endpointName);
      serviceMapping.addPortMapping(portMapping);

      javaWsdlMapping.addServiceInterfaceMappings(serviceMapping);

      ServiceEndpointInterfaceMapping seiMapping = new ServiceEndpointInterfaceMapping(javaWsdlMapping);
      seiMapping.setServiceEndpointInterface(endpoint.getServiceEndpointInterfaceName());
      seiMapping.setWsdlPortType(new QName(wsdl.getTargetNamespace(), interfaceName, "portTypeNS"));
      seiMapping.setWsdlBinding(new QName(wsdl.getTargetNamespace(), interfaceName + "Binding", "bindingNS"));
      for (OperationMetaData operation : endpoint.getOperations())
      {
         ServiceEndpointMethodMapping methodMapping = new ServiceEndpointMethodMapping(seiMapping);
         methodMapping.setJavaMethodName(operation.getJavaName());
         methodMapping.setWsdlOperation(operation.getXmlName().getLocalPart());
         boolean isWrapped = operation.isDocumentWrapped();
         methodMapping.setWrappedElement(isWrapped);
         int i = 0;
         for (ParameterMetaData param : operation.getParameters())
         {
            if (isWrapped && param.isInHeader() == false)
            {
               List<String> wrappedTypes = param.getWrappedTypes();
               List<QName> wrappedElementNames = param.getWrappedElementNames();
               for (int c = 0; c < wrappedTypes.size(); c++)
               {
                  String type = JavaUtils.convertJVMNameToSourceName(wrappedTypes.get(c), endpoint.getClassLoader());
                  String name = wrappedElementNames.get(c).getLocalPart();

                  buildParamMapping(methodMapping, interfaceName, operation, name, type, "IN", false, i++);
               }
            }
            else
            {
               String name = param.getXmlName().getLocalPart();
               String type = JavaUtils.convertJVMNameToSourceName(param.getJavaTypeName(), endpoint.getClassLoader());
               buildParamMapping(methodMapping, interfaceName, operation, name, type, param.getMode().toString(), param.isInHeader(), i++);
            }
         }

         ParameterMetaData returnParam = operation.getReturnParameter();
         if (returnParam != null && ((! isWrapped) || (! returnParam.getWrappedElementNames().isEmpty())))
         {
            String name, type;
            if (isWrapped)
            {
               name = returnParam.getWrappedElementNames().get(0).getLocalPart();
               type = returnParam.getWrappedTypes().get(0);
            }
            else
            {
               name = returnParam.getXmlName().getLocalPart();
               type = returnParam.getJavaTypeName();
            }

            type = JavaUtils.convertJVMNameToSourceName(type, endpoint.getClassLoader());

            buildReturnParamMapping(methodMapping, interfaceName, operation, name, type);
         }
         seiMapping.addServiceEndpointMethodMapping(methodMapping);

         for(FaultMetaData fmd : operation.getFaults())
         {
            JavaXmlTypeMapping typeMapping = mappedTypes.get(fmd.getXmlType());
            if (typeMapping == null)
               continue;

            String javaTypeName = fmd.getJavaTypeName();
            if (mappedExceptions.contains(javaTypeName))
               continue;

            mappedExceptions.add(javaTypeName);

            ExceptionMapping mapping = new ExceptionMapping(javaWsdlMapping);

            mapping.setExceptionType(javaTypeName);
            QName name = new QName(wsdl.getTargetNamespace(), fmd.getXmlName().getLocalPart());
            mapping.setWsdlMessage(name);

            // Variable mappings generated from SchemaTypesCreater have their order preserved
            for (VariableMapping variableMapping : typeMapping.getVariableMappings())
               mapping.addConstructorParameter(variableMapping.getXmlElementName());

            javaWsdlMapping.addExceptionMappings(mapping);
         }
      }

      javaWsdlMapping.addServiceEndpointInterfaceMappings(seiMapping);

      // Add package mapping for SEI
      String name = endpoint.getServiceEndpointInterface().getPackage().getName();
      String namespace = packageNamespaceMap.get(name);
      if (namespace == null)
         namespace = WSDLUtils.getInstance().getTypeNamespace(name);
      addPackageMapping(name, namespace);
   }

   private void buildParamMapping(ServiceEndpointMethodMapping methodMapping, String interfaceName, OperationMetaData operation,
         String name, String type, String mode, boolean header, int position)
   {
      MethodParamPartsMapping paramMapping = new MethodParamPartsMapping(methodMapping);
      paramMapping.setParamPosition(position);
      paramMapping.setParamType(type);

      WsdlMessageMapping messageMapping = new WsdlMessageMapping(paramMapping);
      messageMapping.setWsdlMessagePartName(name);
      String messageName = interfaceName + "_" + operation.getXmlName().getLocalPart();
      if ("OUT".equals(mode))
         messageName += "Response";
      QName messageQName = new QName(wsdl.getTargetNamespace(), messageName, "wsdlMsgNS");

      messageMapping.setWsdlMessage(messageQName);
      messageMapping.setParameterMode(mode);
      messageMapping.setSoapHeader(header);
      paramMapping.setWsdlMessageMapping(messageMapping);
      methodMapping.addMethodParamPartsMapping(paramMapping);
   }

   private void buildReturnParamMapping(ServiceEndpointMethodMapping methodMapping, String interfaceName, OperationMetaData operation, String name, String type)
   {
      WsdlReturnValueMapping returnMapping = new WsdlReturnValueMapping(methodMapping);
      returnMapping.setMethodReturnValue(type);
      returnMapping.setWsdlMessagePartName(name);
      String messageName = interfaceName + "_" + operation.getXmlName().getLocalPart() + "Response";;
      QName messageQName = new QName(wsdl.getTargetNamespace(), messageName, "wsdlMsgNS");
      returnMapping.setWsdlMessage(messageQName);
      methodMapping.setWsdlReturnValueMapping(returnMapping);
   }

   /**
    * During the WSDL generation process, a typeMapping will be
    * created that maps xml types -> java types
    *
    * @return  typeMapping
    */
   public TypeMapping getTypeMapping()
   {
      return this.javaToXSD.getSchemaCreator().getTypeMapping();
   }

   /**
    * A customized Package->Namespace map
    *
    * @param map
    */
   public void setPackageNamespaceMap(Map<String,String> map)
   {
      this.packageNamespaceMap = map;
      this.javaToXSD.setPackageNamespaceMap(map);
   }

   public void setEndpoint(Class cls)
   {
      this.seiClass = cls;
   }

   public void setFeatures(Map<String, Boolean> features)
   {
      this.features = features;
   }

   public void setJavaToXSD(JavaToXSDIntf jxsd)
   {
      this.javaToXSD = jxsd;
   }

   public JavaWsdlMapping getJavaWsdlMapping()
   {
      return javaWsdlMapping;
   }

   //************************************************************************
   //
   //**************************PRIVATE METHODS*****************************
   //
   //************************************************************************

   protected void generateType(QName xmlType, Class javaType, Map<String, QName> elementNames) throws IOException
   {
      if(Holder.class.isAssignableFrom(javaType))
         javaType = WSDLUtils.getInstance().getJavaTypeForHolder(javaType);
      JBossXSModel xsModel = javaToXSD.generateForSingleType(xmlType, javaType, elementNames);
      //  Now that the schema object graph is built,
      //  ask JavaToXSD to provide a list of xsmodels to be plugged
      //  into WSDLTypes
      if (xsModel == null)
         throw new WSException("XSModel is null");

      WSDLTypes wsdlTypes = wsdl.getWsdlTypes();
      wsdlTypes.addSchemaModel(xmlType.getNamespaceURI(), xsModel);
      wsdl.registerNamespaceURI(xmlType.getNamespaceURI(), null);

      //Also get any custom namespaces
      SchemaCreatorIntf schemaCreator = javaToXSD.getSchemaCreator();
      mergeJavaWsdlMapping(schemaCreator.getJavaWsdlMapping());

      HashMap map = schemaCreator.getCustomNamespaceMap();
      Set keys = map != null ? map.keySet() : null;
      Iterator iter = (keys != null && !keys.isEmpty()) ? keys.iterator() : null;
      while (iter != null && iter.hasNext())
      {
         String pref = (String)iter.next();
         String ns = (String)map.get(pref);
         if (ns != null)
            wsdl.registerNamespaceURI(ns, null);
      }
   }

   private void mergeJavaWsdlMapping(JavaWsdlMapping source)
   {
      // For now we just merge types and packages
      for (PackageMapping packageMapping : source.getPackageMappings())
      {
         String name = packageMapping.getPackageType();
         String namespaceURI = packageMapping.getNamespaceURI();

         addPackageMapping(name, namespaceURI);
      }

      for (JavaXmlTypeMapping type : source.getJavaXmlTypeMappings())
      {
         QName name = type.getRootTypeQName();
         if (name == null)
            name = type.getAnonymousTypeQName();

         if (mappedTypes.containsKey(name))
            continue;

         mappedTypes.put(name, type);

         JavaXmlTypeMapping typeCopy = new JavaXmlTypeMapping(javaWsdlMapping);
         typeCopy.setQNameScope(type.getQnameScope());
         typeCopy.setAnonymousTypeQName(type.getAnonymousTypeQName());
         typeCopy.setJavaType(type.getJavaType());
         typeCopy.setRootTypeQName(type.getRootTypeQName());

         for (VariableMapping variable : type.getVariableMappings())
         {
            VariableMapping variableCopy = new VariableMapping(typeCopy);
            variableCopy.setDataMember(variable.isDataMember());
            variableCopy.setJavaVariableName(variable.getJavaVariableName());
            variableCopy.setXmlAttributeName(variable.getXmlAttributeName());
            variableCopy.setXmlElementName(variable.getXmlElementName());
            variableCopy.setXmlWildcard(variable.getXmlWildcard());

            typeCopy.addVariableMapping(variableCopy);
         }

         javaWsdlMapping.addJavaXmlTypeMappings(typeCopy);
      }
   }

   private void addPackageMapping(String name, String namespaceURI)
   {
      if (mappedPackages.contains(name))
         return;

      mappedPackages.add(name);
      PackageMapping copy = new PackageMapping(javaWsdlMapping);
      copy.setPackageType(name);

      copy.setNamespaceURI(namespaceURI);
      javaWsdlMapping.addPackageMapping(copy);
   }

   protected void generateElement(QName xmlName, QName xmlType)
   {
      WSDLTypes types = wsdl.getWsdlTypes();
      String namespaceURI = xmlType.getNamespaceURI();
      JBossXSModel schemaModel = types.getSchemaModel();

      XSTypeDefinition type;
      if (Constants.NS_SCHEMA_XSD.equals(namespaceURI))
         type = SchemaUtils.getInstance().getSchemaBasicType(xmlType.getLocalPart());
      else
         type = schemaModel.getTypeDefinition(xmlType.getLocalPart(), namespaceURI);

      WSSchemaUtils utils = WSSchemaUtils.getInstance(schemaModel.getNamespaceRegistry(), null);
      JBossXSElementDeclaration element =
         utils.createGlobalXSElementDeclaration(xmlName.getLocalPart(), type, xmlName.getNamespaceURI());
      schemaModel.addXSElementDeclaration(element);

      wsdl.registerNamespaceURI(xmlName.getNamespaceURI(), null);
   }

   protected String getXMLNameForArray(QName el)
   {
      WSDLUtils utils = WSDLUtils.getInstance();
      String xmlName = "";
      String localpart = el.getLocalPart();

      while(localpart.lastIndexOf("Array") > -1)
      {
         xmlName += "arrayOf";
         localpart = utils.chop(localpart, "Array");
      }
      xmlName += localpart;

      return xmlName;
   }


   /**
    * Given a Java class, return the XML Type
    *
    * @param javaType
    * @return
    */

   protected QName getXMLSchemaType(Class javaType)
   {
      LiteralTypeMapping ty = javaToXSD.getSchemaCreator().getTypeMapping();
      QName qn = ty.getXMLType(javaType);
      if( qn != null && (qn.getPrefix() == null || qn.getPrefix().length() == 0) )
      {
         String prefix = wsdl.getPrefix(qn.getNamespaceURI());
         if( prefix != null)
            qn = new QName( qn.getNamespaceURI(),qn.getLocalPart(),prefix);
      }
      return qn;
   }

   /**
    * Given a XML Type, return the Java class
    *
    * @param xmlType
    * @return
    */
   protected Class getJavaType(QName xmlType)
   {
      LiteralTypeMapping ty = javaToXSD.getSchemaCreator().getTypeMapping();
      return ty.getJavaType(xmlType);
   }

   //PRIVATE METHODS
   private boolean needsToolsOverride(Class paramType)
   {
      if(ByteArrayHolder.class == paramType)
         return true;
      return false;
   }

   private QName getOverrideQName(Class paramType)
   {
      if(ByteArrayHolder.class == paramType)
         return Constants.TYPE_LITERAL_BASE64BINARY;
      throw new WSException("paramType not recognized");
   }

   private void generateWSDLFaults(WSDLInterfaceOperation op, QName xmlName)
   {
      /**
       * Idea behind faults is that there is a fault at the interface level to aid reuse.
       * Operations that define faults just hold a reference to the faults at the interface level.
       */
      WSDLInterface wsdlInterface  = op.getWsdlInterface();
      //    Check if the fault has already been added to the interface
      WSDLInterfaceFault fault = wsdlInterface.getFault(new NCName(xmlName.getLocalPart()));
      if(fault == null)
      {
         //Add the fault to the interface
         fault = new WSDLInterfaceFault(wsdlInterface);
         fault.setName(new NCName(xmlName.getLocalPart()));
         fault.setXmlName(xmlName);
         wsdlInterface.addFault(fault);
      }

      //Now add a operation fault
      WSDLInterfaceOperationOutfault outFault = new WSDLInterfaceOperationOutfault(op);
      outFault.setRef(xmlName);
      op.addOutfault(outFault);
   }

   private WSDLInterfaceOperationOutput getWSDLInterfaceOperationOutput(WSDLInterfaceOperation intf, ParameterMetaData pmd)
   {
      QName xmlType = pmd.getXmlType();
      QName xmlName = pmd.getXmlName();

      String prefix = wsdl.getPrefix(xmlType.getNamespaceURI());
      WSDLProperty wprop = new WSDLProperty(Constants.WSDL_PROPERTY_RPC_XMLTYPE,
            prefix + ":" + xmlType.getLocalPart());

      WSDLInterfaceOperationOutput wsdlInterfaceOperationOutput = new WSDLInterfaceOperationOutput(intf);
      wsdlInterfaceOperationOutput.addProperty(new WSDLProperty(Constants.WSDL_PROPERTY_PART_NAME, xmlName.getLocalPart()));
      wsdlInterfaceOperationOutput.setElement(xmlName);
      wsdlInterfaceOperationOutput.setMessageLabel(new NCName("OUT"));
      wsdlInterfaceOperationOutput.addProperty(wprop);

      if (pmd.isInHeader())
         wsdlInterfaceOperationOutput.addProperty(new WSDLProperty(Constants.WSDL_PROPERTY_APPLICATION_DATA, null));

      return wsdlInterfaceOperationOutput;
   }

   private WSDLInterfaceOperationInput getWSDLInterfaceOperationInput(WSDLInterfaceOperation intf, ParameterMetaData pmd)
   {
      QName xmlType = pmd.getXmlType();
      QName xmlName = pmd.getXmlName();

      WSDLInterfaceOperationInput wsdlInput = new WSDLInterfaceOperationInput(intf);
      String prefix = wsdl.getPrefix(xmlType.getNamespaceURI());
      //qn refers to the XMLType
      WSDLProperty wsdlProperty = new WSDLProperty(Constants.WSDL_PROPERTY_RPC_XMLTYPE,
                                    prefix + ":" + xmlType.getLocalPart());
      wsdlInput.addProperty(wsdlProperty);
      wsdlInput.setElement( xmlName );
      //Plus add a property
      wsdlInput.addProperty(new WSDLProperty(Constants.WSDL_PROPERTY_PART_NAME,xmlName.getLocalPart()));
      wsdlInput.setMessageLabel(new NCName("IN"));

      if (pmd.isInHeader())
         wsdlInput.addProperty(new WSDLProperty(Constants.WSDL_PROPERTY_APPLICATION_DATA, null));

      return wsdlInput;
   }

   private boolean checkAttachmentBasedOperation(OperationMetaData op)
   {
      boolean isAttach = false;

      List<ParameterMetaData> params = op.getParameters();
      for(ParameterMetaData param : params)
      {
         if(param.isSwA())
         {
            isAttach = true;
            break;
         }
      }

      return isAttach;
   }
}
