/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.core.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;

/**
 * Netty headers abstraction.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class NettyHeaderSource implements MimeHeaderSource
{

   private final Map<String, List<String>> req;
   private final Map<String, List<String>> res;

   public NettyHeaderSource(final Map<String, List<String>> req, final Map<String, List<String>> res)
   {
      super();
      
      this.req = req;
      this.res = res;
   }

   public MimeHeaders getMimeHeaders()
   {
      if (req.size() == 0) return null;

      MimeHeaders headers = new MimeHeaders();

      Iterator<String> e = req.keySet().iterator();
      String key = null;
      String value = null;
      while (e.hasNext())
      {
         key = e.next();
         value = req.get(key).get(0);
         
         headers.addHeader(key, value);
      }

      return headers;
   }

   public void setMimeHeaders(final MimeHeaders headers)
   {
      Iterator i = headers.getAllHeaders();
      String key = null;
      while (i.hasNext())
      {
         MimeHeader header = (MimeHeader)i.next();
         key = header.getName();
         List<String> values = new LinkedList<String>();
         values.add(header.getValue());
         res.put(key, values);
      }
   }

   public Map<String, List<String>> getHeaderMap()
   {
      Map<String, List<String>> headerMap = new HashMap<String, List<String>>();

      Iterator<String> e = req.keySet().iterator();
      if (e != null)
      {
         String key = null;
         List<String> value = null;
         while (e.hasNext())
         {
            key = e.next();
            value = req.get(key);
            headerMap.put(key, value);
         }
      }

      return headerMap;
   }

   public void setHeaderMap(Map<String, List<String>> headers)
   {
      Iterator<String> it = headers.keySet().iterator();
      String key = null;
      List<String> value = null;
      while (it.hasNext())
      {
         key = it.next();
         value = headers.get(key);
         res.put(key, value);
      }
   }

}
