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

import org.apache.xerces.xs.XSModel;
import org.jboss.logging.Logger;
import org.jboss.ws.binding.BindingException;
import org.jboss.ws.jaxb.JAXBConstants;
import org.jboss.ws.jaxb.JAXBMarshaller;
import org.jboss.ws.jaxb.JBossXBMarshallerImpl;
import org.jboss.ws.jaxb.XercesXSMarshallerImpl;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.w3c.dom.NamedNodeMap;

import javax.xml.namespace.QName;
import java.io.StringWriter;

/**
 * A Serializer that can handle complex types by delegating to JAXB.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 04-Dec-2004
 */
public class JAXBSerializer extends SerializerSupport
{
   // provide logging
   private static final Logger log = Logger.getLogger(JAXBSerializer.class);

   private JAXBMarshaller marshaller;

   public JAXBSerializer() throws BindingException
   {
      // Get the JAXB marshaller for complex objects
      marshaller = new JBossXBMarshallerImpl();
   }

   /**
    * For marshalling the WS layer passes to the JAXB layer
    *
    *    - optional java object instance
    *    - required map of packaged or generated XSDSchema
    *    - required QName of the root element
    *    - optional QName of the root complex type
    *    - optional instance of JavaWsdlMapping
    *
    * If the object value is null, the corresponding XML representation of the nillable element should be marshalled.
    * The xmlType is redundant if the xmlName corresponds to a global element definition in schema.
    * If the java mapping is null, default mapping rules apply.
    *
    * The result is a self contained (i.e. contains all namespace definitions) XML document without the XML declaration.
    * In case of an marshalling problem a descriptive exception is thrown.
    */
   public String serialize(QName xmlName, QName xmlType, Object value, SerializationContextImpl serContext, NamedNodeMap attributes) throws BindingException
   {
      log.debug("serialize: [xmlName=" + xmlName + ",xmlType=" + xmlType + "]");
      try
      {

         String xmlFragment = null;

         // Get the parsed model
         XSModel model = serContext.getXsModel();

         // Get the jaxrpc-mapping.xml object graph
         JavaWsdlMapping jaxrpcMapping = serContext.getJavaWsdlMapping();

         StringWriter strwr = new StringWriter();

         // schemabinding marshaller is the default delegate
         JAXBMarshaller delegate = marshaller;

         if(value instanceof Exception)
         {
            // todo: CTS workaround for custom exceptions, clarify when Alexey is back
            // causes NPE in MarshallerImpl:458
            delegate = new XercesXSMarshallerImpl();
         }

         // marshalling context
         delegate.setProperty(JAXBConstants.JAXB_XS_MODEL, model);
         delegate.setProperty(JAXBConstants.JAXB_TYPE_QNAME, xmlType);
         delegate.setProperty(JAXBConstants.JAXB_ROOT_QNAME, xmlName);
         delegate.setProperty(JAXBConstants.JAXB_JAVA_MAPPING, jaxrpcMapping);

         // marshall
         delegate.marshal(value, strwr);
         xmlFragment = strwr.toString();

         log.debug("serialized: " + xmlFragment);

         return xmlFragment;
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new BindingException(e);
      }
   }
}
