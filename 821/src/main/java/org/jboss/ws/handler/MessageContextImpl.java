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
package org.jboss.ws.handler;

// $Id$

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.rpc.handler.MessageContext;

/**
 * The message context that is processed by a handler
 * in the handle method.
 * <p/>
 * Provides methods to manage a property set.
 * MessageContext properties enable handlers in a handler chain to share
 * processing related state.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 06-May-2004
 */
public class MessageContextImpl implements MessageContext
{
   public static final String SERVLET_CONTEXT = "javax.xml.ws.servlet.context";
   public static final String SERVLET_REQUEST = "javax.xml.ws.servlet.request";
   public static final String SERVLET_RESPONSE = "javax.xml.ws.servlet.response";
   public static final String SERVLET_SESSION = "javax.xml.ws.servlet.session";

   // The map of the properties
   private HashMap<String, Object> props = new HashMap<String, Object>();

   /**
    * Returns true if the MessageContext contains a property with the specified name.
    *
    * @param name Name of the property whose presense is to be tested
    * @return Returns true if the MessageContext contains the property; otherwise false
    */
   public boolean containsProperty(String name)
   {
      return props.containsKey(name);
   }

   /**
    * Gets the value of a specific property from the MessageContext
    *
    * @param name Name of the property whose value is to be retrieved
    * @return Value of the property
    * @throws IllegalArgumentException if an illegal property name is specified
    */
   public Object getProperty(String name)
   {
      return props.get(name);
   }

   /**
    * Returns an Iterator view of the names of the properties in this MessageContext
    *
    * @return Iterator for the property names
    */
   public Iterator getPropertyNames()
   {
      return props.keySet().iterator();
   }

   /**
    * Removes a property (name-value pair) from the MessageContext
    *
    * @param name Name of the property to be removed
    * @throws IllegalArgumentException if an illegal property name is specified
    */
   public void removeProperty(String name)
   {
      props.remove(name);
   }

   /**
    * Sets the name and value of a property associated with the MessageContext.
    * If the MessageContext contains a value of the same property, the old value is replaced.
    *
    * @param name  Name of the property associated with the MessageContext
    * @param value Value of the property
    * @throws IllegalArgumentException      If some aspect of the property is prevents it from being stored in the context
    * @throws UnsupportedOperationException If this method is not supported.
    */
   public void setProperty(String name, Object value)
   {
      props.put(name, value);
   }
}
