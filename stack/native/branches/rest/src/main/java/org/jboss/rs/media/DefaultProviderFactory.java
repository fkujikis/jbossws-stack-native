package org.jboss.rs.media;

import org.jboss.rs.media.xml.JAXBProvider;
import org.jboss.rs.media.simple.StringProvider;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.HeaderProvider;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.ProviderFactory;
import java.util.List;
import java.util.ArrayList;


/**
 * Ugly hack.
 * TODO: Provide a scoped (classloading) reader/writer registry that this class can delegate to.
 */
public class DefaultProviderFactory extends ProviderFactory
{

   private static List<MessageBodyReader> reader = new ArrayList<MessageBodyReader>();
   private static List<MessageBodyWriter> writer = new ArrayList<MessageBodyWriter>();

   static final JAXBProvider JAXB = new JAXBProvider();
   static final StringProvider STRING = new StringProvider();

   static
   {
      reader.add( JAXB );
      reader.add( STRING );

      writer.add( JAXB );
      writer.add( STRING );
   }

   public static ProviderFactory newInstance()
   {
      return new DefaultProviderFactory();
   }

   public <T> T createInstance(Class<T> aClass)
   {
      return null;
   }

   public <T> HeaderProvider<T> createHeaderProvider(Class<T> aClass)
   {
      throw new IllegalArgumentException("Not implemented");
   }

   public <T> MessageBodyReader<T> createMessageBodyReader(Class<T> aClass, MediaType mediaType)
   {
      MessageBodyReader match = null;

      for(MessageBodyReader r : reader)
      {
         if(r.isReadable(aClass))
         {
            match = r;
            break;
         }
      }

      if(null==match)
         throw new RuntimeException("No reader for type: " + aClass + ", mediaType="+mediaType);

      return match;
   }

   public <T> MessageBodyWriter<T> createMessageBodyWriter(Class<T> aClass, MediaType mediaType)
   {
     MessageBodyWriter match = null;

      for(MessageBodyWriter w : writer)
      {
         if(w.isWriteable(aClass))
         {
            match = w;
            break;
         }
      }

      if(null==match)
         throw new RuntimeException("No writer for type: " + aClass + ", mediaType="+mediaType);

      return match;
   }
}
