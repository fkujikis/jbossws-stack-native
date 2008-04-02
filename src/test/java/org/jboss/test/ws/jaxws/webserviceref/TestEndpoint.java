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

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;

/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.0-b26-ea3
 * Generated source version: 2.0
 *
 */
@WebService(name = "TestEndpoint", targetNamespace = "http://org.jboss.ws/wsref", wsdlLocation = "http://localhost.localdomain:8080/jaxws-samples-webserviceref?wsdl")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface TestEndpoint {


    /**
     *
     * @param arg0
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "http://org.jboss.ws/wsref", partName = "return")
    public String echo(
        @WebParam(name = "arg0", partName = "arg0")
        String arg0);

}
