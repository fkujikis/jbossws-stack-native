/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.ws.core.jaxws.binding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.core.client.UnMarshaller;
import org.jboss.ws.core.soap.MessageFactoryImpl;
import org.jboss.ws.extensions.json.BadgerFishDOMDocumentParser;
import org.w3c.dom.Document;

/**
 * @author Thomas.Diesler@jboss.org
 * @since 25-Nov-2004
 */
public class JsonMessageUnMarshaller implements UnMarshaller
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(JsonMessageUnMarshaller.class);
   // Provide logging
   private static Logger log = Logger.getLogger(JsonMessageUnMarshaller.class);

   public Object read(InputStream inputStream, Map<String, Object> metadata, Map<String, Object> headers) throws IOException
   {
      if (log.isTraceEnabled())
      {
         log.trace("Read input stream with metadata=" + metadata);
      }

      // TODO: this should not be a SOAP message
      try
      {
         MessageFactoryImpl factory = new MessageFactoryImpl();
         SOAPMessage soapMsg = factory.createMessage();
         Document doc = new BadgerFishDOMDocumentParser().parse(inputStream);
         soapMsg.getSOAPBody().addDocument(doc);
         return soapMsg;
      }
      catch (SOAPException ex)
      {
         IOException ioex = new IOException(BundleUtils.getMessage(bundle, "CANNOT_UNMARSHALL_JSON_INPUT_STREAM"));
         ioex.initCause(ex);
         throw ioex;
      }
   }
}
