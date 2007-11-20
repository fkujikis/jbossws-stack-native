package org.jboss.rs.media.xml;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JAXBProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object>
{
   public boolean isReadable(Class<?> aClass)
   {
      return aClass.isAnnotationPresent(XmlRootElement.class);
   }

   public java.lang.Object readFrom(Class<java.lang.Object> aClass, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) throws IOException
   {
      try
      {
         JAXBContext jaxb = JAXBContext.newInstance(aClass);
         Object obj = jaxb.createUnmarshaller().unmarshal(inputStream);

         if(obj instanceof JAXBElement)
            obj = ((JAXBElement)obj).getValue();
         
         return obj;
      } catch (JAXBException e)
      {
         throw new IOException(e.getMessage());
      }
   }

   public boolean isWriteable(Class<?> aClass)
   {
      return aClass.isAnnotationPresent(XmlRootElement.class);
   }

   public long getSize(java.lang.Object object)
   {
      return -1;
   }

   public void writeTo(java.lang.Object object, MediaType mediaType, MultivaluedMap<String, java.lang.Object> multivaluedMap, OutputStream outputStream) throws IOException
   {
      try
      {
         JAXBContext jaxb = JAXBContext.newInstance(object.getClass());
         jaxb.createMarshaller().marshal(object, outputStream);
      } catch (JAXBException e)
      {
         throw new IOException(e.getMessage());
      }
   }

}
