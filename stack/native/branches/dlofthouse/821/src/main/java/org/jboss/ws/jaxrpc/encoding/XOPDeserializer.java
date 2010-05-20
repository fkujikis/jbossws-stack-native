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

// $Id: XOPDeserializer.java 275 2006-05-04 21:36:29Z jason.greene@jboss.com $

import java.io.IOException;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;

import org.jboss.logging.Logger;
import org.jboss.ws.binding.BindingException;
import org.jboss.ws.soap.MessageContextAssociation;
import org.jboss.ws.soap.SOAPMessageContextImpl;
import org.jboss.ws.soap.SOAPMessageImpl;

/**
 * Deserializer for XOP
 *
 * @author Thomas.Diesler@jboss.org
 * @since 18-Jan-2006
 */
public class XOPDeserializer extends DeserializerSupport
{
   // provide logging
   private static final Logger log = Logger.getLogger(XOPDeserializer.class);
   
   public Object deserialize(QName xmlName, QName xmlType, String cid, SerializationContextImpl serContext) throws BindingException
   {
      log.debug("deserialize: [xmlName=" + xmlName + ",xmlType=" + xmlType + "]");
      
      try
      {
         SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
         SOAPMessageImpl soapMessage = (SOAPMessageImpl)msgContext.getMessage();
         AttachmentPart part = soapMessage.getAttachmentByContentId(cid);
         if (part == null)
            throw new BindingException("Cannot find attachment part for: " + cid);
         
         DataHandler dataHandler = part.getDataHandler();
         return dataHandler.getContent();
      }
      catch (SOAPException ex)
      {
         throw new BindingException(ex);
      }
      catch (IOException ex)
      {
         throw new BindingException(ex);
      }
   }
}
