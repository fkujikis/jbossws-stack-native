/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.core.jaxrpc.client;

import static org.jboss.ws.NativeMessages.MESSAGES;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;
import javax.xml.rpc.encoding.TypeMappingRegistry;
import javax.xml.rpc.handler.HandlerChain;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.HandlerRegistry;

import org.jboss.ws.NativeLoggers;
import org.jboss.ws.common.ResourceLoaderAdapter;
import org.jboss.ws.core.StubExt;
import org.jboss.ws.metadata.builder.jaxrpc.JAXRPCClientMetaDataBuilder;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaDataJAXRPC;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData.HandlerType;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedInitParamMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedPortComponentRefMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedStubPropertyMetaData;

/**
 * Service class acts as a factory for:
 * <ul>
 * <li>Dynamic proxy for the target service endpoint.
 * <li>Instance of the type javax.xml.rpc.Call for the dynamic invocation of a
 * remote operation on the target service endpoint.
 * <li>Instance of a generated stub class
 * </ul>
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-Oct-2004
 */
public class ServiceImpl implements ServiceExt, Serializable, Externalizable
{
   // The service meta data that is associated with this JAXRPC Service
   private transient ServiceMetaData serviceMetaData;
   private QName serviceName;
   // The optional WSDL location
   private URL wsdlLocation;
   private URL mappingURL;
   private JavaWsdlMapping mappingConfig;
   // The <service-ref> meta data
   private UnifiedServiceRefMetaData usrMetaData;

   // The handler registry
   private transient HandlerRegistryImpl handlerRegistry;

   public ServiceImpl() {
       // for deserialization only
   }

   /**
    * Construct a Service without WSDL meta data
    */
   public ServiceImpl(QName serviceName)
   {
      this.serviceName = serviceName;
      init();
   }

   /**
    * Construct a Service that has access to some WSDL meta data
    */
   public ServiceImpl(QName serviceName, URL wsdlURL, URL mappingURL, URL securityURL)
   {
      this.serviceName = serviceName;
      this.wsdlLocation = wsdlURL;
      this.mappingURL = mappingURL;
      init();
   }

   /**
    * Construct a Service that has access to some WSDL meta data
    */
   public ServiceImpl(QName serviceName, URL wsdlURL, JavaWsdlMapping mappingURL, UnifiedServiceRefMetaData usrMetaData)
   {
      this.serviceName = serviceName;
      this.wsdlLocation = wsdlURL;
      this.mappingConfig = mappingURL;
      this.usrMetaData = usrMetaData;
      init();
   }

   public void writeExternal(final ObjectOutput out) throws IOException {
       out.writeObject(serviceName);
       out.writeObject(wsdlLocation);
       out.writeObject(mappingURL);
       out.writeObject(mappingConfig);
       out.writeObject(usrMetaData);
   }

   public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
       serviceName = (QName)in.readObject();
       wsdlLocation = (URL)in.readObject();
       mappingURL = (URL)in.readObject();
       mappingConfig = (JavaWsdlMapping)in.readObject();
       usrMetaData = (UnifiedServiceRefMetaData)in.readObject();
       init();
   }

   private void init() {
       if ((wsdlLocation == null) && (mappingURL == null) && (mappingConfig == null) && (usrMetaData == null)) {
           UnifiedMetaData wsMetaData = new UnifiedMetaData(new ResourceLoaderAdapter());
           serviceMetaData = new ServiceMetaData(wsMetaData, serviceName);
           handlerRegistry = new HandlerRegistryImpl(serviceMetaData);
           return;
       }
       if (mappingURL != null) {
           JAXRPCClientMetaDataBuilder builder = new JAXRPCClientMetaDataBuilder();
           ClassLoader ctxClassLoader = SecurityActions.getContextClassLoader();
           serviceMetaData = builder.buildMetaData(serviceName, wsdlLocation, mappingURL, null, ctxClassLoader);
           handlerRegistry = new HandlerRegistryImpl(serviceMetaData);
           return;
       }
       JAXRPCClientMetaDataBuilder builder = new JAXRPCClientMetaDataBuilder();
       ClassLoader ctxClassLoader = SecurityActions.getContextClassLoader();
       serviceMetaData = builder.buildMetaData(serviceName, wsdlLocation, mappingConfig, usrMetaData, ctxClassLoader);
       handlerRegistry = new HandlerRegistryImpl(serviceMetaData);
   }

   public ServiceMetaData getServiceMetaData()
   {
      return serviceMetaData;
   }

   /**
    * Gets the location of the WSDL document for this Service.
    *
    * @return URL for the location of the WSDL document for this service
    */
   public URL getWSDLDocumentLocation()
   {
      return wsdlLocation;
   }

   /**
    * Gets the name of this service.
    *
    * @return Qualified name of this service
    */
   public QName getServiceName()
   {
      return serviceMetaData.getServiceName();
   }

   /**
    * Creates a Call instance.
    *
    * @param portName
    *            Qualified name for the target service endpoint
    * @return Call instance
    * @throws javax.xml.rpc.ServiceException
    *             If any error in the creation of the Call object
    */
   public Call createCall(QName portName) throws ServiceException
   {
      String nsURI = portName.getNamespaceURI();
      serviceMetaData.assertTargetNamespace(nsURI);
      CallImpl call = new CallImpl(this, portName, null);
      return call;
   }

   /**
    * Creates a Call instance.
    *
    * @param portName
    *            Qualified name for the target service endpoint
    * @param operationName
    *            Name of the operation for which this Call object is to be
    *            created.
    * @return Call instance
    * @throws javax.xml.rpc.ServiceException
    *             If any error in the creation of the Call object
    */
   public Call createCall(QName portName, String operationName) throws ServiceException
   {
      String nsURI = portName.getNamespaceURI();
      serviceMetaData.assertTargetNamespace(nsURI);
      QName opName = new QName(nsURI, operationName);
      CallImpl call = new CallImpl(this, portName, opName);
      return call;
   }

   /**
    * Creates a Call instance.
    *
    * @param portName
    *            Qualified name for the target service endpoint
    * @param opName
    *            Qualified name of the operation for which this Call object is
    *            to be created.
    * @return Call instance
    * @throws javax.xml.rpc.ServiceException
    *             If any error in the creation of the Call object
    */
   public Call createCall(QName portName, QName opName) throws ServiceException
   {
      serviceMetaData.assertTargetNamespace(portName.getNamespaceURI());
      serviceMetaData.assertTargetNamespace(opName.getNamespaceURI());
      CallImpl call = new CallImpl(this, portName, opName);
      return call;
   }

   /**
    * Creates a Call object not associated with specific operation or target
    * service endpoint. This Call object needs to be configured using the
    * setter methods on the Call interface.
    *
    * @return Call object
    * @throws javax.xml.rpc.ServiceException
    *             If any error in the creation of the Call object
    */
   public Call createCall() throws ServiceException
   {
      CallImpl call = new CallImpl(this);
      return call;
   }

   /**
    * Gets an array of preconfigured Call objects for invoking operations on
    * the specified port. There is one Call object per operation that can be
    * invoked on the specified port. Each Call object is pre-configured and
    * does not need to be configured using the setter methods on Call
    * interface. <p/> Each invocation of the getCalls method returns a new
    * array of preconfigured Call objects <p/> This method requires the Service
    * implementation class to have access to the WSDL related metadata.
    *
    * @param portName
    *            Qualified name for the target service endpoint
    * @return Call[] Array of pre-configured Call objects
    * @throws javax.xml.rpc.ServiceException
    *             If this Service class does not have access to the required
    *             WSDL metadata or if an illegal endpointName is specified.
    */
   public Call[] getCalls(QName portName) throws ServiceException
   {
      EndpointMetaData epMetaData = serviceMetaData.getEndpoint(portName);
      if (epMetaData == null)
         throw MESSAGES.cannotFindEndpointForName(portName);

      List<Call> calls = new ArrayList<Call>();
      for (OperationMetaData opMetaData : epMetaData.getOperations())
      {
         Call call = createCall(portName, opMetaData.getQName());
         calls.add(call);
      }

      Call[] callArr = new Call[calls.size()];
      calls.toArray(callArr);

      return callArr;
   }

   /**
    * J2EE components should not use the getHandlerRegistry() method. A
    * container provider must throw a java.lang.UnsupportedOperationException
    * from the getHandlerRegistry() method of the Service Interface. Handler
    * support is documented in Chapter 6 Handlers.
    */
   public HandlerRegistry getHandlerRegistry()
   {
      throw MESSAGES.shouldNotUseMethod("getHandlerRegistry()");
   }

   /**
    * Get a HandlerRegistry that can be used to dynamically change the client
    * side handler chain associated with a given endpoint.
    */
   public HandlerRegistry getDynamicHandlerRegistry()
   {
      return handlerRegistry;
   }

   /**
    * J2EE components should not use the getTypeMappingRegistry() method. A
    * container provider must throw a java.lang.UnsupportedOperationException
    * from the getTypeMappingRegistry() method of the Service Interface.
    */
   public TypeMappingRegistry getTypeMappingRegistry()
   {
      throw MESSAGES.shouldNotUseMethod("getTypeMappingRegistry()");
   }

   /**
    * Returns an Iterator for the list of QNames of service endpoints grouped
    * by this service
    *
    * @return Returns java.util.Iterator with elements of type
    *         javax.xml.namespace.QName
    * @throws javax.xml.rpc.ServiceException
    *             If this Service class does not have access to the required
    *             WSDL metadata
    */
   public Iterator getPorts() throws ServiceException
   {
      ArrayList<QName> list = new ArrayList<QName>();
      if (serviceMetaData != null)
      {
         for (EndpointMetaData epMetaData : serviceMetaData.getEndpoints())
         {
            list.add(epMetaData.getPortName());
         }
      }
      return list.iterator();
   }

   /**
    * The getPort method returns either an instance of a generated stub
    * implementation class or a dynamic proxy. The parameter
    * serviceEndpointInterface specifies the service endpoint interface that is
    * supported by the returned stub or proxy. In the implementation of this
    * method, the JAX-RPC runtime system takes the responsibility of selecting
    * a protocol binding (and a port) and configuring the stub accordingly. The
    * returned Stub instance should not be reconfigured by the client.
    */
   public Remote getPort(Class seiClass) throws ServiceException
   {
      if (seiClass == null)
         throw MESSAGES.illegalNullArgument("seiClass");

      String seiName = seiClass.getName();
      if (Remote.class.isAssignableFrom(seiClass) == false)
         throw new ServiceException(MESSAGES.notImplementRemote(seiName));

      if (serviceMetaData == null)
         throw MESSAGES.serviceMetaDataNotAvailable();

      try
      {
         EndpointMetaData epMetaData = serviceMetaData.getEndpointByServiceEndpointInterface(seiName);
         if (epMetaData == null && serviceMetaData.getEndpoints().size() == 1)
         {
            epMetaData = serviceMetaData.getEndpoints().get(0);
            epMetaData.setServiceEndpointInterfaceName(seiName);
         }

         if (epMetaData == null)
            throw MESSAGES.cannotFindEndpointMetaData(seiName);

         return createProxy(seiClass, epMetaData);
      }
      catch (ServiceException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         throw new ServiceException(ex);
      }
   }

   /**
    * The getPort method returns either an instance of a generated stub
    * implementation class or a dynamic proxy. A service client uses this
    * dynamic proxy to invoke operations on the target service endpoint. The
    * serviceEndpointInterface specifies the service endpoint interface that is
    * supported by the created dynamic proxy or stub instance.
    */
   public Remote getPort(QName portName, Class seiClass) throws ServiceException
   {
      if (seiClass == null)
         throw MESSAGES.illegalNullArgument("seiClass");

      if (serviceMetaData == null)
         throw MESSAGES.serviceMetaDataNotAvailable();

      String seiName = seiClass.getName();
      if (Remote.class.isAssignableFrom(seiClass) == false)
         throw new ServiceException(MESSAGES.notImplementRemote(seiName));

      EndpointMetaData epMetaData = serviceMetaData.getEndpoint(portName);
      if (epMetaData == null)
         throw MESSAGES.cannotFindEndpointMetaData(portName);

      try
      {
         if (epMetaData.getServiceEndpointInterfaceName() == null)
            epMetaData.setServiceEndpointInterfaceName(seiName);

         return createProxy(seiClass, epMetaData);
      }
      catch (ServiceException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         throw new ServiceException(ex);
      }
   }

   private Remote createProxy(Class seiClass, EndpointMetaData epMetaData) throws Exception
   {
      CallImpl call = new CallImpl(this, epMetaData);
      initStubProperties(call, seiClass.getName());

      PortProxy handler = new PortProxy(call);
      ClassLoader cl = epMetaData.getClassLoader();
      Remote proxy = (Remote)Proxy.newProxyInstance(cl, new Class[] { seiClass, Stub.class, StubExt.class }, handler);

      // Setup the handler chain
      setupHandlerChain(epMetaData);

      return proxy;
   }

   private int initStubProperties(CallImpl call, String seiName)
   {
      // nothing to do
      if (usrMetaData == null)
         return 0;

      int propCount = 0;
      for (UnifiedPortComponentRefMetaData upcRef : usrMetaData.getPortComponentRefs())
      {
         if (seiName.equals(upcRef.getServiceEndpointInterface()))
         {
            for (UnifiedStubPropertyMetaData prop : upcRef.getStubProperties())
            {
               call.setProperty(prop.getPropName(), prop.getPropValue());
               propCount++;
            }
         }
      }
      return propCount;
   }

   /**
    * Get the handler chain for the given endpoint name, maybe null.
    */
   public HandlerChain getHandlerChain(QName portName)
   {
      return handlerRegistry.getHandlerChainInstance(portName);
   }

   /**
    * Register a handler chain for the given endpoint name
    */
   public void registerHandlerChain(QName portName, List infos, Set roles)
   {
      handlerRegistry.registerClientHandlerChain(portName, infos, roles);
   }

   public void setupHandlerChain(EndpointMetaData epMetaData)
   {
      if (epMetaData.isHandlersInitialized() == false)
      {
         QName portName = epMetaData.getPortName();
         Set<String> handlerRoles = new HashSet<String>();
         List<HandlerInfo> handlerInfos = new ArrayList<HandlerInfo>();
         for (HandlerMetaData handlerMetaData : epMetaData.getHandlerMetaData(HandlerType.ALL))
         {
            HandlerMetaDataJAXRPC jaxrpcMetaData = (HandlerMetaDataJAXRPC)handlerMetaData;
            handlerRoles.addAll(jaxrpcMetaData.getSoapRoles());

            HashMap hConfig = new HashMap();
            for (UnifiedInitParamMetaData param : jaxrpcMetaData.getInitParams())
            {
               hConfig.put(param.getParamName(), param.getParamValue());
            }

            Set<QName> headers = jaxrpcMetaData.getSoapHeaders();
            QName[] headerArr = new QName[headers.size()];
            headers.toArray(headerArr);

            Class hClass = jaxrpcMetaData.getHandlerClass();
            hConfig.put(HandlerType.class.getName(), jaxrpcMetaData.getHandlerType());
            HandlerInfo info = new HandlerInfo(hClass, hConfig, headerArr);

            NativeLoggers.JAXRPC_LOGGER.addingClientSideHandlerToEndpoint(portName, info);
            handlerInfos.add(info);
         }

         // register the handlers with the client engine
         if (handlerInfos.size() > 0)
            registerHandlerChain(portName, handlerInfos, handlerRoles);

         epMetaData.setHandlersInitialized(true);
      }
   }
}
