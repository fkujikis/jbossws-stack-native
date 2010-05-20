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
package org.jboss.ws.xop;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.soap.MessageContextAssociation;
import org.jboss.ws.soap.SOAPMessageContextImpl;
import org.jboss.ws.soap.SOAPMessageImpl;
import org.jboss.ws.soap.attachment.ContentHandlerRegistry;
import org.jboss.xb.binding.sunday.xop.XOPObject;
import org.jboss.xb.binding.sunday.xop.XOPUnmarshaller;

import javax.activation.DataHandler;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The XOPUnmarshallerImpl allows callbacks from the binding layer towards the
 * soap processing components in order to optimize binary processing.
 *
 * @see XOPMarshallerImpl
 * @see org.jboss.ws.jaxrpc.encoding.JAXBDeserializer
 * @see org.jboss.ws.jaxrpc.encoding.SimpleDeserializer
 *
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since May 9, 2006
 * @version $Id$
 */
public class XOPUnmarshallerImpl implements XOPUnmarshaller {

   private static final Logger log = Logger.getLogger(XOPUnmarshallerImpl.class);

   static
   {
      // Load JAF content handlers
      ContentHandlerRegistry.register();
   }

   public boolean isXOPPackage()
   {
      return XOPContext.isXOPPackage();
   }

   public XOPObject getAttachmentAsDataHandler(String cid)
   {
      try
      {
         // Always return the DataHandler, it's the preferred SEI parameter type.
         // If necessary the conversion can take just place in SOAPContentElement
         AttachmentPart part = XOPContext.getAttachmentByCID(cid);
         XOPObject xopObject = new XOPObject(part.getDataHandler());
         xopObject.setContentType(part.getDataHandler().getContentType());

         return xopObject;
      }
      catch (SOAPException ex)
      {
         throw new WSException(ex);
      }
   }

   public byte[] getAttachmentAsByteArray(String cid)
   {
      try
      {
         AttachmentPart part = XOPContext.getAttachmentByCID(cid);
         DataHandler dh = part.getDataHandler();
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         dh.writeTo(bout);

         return bout.toByteArray();
      }
      catch (SOAPException ex)
      {
         throw new WSException(ex);
      }
      catch(IOException e)
      {
         throw new WSException(e);
      }

   }
}
