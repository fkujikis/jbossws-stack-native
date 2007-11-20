package org.jboss.rs.media.simple;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.*;

public class StringProvider implements MessageBodyReader<String>, MessageBodyWriter<String>
{

   public boolean isReadable(Class<?> aClass)
   {
      return aClass == String.class;
   }

   public String readFrom(Class<String> aClass, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) throws IOException
   {
      Reader reader = new InputStreamReader(inputStream);
     StringBuilder sb = new StringBuilder();
     char[] c = new char[1024];
     int l;
     while ((l = reader.read(c)) != -1) {
        sb.append(c, 0, l);
     }
     return sb.toString();
   }

   public boolean isWriteable(Class<?> aClass)
   {
      return aClass == String.class;
   }

   public long getSize(String s)
   {
      return s.length();
   }

   public void writeTo(String s, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) throws IOException
   {
      outputStream.write( s.getBytes() );   
   }
}
