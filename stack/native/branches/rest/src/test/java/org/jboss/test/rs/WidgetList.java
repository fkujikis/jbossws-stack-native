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
package org.jboss.test.rs;

import javax.ws.rs.GET;
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.POST;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.HttpHeaders;

@UriTemplate("widgets")
public class WidgetList
{
   @GET
   @ProduceMime({"text/plain"})
   public String getDescription() {
      return "A widgetlist";
   }

   @GET
   @UriTemplate("offers")
   public WidgetList getDiscounted() {
      return null;
   }

   @POST
   @UriTemplate("special")
   @ConsumeMime({"text/xml", "application/xml"})
   public void setDiscounted(
     @HttpContext HttpHeaders headers,
     Widget special
   )
   {
                      
   }

   @UriTemplate("{id}")
   public Widget findWidget(@UriParam("id") String id) {
      return new Widget(id);
   }
}
