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
package org.jboss.ws.metadata;

// $Id$

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.jaxrpc.Style;
import org.jboss.ws.jaxrpc.Use;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData.HandlerType;

/**
 * A Service component describes a set of endpoints.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-May-2005
 */
public abstract class EndpointMetaData extends ExtensibleMetaData
{
   // provide logging
   private static Logger log = Logger.getLogger(EndpointMetaData.class);

   // The parent meta data.
   private ServiceMetaData service;

   // The REQUIRED name
   private QName name;
   // The REQUIRED config-name
   private String configName;
   // The REQUIRED config-file
   private String configFile;
   // The endpoint address
   private String endpointAddress;
   // The endpoint interface name
   private String seiName;
   // The optional authentication method
   private String authMethod;
   // The optional transport guarantee
   private String transportGuarantee;
   // Arbitrary properties given by <call-property>
   private Properties properties;
   // The SOAPBinding style
   private Style style;
   // The SOAPBinding use
   private Use use;
   // The SOAPBinding parameter style
   private ParameterStyle parameterStyle;
   // The list of service meta data
   private List<OperationMetaData> operations = new ArrayList<OperationMetaData>();
   // The optional handlers
   private List<UnifiedHandlerMetaData> jaxrpcHandlers = new ArrayList<UnifiedHandlerMetaData>();

   public EndpointMetaData(ServiceMetaData service, QName name)
   {
      this.service = service;
      this.name = name;
   }

   public ServiceMetaData getServiceMetaData()
   {
      return service;
   }

   public QName getName()
   {
      return name;
   }

   public String getConfigFile()
   {
      return configFile;
   }

   public void setConfigFile(String configFile)
   {
      this.configFile = configFile;
   }

   public String getConfigName()
   {
      return configName;
   }

   public void setConfigName(String configName)
   {
      this.configName = configName;
   }

   public String getEndpointAddress()
   {
      return endpointAddress;
   }

   public void setEndpointAddress(String endpointAddress)
   {
      this.endpointAddress = endpointAddress;
   }

   public String getServiceEndpointInterfaceName()
   {
      return seiName;
   }

   public void setServiceEndpointInterfaceName(String endpointInterfaceName)
   {
      this.seiName = endpointInterfaceName;
   }

   /** Get the class loader associated with the endpoint meta data */
   public ClassLoader getClassLoader()
   {
      ClassLoader classLoader = getServiceMetaData().getUnifiedMetaData().getClassLoader();
      return classLoader;
   }

   /** Get the class loader associated with the endpoint meta data */
   public URLClassLoader getResourceLoader()
   {
      URLClassLoader classLoader = getServiceMetaData().getUnifiedMetaData().getResourceLoader();
      return classLoader;
   }

   /** Lazily load the SEI. The SEI class loader may not be available at creation time.
    */
   public Class getServiceEndpointInterface()
   {
      String seiName = getServiceEndpointInterfaceName();
      if (seiName == null)
         throw new WSException("ServiceEndpointInterface name not available");

      Class seiClass = null;
      try
      {
         ClassLoader classLoader = getClassLoader();
         if (classLoader == null)
            throw new WSException("Class loader not available");

         seiClass = classLoader.loadClass(seiName);
      }
      catch (ClassNotFoundException ex)
      {
         throw new WSException("Cannot load SEI: " + seiName, ex);
      }
      return seiClass;
   }

   public Use getEncodingStyle()
   {
      if (use == null)
         use = Use.getDefaultUse();

      return use;
   }

   public void setEncodingStyle(Use encStyle)
   {
      if (use != null && use.equals(encStyle) == false)
         throw new WSException("Mixed encoding styles not supported");

      this.use = encStyle;
   }

   public Style getStyle()
   {
      if (style == null)
      {
         style = Style.getDefaultStyle();
         log.warn("Cannot obtain style, using default: " + style);
      }

      return style;
   }

   public void setStyle(Style styleValue)
   {
      if (style != null && style.equals(styleValue) == false)
         throw new WSException("Mixed styles not supported");

      this.style = styleValue;
   }

   public ParameterStyle getParameterStyle()
   {
      if (parameterStyle == null)
         parameterStyle = ParameterStyle.WRAPPED;

      return parameterStyle;
   }

   public void setParameterStyle(ParameterStyle styleValue)
   {
      if (parameterStyle != null && parameterStyle.equals(styleValue) == false)
         throw new WSException("Mixed SOAP parameter styles not supported");

      this.parameterStyle = styleValue;
   }

   public String getAuthMethod()
   {
      return authMethod;
   }

   public void setAuthMethod(String authMethod)
   {
      this.authMethod = authMethod;
   }

   public String getTransportGuarantee()
   {
      return transportGuarantee;
   }

   public void setTransportGuarantee(String transportGuarantee)
   {
      this.transportGuarantee = transportGuarantee;
   }

   public Properties getProperties()
   {
      return properties;
   }

   public void setProperties(Properties properties)
   {
      this.properties = properties;
   }

   public List<OperationMetaData> getOperations()
   {
      return new ArrayList<OperationMetaData>(operations);
   }

   public OperationMetaData getOperation(QName xmlName)
   {
      OperationMetaData opMetaData = null;
      for (OperationMetaData auxOperation : operations)
      {
         QName opQName = auxOperation.getXmlName();
         if (opQName.equals(xmlName))
         {
            if (opMetaData == null)
            {
               opMetaData = auxOperation;
            }
            else
            {
               throw new WSException("Cannot uniquely indetify operation: " + xmlName);
            }
         }
      }

      if (opMetaData == null && getStyle() == Style.DOCUMENT)
      {
         for (OperationMetaData auxOperation : operations)
         {
            ParameterMetaData paramMetaData = null;
            for (ParameterMetaData auxParam : auxOperation.getParameters())
            {
               ParameterMode mode = auxParam.getMode();
               if (auxParam.isInHeader() == false && mode == ParameterMode.IN)
               {
                  paramMetaData = auxParam;
                  break;
               }
            }
            if (paramMetaData != null && paramMetaData.getXmlName().equals(xmlName))
            {
               if (opMetaData == null)
               {
                  opMetaData = auxOperation;
               }
               else
               {
                  throw new WSException("Cannot uniquely indetify operation: " + xmlName);
               }
            }
         }
      }

      return opMetaData;
   }

   public OperationMetaData getOperation(Method method)
   {
      OperationMetaData opMetaData = null;
      for (OperationMetaData aux : operations)
      {
         if (aux.getJavaMethod().equals(method))
         {
            opMetaData = aux;
         }
      }
      return opMetaData;
   }

   public void addOperation(OperationMetaData opMetaData)
   {
      operations.add(opMetaData);
   }

   public void addHandler(UnifiedHandlerMetaData handler)
   {
      jaxrpcHandlers.add(handler);
   }

   public List<UnifiedHandlerMetaData> getHandlers(HandlerType type)
   {
      if (type == HandlerType.PRE || type == HandlerType.POST)
         throw new IllegalArgumentException("Illegal handler type: " + type);
      
      return new ArrayList<UnifiedHandlerMetaData>(jaxrpcHandlers);
   }

   /**
    * @see UnifiedMetaData#eagerInitialize()
    */
   public void eagerInitialize()
   {
      for (OperationMetaData operation : operations)
         operation.eagerInitialize();
   }
}
