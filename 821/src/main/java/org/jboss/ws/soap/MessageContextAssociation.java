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
package org.jboss.ws.soap;

// $Id$

import java.util.Stack;

import org.jboss.logging.Logger;
import org.jboss.ws.utils.ThreadLocalAssociation;

/**
 * A thread local association with the current message context
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 14-Dec-2004
 */
public class MessageContextAssociation
{
   // provide logging
   private static Logger log = Logger.getLogger(MessageContextAssociation.class);
  
   public static SOAPMessageContextImpl popMessageContext()
   {
      SOAPMessageContextImpl msgContext = null;
      Stack<SOAPMessageContextImpl> stack = ThreadLocalAssociation.localMsgContextAssoc().get();
      if (stack != null && stack.isEmpty() == false)
      {
         msgContext = stack.pop();
      }
      log.debug("popMessageContext: " + msgContext);
      return msgContext;
   }

   public static SOAPMessageContextImpl peekMessageContext()
   {
      SOAPMessageContextImpl msgContext = null;
      Stack<SOAPMessageContextImpl> stack = ThreadLocalAssociation.localMsgContextAssoc().get();
      if (stack != null && stack.isEmpty() == false)
      {
         msgContext = stack.peek();
      }
      log.trace("peekMessageContext: " + msgContext);
      return msgContext;
   }

   public static void pushMessageContext(SOAPMessageContextImpl msgContext)
   {
      log.debug("pushMessageContext: " + msgContext);
      Stack<SOAPMessageContextImpl> stack = ThreadLocalAssociation.localMsgContextAssoc().get();
      if (stack == null)
      {
         stack = new Stack<SOAPMessageContextImpl>();
         ThreadLocalAssociation.localMsgContextAssoc().set(stack);
      }
      stack.push(msgContext);
   }
}
