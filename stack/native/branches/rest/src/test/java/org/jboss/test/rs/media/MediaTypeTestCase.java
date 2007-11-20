package org.jboss.test.rs.media;

import junit.framework.Test;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.jboss.rs.media.JAXBReader;

import java.io.OutputStreamWriter;
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
      URL url = new URL("http://localhost:8080/jbossrs-mediatype" +
        "/books/3897217279");

      Object response = doMediaRequest(url, null);
      assertNotNull(response );
      assertTrue( (response  instanceof BookResource));
      assertEquals( "Sam Ruby", ((BookResource)response ).getAuthor());
   }

   private Object doMediaRequest(URL url, Object data) throws Exception
   {
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setDoOutput( data!=null );
      conn.setRequestProperty("accept", "text/xml");

      if(data !=null)
      {
         OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

         // TODO: resolve MessageBodyWriter

         wr.flush();
         wr.close();
      }

      // TODO: resolve MessageBodyReader

      Object result = null;

      JAXBReader reader = new JAXBReader();
      if(reader.isReadable(BookResource.class))
      {
         result = reader.readFrom(BookResource.class, null, null, conn.getInputStream());
      }


      return result;
   }
}
