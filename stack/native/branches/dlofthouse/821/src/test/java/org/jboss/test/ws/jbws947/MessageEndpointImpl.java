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
package org.jboss.test.ws.jbws947;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPElement;

import org.jboss.util.xml.DOMWriter;

@WebService (serviceName="MessageEndpoint", targetNamespace="http://org.jboss.test.ws/jbws947")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class MessageEndpointImpl
{

   @WebMethod
   public int processElement(SOAPElement msg) throws RemoteException
   {
      try
      {
         String xmlPO = DOMWriter.printNode(msg, false);
         JAXBContext jc = JAXBContext.newInstance("org.jboss.test.ws.jbws947");
         Unmarshaller u = jc.createUnmarshaller();

         JAXBElement poElement = (JAXBElement)u.unmarshal(new ByteArrayInputStream(xmlPO.getBytes()));
         PurchaseOrderType po = (PurchaseOrderType)poElement.getValue();
         
         return po.getItems().getItem().size();
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RemoteException(e.toString(), e);
      }
   }
}
