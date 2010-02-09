/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2930;

import javax.jws.WebService;
import javax.xml.ws.Holder;

/**
 * Test Endpoint implementation.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 9th February 2010
 */
@WebService(name = "PhoneBook", targetNamespace = "http://test.jboss.org/ws/jbws2930", endpointInterface = "org.jboss.test.ws.jaxws.jbws2930.PhoneBook", wsdlLocation = "WEB-INF/wsdl/PhoneBook.wsdl")
public class PhoneBookImpl implements PhoneBook
{

   public void lookup(String firstName, String surname, Holder<String> areaCode, Holder<String> number, Holder<Nickname> nickname)
   {
      areaCode.value = "01234";
      number.value = "567890";
      nickname.value = new Nickname();
      nickname.value.name = "Bob";
   }

}
