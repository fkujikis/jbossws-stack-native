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
package org.jboss.ws.metadata.j2ee;

//$Id: UnifiedHandlerMetaData.java 314 2006-05-11 10:57:59Z thomas.diesler@jboss.com $

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.namespace.QName;

/**
 * The container independent metdata data for a handler element
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class UnifiedHandlerMetaData implements Serializable
{
   private static final long serialVersionUID = 8000854586742278995L;

   public enum HandlerType {PRE, JAXRPC, POST, ALL};
   
   // The required <handler-name> element
   private String handlerName;
   // The required <handler-class> element
   private String handlerClass;
   // The optional <init-param> elements
   private ArrayList<UnifiedInitParamMetaData> initParams = new ArrayList<UnifiedInitParamMetaData>();
   // The optional <soap-header> elements
   private ArrayList<QName> soapHeaders = new ArrayList<QName>();
   // The optional <soap-role> elements
   private ArrayList<String> soapRoles = new ArrayList<String>();
   // The optional <port-name> elements, these only apply to webserve clients
   private ArrayList<String> portNames = new ArrayList<String>();

   public UnifiedHandlerMetaData()
   {
   }

   public void setHandlerName(String value)
   {
      this.handlerName = value;
   }

   public String getHandlerName()
   {
      return handlerName;
   }

   public void setHandlerClass(String handlerClass)
   {
      this.handlerClass = handlerClass;
   }

   public String getHandlerClass()
   {
      return handlerClass;
   }

   public void addInitParam(UnifiedInitParamMetaData param)
   {
      initParams.add(param);
   }

   public UnifiedInitParamMetaData[] getInitParams()
   {
      UnifiedInitParamMetaData[] array = new UnifiedInitParamMetaData[initParams.size()];
      initParams.toArray(array);
      return array;
   }

   public void addSoapHeader(QName qName)
   {
      soapHeaders.add(qName);
   }

   public QName[] getSoapHeaders()
   {
      QName[] array = new QName[soapHeaders.size()];
      soapHeaders.toArray(array);
      return array;
   }

   public void addSoapRole(String value)
   {
      soapRoles.add(value);
   }

   public String[] getSoapRoles()
   {
      String[] array = new String[soapRoles.size()];
      soapRoles.toArray(array);
      return array;
   }

   public void addPortName(String portName)
   {
      if(portName != null)
         portNames.add(portName);
   }

   public String[] getPortNames()
   {
      String[] array = new String[portNames.size()];
      portNames.toArray(array);
      return array;
   }

   public boolean matchesPort(String portName)
   {
      boolean matches = false;

      if(portNames.size()>0 && portNames.contains(portName))
         matches = true;

      return matches;
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer("\nHandlerMetaData:");
      buffer.append("\n name=" + handlerName);
      buffer.append("\n class=" + handlerClass);
      buffer.append("\n params=" + initParams);
      buffer.append("\n headers=" + soapHeaders);
      buffer.append("\n roles=" + soapRoles);
      buffer.append("\n ports=" + portNames);
      return buffer.toString();
   }
}
