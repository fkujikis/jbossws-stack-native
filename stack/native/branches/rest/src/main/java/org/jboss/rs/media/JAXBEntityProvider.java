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
package org.jboss.rs.media;

import javax.ws.rs.ext.EntityProvider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * TODO: Cache JAXBContext for better performance
 *  
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class JAXBEntityProvider implements EntityProvider
{

   /**
    * Supports classes annotated with @XmlRootElement or JAXBElement's that wrap this infomration.
    * @param aClass
    * @return
    */
   public boolean supports(Class aClass)
   {
      return aClass.isAnnotationPresent(XmlRootElement.class) || (JAXBElement.class == aClass);
   }

   public Object readFrom(Class aClass, MediaType mediaType, MultivaluedMap multivaluedMap, InputStream inputStream) throws IOException
   {
      Object result = null;
      try
      {
         JAXBContext context = JAXBContext.newInstance(aClass);
         result = context.createUnmarshaller().unmarshal(inputStream);
      }
      catch (JAXBException e)
      {
         throw new IOException("Unmarshalling failed: " +  e.getMessage());
      }

      return result;

   }

   public void writeTo(Object o, MediaType mediaType, MultivaluedMap multivaluedMap, OutputStream outputStream) throws IOException
   {
      try
      {
         JAXBContext context = JAXBContext.newInstance(o.getClass());
         context.createMarshaller().marshal(o, outputStream);         
      }
      catch (JAXBException e)
      {
         throw new IOException("Marshalling failed: " +  e.getMessage());
      }
   }
}
