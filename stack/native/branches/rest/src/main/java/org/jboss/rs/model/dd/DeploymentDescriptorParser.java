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
package org.jboss.rs.model.dd;

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Ddeployment descriptor parser
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class DeploymentDescriptorParser
{
   public static JbossrsType read(InputStream inputStream)
     throws IOException
   {
      try
      {
         JAXBContextImpl jc = (JAXBContextImpl) JAXBContext.newInstance( "org.jboss.rs.model.dd" );
         Unmarshaller unmarshaller = jc.createUnmarshaller();
         JAXBElement element = ( JAXBElement ) unmarshaller.unmarshal( inputStream );
         return ( JbossrsType) element.getValue();
      }
      catch (JAXBException e)
      {
         throw new RuntimeException("Failed to unmarshall deployment descriptor", e);
      }
   }

   public static void write(JbossrsType dd, OutputStream outputStream)
     throws IOException
   {
      try
      {
         JAXBContext jaxb = JAXBContext.newInstance(JbossrsType.class);
         JAXBElement wrapper = new JAXBElement(new QName("http://org.jboss.rs/", "jbossrs"), JbossrsType.class, dd);
         Marshaller marshaller = jaxb.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         marshaller.marshal(wrapper, outputStream);
      }
      catch (JAXBException e)
      {
          throw new RuntimeException("Failed to marshall deployment descriptor", e);
      }
   }
}
