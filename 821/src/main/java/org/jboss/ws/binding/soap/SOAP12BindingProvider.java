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
package org.jboss.ws.binding.soap;

// $Id$

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.jboss.ws.Constants;
import org.jboss.ws.metadata.OperationMetaData;
import org.jboss.ws.soap.MessageFactoryImpl;

/** A BindingProvider that implements the SOAP-1.2 specifics.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 09-Nov-2004
 */
public class SOAP12BindingProvider extends SOAPBindingProvider
{
   /** Create the SOAP-1.2 message */
   protected SOAPMessage createMessage(OperationMetaData opMetaData) throws SOAPException
   {
      MessageFactoryImpl factory = new MessageFactoryImpl();
      factory.setEnvelopeURI(Constants.NS_SOAP12_ENV);
      return factory.createMessage();
   }
}
