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

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;

import org.jboss.ws.metadata.OperationMetaData;

/** An implementation of this interface transforms the Java call parameters to a SOAPMessage and vice versa.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 16-Oct-2004
 */
public interface BindingProvider
{
   /** On the client side, generate the SOAPMessage from IN parameters. */
   SOAPMessage bindRequestMessage(OperationMetaData opMetaData, EndpointInvocation epInv, Map<QName, UnboundHeader> unboundHeaders) throws BindingException;

   /** On the server side, extract the IN parameters from the SOAPMessage and populate an Invocation object */
   EndpointInvocation unbindRequestMessage(OperationMetaData opMetaData, SOAPMessage reqMessage) throws BindingException;

   /** On the server side, generate the SOAPMessage from OUT parameters in the Invocation object. */
   SOAPMessage bindResponseMessage(OperationMetaData opMetaData, EndpointInvocation epInv) throws BindingException;

   /** On the client side, extract the OUT parameters from the SOAPMessage and return them to the client. */
   void unbindResponseMessage(OperationMetaData opMetaData, SOAPMessage resMessage, EndpointInvocation epInv, Map<QName, UnboundHeader> unboundHeaders) throws BindingException;
}
