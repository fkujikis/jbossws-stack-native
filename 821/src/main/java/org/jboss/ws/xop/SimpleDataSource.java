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
package org.jboss.ws.xop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import org.jboss.xb.binding.JBossXBRuntimeException;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Id$</tt>
 */
public class SimpleDataSource
   implements DataSource
{
   public final byte[] bytes;
   public final String contentType;

   public SimpleDataSource(Object o, String contentType)
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = null;
      try
      {
         oos = new ObjectOutputStream(baos);
         oos.writeObject(o);
      }
      catch(IOException e)
      {
         throw new JBossXBRuntimeException("XOP failed to serialize object " + o + ": " + e.getMessage());
      }
      finally
      {
         if(oos != null)
         {
            try
            {
               oos.close();
            }
            catch(IOException e)
            {
            }
         }
      }
      bytes = baos.toByteArray();

      this.contentType = contentType;
   }

   public String getContentType()
   {
      return contentType;
   }

   public InputStream getInputStream() throws IOException
   {
      return new ByteArrayInputStream(bytes);
   }

   public String getName()
   {
      throw new UnsupportedOperationException("getName is not implemented.");
   }

   public OutputStream getOutputStream() throws IOException
   {
      throw new UnsupportedOperationException("getOutputStream is not implemented.");
   }
}
