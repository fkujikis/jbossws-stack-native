package org.jboss.test.rs.media;

import junit.framework.Test;
import org.jboss.rs.media.DefaultProviderFactory;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.ProviderFactory;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class MediaTypeTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(MediaTypeTestCase.class, "jbossrs-mediatype.war");
   }

   /**
    * Request a xml resource representation
    *
    * @throws Exception
    */
   public void testRequest1() throws Exception
   {
      URL url = new URL("http://localhost:8080/jbossrs-mediatype/books/3897217279");

      Object response = doMediaRequest(url, null);
      assertNotNull(response );
      assertTrue( (response  instanceof BookResource));
      assertEquals( "Sam Ruby", ((BookResource)response ).getAuthor());
   }

   private Object doMediaRequest(URL url, Object data) throws Exception
   {
      ProviderFactory providerFactory = DefaultProviderFactory.newInstance();

      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setDoOutput( data!=null );
      conn.setRequestProperty("accept", "text/xml");

      if(data !=null)
      {
         OutputStream out = conn.getOutputStream();
         MessageBodyWriter writer = providerFactory.createMessageBodyWriter(data.getClass(), null);
         writer.writeTo(data, null, null, out);
         out.flush();
         out.close();
      }

      Object returnValue = null;
      MessageBodyReader reader = providerFactory.createMessageBodyReader(BookResource.class, null);
      if(reader.isReadable(BookResource.class))
      {
         returnValue = reader.readFrom(BookResource.class, null, null, conn.getInputStream());
      }

      return returnValue;
   }
}
