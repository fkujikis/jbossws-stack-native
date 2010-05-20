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
package org.jboss.test.ws.samples.mtom;

import org.jboss.ws.WSException;

import javax.mail.BodyPart;
import javax.mail.internet.MimeMultipart;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.activation.DataHandler;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.io.IOException;

/**
 * Service Endpoint for XOP
 *
 * image/jpeg        java.awt.Image
 * text/plain        java.lang.String
 * text/xml          javax.xml.transform.Source
 * application/xml   javax.xml.transform.Source
 *
 * @author Thomas.Diesler@jboss.org
 * @author Heiko.Braun@jboss.org
 *
 * @since 18-Jan-2006
 */
public class XOPTestImpl implements XOPTest, ServiceLifecycle
{
   private ServletEndpointContext context;

   /**
    * Service endpoint method that processes inlined and optimized values.
    */
   public DataHandler sendMimeImageJPEG(String message, DataHandler xoppart) throws RemoteException
   {
      String expContentType = message.equals("MTOM disabled request") ? "application/octet-stream" : "image/jpeg";

      if(! xoppart.getContentType().equals(expContentType))
         throw new IllegalArgumentException("Wrong content-type: expected "+expContentType+", but was " + xoppart.getContentType());
      return xoppart;
   }

   /**
    * Service endpoint method for text/plain
    */
   public String sendMimeTextPlain(String message, String xoppart) throws RemoteException
   {
      return xoppart;
   }

   /**
    * Service endpoint method for text/xml
    */
   public Source sendMimeTextXML(String message, DataHandler xoppart) throws RemoteException
   {
      if(! xoppart.getContentType().equals("text/xml"))
         throw new IllegalArgumentException("Wrong content-type: expected 'text/xml', but was " + xoppart.getContentType());
      try
      {
         Source payload = (Source)xoppart.getContent();
         return payload;
      }
      catch (IOException e)
      {
         throw new WSException(e.getMessage());
      }
   }

   /**
    * Service endpoint method for application/xml
    */
   public DataHandler sendMimeApplicationXML(String message, Source xoppart) throws RemoteException
   {
      return new DataHandler(xoppart, "application/xml");
   }

   public DataHandler sendOctets(String message, DataHandler xoppart) throws RemoteException {
      if(! xoppart.getContentType().equals("application/octet-stream"))
         throw new IllegalArgumentException("Wrong content-type: expected 'application/octet-stream', but was " + xoppart.getContentType());
      return xoppart;
   }

   public void init(Object context) throws ServiceException
   {
      this.context = (ServletEndpointContext)context;
   }

   public void destroy()
   {
      this.context = null;
   }
}
