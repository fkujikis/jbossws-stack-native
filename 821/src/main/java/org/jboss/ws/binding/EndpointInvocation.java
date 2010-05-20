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
package org.jboss.ws.binding;

// $Id$

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.holders.Holder;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.jaxrpc.ParameterWrapping;
import org.jboss.ws.metadata.OperationMetaData;
import org.jboss.ws.metadata.ParameterMetaData;
import org.jboss.ws.soap.SOAPContentElement;
import org.jboss.ws.utils.HolderUtils;
import org.jboss.ws.utils.JavaUtils;
import org.jboss.ws.utils.MimeUtils;

/** A web service invocation.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 16-Oct-2004
 */
public class EndpointInvocation
{
   // provide logging
   private static final Logger log = Logger.getLogger(EndpointInvocation.class);

   // The operation meta data for this invocation
   private OperationMetaData opMetaData;
   // Map the named endpoint parameters
   private Map<QName, Object> reqPayload = new LinkedHashMap<QName, Object>();
   // Map the named endpoint parameters
   private Map<QName, Object> resPayload = new LinkedHashMap<QName, Object>();
   // The return value
   private Object returnValue;

   public EndpointInvocation(OperationMetaData opMetaData)
   {
      this.opMetaData = opMetaData;
   }

   public OperationMetaData getOperationMetaData()
   {
      return opMetaData;
   }

   public Method getJavaMethod()
   {
      return opMetaData.getJavaMethod();
   }

   public List<QName> getRequestParamNames()
   {
      List<QName> xmlNames = new ArrayList<QName>();
      xmlNames.addAll(reqPayload.keySet());
      return xmlNames;
   }

   public void setRequestParamValue(QName xmlName, Object value)
   {
      log.debug("setRequestParamValue: [name=" + xmlName + ",value=" + getTypeName(value) + "]");
      reqPayload.put(xmlName, value);
   }

   public Object getRequestParamValue(QName xmlName) throws SOAPException
   {
      log.debug("getRequestParamValue: " + xmlName);
      Object paramValue = reqPayload.get(xmlName);
      ParameterMetaData paramMetaData = opMetaData.getParameter(xmlName);
      paramValue = transformPayloadValue(paramMetaData, paramValue);
      return paramValue;
   }

   /** Returns the payload that can be passed on to the endpoint implementation 
    */
   public Object[] getRequestPayload() throws SOAPException
   {
      log.debug("getRequestPayload");
      List<QName> xmlNames = getRequestParamNames();
      ArrayList<Object> objPayload = new ArrayList<Object>(); 
      for (int i = 0; i < xmlNames.size(); i++)
      {
         QName xmlName = xmlNames.get(i);
         Object paramValue = getRequestParamValue(xmlName);

         ParameterMetaData paramMetaData = opMetaData.getParameter(xmlName);
         paramValue = syncEndpointInputParam(paramMetaData, paramValue);
         if(opMetaData.isDocumentWrapped() && paramMetaData.isInHeader() == false)
         {
            List<Object> objList = Arrays.asList((Object[])paramValue);
            objPayload.addAll(objList);
         }
         else
         {
            objPayload.add(paramValue);
         }
      }
      return objPayload.toArray();
   }

   public List<QName> getResponseParamNames()
   {
      List<QName> xmlNames = new ArrayList<QName>();
      xmlNames.addAll(resPayload.keySet());
      return xmlNames;
   }

   public void setResponseParamValue(QName xmlName, Object value)
   {
      log.debug("setResponseParamValue: [name=" + xmlName + ",value=" + getTypeName(value) + "]");
      resPayload.put(xmlName, value);
   }

   public Object getResponseParamValue(QName xmlName) throws SOAPException
   {
      log.debug("getResponseParamValue: " + xmlName);
      Object paramValue = resPayload.get(xmlName);
      ParameterMetaData paramMetaData = opMetaData.getParameter(xmlName);
      paramValue = transformPayloadValue(paramMetaData, paramValue);
      if (paramValue != null)
      {
         Class valueType = paramValue.getClass();
         if (HolderUtils.isHolderType(valueType))
         {
            valueType = HolderUtils.getValueType(valueType);
            paramValue = HolderUtils.getHolderValue(paramValue);
         }
      }
      return paramValue;
   }

   public void setReturnValue(Object value)
   {
      ParameterMetaData retMetaData = opMetaData.getReturnParameter();
      if (value != null && retMetaData == null)
         throw new WSException("Operation does not have a return value: " + opMetaData.getXmlName());

      log.debug("setReturnValue: " + getTypeName(value));
      this.returnValue = value;
   }

   public Object getReturnValue() throws SOAPException
   {
      log.debug("getReturnValue");
      Object paramValue = returnValue;
      ParameterMetaData paramMetaData = opMetaData.getReturnParameter();
      if (paramMetaData != null)
      {
         paramValue = transformPayloadValue(paramMetaData, paramValue);
      }
      return paramValue;
   }

   private Object transformPayloadValue(ParameterMetaData paramMetaData, final Object paramValue) throws SOAPException
   {
      QName xmlName = paramMetaData.getXmlName();
      QName xmlType = paramMetaData.getXmlType();
      Class javaType = paramMetaData.getJavaType();

      Object retValue = paramValue;

      // Handle attachment part
      if (paramValue instanceof AttachmentPart)
      {
         AttachmentPart part = (AttachmentPart)paramValue;

         Set mimeTypes = paramMetaData.getMimeTypes();
         if (DataHandler.class.isAssignableFrom(javaType) && !javaType.equals(Object.class))
         {
            DataHandler handler = part.getDataHandler();
            String mimeType = MimeUtils.getBaseMimeType(handler.getContentType());

            if (mimeTypes != null && !MimeUtils.isMemberOf(mimeType, mimeTypes))
               throw new SOAPException("Mime type " + mimeType + " not allowed for parameter " + xmlName + " allowed types are " + mimeTypes);

            retValue = part.getDataHandler();
         }
         else
         {
            retValue = part.getContent();
            String mimeType = MimeUtils.getBaseMimeType(part.getContentType());

            if (mimeTypes != null && !MimeUtils.isMemberOf(mimeType, mimeTypes))
               throw new SOAPException("Mime type " + mimeType + " not allowed for parameter " + xmlName + " allowed types are " + mimeTypes);

            if (retValue != null)
            {
               Class valueType = retValue.getClass();
               if (JavaUtils.isAssignableFrom(javaType, valueType) == false)
                  throw new SOAPException("javaType [" + javaType.getName() + "] is not assignable from attachment content: " + valueType.getName());
            }
         }
      }
      else if (paramValue instanceof SOAPContentElement)
      {
         // For xsd:anyType we return the SOAPElement
         if (xmlType.getLocalPart().equals("anyType") == false)
         {
            SOAPContentElement soapElement = (SOAPContentElement)paramValue;
            retValue = soapElement.getObjectValue();
         }
      }

      log.debug("transformPayloadValue: " + getTypeName(paramValue) + " -> " + getTypeName(retValue));
      return retValue;
   }

   /** Synchronize the operation IN, INOUT paramters with the call input parameters.
    *  Essetially it unwrapps holders and converts primitives to wrapper types.
    */
   public void initInputParams(Object[] inputParams)
   {
      List<ParameterMetaData> paramMetaDataList = opMetaData.getParameters();
      if (opMetaData.isDocumentWrapped() && paramMetaDataList.size() != 0)
      {
         Object value = ParameterWrapping.wrapRequestParameters(opMetaData, inputParams);
         ParameterMetaData paramMetaData = paramMetaDataList.get(0);
         QName xmlName = paramMetaData.getXmlName();
         setRequestParamValue(xmlName, value);
         
         if (inputParams != null)
         {
            int wrappedParamsCount = paramMetaData.getWrappedVariables().size();
            int lastParam = Math.min(paramMetaDataList.size() - 1, inputParams.length - wrappedParamsCount);

            // document/literal wrapped with bound headers
            for (int i = 0; i < lastParam - 1; i++)
            {
               paramMetaData = paramMetaDataList.get(i + 1);
               if (paramMetaData.isInHeader())
               {
                  xmlName = paramMetaData.getXmlName();
                  Class javaType = paramMetaData.getJavaType();

                  value = inputParams[i + wrappedParamsCount];
                  if (value != null)
                  {
                     Class inputType = value.getClass();

                     if (HolderUtils.isHolderType(inputType))
                     {
                        inputType = HolderUtils.getValueType(inputType);
                        value = HolderUtils.getHolderValue(value);
                     }

                     // Verify that the java type matches a registered xmlType
                     // Attachments are skipped because they don't use type mapping
                     if (!paramMetaData.isSwA() && !paramMetaData.isXOP())
                     {
                        if (JavaUtils.isAssignableFrom(javaType, inputType) == false)
                           throw new WSException("Parameter '" + javaType + "' not assignable from: " + inputType);
                     }
                  }
                  setRequestParamValue(xmlName, value);
               }
            }
         }
      }
      else
      {
         for (int i = 0; i < paramMetaDataList.size(); i++)
         {
            ParameterMetaData paramMetaData = paramMetaDataList.get(i);
            QName xmlName = paramMetaData.getXmlName();
            Class javaType = paramMetaData.getJavaType();

            Object value = inputParams[i];
            if (value != null)
            {
               Class inputType = value.getClass();

               if (HolderUtils.isHolderType(inputType))
               {
                  inputType = HolderUtils.getValueType(inputType);
                  value = HolderUtils.getHolderValue(value);
               }

               // Verify that the java type matches a registered xmlType
               // Attachments are skipped because they don't use type mapping
               if (!paramMetaData.isSwA() && !paramMetaData.isXOP())
               {
                  if (JavaUtils.isAssignableFrom(javaType, inputType) == false)
                     throw new WSException("Parameter '" + javaType + "' not assignable from: " + inputType);
               }
            }
            setRequestParamValue(xmlName, value);
         }
      }
   }

   /** Synchronize the operation paramters with the endpoint method parameters
    */
   private Object syncEndpointInputParam(ParameterMetaData paramMetaData, final Object paramValue)
   {
      Object retValue = paramValue;
      Method method = opMetaData.getJavaMethod();
      Class[] targetParameterTypes = method.getParameterTypes();

      if (opMetaData.isDocumentWrapped())
      {
         // Unwrap the request parameters
         if (paramMetaData.isInHeader() == false)
         {
            retValue = ParameterWrapping.unwrapRequestParameters(opMetaData, paramValue);
         }
         else
         {
            if (paramMetaData.getMode() == ParameterMode.INOUT || paramMetaData.getMode() == ParameterMode.OUT)
            {
               Class javaType = paramMetaData.getJavaType();
               for (int i=0; i < targetParameterTypes.length; i++)
               {
                  Class targetType = targetParameterTypes[i];
                  if (HolderUtils.isHolderType(targetType))
                  {
                     Class valueType = HolderUtils.getValueType(targetType);
                     if (JavaUtils.isAssignableFrom(valueType, javaType))
                     {
                        Holder holder = HolderUtils.getHolderInstance(targetType);
                        HolderUtils.setHolderValue(holder, paramValue);
                        retValue = holder;
                        
                        QName xmlName = paramMetaData.getXmlName();
                        setResponseParamValue(xmlName, holder);
                     }
                  }
               }
            }
         }
      }
      else
      {
         // Replace INOUT and OUT parameters by their respective holder values
         int paramTypeIndex = opMetaData.getParameters().indexOf(paramMetaData);
         Class targetParameterType = targetParameterTypes[paramTypeIndex];

         if (paramMetaData.getMode() == ParameterMode.INOUT || paramMetaData.getMode() == ParameterMode.OUT)
         {
            Holder holder = HolderUtils.getHolderInstance(targetParameterType);
            HolderUtils.setHolderValue(holder, paramValue);
            retValue = holder;

            QName xmlName = paramMetaData.getXmlName();
            setResponseParamValue(xmlName, holder);
         }

         if (JavaUtils.isPrimitive(targetParameterType))
         {
            retValue = JavaUtils.getPrimitiveValue(paramValue);
         }

         if (retValue != null)
         {
            Class valueType = retValue.getClass();
            if (JavaUtils.isAssignableFrom(targetParameterType, valueType) == false)
               throw new WSException("Parameter " + targetParameterType.getName() + " is not assignable from: " + getTypeName(retValue));
         }
      }

      log.debug("syncEndpointInputParam: " + getTypeName(paramValue) + " -> " + getTypeName(retValue));
      return retValue;
   }

   private String getTypeName(Object value)
   {
      String valueType = (value != null ? value.getClass().getName() : null);
      return valueType;
   }
}
