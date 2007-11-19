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
package org.jboss.test.rs.deployment;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import junit.framework.Test;

import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class DeploymentTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(DeploymentTestCase.class, "jbossrs-deployment.war");
   }

   /**
    * Invoke a root resource
    * @throws Exception
    */
   public void testRequest1() throws Exception
   {
      URL url = new URL("http://localhost:8080/jbossrs-deployment/widgets");
      String response = doTextPlainRequest(url, null);
      assertNotNull(response);
      assertEquals("A widgetlist", response);
   }

   /**
    * Invoke a subresource
    *
    * @throws Exception
    */
   public void testRequest2() throws Exception
   {
      URL url = new URL("http://localhost:8080/jbossrs-deployment/widgets/123/id");
      String response = doTextPlainRequest(url, null);
      assertNotNull(response);
      assertEquals("123", response);
   }
   
   private String doTextPlainRequest(URL url, String data) throws Exception
   {
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setDoOutput( data!=null );
      conn.setRequestProperty("accept", "text/*");

      if(data !=null)
      {
         OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

         wr.write(data);
         wr.flush();
         wr.close();
      }

      // Get the response
      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      StringBuffer sb = new StringBuffer();
      while ((line = rd.readLine()) != null)
      {
         sb.append(line);
      }
      rd.close();

      return sb.toString();
   }
}
