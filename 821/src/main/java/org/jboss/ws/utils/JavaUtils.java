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
package org.jboss.ws.utils;

// $Id$

import java.lang.reflect.Array;
import java.util.HashMap;

import org.jboss.logging.Logger;

/** Java utilities
 *
 * @author Thomas.Diesler@jboss.org
 * @since 22-Dec-2004
 */
public class JavaUtils
{
   // provide logging
   private static final Logger log = Logger.getLogger(JavaUtils.class);

   private static HashMap<String, Class> primitiveNames = new HashMap<String, Class>();

   static
   {
      primitiveNames.put("int", int.class);
      primitiveNames.put("short", short.class);
      primitiveNames.put("boolean", boolean.class);
      primitiveNames.put("byte", byte.class);
      primitiveNames.put("long", long.class);
      primitiveNames.put("double", double.class);
      primitiveNames.put("float", float.class);
      primitiveNames.put("char", char.class);
   }

   /**
    * Load a Java type from a given class loader.
    *
    * @param typeName maybe the source notation of a primitve, class name, array of both
    */
   public static Class loadJavaType(String typeName) throws ClassNotFoundException
   {
      return loadJavaType(typeName, null);
   }

   /**
    * Load a Java type from a given class loader.
    *
    * @param typeName maybe the source notation of a primitve, class name, array of both
    */
   public static Class loadJavaType(String typeName, ClassLoader classLoader) throws ClassNotFoundException
   {
      if (classLoader == null)
         classLoader = Thread.currentThread().getContextClassLoader();

      Class javaType = primitiveNames.get(typeName);
      if (javaType == null)
         javaType = getArray(typeName, classLoader);

      if (javaType == null)
         javaType = classLoader.loadClass(typeName);

      return javaType;
   }

   /**
    * True if the given type name is the source notation of a primitive or array of which.
    */
   public static boolean isPrimitive(String javaType)
   {
      return getPrimitiveType(javaType) != null;
   }

   /**
    * True if the given class is a primitive or array of which.
    */
   public static boolean isPrimitive(Class javaType)
   {
      return javaType.isPrimitive() || (javaType.isArray() && isPrimitive(javaType.getComponentType()));
   }

   public static Class getPrimitiveType(String javaType)
   {
      Class type = primitiveNames.get(javaType);
      if (type != null)
         return type;

      try
      {
         // null loader = primitive only
         type = getArray(javaType, null);
      }
      catch (ClassNotFoundException e)
      {
         // This will actually never be thrown since is null
      }

      return type;
   }

   private static Class getArray(String javaType, ClassLoader loader) throws ClassNotFoundException
   {
      if (javaType.charAt(0) == '[')
         return getArrayFromJVMName(javaType, loader);

      if (javaType.endsWith("[]"))
         return getArrayFromSourceName(javaType, loader);

      return null;
   }

   private static Class getArrayFromJVMName(String javaType, ClassLoader loader) throws ClassNotFoundException
   {
      Class componentType;
      int componentStart = javaType.lastIndexOf('[') + 1;
      switch (javaType.charAt(componentStart))
      {
         case 'I': componentType = int.class; break;
         case 'S': componentType = short.class; break;
         case 'Z': componentType = boolean.class; break;
         case 'B': componentType = byte.class; break;
         case 'J': componentType = long.class; break;
         case 'D': componentType = double.class; break;
         case 'F': componentType = float.class; break;
         case 'C': componentType = char.class; break;
         case 'L':
            if (loader == null)
               return null;
            String name = javaType.substring(componentStart + 1, javaType.length() - 1);
            componentType = loader.loadClass(name);
            break;
         default:
            throw new IllegalArgumentException("Invalid binary component for array: " + javaType.charAt(componentStart));
      }

      // componentStart doubles as the number of '['s which is the number of dimensions
      return Array.newInstance(componentType, new int[componentStart]).getClass();
   }

   private static Class getArrayFromSourceName(String javaType, ClassLoader loader) throws ClassNotFoundException
   {
      int arrayStart = javaType.indexOf('[');
      String componentName = javaType.substring(0, arrayStart);

      Class componentType = primitiveNames.get(componentName);
      if (componentType == null)
      {
         if (loader == null)
            return null;

         componentType = loader.loadClass(componentName);
      }

      // [][][][] divided by 2
      int dimensions = (javaType.length() - arrayStart) >> 1;

      return Array.newInstance(componentType, new int[dimensions]).getClass();
   }

   /**
    * Get the corresponding primitive for a give wrapper type.
    * Also handles arrays of which.
    */
   public static Class getPrimitiveType(Class javaType)
   {
      if (javaType == Integer.class)
         return int.class;
      if (javaType == Short.class)
         return short.class;
      if (javaType == Boolean.class)
         return boolean.class;
      if (javaType == Byte.class)
         return byte.class;
      if (javaType == Long.class)
         return long.class;
      if (javaType == Double.class)
         return double.class;
      if (javaType == Float.class)
         return float.class;
      if (javaType == Character.class)
         return char.class;

      if (javaType == Integer[].class)
         return int[].class;
      if (javaType == Short[].class)
         return short[].class;
      if (javaType == Boolean[].class)
         return boolean[].class;
      if (javaType == Byte[].class)
         return byte[].class;
      if (javaType == Long[].class)
         return long[].class;
      if (javaType == Double[].class)
         return double[].class;
      if (javaType == Float[].class)
         return float[].class;
      if (javaType == Character[].class)
         return char[].class;

      if (javaType.isArray() && javaType.getComponentType().isArray())
      {
         Class compType = getPrimitiveType(javaType.getComponentType());
         return Array.newInstance(compType, 0).getClass();
      }

      return javaType;
   }

   /**
    * Get the corresponding primitive value for a give wrapper value.
    * Also handles arrays of which.
    */
   public static Object getPrimitiveValue(Object value)
   {
      if (value == null)
         return null;

      Class javaType = value.getClass();
      if (javaType == Integer.class)
         return ((Integer)value).intValue();
      if (javaType == Short.class)
         return ((Short)value).shortValue();
      if (javaType == Boolean.class)
         return ((Boolean)value).booleanValue();
      if (javaType == Byte.class)
         return ((Byte)value).byteValue();
      if (javaType == Long.class)
         return ((Long)value).longValue();
      if (javaType == Double.class)
         return ((Double)value).doubleValue();
      if (javaType == Float.class)
         return ((Float)value).floatValue();

      if (javaType.isArray())
      {
         int length = Array.getLength(value);
         Object destArr = Array.newInstance(getPrimitiveType(javaType.getComponentType()), length);
         for (int i = 0; i < length; i++)
         {
            Object srcObj = Array.get(value, i);
            Object destObj = getPrimitiveValue(srcObj);
            Array.set(destArr, i, destObj);
         }
         return destArr;
      }

      return value;
   }

   /**
    * Get the corresponding wrapper type for a give primitive.
    * Also handles arrays of which.
    */
   public static Class getWrapperType(Class javaType)
   {
      if (javaType == int.class)
         return Integer.class;
      if (javaType == short.class)
         return Short.class;
      if (javaType == boolean.class)
         return Boolean.class;
      if (javaType == byte.class)
         return Byte.class;
      if (javaType == long.class)
         return Long.class;
      if (javaType == double.class)
         return Double.class;
      if (javaType == float.class)
         return Float.class;
      if (javaType == char.class)
         return Character.class;

      if (javaType == int[].class)
         return Integer[].class;
      if (javaType == short[].class)
         return Short[].class;
      if (javaType == boolean[].class)
         return Boolean[].class;
      if (javaType == byte[].class)
         return Byte[].class;
      if (javaType == long[].class)
         return Long[].class;
      if (javaType == double[].class)
         return Double[].class;
      if (javaType == float[].class)
         return Float[].class;
      if (javaType == char[].class)
         return Character[].class;

      if (javaType.isArray() && javaType.getComponentType().isArray())
      {
         Class compType = getWrapperType(javaType.getComponentType());
         return Array.newInstance(compType, 0).getClass();
      }

      return javaType;
   }

   /**
    * Get the corresponding wrapper value for a give primitive value.
    * Also handles arrays of which.
    */
   public static Object getWrapperValue(Object value)
   {
      if (value == null)
         return null;

      Class javaType = value.getClass();
      if (javaType == int.class)
         return Integer.valueOf("" + value);
      if (javaType == short.class)
         return Short.valueOf("" + value);
      if (javaType == boolean.class)
         return Boolean.valueOf("" + value);
      if (javaType == byte.class)
         return Byte.valueOf("" + value);
      if (javaType == long.class)
         return Long.valueOf("" + value);
      if (javaType == double.class)
         return Double.valueOf("" + value);
      if (javaType == float.class)
         return Float.valueOf("" + value);

      if (javaType.isArray())
      {
         int length = Array.getLength(value);
         Object destArr = Array.newInstance(getWrapperType(javaType.getComponentType()), length);
         for (int i = 0; i < length; i++)
         {
            Object srcObj = Array.get(value, i);
            Object destObj = getWrapperValue(srcObj);
            Array.set(destArr, i, destObj);
         }
         return destArr;
      }

      return value;
   }

   /**
    * Return true if the dest class is assignable from the src.
    * Also handles arrays and primitives.
    */
   public static boolean isAssignableFrom(Class dest, Class src)
   {
      if (dest == null)
         throw new IllegalArgumentException("Destination class cannot be null");
      if (src == null)
         throw new IllegalArgumentException("Source class cannot be null");

      boolean isAssignable = dest.isAssignableFrom(src);
      if (isAssignable == false && dest.getName().equals(src.getName()))
      {
         ClassLoader destLoader = dest.getClassLoader();
         ClassLoader srcLoader = src.getClassLoader();
         log.debug("Not assignable because of conflicting class loaders:\ndstLoader=" + destLoader + "\nsrcLoader=" + srcLoader);
      }

      if (isAssignable == false && isPrimitive(dest))
      {
         dest = getWrapperType(dest);
         isAssignable = dest.isAssignableFrom(src);
      }
      if (isAssignable == false && isPrimitive(src))
      {
         src = getWrapperType(src);
         isAssignable = dest.isAssignableFrom(src);
      }
      return isAssignable;
   }

   public static String convertJVMNameToSourceName(String typeName, ClassLoader loader)
   {
      // TODO Don't use a ClassLoader for this, we need to just convert it
      try
      {
         Class javaType = loadJavaType(typeName, loader);
         typeName = getSourceName(javaType);
      }
      catch (Exception e)
      {
      }

      return typeName;
   }

   public static String getSourceName(Class type)
   {
      if (! type.isArray())
         return type.getName();

      String arrayNotation = "";
      Class component = type;
      while(component.isArray())
      {
         component = component.getComponentType();
         arrayNotation += "[]";
      }

      return component.getName() + arrayNotation;
   }
}
