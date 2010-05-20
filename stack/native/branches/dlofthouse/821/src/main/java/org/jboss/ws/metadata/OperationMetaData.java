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
import java.util.ArrayList;
import java.util.List;

import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.jaxrpc.ParameterWrapping;
import org.jboss.ws.jaxrpc.Style;
import org.jboss.ws.jaxrpc.Use;
import org.jboss.ws.utils.HolderUtils;
import org.jboss.ws.utils.JavaUtils;
import org.w3c.dom.Element;

/**
 * An Operation component describes an operation that a given interface supports.
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @since 12-May-2004
 */
public class OperationMetaData extends ExtensibleMetaData
{
   // provide logging
   private final Logger log = Logger.getLogger(OperationMetaData.class);

   // The parent interface
   private EndpointMetaData epMetaData;

   private QName xmlName;
   private QName responseName;
   private String javaName;
   private Method javaMethod;
   private boolean oneWayOperation;
   private String soapAction;
   private List<ParameterMetaData> parameters = new ArrayList<ParameterMetaData>();
   private List<FaultMetaData> faults = new ArrayList<FaultMetaData>();
   private ParameterMetaData returnParam;

   public OperationMetaData(QName xmlName, String javaName)
   {
      UnifiedMetaData umd = new UnifiedMetaData();
      ServiceMetaData smd = new ServiceMetaData(umd, new QName("mock-service"));
      ServerEndpointMetaData epmd = new ServerEndpointMetaData(smd, new QName("mock-endpoint"));
      initOperationMetaData(epmd, xmlName, javaName);
   }

   public OperationMetaData(EndpointMetaData epMetaData, QName xmlName, String javaName)
   {
      log.trace("new OperationMetaData: [xmlName=" + xmlName + ",javaName=" + javaName + "]");
      initOperationMetaData(epMetaData, xmlName, javaName);
   }

   private void initOperationMetaData(EndpointMetaData epMetaData, QName xmlName, String javaName)
   {
      this.epMetaData = epMetaData;
      this.xmlName = xmlName;
      this.javaName = javaName;

      if (xmlName == null)
         throw new IllegalArgumentException("Invalid null xmlName argument");
      if (javaName == null)
         throw new IllegalArgumentException("Invalid null javaName argument, for: " + xmlName);

      String nsURI = xmlName.getNamespaceURI();
      String localPart = xmlName.getLocalPart();
      this.responseName = new QName(nsURI, localPart + "Response");
   }

   public EndpointMetaData getEndpointMetaData()
   {
      return epMetaData;
   }

   public QName getXmlName()
   {
      return xmlName;
   }

   public QName getResponseName()
   {
      return responseName;
   }

   public String getSOAPAction()
   {
      return soapAction;
   }

   public void setSOAPAction(String soapAction)
   {
      this.soapAction = soapAction;
   }

   public Style getStyle()
   {
      return epMetaData.getStyle();
   }

   public Use getUse()
   {
      return epMetaData.getEncodingStyle();
   }

   public ParameterStyle getParameterStyle()
   {
      return epMetaData.getParameterStyle();
   }

   public boolean isRPCLiteral()
   {
      return getStyle() == Style.RPC && getUse() == Use.LITERAL;
   }
   
   public boolean isDocumentBare()
   {
      return getStyle() == Style.DOCUMENT && getParameterStyle() == ParameterStyle.BARE;
   }

   public boolean isDocumentWrapped()
   {
      return getStyle() == Style.DOCUMENT && getParameterStyle() == ParameterStyle.WRAPPED;
   }

   public String getJavaName()
   {
      return javaName;
   }

   /** Lazily load the java method. The SEI class loader may not be available at creation time
    */
   public Method getJavaMethod()
   {
      ClassLoader loader = getEndpointMetaData().getServiceMetaData().getUnifiedMetaData().getClassLoader();
      if (javaMethod == null || javaMethod.getDeclaringClass().getClassLoader().equals(loader) == false)
      {
         log.debug("Get java method for: " + xmlName);
         
         javaMethod = null;
         try
         {
            Class seiClass = epMetaData.getServiceEndpointInterface();
            if (seiClass == null)
               throw new WSException("ServiceEndpointInterface not available");

            for (Method method : seiClass.getMethods())
            {
               String methodName = method.getName();
               Class[] methodTypes = method.getParameterTypes();

               if (javaName.equals(methodName))
               {
                  log.trace("Found java method: " + method);

                  // match document/literal/wrapped
                  if (isDocumentWrapped() && ParameterWrapping.matchRequestParameters(this, methodTypes))
                  {
                     log.debug("Found wrapped java method: " + method);
                     javaMethod = method;
                     break;
                  }

                  // compare params by java type name
                  if (compareMethodParams(methodTypes, true))
                  {
                     log.debug("Found best matching java method: " + method);
                     javaMethod = method;
                     break;
                  }

                  // compare params by assignability
                  if (compareMethodParams(methodTypes, false))
                  {
                     if (javaMethod != null)
                        throw new WSException("Ambiguous javaMethod: " + method);

                     log.debug("Found possible matching java method: " + method);
                     javaMethod = method;
                     break;
                  }
               }
            }

            if (javaMethod == null)
               throw new WSException("Cannot find java method: " + javaName);
         }
         catch (RuntimeException rte)
         {
            throw rte;
         }
         catch (Exception ex)
         {
            throw new WSException("Cannot load java method: " + javaName);
         }
      }

      return javaMethod;
   }

   /** Return true if this is a generic message style destination that takes a org.w3c.dom.Element
    */
   public boolean isMessageEndpoint()
   {
      boolean isMessageEndpoint = false;
      if (parameters.size() == 1)
      {
         ParameterMetaData inParam = parameters.get(0);
         if (JavaUtils.isAssignableFrom(Element.class, inParam.getJavaType()))
         {
            isMessageEndpoint = true;
         }
      }
      return isMessageEndpoint;
   }

   private boolean compareMethodParams(Class[] methodTypes, boolean matchByTypeName)
   {
      log.trace("Compare method params by type name: " + matchByTypeName);

      boolean pass = (parameters.size() == methodTypes.length);
      if (pass == false)
         log.trace("Unmatched parameter count: " + parameters.size() + "!=" + methodTypes.length);

      for (int i = 0; pass && i < methodTypes.length; i++)
      {
         ParameterMetaData paramMetaData = parameters.get(i);
         Class methodType = methodTypes[i];

         if (paramMetaData.getMode() != ParameterMode.IN)
         {
            if (HolderUtils.isHolderType(methodType))
            {
               methodType = HolderUtils.getValueType(methodType);
            }
            else
            {
               pass = false;
               break;
            }
         }

         if (matchByTypeName)
         {
            String javaTypeName = methodType.getName();
            String paramTypeName = paramMetaData.getJavaTypeName();
            pass = javaTypeName.equals(paramTypeName);
         }
         else
         {
            Class paramType = paramMetaData.getJavaType();
            pass = JavaUtils.isAssignableFrom(methodType, paramType);
         }

         String name = (matchByTypeName) ? paramMetaData.getJavaTypeName() : paramMetaData.getJavaType().getName();
         log.trace((pass ? "Matched" : "Unmatched") + " parameter: " + name  + " == " + methodType.getName());
      }
      return pass;
   }

   public boolean isOneWayOperation()
   {
      return oneWayOperation;
   }

   public void setOneWayOperation(boolean oneWayOperation)
   {
      this.oneWayOperation = oneWayOperation;
      assertOneWayOperation();
   }

   public ParameterMetaData getParameter(QName xmlName)
   {
      ParameterMetaData paramMetaData = null;
      for (int i = 0; paramMetaData == null && i < parameters.size(); i++)
      {
         ParameterMetaData aux = parameters.get(i);
         if (xmlName.equals(aux.getXmlName()))
            paramMetaData = aux;
      }
      return paramMetaData;
   }

   /** Get the IN or INOUT parameter list */
   public List<ParameterMetaData> getInputParameters()
   {
      List<ParameterMetaData> retList = new ArrayList<ParameterMetaData>();
      for (ParameterMetaData paramMetaData : parameters)
      {
         ParameterMode mode = paramMetaData.getMode();
         if (mode == ParameterMode.IN || mode == ParameterMode.INOUT)
            retList.add(paramMetaData);
      }
      return retList;
   }

   /** Get the OUT or INOUT parameter list */
   public List<ParameterMetaData> getOutputParameters()
   {
      List<ParameterMetaData> retList = new ArrayList<ParameterMetaData>();
      for (ParameterMetaData paramMetaData : parameters)
      {
         ParameterMode mode = paramMetaData.getMode();
         if (mode == ParameterMode.OUT || mode == ParameterMode.INOUT)
            retList.add(paramMetaData);
      }
      return retList;
   }

   /** Get the non header parameter list */
   public List<ParameterMetaData> getNonHeaderParameters()
   {
      List<ParameterMetaData> retList = new ArrayList<ParameterMetaData>();
      for (ParameterMetaData paramMetaData : parameters)
      {
         if (paramMetaData.isInHeader() == false)
            retList.add(paramMetaData);
      }
      return retList;
   }

   public List<ParameterMetaData> getParameters()
   {
      return new ArrayList<ParameterMetaData>(parameters);
   }

   public void addParameter(ParameterMetaData pmd)
   {
      log.trace("addParameter: [xmlName=" + pmd.getXmlName() + ",xmlType=" + pmd.getXmlType() + "]");
      parameters.add(pmd);
      assertOneWayOperation();
   }

   public void removeAllParameters()
   {
      parameters.clear();
   }

   public ParameterMetaData getReturnParameter()
   {
      return returnParam;
   }

   public void setReturnParameter(ParameterMetaData returnParam)
   {
      log.trace("setReturnParameter: " + returnParam);
      returnParam.setMode(ParameterMode.OUT);
      this.returnParam = returnParam;
      assertOneWayOperation();
   }

   public List<FaultMetaData> getFaults()
   {
      return new ArrayList<FaultMetaData>(faults);
   }

   public FaultMetaData getFault(QName xmlName)
   {
      FaultMetaData faultMetaData = null;
      for (int i = 0; faultMetaData == null && i < faults.size(); i++)
      {
         FaultMetaData aux = faults.get(i);
         if (aux.getXmlName().equals(xmlName))
            faultMetaData = aux;
      }
      return faultMetaData;
   }

   public FaultMetaData getFault(Class javaType)
   {
      FaultMetaData faultMetaData = null;
      for (FaultMetaData aux : faults)
      {
         if (aux.getJavaType().equals(javaType))
         {
            faultMetaData = aux;
            break;
         }
      }
      return faultMetaData;
   }

   public void addFault(FaultMetaData fault)
   {
      log.trace("addFault: " + fault);
      faults.add(fault);
      assertOneWayOperation();
   }

   public void validate()
   {
      assertBare();
   }

   // A JSR-181 processor is REQUIRED to report an error if an
   // operation marked @Oneway has a return value, declares any checked exceptions or has any
   // INOUT or OUT parameters.
   private void assertOneWayOperation()
   {
      if (oneWayOperation)
      {
         if (returnParam != null)
            throw new WSException("OneWay operations cannot have a return parameter");

         if (faults.size() > 0)
            throw new WSException("OneWay operations cannot have checked exceptions");

         for (ParameterMetaData paramMetaData : parameters)
         {
            if (paramMetaData.getMode() != ParameterMode.IN)
               throw new WSException("OneWay operations cannot have INOUT or OUT parameters");
         }
      }
   }

   private void assertBare()
   {
      if (isDocumentBare())
      {
         int in = 0;
         int out = 0;

         for (ParameterMetaData paramMetaData : parameters)
         {
            if (paramMetaData.isInHeader())
               continue;

            ParameterMode mode = paramMetaData.getMode();
            if (mode != ParameterMode.OUT)
               in++;
            if (mode != ParameterMode.IN)
               out++;
         }

         if (returnParam != null && ! returnParam.isInHeader())
            out++;

         if (! ((oneWayOperation && in == 1 && out == 0) || (in == 1 && out == 1)))
            throw new WSException("The body of a documnet/literal bare message requires only 1 input and only 1 output (or 0 if oneway). method: " + javaName + " in: " + in + " out: " + out);
      }
   }

   /**
    * @see UnifiedMetaData#eagerInitialize()
    */
   public void eagerInitialize()
   {
      for (ParameterMetaData parameter : parameters)
         parameter.eagerInitialize();

      if (returnParam != null)
         returnParam.eagerInitialize();

      for (FaultMetaData fault : faults)
         fault.eagerInitialize();
   }

   public String toString()
   {
      StringBuilder buffer = new StringBuilder("\nOperationMetaData:");
      buffer.append("\n xmlName=" + xmlName);
      buffer.append("\n javaName=" + javaName);
      buffer.append("\n style=" + getStyle() + "/" + getUse() + (getStyle() == Style.DOCUMENT ? "/" + getParameterStyle() : ""));
      buffer.append("\n oneWay=" + oneWayOperation);
      buffer.append("\n soapAction=" + soapAction);
      for (ParameterMetaData param : parameters)
      {
         buffer.append(param);
      }
      if (returnParam != null)
      {
         buffer.append(returnParam.toString());
      }
      for (FaultMetaData fault : faults)
      {
         buffer.append(fault);
      }
      return buffer.toString();
   }
}
