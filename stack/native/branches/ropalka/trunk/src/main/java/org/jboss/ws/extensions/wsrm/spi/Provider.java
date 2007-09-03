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
package org.jboss.ws.extensions.wsrm.spi;

/**
 * WS-RM Provider SPI facade. Each WS-RM provider must override this class.
 *
 * @author richard.opalka@jboss.com
 */
public abstract class Provider
{
   /**
    * Must be overriden in the subclasses
    * @param targetNamespace
    */
   protected Provider()
   {
   }
   
   /**
    * Returns the namespace associated with current WS-RM provider implementation
    * @return
    */
   public abstract String getNamespaceURI();
   
   /**
    * Returns WS-RM provider specific message factory
    * @return message factory
    */
   public abstract MessageFactory getMessageFactory();
   
   /**
    * Gets WS-RM provider by <b>wsrmNamespace</b>
    * @param namespace associated with the WS-RM provider
    * @return WS-RM provider instance
    * @throws IllegalArgumentException if specified <b>wsrmNamespace</b> has no associated WS-RM provider 
    */
   public static Provider getInstance(String wsrmNamespace)
   {
      return null; // TODO: implement
   }
}
