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
package org.jboss.ws.utils;

import java.io.*;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;

import javax.activation.DataHandler;

/**
 * IO utilites
 *
 * @author Thomas.Diesler@jboss.org
 */
public final class IOUtils
{
   private static Logger log = Logger.getLogger(IOUtils.class);

   // Hide the constructor
   private IOUtils()
   {
   }

   public static Writer getCharsetFileWriter(File file, String charset) throws IOException
   {
      return new OutputStreamWriter(new FileOutputStream(file), charset);
   }

   /** Copy the input stream to the output stream
    */
   public static void copyStream(OutputStream outs, InputStream ins) throws IOException
   {
      byte[] bytes = new byte[1024];
      int r = ins.read(bytes);
      while (r > 0)
      {
         outs.write(bytes, 0, r);
         r = ins.read(bytes);
      }
   }
   public static byte[] convertToBytes(DataHandler dh)
   {
      try
      {
         ByteArrayOutputStream buffOS= new ByteArrayOutputStream();
         dh.writeTo(buffOS);
         return buffOS.toByteArray();
      }
      catch (IOException e)
      {
         throw new WSException("Unable to convert DataHandler to byte[]: " + e.getMessage());
      }
   }
}
