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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.jaxrpc.ParameterWrapping;
import org.jboss.ws.utils.JavaUtils;

/**
 * A request/response parameter that a given operation supports.
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @since 12-May-2005
 */
public class ParameterMetaData
{
   // The parent operation
   private OperationMetaData opMetaData;

   private QName xmlName;
   private QName xmlType;
   private String javaTypeName;
   private Class javaType;
   private ParameterMode mode;
   private Set<String> mimeTypes;
   private boolean inHeader;
   private boolean isSwA;
   private boolean isXOP;
   private List<String> wrappedVariables;
   private List<String> wrappedTypes;
   private List<QName> wrappedElementNames;

   // SOAP-ENC:Array
   private boolean soapArrayParam;
   private QName soapArrayCompType;

   public ParameterMetaData(OperationMetaData opMetaData, QName xmlName, QName xmlType, String javaTypeName)
   {
      this.xmlName = xmlName;
      this.xmlType = xmlType;
      this.opMetaData = opMetaData;
      this.mode = ParameterMode.IN;
      this.javaTypeName = javaTypeName;

      if (xmlName == null)
         throw new IllegalArgumentException("Invalid null xmlName argument");
      if (xmlType == null)
         throw new IllegalArgumentException("Invalid null xmlType argument, for: " + xmlName);

      // Remove the prefixes
      if (xmlName.getNamespaceURI().length() > 0)
         this.xmlName = new QName(xmlName.getNamespaceURI(), xmlName.getLocalPart());

      if (xmlType.getNamespaceURI().length() > 0)
         this.xmlType = new QName(xmlType.getNamespaceURI(), xmlType.getLocalPart());

      // Special case to identify attachments
      if (Constants.NS_ATTACHMENT_MIME_TYPE.equals(xmlType.getNamespaceURI()))
      {
         String mimeType = convertXmlTypeToMimeType(xmlType);
         setMimeTypes(mimeType);
         this.isSwA = true;
      }
   }

   public OperationMetaData getOperationMetaData()
   {
      return opMetaData;
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


   public void setJavaTypeName(String javaTypeName)
   {
      this.javaTypeName = javaTypeName;
   }

   public Class getJavaType()
   {
      // If the class loader has changed, make sure we reload the class
      ClassLoader loader = opMetaData.getEndpointMetaData().getServiceMetaData().getUnifiedMetaData().getClassLoader();
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


   public ParameterMode getMode()
   {
      return mode;
   }

   public void setMode(String mode)
   {
      if("IN".equals(mode))
         setMode(ParameterMode.IN);
      else if("INOUT".equals(mode))
         setMode(ParameterMode.INOUT);
      else if("OUT".equals(mode))
         setMode(ParameterMode.OUT);
      else
         throw new IllegalArgumentException("Invalid mode: " + mode);
   }
   
   public void setMode(ParameterMode mode)
   {
      this.mode = mode;
   }

   public Set<String> getMimeTypes()
   {
      return mimeTypes;
   }

   public void setMimeTypes(String mimeStr)
   {
      mimeTypes = new HashSet<String>();
      StringTokenizer st = new StringTokenizer(mimeStr, ",");
      while (st.hasMoreTokens())
         mimeTypes.add(st.nextToken().trim());
   }

   public boolean isInHeader()
   {
      return inHeader;
   }

   public void setInHeader(boolean inHeader)
   {
      this.inHeader = inHeader;
   }

   public boolean isSwA()
   {
      return isSwA;
   }

   public void setSwA(boolean isSwA)
   {
      this.isSwA = isSwA;
   }

   public boolean isXOP()
   {
      return isXOP;
   }

   public void setXOP(boolean isXOP)
   {
      this.isXOP = isXOP;
   }

   /** Get the list of wrapped variables, if this is a document wrapping parameter */
   public List<String> getWrappedVariables()
   {
      return wrappedVariables;
   }

   public void setWrappedVariables(List<String> wrappedVariables)
   {
      this.wrappedVariables = wrappedVariables;
   }

   public List<QName> getWrappedElementNames()
   {
      return wrappedElementNames;
   }

   public void setWrappedElementNames(List<QName> wrappedElementNames)
   {
      this.wrappedElementNames = wrappedElementNames;
   }

   public boolean isSOAPArrayParam()
   {
      return soapArrayParam;
   }

   public void setSOAPArrayParam(boolean soapArrayParam)
   {
      this.soapArrayParam = soapArrayParam;
   }

   public QName getSOAPArrayCompType()
   {
      return soapArrayCompType;
   }

   public void setSOAPArrayCompType(QName xmlType)
   {
      this.soapArrayCompType = xmlType;
   }

   /** Converts a proprietary JBossWS attachment xml type to the MIME type that it represents.
    */
   private String convertXmlTypeToMimeType(QName xmlType)
   {
      StringBuilder mimeName = new StringBuilder(xmlType.getLocalPart());
      int pos = mimeName.indexOf("_");
      if (pos == -1)
         throw new IllegalArgumentException("Invalid mime type: " + xmlType);

      mimeName.setCharAt(pos, '/');
      return mimeName.toString();
   }


   public List<String> getWrappedTypes()
   {
      return wrappedTypes;
   }

   public void setWrappedTypes(List<String> wrappedTypes)
   {
      this.wrappedTypes = wrappedTypes;
   }

   /**
    * @see UnifiedMetaData#eagerInitialize()
    */
   public void eagerInitialize()
   {
      TypesMetaData typesMetaData = getOperationMetaData().getEndpointMetaData().getServiceMetaData().getTypesMetaData();
      if (getOperationMetaData().isDocumentWrapped() && typesMetaData.getTypeMappingByXMLType(xmlType) == null)
      {
         ParameterWrapping.generateWrapper(this, true);
      }

      // Initialize the cache
      getJavaType();
   }

   public String toString()
   {
      boolean isReturn = (opMetaData.getReturnParameter() == this);
      StringBuilder buffer = new StringBuilder("\n" + (isReturn ? "ReturnMetaData:" : "ParameterMetaData:"));
      buffer.append("\n xmlName=" + getXmlName());
      buffer.append("\n xmlType=" + getXmlType());
      buffer.append("\n javaType=" + getJavaTypeName());
      buffer.append("\n mode=" + getMode());
      buffer.append("\n inHeader=" + isInHeader());
      
      if (soapArrayParam)
         buffer.append("\n soapArrayCompType=" + soapArrayCompType);
      
      if (wrappedVariables != null)
         buffer.append("\n wrappedVariables=" + wrappedVariables);
      
      if (wrappedTypes != null)
         buffer.append("\n wrappedTypes=" + wrappedTypes);
      
      if (wrappedElementNames != null)
         buffer.append("\n wrappedElementNames=" + wrappedElementNames);
      
      if (isSwA())
      {
         buffer.append("\n isSwA=" + isSwA());
         buffer.append("\n mimeTypes=" + getMimeTypes());
      }
      
      if (isXOP())
      {
         buffer.append("\n isXOP=" + isXOP());
         buffer.append("\n mimeTypes=" + getMimeTypes());
      }
      
      return buffer.toString();
   }
}