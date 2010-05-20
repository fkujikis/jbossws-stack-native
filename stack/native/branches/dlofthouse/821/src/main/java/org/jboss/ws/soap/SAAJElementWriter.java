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
package org.jboss.ws.soap;

import javax.xml.soap.SOAPElement;
import java.io.*;
import java.util.Iterator;

/**
 * Writes a SAAJ elements to an output stream.
 *
 * @see SOAPElementImpl
 *
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @version $Id$
 * @since Aug 4, 2006
 */
public class SAAJElementWriter {

   // Print writer
   private PrintWriter out;
   // True, if canonical output
   private boolean canonical;
   // True, if pretty printing should be used
   private boolean prettyprint;
   // True, if the XML declaration should be written
   private boolean writeXMLDeclaration;
   // Explicit character set encoding
   private String charsetName;  
   // True, if the XML declaration has been written
   private boolean wroteXMLDeclaration;

   public SAAJElementWriter(Writer w)
   {
      this.out = new PrintWriter(w);
   }

   public SAAJElementWriter(OutputStream stream)
   {
      try
      {
         this.out = new PrintWriter(new OutputStreamWriter(stream, "UTF-8"));
      }
      catch (UnsupportedEncodingException e)
      {
         // ignore, UTF-8 should be available
      }
   }

   public SAAJElementWriter(OutputStream stream, String charsetName)
   {
      try
      {
         this.out = new PrintWriter(new OutputStreamWriter(stream, charsetName));
         this.charsetName = charsetName;
         this.writeXMLDeclaration = true;
      }
      catch (UnsupportedEncodingException e)
      {
         throw new IllegalArgumentException("Unsupported encoding: " + charsetName);
      }
   }

   /**
    * Print a node with explicit prettyprinting.
    * The defaults for all other DOMWriter properties apply.
    *
    */
   public static String printSOAPElement(SOAPElementImpl element, boolean prettyprint)
   {
      StringWriter strw = new StringWriter();
      new SAAJElementWriter(strw).setPrettyprint(prettyprint).print(element);
      return strw.toString();
   }

   public boolean isCanonical()
   {
      return canonical;
   }

   /**
    * Set wheter entities should appear in their canonical form.
    * The default is false.
    */
   public SAAJElementWriter setCanonical(boolean canonical)
   {
      this.canonical = canonical;
      return this;
   }

   public boolean isPrettyprint()
   {
      return prettyprint;
   }

   /**
    * Set wheter element should be indented.
    * The default is false.
    */
   public SAAJElementWriter setPrettyprint(boolean prettyprint)
   {
      this.prettyprint = prettyprint;
      return this;
   }

   public boolean isWriteXMLDeclaration()
   {
      return writeXMLDeclaration;
   }

   /**
    * Set wheter the XML declaration should be written.
    * The default is false.
    */
   public SAAJElementWriter setWriteXMLDeclaration(boolean writeXMLDeclaration)
   {
      this.writeXMLDeclaration = writeXMLDeclaration;
      return this;
   }

   public void print(SOAPElementImpl element)
   {
      printInternal(element);
   }

   private void printInternal(SOAPElementImpl element)
   {
      // is there anything to do?
      if (element == null)
      {
         return;
      }

      if (wroteXMLDeclaration == false && writeXMLDeclaration == true && canonical == false)
      {
         out.print("<?xml version='1.0'");
         if (charsetName != null)
            out.print(" encoding='" + charsetName + "'");

         out.println("?>");
         wroteXMLDeclaration = true;
      }

      writeElement(element, out, prettyprint);

      out.flush();
   }

   private static void writeElement(SOAPElementImpl element, PrintWriter out, boolean pretty) {

      // the element itself
      String endTag = element.write(out, pretty);

      // skip SOAPContentElements
      if(! (element instanceof SOAPContentElement))
      {
         // and it's children
         Iterator it = element.getChildElements();
         while(it.hasNext())
         {
            Object child = it.next();
            if(child instanceof SOAPElement)
            {
               SOAPElementImpl childElement = (SOAPElementImpl)child;
               writeElement(childElement, out, pretty);
            }
         }

      }
      if(endTag!=null)
         out.write(endTag);
   }

}
