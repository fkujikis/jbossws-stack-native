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
package org.jboss.ws.jaxrpc;

// $Id$

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;
import javax.xml.rpc.encoding.SerializerFactory;
import javax.xml.rpc.handler.HandlerChain;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.addressing.AddressingConstantsImpl;
import org.jboss.ws.binding.BindingProvider;
import org.jboss.ws.binding.BindingProviderRegistry;
import org.jboss.ws.binding.EndpointInvocation;
import org.jboss.ws.binding.UnboundHeader;
import org.jboss.ws.handler.HandlerChainBaseImpl;
import org.jboss.ws.jaxrpc.encoding.JAXBDeserializerFactory;
import org.jboss.ws.jaxrpc.encoding.JAXBSerializerFactory;
import org.jboss.ws.metadata.ClientEndpointMetaData;
import org.jboss.ws.metadata.EndpointMetaData;
import org.jboss.ws.metadata.OperationMetaData;
import org.jboss.ws.metadata.ParameterMetaData;
import org.jboss.ws.metadata.ServiceMetaData;
import org.jboss.ws.metadata.TypesMetaData;
import org.jboss.ws.metadata.UnifiedMetaData;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.jboss.ws.soap.EndpointInfo;
import org.jboss.ws.soap.MessageContextAssociation;
import org.jboss.ws.soap.SOAPConnectionImpl;
import org.jboss.ws.soap.SOAPMessageContextImpl;
import org.jboss.ws.utils.HolderUtils;
import org.jboss.ws.utils.JavaUtils;
import org.jboss.ws.utils.ThreadLocalAssociation;

/** Provides support for the dynamic invocation of a service endpoint.
 * The javax.xml.rpc.Service interface acts as a factory for the creation of Call instances.
 *
 * Once a Call instance is created, various setter and getter methods may be used to configure this Call instance.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-Oct-2004
 */
public class CallImpl implements Call
{
   // provide logging
   private static Logger log = Logger.getLogger(CallImpl.class);

   // The service that created this call
   private ServiceImpl jaxrpcService;
   // The endpoint together with the operationName uniquely identify the call operation
   private EndpointMetaData epMetaData;
   // The current operation name
   private QName operationName;
   // The port type name
   private QName portType;
   // A Map<String,Object> of Call properties
   private Map<String, Object> properties = new HashMap<String, Object>();
   // Output parameters
   private EndpointInvocation epInv;
   // A Map<QName,UnboundHeader> of header entries
   private Map<QName, UnboundHeader> unboundHeaders = new LinkedHashMap<QName, UnboundHeader>();

   // The set of supported properties
   private static final Set<String> standardProperties = new HashSet<String>();
   static
   {
      standardProperties.add(Call.ENCODINGSTYLE_URI_PROPERTY);
      standardProperties.add(Call.OPERATION_STYLE_PROPERTY);
      standardProperties.add(Call.SESSION_MAINTAIN_PROPERTY);
      standardProperties.add(Call.SOAPACTION_URI_PROPERTY);
      standardProperties.add(Call.SOAPACTION_USE_PROPERTY);
      standardProperties.add(Call.USERNAME_PROPERTY);
      standardProperties.add(Call.PASSWORD_PROPERTY);

      standardProperties.add(Stub.ENDPOINT_ADDRESS_PROPERTY);
      standardProperties.add(Stub.SESSION_MAINTAIN_PROPERTY);
      standardProperties.add(Stub.USERNAME_PROPERTY);
      standardProperties.add(Stub.PASSWORD_PROPERTY);
   }

   /** Create a call that needs to be configured manually
    */
   CallImpl(ServiceImpl service)
   {
      this.jaxrpcService = service;

      // If the WSDLService has only one endpoint, use it
      ServiceMetaData serviceMetaData = service.getServiceMetaData();
      if (serviceMetaData != null && serviceMetaData.getEndpoints().size() == 1)
      {
         this.epMetaData = serviceMetaData.getEndpoints().get(0);
         setTargetEndpointAddress(epMetaData.getEndpointAddress());
      }
   }

   /** Create a call for a known WSDL endpoint.
    *
    * @param epMetaData A WSDLEndpoint
    */
   CallImpl(ServiceImpl service, EndpointMetaData epMetaData)
   {
      this.jaxrpcService = service;
      this.epMetaData = epMetaData;
      setTargetEndpointAddress(epMetaData.getEndpointAddress());
   }

   /** Create a call for a known WSDL endpoint.
    *
    * @param portName Qualified name for the target service endpoint
    * @throws ServiceException
    */
   CallImpl(ServiceImpl service, QName portName, QName opName) throws ServiceException
   {
      this.jaxrpcService = service;

      ServiceMetaData serviceMetaData = service.getServiceMetaData();
      if (serviceMetaData != null)
      {
         EndpointMetaData epMetaData = null;
         if (serviceMetaData.getEndpoints().size() > 0)
         {
            epMetaData = serviceMetaData.getEndpoint(portName);
            if (epMetaData == null)
               throw new ServiceException("Cannot find endpoint for name: " + portName);
         }

         if (epMetaData != null)
         {
            this.epMetaData = epMetaData;
            setTargetEndpointAddress(epMetaData.getEndpointAddress());
         }
      }

      if (opName != null)
      {
         setOperationName(opName);
      }
   }

   /**
    * Add a header that is not bound to an input parameter.
    * A propriatory extension, that is not part of JAXRPC.
    *
    * @param xmlName The XML name of the header element
    * @param xmlType The XML type of the header element
    */
   public void addUnboundHeader(QName xmlName, QName xmlType, Class javaType, ParameterMode mode)
   {
      UnboundHeader unboundHeader = new UnboundHeader(xmlName, xmlType, javaType, mode);
      unboundHeaders.put(xmlName, unboundHeader);
   }

   /**
    * Get the header value for the given XML name.
    * A propriatory extension, that is not part of JAXRPC.
    *
    * @param xmlName The XML name of the header element
    * @return The header value, or null
    */
   public Object getUnboundHeaderValue(QName xmlName)
   {
      UnboundHeader unboundHeader = unboundHeaders.get(xmlName);
      return (unboundHeader != null ? unboundHeader.getHeaderValue() : null);
   }

   /**
    * Set the header value for the given XML name.
    * A propriatory extension, that is not part of JAXRPC.
    *
    * @param xmlName The XML name of the header element
    */
   public void setUnboundHeaderValue(QName xmlName, Object value)
   {
      UnboundHeader unboundHeader = unboundHeaders.get(xmlName);
      if (unboundHeader == null)
         throw new IllegalArgumentException("Cannot find unbound header: " + xmlName);

      unboundHeader.setHeaderValue(value);
   }

   /**
    * Clear all registered headers.
    * A propriatory extension, that is not part of JAXRPC.
    */
   public void clearUnboundHeaders()
   {
      unboundHeaders.clear();
   }

   /**
    * Remove the header for the given XML name.
    * A propriatory extension, that is not part of JAXRPC.
    */
   public void removeUnboundHeader(QName xmlName)
   {
      unboundHeaders.remove(xmlName);
   }

   /**
    * Get an Iterator over the registered header XML names.
    * A propriatory extension, that is not part of JAXRPC.
    */
   public Iterator getUnboundHeaders()
   {
      return unboundHeaders.keySet().iterator();
   }

   /** Gets the address of a target service endpoint.
    */
   public String getTargetEndpointAddress()
   {
      return (String)properties.get(Stub.ENDPOINT_ADDRESS_PROPERTY);
   }

   /** Sets the address of the target service endpoint. This address must correspond to the transport
    * specified in the binding for this Call instance.
    *
    * @param address Address of the target service endpoint; specified as an URI
    */
   public void setTargetEndpointAddress(String address)
   {
      this.properties.put(Stub.ENDPOINT_ADDRESS_PROPERTY, address);
   }

   /** Gets the name of the operation to be invoked using this Call instance.
    */
   public QName getOperationName()
   {
      return this.operationName;
   }

   /** Sets the name of the operation to be invoked using this Call instance.
    */
   public void setOperationName(QName operationName)
   {
      this.operationName = operationName;
   }

   /** Adds a parameter type and mode for a specific operation.
    */
   public void addParameter(String paramName, QName xmlType, ParameterMode parameterMode)
   {
      TypeMappingImpl typeMapping = getEndpointMetaData().getServiceMetaData().getTypeMapping();
      Class javaType = typeMapping.getJavaType(xmlType);

      // CTS com/sun/ts/tests/jaxrpc/api/javax_xml_rpc/Call/AddGetRemoveAllParametersTest1
      // tests addParameter/getParameter without giving the javaType for a custom parameter
      // IMHO, this flavour of addParameter should only be used for standard types, where
      // the javaType can be derived from the xmlType
      if (javaType == null)
      {
         log.warn("Register unqualified call parameter for: " + xmlType);
         javaType = new UnqualifiedCallParameter(xmlType).getClass();
         typeMapping.register(javaType, xmlType, null, null);
      }

      addParameter(paramName, xmlType, javaType, parameterMode);
   }

   /** Adds a parameter type and mode for a specific operation.
    */
   public void addParameter(String paramName, QName xmlType, Class javaType, ParameterMode mode)
   {
      QName xmlName = new QName(paramName);
      addParameter(xmlName, xmlType, javaType, mode, false);
   }

   /** Add a parameter to the current operation description.
    * This is a propriatary extension that gives full control over the parameter configuration.
    */
   public void addParameter(QName xmlName, QName xmlType, Class javaType, ParameterMode mode, boolean inHeader)
   {
      if (xmlType == null || javaType == null)
         throw new IllegalArgumentException("Invalid null parameter");

      OperationMetaData opMetaData = getOperationMetaData();
      ParameterMetaData paramMetaData = new ParameterMetaData(opMetaData, xmlName, xmlType, javaType.getName());
      opMetaData.addParameter(paramMetaData);
      paramMetaData.setMode(mode);
      paramMetaData.setInHeader(inHeader);

      registerParameterType(xmlType, javaType);
   }

   /** Removes all specified parameters from this Call instance. Note that this method removes only the parameters and
    * not the return type. The setReturnType(null) is used to remove the return type.
    *
    * @throws javax.xml.rpc.JAXRPCException This exception may be thrown If this method is called when the method isParameterAndReturnSpecRequired returns false for this Call's operation.
    */
   public void removeAllParameters()
   {
      OperationMetaData opMetaData = getOperationMetaData();
      opMetaData.removeAllParameters();
   }

   /** Sets the return type for a specific operation. Invoking setReturnType(null) removes the return type for this Call object.
    */
   public void setReturnType(QName xmlType)
   {
      Class javaType = getEndpointMetaData().getServiceMetaData().getTypeMapping().getJavaType(xmlType);
      setReturnType(xmlType, javaType);
   }

   /** Sets the return type for a specific operation.
    */
   public void setReturnType(QName xmlType, Class javaType)
   {
      if (xmlType == null || javaType == null)
         throw new IllegalArgumentException("Invalid null parameter");

      OperationMetaData opMetaData = getOperationMetaData();
      QName xmlName = new QName(Constants.DEFAULT_RPC_RETURN_NAME);
      String javaTypeName = javaType.getName();
      ParameterMetaData retMetaData = new ParameterMetaData(opMetaData, xmlName, xmlType, javaTypeName);
      opMetaData.setReturnParameter(retMetaData);

      registerParameterType(xmlType, javaType);
   }

   private void registerParameterType(QName xmlType, Class javaType)
   {
      ServiceMetaData serviceMetaData = getEndpointMetaData().getServiceMetaData();

      String nsURI = xmlType.getNamespaceURI();
      if (Constants.NS_ATTACHMENT_MIME_TYPE.equals(nsURI) == false)
      {
         TypeMappingImpl typeMapping = serviceMetaData.getTypeMapping();
         Class regJavaType = typeMapping.getJavaType(xmlType);
         if (regJavaType == null)
         {
            typeMapping.register(javaType, xmlType, new JAXBSerializerFactory(), new JAXBDeserializerFactory());
         }
         else if (regJavaType != null && JavaUtils.isAssignableFrom(regJavaType, javaType) == false)
         {
            throw new IllegalArgumentException("Different java type already registered: " + regJavaType.getName());
         }
      }
   }

   /** Invokes a remote method using the one-way interaction mode.
    */
   public void invokeOneWay(Object[] inputParams)
   {
      try
      {
         invokeInternal(operationName, inputParams, true);
      }
      catch (RemoteException rex)
      {
         throw new JAXRPCException("Cannot invokeOneWay", rex.getCause());
      }
   }

   /** Invokes a specific operation using a synchronous request-response interaction mode.
    */
   public Object invoke(Object[] inputParams) throws RemoteException
   {
      return invokeInternal(operationName, inputParams, false);
   }

   /** Invokes a specific operation using a synchronous request-response interaction mode.
    */
   public Object invoke(QName operationName, Object[] inputParams) throws RemoteException
   {
      return invokeInternal(operationName, inputParams, false);
   }

   /** Returns a List values for the output parameters of the last invoked operation.
    *
    * @return java.util.List Values for the output parameters. An empty List is returned if there are no output values.
    * @throws JAXRPCException If this method is invoked for a one-way operation or is invoked before any invoke method has been called.
    */
   public List getOutputValues()
   {
      if (epInv == null)
         throw new JAXRPCException("Output params not available");

      try
      {
         OperationMetaData opMetaData = getOperationMetaData();

         List<Object> objPayload = new ArrayList<Object>();
         for (QName xmlName : epInv.getResponseParamNames())
         {
            Object paramValue = epInv.getResponseParamValue(xmlName);
            if (opMetaData.isDocumentWrapped())
            {
               objPayload = Arrays.asList((Object[])paramValue);
               break;
            }
            else
            {
               objPayload.add(paramValue);
            }
         }
         return objPayload;
      }
      catch (SOAPException ex)
      {
         throw new JAXRPCException("Cannot obtain response payload", ex);
      }
   }

   /** Returns a Map of {name, value} for the output parameters of the last invoked operation.
    *  The parameter names in the returned Map are of type java.lang.String.
    *
    * @return Map Output parameters for the last Call.invoke(). Empty Map is returned if there are no output parameters.
    * @throws JAXRPCException If this method is invoked for a one-way operation or is invoked before any invoke method has been called.
    */
   public Map getOutputParams()
   {
      if (epInv == null)
         throw new JAXRPCException("Output params not available");

      try
      {
         Map<String, Object> outMap = new LinkedHashMap<String, Object>();
         for (QName xmlName : epInv.getResponseParamNames())
         {
            Object value = epInv.getResponseParamValue(xmlName);
            outMap.put(xmlName.getLocalPart(), value);
         }
         return outMap;
      }
      catch (SOAPException ex)
      {
         throw new JAXRPCException("Cannot obtain response payload", ex);
      }
   }

   /**
    * Gets the qualified name of the port type.
    *
    * @return Qualified name of the port type
    */
   public QName getPortTypeName()
   {
      if (portType != null)
      {
         return portType;
      }

      /* This code could be used to derive the portType from the endpoint meta data.
       * However, it breaks CTS com/sun/ts/tests/jaxrpc/api/javax_xml_rpc/Call/Client.java#SetGetPortTypeNameTest2
       if (epMetaData != null)
       {
       ServiceMetaData serviceMetaData = epMetaData.getServiceMetaData();
       WSDLDefinitions wsdlDefinitions = serviceMetaData.getWsdlDefinitions();
       WSDLService wsdlService = wsdlDefinitions.getService(new NCName(serviceMetaData.getName().getLocalPart()));
       WSDLEndpoint wsdlEndpoint = wsdlService.getEndpoint(epMetaData.getName());
       WSDLInterface wsdlInterface = wsdlEndpoint.getInterface();
       return wsdlInterface.getQName();
       }
       */

      // CTS com/sun/ts/tests/jaxrpc/api/javax_xml_rpc/Call/Client.java#SetGetPortTypeNameTest2
      return new QName("");
   }

   /**
    * Gets the return type for a specific operation
    *
    * @return Returns the XML type for the return value
    */
   public QName getReturnType()
   {
      QName retType = null;
      if (operationName != null)
      {
         OperationMetaData opDesc = getOperationMetaData();
         ParameterMetaData retMetaData = opDesc.getReturnParameter();
         if (retMetaData != null)
            retType = retMetaData.getXmlType();
      }
      return retType;
   }

   /**
    * Sets the qualified name of the interface.
    *
    * @param portType - Qualified name of the port type
    */
   public void setPortTypeName(QName portType)
   {
      this.portType = portType;
   }

   /**
    * Indicates whether addParameter and setReturnType methods are to be invoked to specify the parameter and return
    * type specification for a specific operation.
    *
    * @param opName Qualified name of the operation
    * @return Returns true if the Call implementation class requires addParameter and setReturnType to be invoked in the client code for the specified operation. This method returns false otherwise.
    * @throws IllegalArgumentException If invalid operation name is specified
    */
   public boolean isParameterAndReturnSpecRequired(QName opName)
   {
      setOperationName(opName);
      OperationMetaData opMetaData = getOperationMetaData();
      return opMetaData.getParameters().size() == 0 && opMetaData.getReturnParameter() == null;
   }

   /** Gets the names of configurable properties supported by this Call object.
    * @return Iterator for the property names
    */
   public Iterator getPropertyNames()
   {
      return standardProperties.iterator();
   }

   /** Gets the value of a named property.
    */
   public Object getProperty(String name)
   {
      if(null == name)
         throw new JAXRPCException("Unsupported property: " + name);

      // CTS: com/sun/ts/tests/jaxrpc/api/javax_xml_rpc/Call/Client.java#SetGetPropertyTest2
      if (name.startsWith("javax.xml.rpc") && standardProperties.contains(name) == false)
         throw new JAXRPCException("Unsupported property: " + name);

      return properties.get(name);
   }

   /** Sets the value for a named property.
    */
   public void setProperty(String name, Object value)
   {
      if(null == name)
         throw new JAXRPCException("Unsupported property: " + name);

      // CTS: com/sun/ts/tests/jaxrpc/api/javax_xml_rpc/Call/Client.java#SetGetPropertyTest2
      if (name.startsWith("javax.xml.rpc") && standardProperties.contains(name) == false)
         throw new JAXRPCException("Unsupported property: " + name);

      properties.put(name, value);
   }

   /** Removes a named property.
    */
   public void removeProperty(String name)
   {
      properties.remove(name);
   }

   /** Gets the XML type of a parameter by name.
    */
   public QName getParameterTypeByName(String paramName)
   {
      OperationMetaData opMetaData = getOperationMetaData();
      ParameterMetaData paramMetaData = opMetaData.getParameter(new QName(paramName));
      if (paramMetaData != null)
         return paramMetaData.getXmlType();
      else return null;
   }

   /** Call invokation goes as follows:
    *
    * 1) synchronize the operation name with the operation meta data
    * 2) synchronize the input parameters with the operation meta data
    * 3) generate the payload using a BindingProvider
    * 4) get the Invoker from Remoting, based on the target endpoint address
    * 5) do the invocation through the Remoting framework
    * 6) unwrap the result using the BindingProvider
    * 7) return the result
    */
   private Object invokeInternal(QName opName, Object[] inputParams, boolean oneway) throws RemoteException
   {
      if (opName.equals(operationName) == false)
         setOperationName(opName);

      OperationMetaData opMetaData = getOperationMetaData();

      // Check or generate the the schema if this call is unconfigured
      generateOrUpdateSchemas(opMetaData);

      // Associate a message context with the current thread
      SOAPMessageContextImpl msgContext = new SOAPMessageContextImpl();
      MessageContextAssociation.pushMessageContext(msgContext);
      msgContext.setOperationMetaData(opMetaData);

      // copy properties to the message context
      for (String key : properties.keySet())
      {
         Object value = properties.get(key);
         msgContext.setProperty(key, value);
      }

      try
      {
         // Get the binding provider for the given bindingURI
         BindingProvider bindingProvider = BindingProviderRegistry.getDefaultProvider();

         // Create the invocation and sync the input parameters
         epInv = new EndpointInvocation(opMetaData);
         epInv.initInputParams(inputParams);

         // Bind the request message
         SOAPMessage reqMessage = bindingProvider.bindRequestMessage(opMetaData, epInv, unboundHeaders);

         // Call the request handlers
         QName portName = epMetaData.getName();

         if (callRequestHandlerChain(portName, msgContext))
         {
            // Use Stub.ENDPOINT_ADDRESS_PROPERTY
            String targetAddress = getTargetEndpointAddress();

            // Fall back to wsa:To
            AddressingProperties addrProps = (AddressingProperties)msgContext.getProperty(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
            if (targetAddress == null && addrProps != null && addrProps.getTo() != null)
            {
               AddressingConstantsImpl ADDR = new AddressingConstantsImpl();
               String wsaTo = addrProps.getTo().getURI().toString();
               if (wsaTo.equals(ADDR.getAnonymousURI()) == false)
               {
                  try
                  {
                     URL wsaToURL = new URL(wsaTo);
                     log.debug("Sending request to addressing destination: " + wsaToURL);
                     targetAddress = wsaToURL.toExternalForm();
                  }
                  catch (MalformedURLException ex)
                  {
                     log.debug("Not a valid URL: " + wsaTo);
                  }
               }
            }

            // The endpoint address must be known beyond this point
            if (targetAddress == null)
               throw new WSException("Target endpoint address not set");

            // Setup remoting call properties
            Map<String, Object> callProps = new HashMap<String, Object>();
            callProps.putAll(properties);

            syncMessageProperties(callProps, msgContext);

            EndpointInfo epInfo = new EndpointInfo(epMetaData, targetAddress, callProps);
            SOAPMessage resMessage = new SOAPConnectionImpl().call(reqMessage, epInfo, oneway);

            // Associate current message with message context
            msgContext.setMessage(resMessage);
         }

         // Get the return object
         Object retObj = null;
         if (oneway == false)
         {
            // Call the response handlers
            callResponseHandlerChain(portName, msgContext);

            // unbind the return values
            SOAPMessage resMessage = msgContext.getMessage();
            bindingProvider.unbindResponseMessage(opMetaData, resMessage, epInv, unboundHeaders);

            retObj = syncOutputParams(inputParams, epInv);
         }

         return retObj;
      }
      catch (SOAPFaultException ex)
      {
         log.error("Call invocation failed with SOAPFaultException", ex);
         String faultCode = ex.getFaultCode().getLocalPart();
         throw new RemoteException("Call invocation failed with code [" + faultCode + "] because of: " + ex.getFaultString(), ex);
      }
      catch (Exception ex)
      {
         log.error("Call invocation failed with unkown Exception", ex);
         throw new RemoteException("Call invocation failed: " + ex.getMessage(), ex);
      }
      finally
      {
         // Snyc context properties with the stub that clients can access them
         syncMessageProperties(properties, msgContext);

         // Reset the message context association
         MessageContextAssociation.popMessageContext();

         // TODO: usage with of POST handlers needs to be clarified, Heiko
         // ThreadLocalAssociation.clear();
      }
   }

   private void syncMessageProperties(Map<String, Object> props, MessageContext msgContext)
   {
      Iterator it = msgContext.getPropertyNames();
      while (it.hasNext())
      {
         String propName = (String)it.next();
         Object property = msgContext.getProperty(propName);
         props.put(propName, property);
      }
   }

   private boolean callRequestHandlerChain(QName portName, SOAPMessageContextImpl msgContext)
   {
      HandlerChain handlerChain = jaxrpcService.getHandlerChain(portName);
      return (handlerChain != null ? handlerChain.handleRequest(msgContext) : true);
   }

   private boolean callResponseHandlerChain(QName portName, SOAPMessageContextImpl msgContext)
   {
      boolean status = true;
      String[] roles = null;

      HandlerChain handlerChain = jaxrpcService.getHandlerChain(portName);
      if (handlerChain != null)
      {
         roles = handlerChain.getRoles();
         status = handlerChain.handleResponse(msgContext);
      }

      // BP-1.0 R1027
      HandlerChainBaseImpl.checkMustUnderstand(msgContext, roles);

      return status;
   }

   /** Generate or update the XSD schema for all parameters and the return.
    *  This should only be done when the Call is unconfigured, hence there is no WSDL
    */
   private void generateOrUpdateSchemas(OperationMetaData opMetaData)
   {
      ServiceMetaData serviceMetaData = opMetaData.getEndpointMetaData().getServiceMetaData();
      if (serviceMetaData.getWsdlFile() == null)
      {
         TypesMetaData typesMetaData = serviceMetaData.getTypesMetaData();
         for (ParameterMetaData paramMetaData : opMetaData.getParameters())
         {
            generateOrUpdateParameterSchema(typesMetaData, paramMetaData);
         }

         ParameterMetaData retMetaData = opMetaData.getReturnParameter();
         if (retMetaData != null)
         {
            generateOrUpdateParameterSchema(typesMetaData, retMetaData);
         }
      }
   }

   /** Generate or update the XSD schema for a given parameter
    *  This should only be done if the parameter is not an attachment
    */
   private void generateOrUpdateParameterSchema(TypesMetaData typesMetaData, ParameterMetaData paramMetaData)
   {
      if (paramMetaData.isSwA() == false)
      {
         QName xmlType = paramMetaData.getXmlType();
         Class javaType = paramMetaData.getJavaType();

         ServiceMetaData serviceMetaData = getEndpointMetaData().getServiceMetaData();
         TypeMappingImpl typeMapping = serviceMetaData.getTypeMapping();
         SerializerFactory serFactory = typeMapping.getSerializer(javaType, xmlType);
         if (serFactory instanceof JAXBSerializerFactory)
         {
            SchemaGenerator xsdGenerator =  new SchemaGenerator();
            JBossXSModel model = xsdGenerator.generateXSDSchema(xmlType, javaType);
            typesMetaData.addSchemaModel(model);
         }
      }
   }

   /** Get the OperationMetaData for the given operation name
    * If it does not exist, it will be created
    */
   public OperationMetaData getOperationMetaData()
   {
      if (operationName == null)
         throw new WSException("Operation name not set");

      return getOperationMetaData(operationName);
   }

   /** Get the OperationMetaData for the given operation name
    * If it does not exist, it will be created
    */
   public OperationMetaData getOperationMetaData(QName opName)
   {
      if (opName == null)
         throw new IllegalArgumentException("Cannot get OperationMetaData for null");

      EndpointMetaData epMetaData = getEndpointMetaData();
      OperationMetaData opMetaData = epMetaData.getOperation(opName);
      if (opMetaData == null && jaxrpcService.getWSDLDocumentLocation() == null)
      {
         opMetaData = new OperationMetaData(epMetaData, opName, opName.getLocalPart());
         epMetaData.addOperation(opMetaData);
      }

      if (opMetaData == null)
         throw new JAXRPCException("Cannot obtain operation meta data for: " + opName);

      return opMetaData;
   }

   // Get the EndpointMetaData for all OperationMetaData
   public EndpointMetaData getEndpointMetaData()
   {
      if (epMetaData == null)
      {
         UnifiedMetaData wsMetaData = new UnifiedMetaData();
         ServiceMetaData serviceMetaData = new ServiceMetaData(wsMetaData, new QName(Constants.NS_JBOSSWS_URI, "AnonymousService"));
         wsMetaData.addService(serviceMetaData);

         epMetaData = new ClientEndpointMetaData(serviceMetaData, new QName(Constants.NS_JBOSSWS_URI, "AnonymousEndpoint"));
         epMetaData.setStyle(Style.RPC);

         serviceMetaData.addEndpoint(epMetaData);
      }
      return epMetaData;
   }

   /** Synchronize the operation paramters with the call output parameters.
    */
   private Object syncOutputParams(Object[] inParams, EndpointInvocation epInv) throws SOAPException
   {
      Object retValue = null;

      // Assign the return value, if we have a return param
      OperationMetaData opMetaData = getOperationMetaData();
      ParameterMetaData retMetaData = opMetaData.getReturnParameter();
      if (retMetaData != null)
      {
         retValue = epInv.getReturnValue();
         if (opMetaData.isDocumentWrapped())
            retValue = ParameterWrapping.unwrapResponseParameter(opMetaData, retValue);

         if (JavaUtils.isPrimitive(retMetaData.getJavaType()))
            retValue = JavaUtils.getPrimitiveValue(retValue);
      }

      // Set the holder values for INOUT parameters
      int index = 0;
      for (ParameterMetaData paramMetaData : opMetaData.getParameters())
      {
         ParameterMode paramMode = paramMetaData.getMode();

         if (paramMode == ParameterMode.INOUT || paramMode == ParameterMode.OUT)
         {
            QName xmlName = paramMetaData.getXmlName();
            Object value = epInv.getResponseParamValue(xmlName);
            log.debug("holder [" + index + "] " + xmlName);
            HolderUtils.setHolderValue(inParams[index], value);
         }

         if (index == 0 && opMetaData.isDocumentWrapped())
            index = paramMetaData.getWrappedVariables().size() - 1;

         index++;
      }

      return retValue;
   }
}
