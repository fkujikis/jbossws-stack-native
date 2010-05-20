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
package org.jboss.ws.binding;

// $Id$

import java.util.HashMap;

import javax.xml.rpc.JAXRPCException;

import org.jboss.ws.Constants;
import org.jboss.ws.binding.soap.SOAP11BindingProvider;

/** A registry of binding providers.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 16-Oct-2004
 */
public class BindingProviderRegistry
{
   private static Class defaultProvider = SOAP11BindingProvider.class;
   private static HashMap providers = new HashMap();

   // Put the default provider in the map
   static
   {
      providers.put(Constants.NS_SOAP11_ENV, defaultProvider);
   }

   /** Get a binding provider for a given URI
    */
   public static BindingProvider getProvider(String bindingURI)
   {
      if (bindingURI == null)
         return getDefaultProvider();

      try
      {
         Class provClass = (Class)providers.get(bindingURI);
         BindingProvider prov = (BindingProvider)provClass.newInstance();
         return prov;
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new JAXRPCException(e);
      }
   }

   /** Get the default binding provider */
   public static BindingProvider getDefaultProvider()
   {
      try
      {
         BindingProvider prov = (BindingProvider)defaultProvider.newInstance();
         return prov;
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new JAXRPCException(e);
      }
   }
}
