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

import javax.xml.namespace.QName;

import org.jboss.ws.WSException;
import org.jboss.ws.jaxrpc.ParameterWrapping;
import org.jboss.ws.utils.JavaUtils;

/**
 * A Fault component describes a fault that a given operation supports.
 *
 * @author Thomas.Diesler@jboss.org
 * @author jason.greene@jboss.com
 * @since 12-May-2005
 */
public class FaultMetaData
{
   // The parent operation
   private OperationMetaData operation;

   private QName xmlName;
   private QName xmlType;
   private String javaTypeName;
   private Class javaType;

   public FaultMetaData(OperationMetaData operation, QName xmlName, QName xmlType, String javaTypeName)
   {
      if (xmlName == null)
         throw new IllegalArgumentException("Invalid null xmlName argument");
      if (xmlType == null)
         throw new IllegalArgumentException("Invalid null xmlType argument, for: " + xmlName);
      if (javaTypeName == null)
         throw new IllegalArgumentException("Invalid null javaTypeName argument, for: " + xmlName);

      this.operation = operation;
      this.xmlName = xmlName;
      this.xmlType = xmlType;
      this.javaTypeName = javaTypeName;
   }

   public OperationMetaData getOperationMetaData()
   {
      return operation;
   }

   public QName getXmlName()
   {
      return xmlName;
   }

   public QName getXmlType()
   {
      return xmlType;
   }

   public String getJavaTypeName()
   {
      return javaTypeName;
   }

   public Class getJavaType()
   {
      // If the class loader has changed, make sure we reload the class
      ClassLoader loader = operation.getEndpointMetaData().getServiceMetaData().getUnifiedMetaData().getClassLoader();
      if (loader == null)
         throw new WSException("ClassLoader not available");

      if (javaTypeName == null)
         return null;

      if (javaType == null || ((!javaType.isPrimitive()) && javaType.getClassLoader() != loader))
      {
         try
         {
            javaType = JavaUtils.loadJavaType(javaTypeName, loader);
         }
         catch (ClassNotFoundException ex)
         {
            throw new WSException("Cannot load java type: " + javaTypeName, ex);
         }
      }

      return javaType;
   }

   public void eagerInitialize()
   {
      // Initialize the cache
      getJavaType();
   }

   public String toString()
   {
      StringBuilder buffer = new StringBuilder("\nFaultMetaData");
      buffer.append("\n xmlName=" + xmlName);
      buffer.append("\n xmlType=" + xmlType);
      buffer.append("\n javaType=" + javaTypeName);
      return buffer.toString();
   }
}