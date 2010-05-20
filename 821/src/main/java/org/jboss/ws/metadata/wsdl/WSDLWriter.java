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
package org.jboss.ws.metadata.wsdl;

// $Id$

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.util.xml.DOMUtils;
import org.jboss.util.xml.DOMWriter;
import org.jboss.ws.Constants;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.w3c.dom.Element;

/**
 * A helper that writes out a WSDL definition
 *
 * @author Thomas.Diesler@jboss.org
 * @author Anil.Saldhana@jboss.org
 * @since 10-Oct-2004
 */
public abstract class WSDLWriter
{
   // provide logging
   protected static final Logger log = Logger.getLogger(WSDLWriter.class);

   protected WSDLDefinitions wsdl;

   protected WSDLUtils utils = WSDLUtils.getInstance();

   /**
    * Include or import WSDL Types
    */
   protected boolean includeSchemaInWSDL = true;

   /** Use WSDLDefinitions.writeWSDL instead. */
   protected WSDLWriter(WSDLDefinitions wsdl)
   {
      if (wsdl == null)
         throw new IllegalArgumentException("WSDL definitions is NULL");

      this.wsdl = wsdl;
   }

   public void write(Writer writer) throws IOException
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append(Constants.XML_HEADER);

      appendDefinitions(buffer);
      appendTypes(buffer);
      appendInterfaces(buffer);
      appendBindings(buffer);
      appendServices(buffer);

      buffer.append("</definitions>");

      Element element = DOMUtils.parse(buffer.toString());
      new DOMWriter(writer).setPrettyprint(true).print(element);
   }

   protected void appendDefinitions(StringBuilder buffer)
   {
      buffer.append("<definitions");
      //Append service name as done by wscompile, if there is just one
      WSDLService[] services = wsdl.getServices();
      if (services != null && services.length == 1)
      {
         WSDLService ser = services[0];
         buffer.append(" name='" + ser.getName() + "'");
      }
      buffer.append(" targetNamespace='" + wsdl.getTargetNamespace() + "'");
      buffer.append(" xmlns='" + wsdl.getWsdlNamespace() + "'");
      Iterator it = wsdl.getRegisteredNamespaceURIs();
      while (it.hasNext())
      {
         String namespaceURI = (String)it.next();
         String prefix = wsdl.getPrefix(namespaceURI);
         if (prefix.length() > 0)
            buffer.append(" xmlns:" + prefix + "='" + namespaceURI + "'");
      }
      buffer.append(">");
   }

   protected void appendTypes(StringBuilder buffer)
   {
      buffer.append("<types>");
      JBossXSModel xsM = wsdl.getWsdlTypes().getSchemaModel();
      String schema = xsM.serialize();
      buffer.append(schema);
      buffer.append("</types>");
   }

   protected abstract void appendInterfaces(StringBuilder buffer);

   protected abstract void appendBindings(StringBuilder buffer);

   protected abstract void appendServices(StringBuilder buffer);

   /** Get a prefixed name of form prefix:localPart */
   protected String getQNameRef(QName qname)
   {
      String retStr = qname.getLocalPart();

      String prefix = qname.getPrefix();
      String nsURI = qname.getNamespaceURI();
      if (prefix.length() == 0 && nsURI.length() > 0)
      {
         qname = wsdl.registerQName(qname);
         prefix = qname.getPrefix();
      }

      if (prefix.length() > 0)
         retStr = prefix + ":" + retStr;

      return retStr;
   }

   public WSDLDefinitions getWsdl()
   {
      return wsdl;
   }

   public void setWsdl(WSDLDefinitions wsdl)
   {
      this.wsdl = wsdl;
   }

   public boolean isIncludeTypesInWSDL()
   {
      return includeSchemaInWSDL;
   }

   public void logException(Exception e)
   {
      if (log.isTraceEnabled())
      {
         log.trace(e);
      }
   }

   public void logMessage(String msg)
   {
      if (log.isTraceEnabled())
      {
         log.trace(msg);
      }
   }
}
