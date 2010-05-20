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
// $Id$
package org.jboss.ws.jaxrpc;

import java.lang.reflect.Method;

import javax.xml.rpc.ServiceException;

import org.jboss.logging.Logger;
import org.jboss.ws.metadata.EndpointMetaData;
import org.jboss.ws.metadata.OperationMetaData;

/** A helper that synchronizes the SEI with the endpoint meta data
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @since 19-May-2005
 */
public class MetaDataSynchronization
{
   // provide logging
   private static final Logger log = Logger.getLogger(MetaDataSynchronization.class);

   public static void synchronizeServiceEndpointInterface(EndpointMetaData epMetaData, Class seiClass) throws ServiceException
   {
      log.debug("synchronize: [epMetaData=" + epMetaData.getName() + ",sei=" + seiClass.getName() + "]");

      if (epMetaData.getServiceEndpointInterface() != seiClass)
         throw new IllegalArgumentException("Endpoint meta data SEI missmatch, expected: " + epMetaData.getServiceEndpointInterfaceName());

      Method[] methods = seiClass.getMethods();
      for (int i = 0; i < methods.length; i++)
      {
         Method method = methods[i];
         log.debug("synchronize method: " + method);

         OperationMetaData opMetaData = epMetaData.getOperation(method);
         if (opMetaData == null)
            throw new ServiceException("Cannot obtain operation meta data for method: " + method);
      }
   }
}
