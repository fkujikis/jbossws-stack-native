package org.jboss.rs.media.xml;

import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.OutputStream;
import java.io.IOException;

public class JAXBWriter implements MessageBodyWriter
{

   public boolean isWriteable(Class aClass)
   {
      return aClass.isAnnotationPresent(XmlRootElement.class);
   }

   public long getSize(Object o)
   {
      return -1;  
   }

   public void writeTo(Object o, MediaType mediaType, MultivaluedMap multivaluedMap, OutputStream outputStream)
     throws IOException
   {
      try
      {
         JAXBContext jaxb = JAXBContext.newInstance(o.getClass());
         jaxb.createMarshaller().marshal(o, outputStream);
      } catch (JAXBException e)
      {
         throw new IOException(e.getMessage());
      }
   }
}
