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
package org.jboss.test.ws.jaxrpc.jbws1647;

/**
 * 
 * @author darran.lofthouse@jboss.com
 * @since 15 May 2007
 */
public class RpcHandler extends AbstractHandler
{

   public static final String MESSAGE_BODY = 
      "<env:Body>" +
      "  <!-- Comment After Body -->" +
      "  <jbw:echoMessage xmlns:jbw='http://org.jboss.test.ws/jbws1647'>" +
      "    <!-- Comment After Operation -->" +
      "    <String_1>My Message</String_1>" +
      "    <!-- Comment After Parameter -->" +
      "  </jbw:echoMessage>" +
      "  <!-- Comment Before Close Body -->" +
      "</env:Body>";
   
   public String getMessageBody()
   {
      return MESSAGE_BODY;
   }

}
