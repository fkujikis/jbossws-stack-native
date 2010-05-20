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

// $Id: JSR181MetaDataBuilder.java 387 2006-05-20 14:45:47Z thomas.diesler@jboss.com $

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.HandlerChain;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.InitParam;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPMessageHandler;
import javax.jws.soap.SOAPMessageHandlers;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.encoding.TypeMappingRegistry;
import javax.xml.rpc.holders.Holder;
import javax.xml.ws.addressing.AddressingProperties;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.addressing.AddressingPropertiesImpl;
import org.jboss.ws.addressing.metadata.AddressingOpMetaExt;
import org.jboss.ws.annotation.PortComponent;
import org.jboss.ws.jaxrpc.ParameterWrapping;
import org.jboss.ws.jaxrpc.Style;
import org.jboss.ws.jaxrpc.TypeMappingImpl;
import org.jboss.ws.jaxrpc.TypeMappingRegistryImpl;
import org.jboss.ws.jaxrpc.Use;
import org.jboss.ws.metadata.EndpointMetaData;
import org.jboss.ws.metadata.FaultMetaData;
import org.jboss.ws.metadata.OperationMetaData;
import org.jboss.ws.metadata.ParameterMetaData;
import org.jboss.ws.metadata.ServerEndpointMetaData;
import org.jboss.ws.metadata.ServiceMetaData;
import org.jboss.ws.metadata.TypeMappingMetaData;
import org.jboss.ws.metadata.TypesMetaData;
import org.jboss.ws.metadata.UnifiedMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedInitParamMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedWebMetaData;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.ws.metadata.jsr181.HandlerChainMetaData;
import org.jboss.ws.metadata.jsr181.HandlerConfigFactory;
import org.jboss.ws.metadata.jsr181.HandlerConfigMetaData;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLUtils;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;
import org.jboss.ws.server.ServerConfig;
import org.jboss.ws.server.ServerConfigFactory;
import org.jboss.ws.tools.JavaToWSDL;
import org.jboss.ws.tools.ToolsUtils;
import org.jboss.ws.utils.HolderUtils;
import org.jboss.ws.utils.IOUtils;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;

/** An abstract annotation meta data builder.
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @since 15-Oct-2005
 */
public abstract class JSR181MetaDataBuilder extends AbstractMetaDataBuilder
{
   // provide logging
   private final Logger log = Logger.getLogger(JSR181MetaDataBuilder.class);

   public JSR181MetaDataBuilder()
   {
   }

   protected ServerEndpointMetaData setupEndpointFromAnnotations(UnifiedMetaData wsMetaData, UnifiedDeploymentInfo udi, Class sepClass, String linkName)
         throws ClassNotFoundException
   {
      WebService anWebService = (WebService)sepClass.getAnnotation(WebService.class);
      if (anWebService == null)
         throw new WSException("Cannot obtain @WebService annotaion from: " + sepClass.getName());

      Class seiClass = null;
      String seiName = null;
      if (anWebService.endpointInterface().length() > 0)
      {
         seiName = anWebService.endpointInterface();
         seiClass = udi.annotationsCl.loadClass(seiName);
         anWebService = (WebService)seiClass.getAnnotation(WebService.class);
         if (anWebService == null)
            throw new WSException("Interface does not have a @WebService annotation: " + seiName);
      }

      Class wsClass = (seiClass != null ? seiClass : sepClass);

      WSDLUtils wsdlUtils = WSDLUtils.getInstance();

      String name = anWebService.name();
      if (name.length() == 0)
         name = wsdlUtils.getJustClassName(wsClass);

      String serviceName = anWebService.serviceName();
      if (serviceName.length() == 0)
         serviceName = name + "Service";

      String targetNS = anWebService.targetNamespace();
      if (targetNS.length() == 0)
         targetNS = wsdlUtils.getTypeNamespace(wsClass);

      /* JSR181-2.0
      String portName = anWebService.portName();
      if (portName.length() == 0)
         portName = name + "Port";
      */
      String portName = name + "Port";

      ServiceMetaData serviceMetaData = new ServiceMetaData(wsMetaData, new QName(targetNS, serviceName));
      wsMetaData.addService(serviceMetaData);

      // WSSE
      try
      {
         WSSecurityConfiguration securityConfiguration = getWsSecurityConfiguration(udi);
         serviceMetaData.setSecurityConfiguration(securityConfiguration);
      }
      catch (IOException e)
      {
         log.warn("Unable to process WSSecurityConfiguration: " + e.getMessage());
      }

      // Setup the ServerEndpointMetaData
      QName portQName = new QName(targetNS, portName);
      ServerEndpointMetaData sepMetaData = new ServerEndpointMetaData(serviceMetaData, portQName);
      sepMetaData.setLinkName(linkName);
      sepMetaData.setAnnotated(true);

      sepMetaData.setServiceEndpointImplName(sepClass.getName());
      sepMetaData.setServiceEndpointInterfaceName(wsClass.getName());

      serviceMetaData.addEndpoint(sepMetaData);

      // Process an optional @SOAPBinding annotation
      if (wsClass.isAnnotationPresent(SOAPBinding.class))
      {
         processSOAPBinding(wsClass, sepMetaData);
      }

      // Process an @WebMethod annotations
      int webMethodCount = 0;
      boolean includeAllMethods = (wsClass == seiClass);
      for (Method method : wsClass.getMethods())
      {
         if (includeAllMethods || method.isAnnotationPresent(WebMethod.class))
         {
            processWebMethod(sepMetaData, method);
            webMethodCount++;
         }
      }

      // @WebService should expose all inherited methods if @WebMethod is never specified
      // http://jira.jboss.org/jira/browse/JBWS-754
      if (seiClass != null && webMethodCount == 0)
      {
         Class superClass = seiClass.getSuperclass();
         while (superClass != null)
         {
            for (Method method : superClass.getMethods())
            {
               processWebMethod(sepMetaData, method);
               webMethodCount++;
            }

            superClass = superClass.getSuperclass();
         }
      }

      if (webMethodCount == 0)
         throw new WSException("At least one @WebMethod annotation is required");

      // Process an optional @HandlerChain annotation
      if (sepClass.isAnnotationPresent(HandlerChain.class))
         processHandlerChain(sepClass, sepMetaData);
      else if (wsClass.isAnnotationPresent(HandlerChain.class))
         processHandlerChain(wsClass, sepMetaData);

      // Process an optional @SOAPMessageHandlers annotation
      if (sepClass.isAnnotationPresent(SOAPMessageHandlers.class))
         processSOAPMessageHandlers(sepClass, sepMetaData);
      else if (wsClass.isAnnotationPresent(SOAPMessageHandlers.class))
         processSOAPMessageHandlers(wsClass, sepMetaData);

      // Process or generate WSDL
      processOrGenerateWSDL(wsClass, serviceMetaData, sepMetaData);

      // Read the generated WSDL and initialize the schema model
      WSDLDefinitions wsdlDefinitions = serviceMetaData.getWsdlDefinitions();
      JBossXSModel schemaModel = wsdlDefinitions.getWsdlTypes().getSchemaModel();
      serviceMetaData.getTypesMetaData().setSchemaModel(schemaModel);

      // Set the endpoint address
      processPortComponent(udi, wsClass, linkName, sepMetaData);

      // init service endpoint id
      ObjectName sepID = getServiceEndpointID(udi, sepMetaData);
      sepMetaData.setServiceEndpointID(sepID);

      return sepMetaData;
   }

   private void processSOAPBinding(Class wsClass, ServerEndpointMetaData epMetaData)
   {
      SOAPBinding anSoapBinding = (SOAPBinding)wsClass.getAnnotation(SOAPBinding.class);
      SOAPBinding.Style attrStyle = anSoapBinding.style();
      Style style = (attrStyle == SOAPBinding.Style.RPC ? Style.RPC : Style.DOCUMENT);
      epMetaData.setStyle(style);

      SOAPBinding.Use attrUse = anSoapBinding.use();
      if (attrUse == SOAPBinding.Use.ENCODED)
         throw new WSException("SOAP encoding is not supported for JSR-181 deployments. It is also disallowed by"
               + " the WS-I Basic Profile 1.1. Please switch to literal encoding");

      epMetaData.setEncodingStyle(Use.LITERAL);

      SOAPBinding.ParameterStyle attrParamStyle = anSoapBinding.parameterStyle();
      ParameterStyle paramStyle = (attrParamStyle == SOAPBinding.ParameterStyle.BARE ? ParameterStyle.BARE : ParameterStyle.WRAPPED);
      epMetaData.setParameterStyle(paramStyle);
   }

   private void processOrGenerateWSDL(Class wsClass, ServiceMetaData serviceMetaData, EndpointMetaData endpointMetaData)
   {
      WebService anWebService = (WebService)wsClass.getAnnotation(WebService.class);

      String wsdlLocation = anWebService.wsdlLocation();
      if (wsdlLocation.length() > 0)
      {
         serviceMetaData.setWsdlFile(wsdlLocation);
      }
      else
      {
         // Generate the wsdl
         ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
         try
         {
            UnifiedMetaData wsMetaData = serviceMetaData.getUnifiedMetaData();
            Thread.currentThread().setContextClassLoader(wsMetaData.getClassLoader());
            String serviceName = serviceMetaData.getName().getLocalPart();

            JavaToWSDL javaToWSDL = new JavaToWSDL(Constants.NS_WSDL11);
            javaToWSDL.setUnifiedMetaData(wsMetaData);
            javaToWSDL.setQualifiedElements(true);
            WSDLDefinitions wsdlDefinitions = javaToWSDL.generate(wsClass);

            // Add generated mapping
            JavaWsdlMapping mapping = javaToWSDL.getJavaWsdlMapping();
            String fakeMappingName = serviceName + "-annotation-generated";
            serviceMetaData.setJaxrpcMappingFile(fakeMappingName);
            serviceMetaData.getUnifiedMetaData().addMappingDefinition(fakeMappingName, mapping);

            ServerConfigFactory factory = ServerConfigFactory.getInstance();
            ServerConfig config = factory.getServerConfig();
            File tmpdir = new File(config.getServerTempDir().getCanonicalPath() + "/jbossws");
            tmpdir.mkdirs();

            File wsdlTmpFile = File.createTempFile(serviceName, ".wsdl", tmpdir);
            wsdlTmpFile.deleteOnExit();

            Writer writer = IOUtils.getCharsetFileWriter(wsdlTmpFile, Constants.DEFAULT_XML_CHARSET);
            wsdlDefinitions.write(writer, Constants.DEFAULT_XML_CHARSET);
            writer.close();

            serviceMetaData.setWsdlFile(wsdlTmpFile.toURL().toExternalForm());
         }
         catch (RuntimeException rte)
         {
            throw rte;
         }
         catch (IOException e)
         {
            throw new WSException("Cannot write generated wsdl", e);
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(ctxLoader);
         }
      }
   }

   private void processPortComponent(UnifiedDeploymentInfo udi, Class wsClass, String linkName, ServerEndpointMetaData epMetaData)
   {
      String contextRoot = null;

      // init contextRoot from jboss-web.xml
      if (udi.metaData instanceof UnifiedWebMetaData)
      {
         UnifiedWebMetaData webMetaData = (UnifiedWebMetaData)udi.metaData;
         contextRoot = webMetaData.getContextRoot();
      }

      PortComponent anPortComponent = (PortComponent)wsClass.getAnnotation(PortComponent.class);
      if (anPortComponent != null)
      {
         if (anPortComponent.contextRoot().length() > 0)
         {
            contextRoot = anPortComponent.contextRoot();
         }
         else
         {
            String shortName = udi.shortName;
            contextRoot = "/" + shortName.substring(0, shortName.indexOf('.'));
         }
         epMetaData.setContextRoot(contextRoot);

         String urlPattern;
         if (anPortComponent.urlPattern().length() > 0)
         {
            urlPattern = anPortComponent.urlPattern();
         }
         else
         {
            urlPattern = "/" + linkName;
         }
         epMetaData.setURLPattern(urlPattern);

         String servicePath = contextRoot + urlPattern;
         epMetaData.setEndpointAddress(getServiceEndpointAddress(null, servicePath));
         
         // setup authetication method
         String authMethod = anPortComponent.authMethod();
         if (authMethod.length() > 0)
            epMetaData.setAuthMethod(authMethod);
         
         // setup transport guarantee
         String transportGuarantee = anPortComponent.transportGuarantee();
         if (transportGuarantee.length() > 0)
            epMetaData.setTransportGuarantee(transportGuarantee);
      }
      else
      {
         if (contextRoot == null)
         {
            String shortName = udi.shortName;
            contextRoot = "/" + shortName.substring(0, shortName.indexOf('.'));
         }
         epMetaData.setContextRoot(contextRoot);

         String urlPattern = "/" + linkName;
         epMetaData.setURLPattern(urlPattern);

         String servicePath = contextRoot + urlPattern;
         epMetaData.setEndpointAddress(getServiceEndpointAddress(null, servicePath));
      }

      // replace the SOAP address
      replaceAddressLocation(epMetaData);
   }

   private WebParam getWebParamAnnotation(Method method, int pos)
   {
      for (Annotation annotation : method.getParameterAnnotations()[pos])
         if (annotation instanceof WebParam)
            return (WebParam)annotation;

      return null;
   }

   private void processWebMethod(ServerEndpointMetaData epMetaData, Method method)
   {
      ServiceMetaData serviceMetaData = epMetaData.getServiceMetaData();
      TypesMetaData typesMetaData = serviceMetaData.getTypesMetaData();
      String targetNS = epMetaData.getName().getNamespaceURI();

      // reflection defaults
      String soapAction = "";
      String operationName = method.getName();

      // annotation values that override defaults
      if (method.isAnnotationPresent(WebMethod.class))
      {
         WebMethod anWebMethod = method.getAnnotation(WebMethod.class);
         soapAction = anWebMethod.action();
         if (anWebMethod.operationName().length() > 0)
            operationName = anWebMethod.operationName();
      }

      String javaName = method.getName();
      OperationMetaData opMetaData = new OperationMetaData(epMetaData, new QName(targetNS, operationName), javaName);
      opMetaData.setOneWayOperation(method.isAnnotationPresent(Oneway.class));
      opMetaData.setSOAPAction(soapAction);
      epMetaData.addOperation(opMetaData);

      Map<String, Integer> typeIndexes = new HashMap<String, Integer>();

      List<QName> wrappedElementNames = null;
      List<String> wrappedVariables = null;
      List<String> wrappedTypes = null;
      ParameterMetaData wrappedParameter = null;

      // Get the type mapping for the encoding style
      String encStyle = opMetaData.getUse().toURI();
      TypeMappingRegistry tmRegistry = new TypeMappingRegistryImpl();
      TypeMappingImpl typeMapping = (TypeMappingImpl)tmRegistry.getTypeMapping(encStyle);

      // Build parameter meta data
      QName xmlName = null;
      QName xmlType = null;
      Class[] parameterTypes = method.getParameterTypes();

      if (opMetaData.isDocumentWrapped())
      {
         xmlName = opMetaData.getXmlName();
         xmlType = opMetaData.getXmlName();
         String epName = epMetaData.getName().getLocalPart();
         if (epName.endsWith("Port"))
            epName = epName.substring(0, epName.lastIndexOf("Port"));

         wrappedParameter = new ParameterMetaData(opMetaData, xmlName, xmlType, null);
         wrappedElementNames = new ArrayList<QName>(parameterTypes.length);
         wrappedVariables = new ArrayList<String>(parameterTypes.length);
         wrappedTypes = new ArrayList<String>(parameterTypes.length);
         wrappedParameter.setWrappedElementNames(wrappedElementNames);
         wrappedParameter.setWrappedVariables(wrappedVariables);
         wrappedParameter.setWrappedTypes(wrappedTypes);

         opMetaData.addParameter(wrappedParameter);

         if (!opMetaData.isOneWayOperation())
         {
            xmlName = new QName(targetNS, operationName + "Response");
            xmlType = new QName(targetNS, operationName + "Response");

            ParameterMetaData retMetaData = new ParameterMetaData(opMetaData, xmlName, xmlType, null);
            retMetaData.setWrappedVariables(new ArrayList<String>(1));
            retMetaData.setWrappedElementNames(new ArrayList<QName>(1));
            retMetaData.setWrappedTypes(new ArrayList<String>(1));
            opMetaData.setReturnParameter(retMetaData);
         }
      }

      for (int i = 0; i < parameterTypes.length; i++)
      {
         Class javaType = parameterTypes[i];
         String javaTypeName = javaType.getName();
         WebParam anWebParam = getWebParamAnnotation(method, i);
         boolean isWrapped = opMetaData.isDocumentWrapped() && (anWebParam == null || !anWebParam.header());

         if (Holder.class.isAssignableFrom(javaType))
         {
            javaType = HolderUtils.getValueType(javaType);
            javaTypeName = javaType.getName();
         }

         xmlType = typeMapping.getXMLType(javaType);
         if (xmlType == null)
            xmlType = getWebParamType(opMetaData, javaType);

         TypeMappingMetaData tmMetaData = new TypeMappingMetaData(typesMetaData, xmlType, javaTypeName);
         typesMetaData.addTypeMapping(tmMetaData);

         if (isWrapped)
         {
            QName wrappedElementName = getWebParamName(opMetaData, typeIndexes, javaType, anWebParam);
            wrappedElementNames.add(wrappedElementName);
            String variable = wrappedElementName.getLocalPart();
            if (variable.length() == 0)
               throw new WSException("A web parameter had a name with 0 length");

            variable = convertToProperty(variable);

            wrappedVariables.add(variable);
            wrappedTypes.add(javaTypeName);
         }
         else
         {
            xmlName = getWebParamName(opMetaData, typeIndexes, javaType, anWebParam);
            xmlType = typeMapping.getXMLType(javaType);
            if (xmlType == null)
               xmlType = getWebParamType(opMetaData, javaType);

            ParameterMetaData paramMetaData = new ParameterMetaData(opMetaData, xmlName, xmlType, javaTypeName);
            if (anWebParam != null)
            {
               if (anWebParam.mode() == WebParam.Mode.INOUT)
                  paramMetaData.setMode(ParameterMode.INOUT);
               if (anWebParam.mode() == WebParam.Mode.OUT)
                  paramMetaData.setMode(ParameterMode.OUT);

               if (anWebParam.header())
                  paramMetaData.setInHeader(true);
            }

            opMetaData.addParameter(paramMetaData);
         }
      }

      // Build result meta data
      Class returnType = method.getReturnType();
      String returnTypeName = returnType.getName();
      if ((returnType == void.class) == false)
      {
         if (opMetaData.isOneWayOperation())
            throw new IllegalArgumentException("[JSR-181 2.5.1] The method '" + method.getName() + "' can not have a return value if it is marked OneWay");

         xmlType = typeMapping.getXMLType(returnType);
         if (xmlType == null)
            xmlType = getWebResultType(targetNS, returnType);

         TypeMappingMetaData tmMetaData = new TypeMappingMetaData(typesMetaData, xmlType, returnTypeName);
         typesMetaData.addTypeMapping(tmMetaData);

         if (opMetaData.isDocumentWrapped())
         {
            QName elementName = getWebResultName(opMetaData, returnType, method.getAnnotation(WebResult.class));

            ParameterMetaData retMetaData = opMetaData.getReturnParameter();
            retMetaData.getWrappedElementNames().add(elementName);
            retMetaData.getWrappedVariables().add(convertToProperty(elementName.getLocalPart()));
            retMetaData.getWrappedTypes().add(returnTypeName);
         }
         else
         {
            xmlName = getWebResultName(opMetaData, returnType, method.getAnnotation(WebResult.class));
            ParameterMetaData retMetaData = new ParameterMetaData(opMetaData, xmlName, xmlType, returnTypeName);
            opMetaData.setReturnParameter(retMetaData);
         }
      }

      // Generate temporary wrapper types so that tools can generate the correct wsdl
      if (opMetaData.isDocumentWrapped())
      {
         ParameterWrapping.generateWrapper(wrappedParameter, false);
         if (!opMetaData.isOneWayOperation())
            ParameterWrapping.generateWrapper(opMetaData.getReturnParameter(), false);
      }

      // Add faults
      for (Class exClass : method.getExceptionTypes())
         if (!RemoteException.class.isAssignableFrom(exClass))
            addFault(opMetaData, typesMetaData, exClass);

      // process op meta data extension
      processOpMetaExtensions(epMetaData, opMetaData);

   }

   private String convertToProperty(String variable)
   {
      if (Character.isUpperCase(variable.charAt(0)))
      {
         char c = Character.toLowerCase(variable.charAt(0));
         StringBuilder builder = new StringBuilder(variable);
         builder.setCharAt(0, c);
         variable = builder.toString();
      }

      return variable;
   }

   // Process an optional @HandlerChain annotation
   private void processHandlerChain(Class wsClass, ServerEndpointMetaData epMetaData)
   {
      if (wsClass.isAnnotationPresent(SOAPMessageHandlers.class))
         throw new WSException("Cannot combine @HandlerChain with @SOAPMessageHandlers");

      HandlerChain anHandlerChain = (HandlerChain)wsClass.getAnnotation(HandlerChain.class);

      String handlerChainFile = anHandlerChain.file();
      try
      {
         HandlerConfigMetaData handlerConfigMetaData = null;
         
         URL fileURL = null;
         String filename = anHandlerChain.file();

         // Try the filename as URL
         try
         {
            fileURL = new URL(filename);
         }
         catch (MalformedURLException ex)
         {
            // ignore
         }

         // Try the filename as File
         if (fileURL == null)
         {
            try
            {
               File file = new File(filename);
               if (file.exists())
                  fileURL = file.toURL();
            }
            catch (MalformedURLException e)
            {
               // ignore
            }
         }

         // Try the filename as Resource
         if (fileURL == null)
         {
            fileURL = epMetaData.getResourceLoader().getResource(filename);
         }

         if (fileURL == null)
            throw new WSException("Cannot resolve URL to handler file: " + filename);
         
         InputStream is = fileURL.openStream();
         try
         {
            Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
            ObjectModelFactory factory = new HandlerConfigFactory(fileURL);
            handlerConfigMetaData = (HandlerConfigMetaData)unmarshaller.unmarshal(is, factory, null);
         }
         finally
         {
            is.close();
         }

         for (HandlerChainMetaData handlerChainMetaData : handlerConfigMetaData.getHandlerChains())
         {
            String hcName = handlerChainMetaData.getHandlerChainName();
            if (hcName.equals(anHandlerChain.name()) || anHandlerChain.name() == null)
            {
               for (UnifiedHandlerMetaData handlerMetaData : handlerChainMetaData.getHandlers())
               {
                  epMetaData.addHandler(handlerMetaData);
               }
            }
         }
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WSException("Cannot process handler chain: " + handlerChainFile, ex);
      }
   }

   // Process an optional @SOAPMessageHandlers annotation
   private void processSOAPMessageHandlers(Class wsClass, ServerEndpointMetaData epMetaData)
   {
      if (wsClass.isAnnotationPresent(HandlerChain.class))
         throw new WSException("Cannot combine @SOAPMessageHandlers with @HandlerChain");

      SOAPMessageHandlers anSOAPMessageHandlers = (SOAPMessageHandlers)wsClass.getAnnotation(SOAPMessageHandlers.class);

      for (SOAPMessageHandler handler : anSOAPMessageHandlers.value())
      {
         UnifiedHandlerMetaData handlerMetaData = new UnifiedHandlerMetaData();
         handlerMetaData.setHandlerName(handler.name());
         handlerMetaData.setHandlerClass(handler.className());         
         for (InitParam initParam : handler.initParams())
         {
            UnifiedInitParamMetaData initParamMetaData = new UnifiedInitParamMetaData();
            initParamMetaData.setParamName(initParam.name());
            initParamMetaData.setParamValue(initParam.value());
            handlerMetaData.addInitParam(initParamMetaData);
         }
         for (String role : handler.roles())
         {
            handlerMetaData.addSoapRole(role);
         }
         for (String header : handler.headers())
         {
            handlerMetaData.addSoapHeader(QName.valueOf(header));
         }
         epMetaData.addHandler(handlerMetaData);
      }
   }

   private QName getWebParamName(OperationMetaData opMetaData, Map<String, Integer> typeIndexes, Class javaType, WebParam webParam)
   {
      QName xmlName = null;
      String namespaceURI = opMetaData.getXmlName().getNamespaceURI();
      if (webParam != null)
      {
         if (webParam.targetNamespace().length() > 0)
            namespaceURI = webParam.targetNamespace();

         if (webParam.name().length() > 0)
         {
            if (opMetaData.getStyle() != Style.RPC || webParam.header())
               xmlName = new QName(namespaceURI, webParam.name());
            else xmlName = new QName(webParam.name());
         }
      }

      if (xmlName == null && opMetaData.isDocumentBare())
         xmlName = new QName(namespaceURI, opMetaData.getXmlName().getLocalPart());

      if (xmlName == null)
      {
         WSDLUtils wsdlUtils = WSDLUtils.getInstance();
         String shortName = wsdlUtils.getJustClassName(javaType);
         Integer index = (Integer)typeIndexes.get(shortName);
         index = (index != null ? new Integer(index.intValue() + 1) : new Integer(1));
         typeIndexes.put(shortName, index);

         if (opMetaData.isDocumentWrapped())
            xmlName = new QName(namespaceURI, shortName + "_" + index);
         else xmlName = new QName(shortName + "_" + index);
      }

      return xmlName;
   }

   private QName getWebParamType(OperationMetaData opMetaData, Class javaType)
   {
      String namespaceURI = opMetaData.getXmlName().getNamespaceURI();
      return ToolsUtils.getXMLType(javaType, namespaceURI);
   }

   private QName getWebResultName(OperationMetaData opMetaData, Class javaType, WebResult anWebResult)
   {
      QName xmlName = null;
      String namespaceURI = opMetaData.getXmlName().getNamespaceURI();

      if (anWebResult != null)
      {
         if (anWebResult.targetNamespace().length() > 0)
            namespaceURI = anWebResult.targetNamespace();

         // Unlike WebParam.name, the default of WebResult.name is "return", so this condition will always be met.
         if (anWebResult.name().length() > 0)
         {
            if (opMetaData.getStyle() != Style.RPC)
               xmlName = new QName(namespaceURI, anWebResult.name());
            else xmlName = new QName(anWebResult.name());
         }
      }
      if (xmlName == null && opMetaData.isDocumentBare())
         xmlName = new QName(namespaceURI, opMetaData.getResponseName().getLocalPart());

      if (xmlName == null)
      {
         xmlName = new QName(Constants.DEFAULT_RPC_RETURN_NAME);
      }
      return xmlName;
   }

   private void addFault(OperationMetaData omd, TypesMetaData tmd, Class<?> exception)
   {
      if (omd.isOneWayOperation())
         throw new IllegalStateException("JSR-181 4.3.1 - A JSR-181 processor is REQUIRED to report an error if an operation marked "
               + "@Oneway has a return value, declares any checked exceptions or has any INOUT or OUT parameters.");

      String name = WSDLUtils.getInstance().getJustClassName(exception);
      QName xmlName = new QName(omd.getXmlName().getNamespaceURI(), name);

      FaultMetaData fmd = new FaultMetaData(omd, xmlName, xmlName, exception.getName());
      omd.addFault(fmd);

      TypeMappingMetaData mapping = new TypeMappingMetaData(tmd, xmlName, exception.getName());
      tmd.addTypeMapping(mapping);
   }

   private QName getWebResultType(String defaultNS, Class javaType)
   {
      return ToolsUtils.getXMLType(javaType, defaultNS);
   }

   /**
    * Process operation meta data extensions.
    *
    * @param epMetaData
    * @param opMetaData
    */
   private void processOpMetaExtensions(ServerEndpointMetaData epMetaData, OperationMetaData opMetaData)
   {
      // Until there is a addressing annotion we fallback to implicit action asosciation
      // TODO: figure out a way to assign message name instead of IN and OUT
      String tns = epMetaData.getName().getNamespaceURI();
      String portTypeName = epMetaData.getName().getLocalPart();

      AddressingProperties ADDR = new AddressingPropertiesImpl();
      AddressingOpMetaExt addrExt = new AddressingOpMetaExt(ADDR.getNamespaceURI());
      addrExt.setInboundAction(tns + "/" + portTypeName + "/IN");

      if (!opMetaData.isOneWayOperation())
         addrExt.setOutboundAction(tns + "/" + portTypeName + "/OUT");

      opMetaData.addExtension(addrExt);
   }
}
