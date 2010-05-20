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

// $Id: WebMetaDataAdaptor.java 354 2006-05-16 13:46:47Z thomas.diesler@jboss.com $

import org.jboss.metadata.WebMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedWebMetaData;

/**
 * Build container independent web meta data 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class WebMetaDataAdaptor
{
   public static UnifiedWebMetaData buildUnifiedWebMetaData(WebMetaData wmd)
   {
      UnifiedWebMetaData umd = new UnifiedWebMetaData();
      umd.setContextRoot(wmd.getContextRoot());
      umd.setServletMappings(wmd.getServletMappings());
      umd.setServletClassMap(wmd.getServletClassMap());
      umd.setConfigName(wmd.getConfigName());
      umd.setConfigFile(wmd.getConfigFile());
      umd.setContextLoader(wmd.getContextLoader());
      umd.setSecurityDomain(wmd.getSecurityDomain());
      //umd.setWsdlPublishLocationMap(wmd.getWsdlPublishLocationMap());
      return umd;
   }
}
