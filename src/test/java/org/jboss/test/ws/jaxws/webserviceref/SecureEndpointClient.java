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
package org.jboss.test.ws.jaxws.webserviceref;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceRef;

import org.jboss.logging.Logger;
import org.jboss.test.ws.jaxws.webserviceref.SecureEndpointService;

public class SecureEndpointClient
{
   // Provide logging
   private static Logger log = Logger.getLogger(SecureEndpointClient.class);

   @WebServiceRef(name = "SecureService1")
   static SecureEndpointService secureService1;

   @WebServiceRef(name = "SecureService2")
   static Service secureService2;

   @WebServiceRef(name = "SecurePort1")
   static SecureEndpoint securePort1;

   static String retStr;

   public static void main(String[] args)
   {
      String reqMsg = args[0];
      log.info("echo: " + reqMsg);

      String username = null;
      if (args.length > 1)
         username = args[1];

      String password = null;
      if (args.length > 2)
         password = args[2];

      SecureEndpoint port = secureService1.getSecureEndpointPort();
      String retMsg = invokeEndpoint(port, reqMsg, username, password);

      port = secureService2.getPort(SecureEndpoint.class);
      retMsg += "|" + invokeEndpoint(port, reqMsg, username, password);

      port = securePort1;
      retMsg += "|" + invokeEndpoint(port, reqMsg, username, password);

      retStr = retMsg;
   }

   private static String invokeEndpoint(SecureEndpoint port, String inStr, String username, String password)
   {
      if (username != null && password != null)
      {
         BindingProvider bindingProvider = (BindingProvider)port;
         bindingProvider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
         bindingProvider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
      }

      return port.echo(inStr);
   }
}
