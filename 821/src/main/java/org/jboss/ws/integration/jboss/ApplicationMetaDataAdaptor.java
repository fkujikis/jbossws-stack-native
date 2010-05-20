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
package org.jboss.ws.integration.jboss;

// $Id: ApplicationMetaDataAdaptor.java 354 2006-05-16 13:46:47Z thomas.diesler@jboss.com $

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.EjbPortComponentMetaData;
import org.jboss.metadata.MessageDrivenMetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedApplicationMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedBeanMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedEjbPortComponentMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedMessageDrivenMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedSessionMetaData;

/**
 * Build container independent application meta data 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class ApplicationMetaDataAdaptor
{
   public static UnifiedApplicationMetaData buildUnifiedApplicationMetaData(ApplicationMetaData apmd)
   {
      UnifiedApplicationMetaData umd = new UnifiedApplicationMetaData();
      buildUnifiedBeanMetaData(umd, apmd);
      umd.setConfigName(apmd.getConfigName());
      umd.setConfigFile(apmd.getConfigFile());
      umd.setWebServiceContextRoot(apmd.getWebServiceContextRoot());
      umd.setSecurityDomain(apmd.getSecurityDomain());
      //umd.setWsdlPublishLocationMap(apmd.getWsdlPublishLocationMap());
      return umd;
   }

   private static void buildUnifiedBeanMetaData(UnifiedApplicationMetaData umd, ApplicationMetaData metaData)
   {
      List<UnifiedBeanMetaData> beans = new ArrayList<UnifiedBeanMetaData>();
      Iterator it = metaData.getEnterpriseBeans();
      while (it.hasNext())
      {
         BeanMetaData bmd = (BeanMetaData)it.next();
         UnifiedBeanMetaData ubmd = buildUnifiedBeanMetaData(bmd);
         if (ubmd != null)
         {
            beans.add(ubmd);
         }
      }
      umd.setEnterpriseBeans(beans);
   }

   private static UnifiedBeanMetaData buildUnifiedBeanMetaData(BeanMetaData bmd)
   {
      UnifiedBeanMetaData ubmd = null;
      if (bmd instanceof SessionMetaData)
      {
         ubmd = new UnifiedSessionMetaData();
      }
      else if (bmd instanceof MessageDrivenMetaData)
      {
         ubmd = new UnifiedMessageDrivenMetaData();
         ((UnifiedMessageDrivenMetaData)ubmd).setDestinationJndiName(((MessageDrivenMetaData)bmd).getDestinationJndiName());
      }

      if (ubmd != null)
      {
         ubmd.setEjbName(bmd.getEjbName());
         ubmd.setEjbClass(bmd.getEjbClass());
         ubmd.setServiceEndpoint(bmd.getServiceEndpoint());
         ubmd.setHome(bmd.getHome());
         ubmd.setLocalHome(bmd.getLocalHome());
         ubmd.setJndiName(bmd.getJndiName());
         ubmd.setLocalJndiName(bmd.getLocalJndiName());

         EjbPortComponentMetaData pcmd = bmd.getPortComponent();
         if (pcmd != null)
         {
            UnifiedEjbPortComponentMetaData upcmd = new UnifiedEjbPortComponentMetaData();
            upcmd.setPortComponentName(pcmd.getPortComponentName());
            upcmd.setPortComponentURI(pcmd.getPortComponentURI());
            upcmd.setAuthMethod(pcmd.getAuthMethod());
            upcmd.setTransportGuarantee(pcmd.getTransportGuarantee());
            ubmd.setPortComponent(upcmd);
         }
      }
      return ubmd;
   }
}
