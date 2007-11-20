package org.jboss.rs.media;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.io.IOException;

public class JAXBReader implements MessageBodyReader
{

   public boolean isReadable(Class aClass)
   {
      return aClass.isAnnotationPresent(XmlRootElement.class);
   }

   public Object readFrom(Class aClass, MediaType mediaType, MultivaluedMap multivaluedMap, InputStream inputStream)
     throws IOException
   {
      try
      {
         JAXBContext jaxb = JAXBContext.newInstance(aClass);
         Object obj = jaxb.createUnmarshaller().unmarshal(inputStream);         
         return obj;
      } catch (JAXBException e)
      {
         throw new IOException(e.getMessage());
      }
   }
}
