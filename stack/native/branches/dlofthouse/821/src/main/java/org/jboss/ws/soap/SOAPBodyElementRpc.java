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

import org.jboss.util.xml.DOMWriter;
import org.jboss.ws.WSException;
import org.w3c.dom.Element;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBodyElement;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

/**
 * An abstract implemenation of the SOAPBodyElement
 * <p/>
 * This class should not expose functionality that is not part of
 * {@link javax.xml.soap.SOAPBodyElement}. Client code should use <code>SOAPBodyElement</code>.
 *
 * @author Thomas.Diesler@jboss.org
 */
public class SOAPBodyElementRpc extends SOAPElementImpl implements SOAPBodyElement
{
   public SOAPBodyElementRpc(Name name)
   {
      super(name);
   }

   public SOAPBodyElementRpc(SOAPElementImpl element)
   {
      super(element);
   }
   public String write(Writer writer, boolean pretty) {
      try
      {

         writer.write('<');
         String prefix = getPrefix()!=null ? getPrefix():"";
         String fqn = prefix.length()>0 ? prefix+":"+getLocalName() : getLocalName();
         writer.write(fqn);

         // namespaces
         Iterator it = getNamespacePrefixes();
         while(it.hasNext())
         {
            String nsPrefix = (String)it.next();
            writer.write(" xmlns:"+nsPrefix+"='"+getNamespaceURI(nsPrefix)+"'");
         }

          // attributes
         Iterator attNames = getAllAttributes();
         while(attNames.hasNext())
         {
            NameImpl name = (NameImpl)attNames.next();
            String attPrefix = name.getPrefix()!=null ? name.getPrefix():"";
            String attFqn = attPrefix.length()>0 ? attPrefix+":"+name.getLocalName() : name.getLocalName();
            writer.write(" "+attFqn);
            writer.write("='"+getAttributeValue(name)+"'");
         }

         writer.write('>');

         // children
         Iterator children = getChildElements();
         while(children.hasNext())
         {
            Object child = children.next();
            if( (child instanceof SOAPContentElement) == false)
            {
               DOMWriter domWriter = new DOMWriter(writer);
               domWriter.setPrettyprint(pretty);
               domWriter.print((Element)child);
            }
         }

         if(pretty)
            writer.write("\n");

         return("</"+fqn+">");

      }
      catch (IOException e)
      {
         throw new WSException(e.getMessage());
      }
   }
}
