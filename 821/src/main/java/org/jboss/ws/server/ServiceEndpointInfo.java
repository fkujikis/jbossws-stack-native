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
package org.jboss.ws.server;

// $Id$

import javax.management.ObjectName;

import org.jboss.ws.WSException;
import org.jboss.ws.deployment.UnifiedDeploymentInfo;
import org.jboss.ws.handler.ServerHandlerChain;
import org.jboss.ws.metadata.ServerEndpointMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedApplicationMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedBeanMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedMessageDrivenMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedSessionMetaData;

/**
 * This object registered with the EndpointManager service.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 16-Jan-2005
 */
public class ServiceEndpointInfo
{
   /** Endpoint type enum */
   public enum Type
   {
      JSE, SLSB21, SLSB30, MDB21
   }

   // The deployment info for this endpoint
   private UnifiedDeploymentInfo udi;
   // The endpoint type
   private Type type;
   // The endpoint meta data
   private ServerEndpointMetaData sepMetaData;
   // The service endpoint invoker
   private ServiceEndpointInvoker seInvoker;
   // This endpoints handler chain
   private ServerHandlerChain preHandlerChain;
   // This endpoints handler chain
   private ServerHandlerChain jaxrpcHandlerChain;
   // This endpoints handler chain
   private ServerHandlerChain postHandlerChain;
   // The current state of the endpoint
   private ServiceEndpoint.State state;

   public ServiceEndpointInfo(UnifiedDeploymentInfo udi, ServerEndpointMetaData sepMetaData)
   {
      this.udi = udi;
      this.sepMetaData = sepMetaData;

      // Set the endpoint type
      if (udi.type == UnifiedDeploymentInfo.Type.JSR109_JSE || udi.type == UnifiedDeploymentInfo.Type.JSR181_JSE)
      {
         this.type = Type.JSE;
      }
      else if (udi.type == UnifiedDeploymentInfo.Type.JSR109_EJB21 || udi.type == UnifiedDeploymentInfo.Type.JSR181_EJB21)
      {
         String ejbName = sepMetaData.getLinkName();
         if (ejbName == null)
            throw new WSException("Cannot obtain ejb-link from port component");

         UnifiedApplicationMetaData applMetaData = (UnifiedApplicationMetaData)udi.metaData;
         UnifiedBeanMetaData beanMetaData = (UnifiedBeanMetaData)applMetaData.getBeanByEjbName(ejbName);
         if (beanMetaData == null)
            throw new WSException("Cannot obtain ejb meta data for: " + ejbName);

         if (beanMetaData instanceof UnifiedSessionMetaData)
         {
            this.type = Type.SLSB21;
         }
         else if (beanMetaData instanceof UnifiedMessageDrivenMetaData)
         {
            this.type = Type.MDB21;
         }
      }
      else if (udi.type == UnifiedDeploymentInfo.Type.JSR181_EJB3)
      {
         this.type = Type.SLSB30;
      }

      if (type == null)
         throw new WSException("Unsupported endpoint type");
   }

   public ServerEndpointMetaData getServerEndpointMetaData()
   {
      return sepMetaData;
   }

   public ObjectName getServiceEndpointID()
   {
      return sepMetaData.getServiceEndpointID();
   }

   public UnifiedDeploymentInfo getUnifiedDeploymentInfo()
   {
      return udi;
   }

   public Type getType()
   {
      return type;
   }

   public ServerHandlerChain getPreHandlerChain()
   {
      return preHandlerChain;
   }

   public void setPreHandlerChain(ServerHandlerChain preHandlerChain)
   {
      this.preHandlerChain = preHandlerChain;
   }

   public ServerHandlerChain getJaxRpcHandlerChain()
   {
      return jaxrpcHandlerChain;
   }

   public void setJaxRpcHandlerChain(ServerHandlerChain handlerChain)
   {
      this.jaxrpcHandlerChain = handlerChain;
   }

   public ServerHandlerChain getPostHandlerChain()
   {
      return postHandlerChain;
   }

   public void setPostHandlerChain(ServerHandlerChain postHandlerChain)
   {
      this.postHandlerChain = postHandlerChain;
   }

   public ServiceEndpointInvoker getInvoker()
   {
      return seInvoker;
   }

   public void setInvoker(ServiceEndpointInvoker seInvoker)
   {
      this.seInvoker = seInvoker;
   }

   public ServiceEndpoint.State getState()
   {
      return state;
   }

   public void setState(ServiceEndpoint.State state)
   {
      this.state = state;
   }

   /**
    * Returns a string representation of the object.
    */
   public String toString()
   {
      StringBuilder buffer = new StringBuilder("[id=" + getServiceEndpointID() + "state=" + state + "]");
      return buffer.toString();
   }
}
