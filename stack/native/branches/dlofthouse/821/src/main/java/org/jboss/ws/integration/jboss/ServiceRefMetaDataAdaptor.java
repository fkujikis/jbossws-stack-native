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

// $Id: ServiceRefMetaDataAdaptor.java 354 2006-05-16 13:46:47Z thomas.diesler@jboss.com $

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.jboss.webservice.metadata.serviceref.HandlerMetaData;
import org.jboss.webservice.metadata.serviceref.InitParamMetaData;
import org.jboss.webservice.metadata.serviceref.PortComponentRefMetaData;
import org.jboss.webservice.metadata.serviceref.ServiceRefMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedInitParamMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedPortComponentRefMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedServiceRefMetaData;

import javax.xml.namespace.QName;

/**
 * Build container independent service ref meta data 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class ServiceRefMetaDataAdaptor
{
   public static UnifiedServiceRefMetaData buildUnifiedServiceRefMetaData(ServiceRefMetaData srmd)
   {
      UnifiedServiceRefMetaData usrmd = new UnifiedServiceRefMetaData();
      usrmd.setServiceRefName(srmd.getServiceRefName());
      usrmd.setServiceInterface(srmd.getServiceInterface());
      usrmd.setWsdlFile(srmd.getWsdlFile());
      usrmd.setJaxrpcMappingFile(srmd.getJaxrpcMappingFile());
      usrmd.setServiceQName(srmd.getServiceQName());

      LinkedHashMap<String, UnifiedPortComponentRefMetaData> pcrefs = new LinkedHashMap<String, UnifiedPortComponentRefMetaData>();
      for (PortComponentRefMetaData pcmd : srmd.getPortComponentRefs())
      {
         UnifiedPortComponentRefMetaData upcmd = new UnifiedPortComponentRefMetaData();
         upcmd.setServiceEndpointInterface(pcmd.getServiceEndpointInterface());
         upcmd.setPortComponentLink(pcmd.getPortComponentLink());
         upcmd.setCallProperties(pcmd.getCallProperties());
         pcrefs.put(pcmd.getServiceEndpointInterface(), upcmd);
      }
      usrmd.setPortComponentRefs(pcrefs);

      ArrayList<UnifiedHandlerMetaData> handlers = new ArrayList<UnifiedHandlerMetaData>();
      for (HandlerMetaData hmd : srmd.getHandlers())
      {
         UnifiedHandlerMetaData uhmd = new UnifiedHandlerMetaData();
         uhmd.setHandlerName(hmd.getHandlerName());
         uhmd.setHandlerClass(hmd.getHandlerClass());

         for(QName soapHeader : hmd.getSoapHeaders())
         {
            uhmd.addSoapHeader(soapHeader);
         }

         for(String portName : hmd.getPortNames())
         {
            uhmd.addPortName(portName);
         }

         for (InitParamMetaData ipmd : hmd.getInitParams())
         {
            UnifiedInitParamMetaData uipmd = new UnifiedInitParamMetaData();
            uipmd.setParamName(ipmd.getParamName());
            uipmd.setParamValue(ipmd.getParamValue());
            uhmd.addInitParam(uipmd);
         }
         handlers.add(uhmd);
      }
      usrmd.setHandlers(handlers);

      usrmd.setConfigName(srmd.getConfigName());
      usrmd.setConfigFile(srmd.getConfigFile());
      usrmd.setWsdlOverride(srmd.getWsdlOverride());
      usrmd.setCallProperties(srmd.getCallProperties());
      usrmd.setResourceCL(srmd.getResourceCL());

      return usrmd;
   }
}
