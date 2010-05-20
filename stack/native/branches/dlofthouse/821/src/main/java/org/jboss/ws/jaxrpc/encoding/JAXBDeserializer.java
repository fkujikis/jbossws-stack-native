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
package org.jboss.ws.jaxrpc.encoding;

// $Id$

import java.io.ByteArrayInputStream;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSModel;
import org.jboss.logging.Logger;
import org.jboss.ws.binding.BindingException;
import org.jboss.ws.jaxb.JAXBConstants;
import org.jboss.ws.jaxb.JBossXBUnmarshallerImpl;
import org.jboss.ws.jaxb.JAXBUnmarshaller;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;

/**
 * A Deserializer that can handle complex types by delegating to JAXB.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 04-Dec-2004
 */
public class JAXBDeserializer extends DeserializerSupport
{
   // provide logging
   private static final Logger log = Logger.getLogger(JAXBDeserializer.class);

   private JAXBUnmarshaller unmarshaller;

   public JAXBDeserializer() throws BindingException
   {
      // Get the JAXB marshaller for complex objects
      unmarshaller = new JBossXBUnmarshallerImpl();
   }

   /**
    * For unmarshalling the WS layer passes to the JAXB layer
    *
    *    - required self contained xml content
    *    - required map of packaged or generated XSDSchema
    *    - optional QName of the root complex type
    *    - optional instance of JavaWsdlMapping
    *
    * The xmlType is redundant if the root element name corresponds to a global element definition in schema.
    * If the java mapping is null, default mapping rules apply.
    *
    * The result is an object instance or null.
    * In case of an unmarshalling problem a descriptive exception is thrown.
    */
   public Object deserialize(QName xmlName, QName xmlType, String val, SerializationContextImpl serContext) throws BindingException
   {
      log.debug("deserialize: [xmlName=" + xmlName + ",xmlType=" + xmlType + "]");

      Object value = null;
      String typeName = xmlType.getLocalPart();

         try
         {
            // Get the parsed model
            XSModel model = serContext.getXsModel();

            // Get the jaxrpc-mapping.xml meta data
            JavaWsdlMapping jaxrpcMapping = serContext.getJavaWsdlMapping();

            unmarshaller.setProperty(JAXBConstants.JAXB_XS_MODEL, model);
            unmarshaller.setProperty(JAXBConstants.JAXB_ROOT_QNAME, xmlName);
            unmarshaller.setProperty(JAXBConstants.JAXB_TYPE_QNAME, xmlType);
            unmarshaller.setProperty(JAXBConstants.JAXB_JAVA_MAPPING, jaxrpcMapping);

            ByteArrayInputStream ins = new ByteArrayInputStream(val.getBytes("UTF-8"));
            value = unmarshaller.unmarshal(ins);
         }
         catch (RuntimeException e)
         {
            throw e;
         }
         catch (Exception e)
         {
            throw new BindingException(e);
         }

      log.debug("deserialized: " + (value != null ? value.getClass().getName() : null));
      return value;

   }
}
