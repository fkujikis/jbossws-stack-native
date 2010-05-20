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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;

import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.jaxrpc.encoding.JAXBDeserializerFactory;
import org.jboss.ws.jaxrpc.encoding.JAXBSerializerFactory;
import org.jboss.ws.metadata.EndpointMetaData;
import org.jboss.ws.metadata.OperationMetaData;
import org.jboss.ws.metadata.ParameterMetaData;
import org.jboss.ws.metadata.ServiceMetaData;
import org.jboss.ws.metadata.TypeMappingMetaData;
import org.jboss.ws.metadata.TypesMetaData;
import org.jboss.ws.utils.JavaUtils;

/** A helper class to wrap/unwrap ducument style request/response structures.
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @since 06-Jun-2005
 */
public class ParameterWrapping
{
   // provide logging
   private static Logger log = Logger.getLogger(ParameterWrapping.class);

   private static void assertOperationMetaData(OperationMetaData opMetaData)
   {
      if (opMetaData.getStyle() != Style.DOCUMENT)
         throw new WSException("Unexpected style: " + opMetaData.getStyle());

      if (opMetaData.getParameterStyle() != ParameterStyle.WRAPPED)
         throw new WSException("Unexpected parameter style: " + opMetaData.getParameterStyle());
   }

   public static boolean matchRequestParameters(OperationMetaData opMetaData, Class[] paramTypes)
   {
      assertOperationMetaData(opMetaData);

      // [JBWS-1125] Support empty soap body elements
      if (opMetaData.getParameters().size() == 0)
      {
         log.debug("Detected document/literal/wrapped with no parameter part");
         return true;
      }
      
      ParameterMetaData paramMetaData = opMetaData.getParameters().get(0);
      List<String> varNames = paramMetaData.getWrappedVariables();
      Class reqStructType = paramMetaData.getJavaType();

      log.debug("matchRequestParameters: " + reqStructType.getName());
      try
      {
         boolean pass = true;
         for (int i = 0; pass && i < varNames.size(); i++)
         {
            String varName = varNames.get(i);
            Method method = null;
            try
            {
               PropertyDescriptor pd = new PropertyDescriptor(varName, reqStructType);
               method = pd.getWriteMethod();
            }
            catch (IntrospectionException ex)
            {
               // jaxws-ri wsimport generates setter without the underscore
               if (varName.indexOf("_") > 0)
               {
                  varName = varName.replace("_", "");
                  PropertyDescriptor pd = new PropertyDescriptor(varName, reqStructType);
                  method = pd.getWriteMethod();
               }
            }

            if (method == null)
               throw new NoSuchMethodError("No write method for: " + varName);

            Class methodType = method.getParameterTypes()[0];
            Class paramType = paramTypes[i];
            pass = JavaUtils.isAssignableFrom(methodType, paramType);
         }
         return pass;
      }
      catch (Exception ex)
      {
         log.debug("Invalid request wrapper: " + ex);
         return false;
      }
   }

   public static boolean matchResponseParameters(OperationMetaData opMetaData, Class returnType)
   {
      assertOperationMetaData(opMetaData);

      // [JBWS-1125] Support empty soap body elements
      if (opMetaData.getReturnParameter() == null)
      {
         log.debug("Detected document/literal/wrapped with no return part");
         return true;
      }
      
      ParameterMetaData paramMetaData = opMetaData.getReturnParameter();
      Class resStructType = paramMetaData.getJavaType();

      log.debug("matchResponseParameters: " + resStructType.getName());
      try
      {
         boolean pass = (returnType == void.class);
         if (pass == false)
         {
            try
            {
               resStructType.getConstructor(new Class[] { returnType });
               pass = true;
            }
            catch (NoSuchMethodException ex)
            {
               // jaxws-ri wsimport does not generate a ctor with return param type
               resStructType.getMethod("setResult", returnType);
               pass = true;
            }
         }
         return pass;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         log.debug("Invalid response wrapper: " + ex);
         return false;
      }
   }

   public static Object wrapRequestParameters(OperationMetaData opMetaData, Object[] inParams)
   {
      assertOperationMetaData(opMetaData);

      // [JBWS-1125] Support empty soap body elements
      if (opMetaData.getParameters().size() == 0)
      {
         log.debug("Detected document/literal/wrapped with no parameter part");
         return null;
      }
      
      ParameterMetaData paramMetaData = opMetaData.getParameters().get(0);
      List<String> varNames = paramMetaData.getWrappedVariables();
      Class reqStructType = paramMetaData.getJavaType();

      log.debug("wrapRequestParameters: " + reqStructType.getName());
      try
      {
         Object reqStruct = reqStructType.newInstance();
         for (int i = 0; i < varNames.size(); i++)
         {
            String varName = varNames.get(i);
            Method method = null;
            try
            {
               PropertyDescriptor pd = new PropertyDescriptor(varName, reqStructType);
               method = pd.getWriteMethod();
            }
            catch (IntrospectionException ex)
            {
               // jaxws-ri wsimport generates setter without the underscore
               if (varName.indexOf("_") > 0)
               {
                  varName = varName.replace("_", "");
                  PropertyDescriptor pd = new PropertyDescriptor(varName, reqStructType);
                  method = pd.getWriteMethod();
               }
            }

            if (method == null)
               throw new NoSuchMethodError("No write method for: " + varName);

            Object value = inParams[i];
            log.debug(" " + method.getName() + ": " + (value != null ? value.getClass().getName() : null));
            method.invoke(reqStruct, new Object[] { value });
         }

         return reqStruct;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception e)
      {
         throw new WSException("Cannot wrap request structure: " + e);
      }
   }

   public static Object[] unwrapRequestParameters(OperationMetaData opMetaData, Object reqStruct)
   {
      assertOperationMetaData(opMetaData);

      if (reqStruct == null)
         throw new IllegalArgumentException("Request struct cannot be null");

      ParameterMetaData paramMetaData = opMetaData.getParameters().get(0);
      List<String> varNames = paramMetaData.getWrappedVariables();
      Class reqStructType = reqStruct.getClass();

      log.debug("unwrapRequestParameters: " + reqStructType.getName());
      Object[] inParams = new Object[varNames.size()];
      try
      {
         for (int i = 0; i < varNames.size(); i++)
         {
            String varName = varNames.get(i);
            Method method = null;
            try
            {
               PropertyDescriptor pd = new PropertyDescriptor(varName, reqStructType);
               method = pd.getReadMethod();
            }
            catch (IntrospectionException ex)
            {
               // jaxws-ri wsimport generates getter without the underscore
               if (varName.indexOf("_") > 0)
               {
                  varName = varName.replace("_", "");
                  PropertyDescriptor pd = new PropertyDescriptor(varName, reqStructType);
                  method = pd.getReadMethod();
               }
            }

            if (method == null)
               throw new NoSuchMethodError("No read method for: " + varName);

            Object value = method.invoke(reqStruct, new Object[] {});
            log.debug(" " + method.getName() + ": " + (value != null ? value.getClass().getName() : null));
            inParams[i] = value;
         }
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException("Cannot unwrap request structure: " + e);
      }

      return inParams;
   }

   public static Object wrapResponseParameter(OperationMetaData opMetaData, Object outParam)
   {
      assertOperationMetaData(opMetaData);

      // [JBWS-1125] Support empty soap body elements
      if (opMetaData.getReturnParameter() == null)
      {
         log.debug("Detected document/literal/wrapped with no return part");
         return null;
      }
      
      ParameterMetaData paramMetaData = opMetaData.getReturnParameter();
      List<String> varNames = paramMetaData.getWrappedVariables();
      Class resStructType = paramMetaData.getJavaType();

      if (outParam != null && outParam.getClass() == resStructType)
      {
         log.debug("Response parameter already wrapped" + resStructType.getName());
         return outParam;
      }

      log.debug("wrapResponseParameter: " + resStructType.getName());
      try
      {
         Object resStruct = resStructType.newInstance();
         if (varNames.size() > 0)
         {
            String varName = varNames.get(0);
            Method method = null;
            try
            {
               PropertyDescriptor pd = new PropertyDescriptor(varName, resStructType);
               method = pd.getWriteMethod();
            }
            catch (IntrospectionException ex)
            {
               // jaxws-ri wsimport generates setter without the underscore
               if (varName.indexOf("_") > 0)
               {
                  varName = varName.replace("_", "");
                  PropertyDescriptor pd = new PropertyDescriptor(varName, resStructType);
                  method = pd.getWriteMethod();
               }
            }

            if (method == null)
               throw new NoSuchMethodError("No write method for: " + varName);

            Object value = outParam;
            log.debug(" " + method.getName() + ": " + (value != null ? value.getClass().getName() : null));
            method.invoke(resStruct, new Object[] { value });
         }
         return resStruct;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception e)
      {
         throw new WSException("Cannot wrap response structure: " + e);
      }
   }

   public static Object unwrapResponseParameter(OperationMetaData opMetaData, Object resStruct)
   {
      assertOperationMetaData(opMetaData);

      Object retValue = null;
      if (resStruct != null)
      {
         ParameterMetaData paramMetaData = opMetaData.getReturnParameter();
         List<String> varNames = paramMetaData.getWrappedVariables();
         Class resStructType = resStruct.getClass();

         log.debug("unwrapResponseParameter: " + resStructType.getName());
         try
         {
            if (varNames.size() > 0)
            {
               String varName = varNames.get(0);
               Method method = null;
               try
               {
                  PropertyDescriptor pd = new PropertyDescriptor(varName, resStructType);
                  method = pd.getReadMethod();
               }
               catch (IntrospectionException ex)
               {
                  // jaxws-ri wsimport generates getter without the underscore
                  if (varName.indexOf("_") > 0)
                  {
                     varName = varName.replace("_", "");
                     PropertyDescriptor pd = new PropertyDescriptor(varName, resStructType);
                     method = pd.getReadMethod();
                  }
               }

               if (method == null)
                  throw new NoSuchMethodError("No read method for: " + varName);

               Object value = method.invoke(resStruct, new Object[] {});
               log.debug(" " + method.getName() + ": " + (value != null ? value.getClass().getName() : null));
               retValue = value;
            }
         }
         catch (RuntimeException rte)
         {
            throw rte;
         }
         catch (Exception e)
         {
            throw new IllegalArgumentException("Cannot unwrap request structure: " + e);
         }
      }
      return retValue;
   }

   /**
    * This is just a dummy marker class used to identify a generated
    * document/literal wrapped type
    *
    * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
    * @version $Revision$
    */
   public static class WrapperType
   {
   }

   /**
    * Generates a wrapper type and assigns it to the passed ParameterMetaData
    * object. This routine requires the pmd to contain completed wrappedTypes
    * and wrappedVariables properties of the passed ParameterMetaData object.
    *
    * @param pmd a document/literal wrapped parameter
    */
   public static void generateWrapper(ParameterMetaData pmd, boolean addTypeMapping)
   {
      List<String> wrappedTypes = pmd.getWrappedTypes();
      List<String> wrappedVariables = pmd.getWrappedVariables();
      OperationMetaData operationMetaData = pmd.getOperationMetaData();
      EndpointMetaData endpointMetaData = operationMetaData.getEndpointMetaData();
      ServiceMetaData serviceMetaData = endpointMetaData.getServiceMetaData();
      ClassLoader loader = serviceMetaData.getUnifiedMetaData().getClassLoader();

      if (operationMetaData.isDocumentWrapped() == false)
         throw new WSException("Operation is not document/literal (wrapped)");

      if (wrappedTypes == null)
         throw new WSException("Cannot generate a type when their is no type information");

      String serviceName = serviceMetaData.getName().getLocalPart();
      String parameterName = pmd.getXmlName().getLocalPart();
      String endpointName = endpointMetaData.getName().getLocalPart();
      String packageName = endpointMetaData.getServiceEndpointInterface().getPackage().getName();

      String wrapperName = packageName + ".__JBossWS_" + serviceName + "_" + endpointName + "_" + parameterName;
      if (log.isDebugEnabled())
         log.debug("Generating wrapper: " + wrapperName);
      Class wrapperType;

      try
      {
         ClassPool pool = new ClassPool(true);
         pool.appendClassPath(new LoaderClassPath(loader));
         CtClass clazz = pool.makeClass(wrapperName);
         clazz.setSuperclass(pool.get(WrapperType.class.getName()));

         for (int i = 0; i < wrappedTypes.size(); i++)
         {
            String typeName = wrappedTypes.get(i);
            String name = wrappedVariables.get(i);

            CtField field = new CtField(pool.get(typeName), name, clazz);
            field.setModifiers(Modifier.PRIVATE);
            clazz.addField(field);
            clazz.addMethod(CtNewMethod.getter("get" + capitalize(name), field));
            clazz.addMethod(CtNewMethod.setter("set" + capitalize(name), field));
         }

         wrapperType = (Class) pool.toClass(clazz, loader);
      }
      catch (Exception e)
      {
         throw new WSException("Could not generate wrapper type: " + wrapperName, e);
      }

      // Register type mapping if needed
      if (addTypeMapping)
      {
         QName xmlType = pmd.getXmlType();

         TypesMetaData typesMetaData = serviceMetaData.getTypesMetaData();
         TypeMappingMetaData tmMetaData = new TypeMappingMetaData(typesMetaData, xmlType, wrapperName);
         typesMetaData.addTypeMapping(tmMetaData);

         TypeMappingImpl typeMapping = serviceMetaData.getTypeMapping();
         typeMapping.register(wrapperType, xmlType, new JAXBSerializerFactory(), new JAXBDeserializerFactory());
      }

      pmd.setJavaTypeName(wrapperName);
   }

   private static String capitalize(String source)
   {
      if (source == null)
         return null;

      if (source.length() == 0)
         return source;

      char c = Character.toUpperCase(source.charAt(0));

      return c + source.substring(1);
   }
}