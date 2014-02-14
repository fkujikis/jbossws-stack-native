/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.test.ws.jaxws.jbpapp10905;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.jboss.ws.annotation.EndpointConfig;
import org.jboss.wsf.spi.annotation.WebContext;


@Stateless
@WebService(name = "Hello", serviceName = "HelloService", targetNamespace = "http://org.jboss.ws/jbpapp10905")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
@WebContext(contextRoot = "/jaxws-jbpapp10905", urlPattern = "/*")
@EndpointConfig(configName = "Standard WSSecurity Endpoint")
public class HelloJavaBean
{
   @WebMethod(action = "echoOp")
   public Dto echo(Dto dto)
   {
      return dto;
   }
   
   @WebMethod
   public Dto echo2(Dto dto)
   {
      Dto res = new Dto();
      res.setPar1(dto.getPar1() + "2");
      res.setPar2(dto.getPar2() + "2");
      return res;
   }
   
   @WebMethod(action = "echo3Op")
   public Dto echo3(Dto dto)
   {
      Dto res = new Dto();
      res.setPar1(dto.getPar1() + "3");
      res.setPar2(dto.getPar2() + "3");
      return res;
   }
}
