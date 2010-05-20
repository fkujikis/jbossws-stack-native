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

import javax.activation.DataHandler;
import javax.mail.internet.MimeMultipart;
import javax.xml.soap.SOAPElement;
import javax.xml.transform.Source;

import org.jboss.util.NotImplementedException;
import org.jboss.ws.Constants;
import org.jboss.ws.jaxrpc.encoding.*;
import org.w3c.dom.Element;
import java.awt.*;

/**
 * This is the representation of a type mapping.
 * This TypeMapping implementation supports the literal encoding style.
 *
 * The TypeMapping instance maintains a tuple of the type
 * {XML typeQName, Java Class, SerializerFactory, DeserializerFactory}.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-Oct-2004
 */
public class LiteralTypeMapping extends TypeMappingImpl
{

   /**
    * Construct the default literal type mapping.
    * Registers javaTypes for all standard XMLSchema types specified by JAXRPC.
    *
    * Note, the order of registered types is important
    * The last xmlType wins for a given javaType
    *
    */
   public LiteralTypeMapping()
   {
      // XOP default mapping
      JAXBSerializerFactory jaxbSF = new JAXBSerializerFactory();
      JAXBDeserializerFactory jaxbDF = new JAXBDeserializerFactory();

      registerInternal(DataHandler.class, Constants.TYPE_XMIME_DEFAULT, jaxbSF, jaxbDF);
      registerInternal(DataHandler.class, Constants.TYPE_XMIME_DEFAULT, jaxbSF, jaxbDF);
      registerInternal(DataHandler.class, Constants.TYPE_XMIME_DEFAULT, jaxbSF, jaxbDF);
      registerInternal(DataHandler.class, Constants.TYPE_XMIME_DEFAULT, jaxbSF, jaxbDF);
      registerInternal(DataHandler.class, Constants.TYPE_XMIME_DEFAULT, jaxbSF, jaxbDF);

      registerInternal(String.class, Constants.TYPE_XMIME_DEFAULT, jaxbSF, jaxbDF);
      registerInternal(Image.class, Constants.TYPE_XMIME_DEFAULT, jaxbSF, jaxbDF);
      registerInternal(Source.class, Constants.TYPE_XMIME_DEFAULT, jaxbSF, jaxbDF);
      registerInternal(MimeMultipart.class, Constants.TYPE_XMIME_DEFAULT, jaxbSF, jaxbDF);
      
      registerStandardLiteralTypes();

      // register default mime mappings
      registerInternal(DataHandler.class, Constants.TYPE_MIME_APPLICATION_XML, null, null);
      registerInternal(DataHandler.class, Constants.TYPE_MIME_IMAGE_GIF, null, null);
      registerInternal(DataHandler.class, Constants.TYPE_MIME_IMAGE_JPEG, null, null);
      registerInternal(DataHandler.class, Constants.TYPE_MIME_TEXT_PLAIN, null, null);
      registerInternal(DataHandler.class, Constants.TYPE_MIME_TEXT_XML, null, null);
      registerInternal(MimeMultipart.class, Constants.TYPE_MIME_MULTIPART_MIXED, null, null);

      // register mapping for xsd:anyType
      registerInternal(SOAPElement.class, Constants.TYPE_LITERAL_ANYTYPE, new SOAPElementSerializerFactory(), new SOAPElementDeserializerFactory());
      registerInternal(Element.class, Constants.TYPE_LITERAL_ANYTYPE, new ElementSerializerFactory(), new ElementDeserializerFactory());
   }

   /**
    * Returns the encodingStyle URIs (as String[]) supported by this TypeMapping instance.
    * A TypeMapping that contains only encoding style independent serializers and deserializers
    * returns null from this method.
    *
    * @return Array of encodingStyle URIs for the supported encoding styles
    */
   public String[] getSupportedEncodings()
   {
      return new String[] { "" };
   }

   /**
    * Sets the encodingStyle URIs supported by this TypeMapping instance. A TypeMapping that contains only encoding
    * independent serializers and deserializers requires null as the parameter for this method.
    *
    * @param encodingStyleURIs Array of encodingStyle URIs for the supported encoding styles
    */
   public void setSupportedEncodings(String[] encodingStyleURIs)
   {
      throw new NotImplementedException();
   }
}
