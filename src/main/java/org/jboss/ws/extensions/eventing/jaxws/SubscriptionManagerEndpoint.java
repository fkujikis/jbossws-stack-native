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
package org.jboss.ws.extensions.eventing.jaxws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.ws.Action;

/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.0-b26-ea3
 * Generated source version: 2.0
 * 
 */
@WebService(name = "SubscriptionManager", targetNamespace = "http://schemas.xmlsoap.org/ws/2004/08/eventing")
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
public interface SubscriptionManagerEndpoint {


   /**
    *
    * @param body
    * @return
    *     returns org.jboss.ws.extensions.eventing.GetStatusResponse
    */
   @WebMethod(operationName = "GetStatusOp")
   @WebResult(name = "GetStatusResponse", targetNamespace = "http://schemas.xmlsoap.org/ws/2004/08/eventing", partName = "body")
   @Action(
      input = "http://schemas.xmlsoap.org/ws/2004/08/eventing/GetStatus",
      output = "http://schemas.xmlsoap.org/ws/2004/08/eventing/GetStatusResponse"
   )
   public GetStatusResponse getStatusOp(
      @WebParam(name = "GetStatus", targetNamespace = "http://schemas.xmlsoap.org/ws/2004/08/eventing", partName = "body")
      GetStatus body);

   /**
    *
    * @param body
    * @return
    *     returns org.jboss.ws.extensions.eventing.RenewResponse
    */
   @WebMethod(operationName = "RenewOp")
   @WebResult(name = "RenewResponse", targetNamespace = "http://schemas.xmlsoap.org/ws/2004/08/eventing", partName = "body")
   @Action(
      input = "http://schemas.xmlsoap.org/ws/2004/08/eventing/Renew",
      output = "http://schemas.xmlsoap.org/ws/2004/08/eventing/RenewResponse"
   )
   public RenewResponse renewOp(
      @WebParam(name = "Renew", targetNamespace = "http://schemas.xmlsoap.org/ws/2004/08/eventing", partName = "body")
      Renew body);

   /**
    *
    * @param body
    */
   @WebMethod(operationName = "UnsubscribeOp")
   @Action(
      input = "http://schemas.xmlsoap.org/ws/2004/08/eventing/Unsubscribe",
      output = "http://schemas.xmlsoap.org/ws/2004/08/eventing/UnsubscribeResponse"
   )
   public void unsubscribeOp(
      @WebParam(name = "Unsubscribe", targetNamespace = "http://schemas.xmlsoap.org/ws/2004/08/eventing", partName = "body")
      Unsubscribe body);

}
