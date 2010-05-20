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
package org.jboss.test.ws.samples.wsaddressing;

//$Id$

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPMessageHandler;
import javax.jws.soap.SOAPMessageHandlers;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.jboss.logging.Logger;

/**
 * WS-Addressing stateful service endpoint
 *
 * @author Thomas.Diesler@jboss.org
 * @since 24-Nov-2005
 */
@WebService(name = "StatefulEndpoint", targetNamespace = "http://org.jboss.ws/samples/wsaddressing", serviceName = "TestService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@SOAPMessageHandlers( { 
   @SOAPMessageHandler(className = "org.jboss.ws.addressing.soap.SOAPServerHandler"),
   @SOAPMessageHandler(className = "org.jboss.test.ws.samples.wsaddressing.ServerHandler")
   })
public class StatefulEndpointImpl implements StatefulEndpoint, ServiceLifecycle
{
   // provide logging
   private static Logger log = Logger.getLogger(StatefulEndpointImpl.class);

   public static final QName IDQN = new QName("http://somens", "clientid", "ns1");

   // The state map for all clients
   private static Map<String, List<String>> clientStateMap = new HashMap<String, List<String>>();
   
   private ServletEndpointContext servletEndpointContext;
   private String clientid;
   private static List<String> items;

   @WebMethod
   public void addItem(String item)
   {
      initSessionState();
      log.info("addItem [clientid=" + clientid + "]: " + item);
      items.add(item);
   }

   @WebMethod
   public void checkout()
   {
      initSessionState();
      log.info("checkout [clientid=" + clientid + "]");
      clientStateMap.remove(clientid);
   }

   @WebMethod
   public String getItems()
   {
      initSessionState();
      log.info("getItems [clientid=" + clientid + "]: " + items);
      return items.toString();
   }

   private void initSessionState()
   {
      MessageContext msgContext = servletEndpointContext.getMessageContext();
      clientid = (String)msgContext.getProperty("clientid");
      if (clientid == null)
         throw new IllegalStateException("Cannot obtain clientid");
      
      // Get the client's items
      items = clientStateMap.get(clientid);
      if (items == null)
      {
         items = new ArrayList<String>();
         clientStateMap.put(clientid, items);
      }
   }

   public void init(Object context) throws ServiceException
   {
      log.info("init ServiceLifecycle");
      servletEndpointContext = (ServletEndpointContext)context;
   }

   public void destroy()
   {
      log.info("destroy ServiceLifecycle");
   }
}
