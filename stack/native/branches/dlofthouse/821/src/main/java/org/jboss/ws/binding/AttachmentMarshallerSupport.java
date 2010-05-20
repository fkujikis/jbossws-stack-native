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

import org.jboss.ws.soap.SOAPMessageContextImpl;
import org.jboss.ws.soap.MessageContextAssociation;
import org.jboss.ws.soap.SOAPMessageImpl;

/**
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since May 10, 2006
 */
public class AttachmentMarshallerSupport {

   public static boolean isXOPPackage() {
      boolean isXOP = false;
      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
      if(msgContext!=null) {
         SOAPMessageImpl soapMessage = (SOAPMessageImpl)msgContext.getMessage();
         isXOP = (soapMessage != null && soapMessage.isXOPMessage());
      }
      return isXOP;
   }
}
