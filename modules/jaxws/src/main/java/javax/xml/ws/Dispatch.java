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
package javax.xml.ws;

import java.util.concurrent.Future;

/** The <code>Dispatch</code> interface provides support 
 *  for the dynamic invocation of a service endpoint operations. The
 *  <code>javax.xml.ws.Service</code>
 *  interface acts as a factory for the creation of <code>Dispatch</code>
 *  instances.
 *
 *  @since JAX-WS 2.0
 **/
public interface Dispatch<T> extends BindingProvider
{

   /** Invoke a service operation synchronously.
    * 
    * The client is responsible for ensuring that the <code>msg</code> object
    * when marshalled is formed according to the requirements of the protocol
    * binding in use.
    *
    * @param msg An object that will form the message or payload of
    *     the message used to invoke the operation.
    * @return The response message or message payload to the
    *     operation invocation.
    * @throws WebServiceException If a fault occurs during communication with
    *     the service
    * @throws WebServiceException If there is any error in the configuration of
    *     the <code>Dispatch</code> instance
    **/
   public T invoke(T msg);

   /** Invoke a service operation asynchronously.  The
    *  method returns without waiting for the response to the operation
    *  invocation, the results of the operation are obtained by polling the
    *  returned <code>Response</code>.
    * 
    * The client is responsible for ensuring that the <code>msg</code> object 
    * when marshalled is formed according to the requirements of the protocol
    * binding in use.
    *
    * @param msg An object that will form the message or payload of
    *     the message used to invoke the operation.
    * @return The response message or message payload to the
    *     operation invocation.
    * @throws WebServiceException If there is any error in the configuration of
    *     the <code>Dispatch</code> instance
    **/
   public Response<T> invokeAsync(T msg);

   /** Invoke a service operation asynchronously. The
    *  method returns without waiting for the response to the operation
    *  invocation, the results of the operation are communicated to the client
    *  via the passed in handler.
    * 
    * The client is responsible for ensuring that the <code>msg</code> object 
    * when marshalled is formed according to the requirements of the protocol
    * binding in use.
    *
    * @param msg An object that will form the message or payload of
    *     the message used to invoke the operation.
    * @param handler The handler object that will receive the
    *     response to the operation invocation.
    * @return A <code>Future</code> object that may be used to check the status
    *     of the operation invocation. This object MUST NOT be used to try to
    *     obtain the results of the operation - the object returned from
    *     <code>Future<?>.get()</code> is implementation dependent
    *     and any use of it will result in non-portable behaviour.
    * @throws WebServiceException If there is any error in the configuration of
    *     the <code>Dispatch</code> instance
    **/
   public Future<?> invokeAsync(T msg, AsyncHandler<T> handler);

   /** Invokes a service operation using the one-way
    *  interaction mode. The operation invocation is logically non-blocking,
    *  subject to the capabilities of the underlying protocol, no results
    *  are returned. When
    *  the protocol in use is SOAP/HTTP, this method MUST block until
    *  an HTTP response code has been received or an error occurs.
    *
    * The client is responsible for ensuring that the <code>msg</code> object 
    * when marshalled is formed according to the requirements of the protocol
    * binding in use.
    *
    * @param msg An object that will form the message or payload of
    *     the message used to invoke the operation.
    * @throws WebServiceException If there is any error in the configuration of
    *     the <code>Dispatch</code> instance or if an error occurs during the
    *     invocation.
    **/
   public void invokeOneWay(T msg);
}
