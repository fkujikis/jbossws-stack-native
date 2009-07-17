/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.ws.util.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.stream.StreamSource;

import org.jboss.ws.WSException;
import org.jboss.wsf.common.IOUtils;

/**
 * A StreamSource that can be read repeatedly. 
 * @author Thomas.Diesler@jboss.org
 * @author Richard.Opalka@jboss.org
 * @since 29-Mar-2007
 */
public final class BufferedStreamSource extends StreamSource
{
   private byte[] bytes;
   private char[] chars;

   public BufferedStreamSource(StreamSource source)
   {
      try
      {
         final InputStream sourceInputStream = source.getInputStream();
         if (sourceInputStream != null)
         {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            IOUtils.copyStream(baos, sourceInputStream);
            bytes = baos.toByteArray();
         }

         final Reader sourceReader = source.getReader();
         if ((sourceInputStream == null) && (sourceReader != null))
         {
            final char[] buffer = new char[1024];
            final CharArrayWriter charArrayWriter = new CharArrayWriter(buffer.length);
            int countOfReadChars = sourceReader.read(buffer);
            while (countOfReadChars > 0)
            {
               charArrayWriter.write(buffer, 0, countOfReadChars);
               countOfReadChars = sourceReader.read(buffer);
            }
            chars = charArrayWriter.toCharArray();
         }
      }
      catch (IOException ex)
      {
         WSException.rethrow(ex);
      }
   }

   public BufferedStreamSource(byte[] bytes)
   {
      this.bytes = bytes;
   }

   @Override
   public final InputStream getInputStream()
   {
      return (bytes != null ? new ByteArrayInputStream(bytes) : null);
   }

   @Override
   public final Reader getReader()
   {
      return (chars != null ? new CharArrayReader(chars) : null);
   }

   @Override
   public final String toString()
   {
      String retVal = null;
      if (bytes != null)
      {
         try
         {
            retVal = new String(bytes, "UTF-8");
         }
         catch (UnsupportedEncodingException e)
         {
            WSException.rethrow(e);
         }
      }
      else if (chars != null)
      {
         retVal = new String(chars);
      }
         
      return "" + retVal;
   }

   @Override
   public final void setInputStream(InputStream inputStream)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public final void setReader(Reader reader)
   {
      throw new UnsupportedOperationException();
   }

}
