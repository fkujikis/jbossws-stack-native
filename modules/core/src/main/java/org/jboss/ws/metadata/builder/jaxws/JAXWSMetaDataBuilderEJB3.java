/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.metadata.builder.jaxws;

import java.util.Iterator;
import java.util.ResourceBundle;

import javax.jws.WebService;
import javax.xml.ws.WebServiceProvider;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Service;
import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBMetaData;

/**
 * A server side meta data builder that is based on JSR-181 annotations
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @since 19-May-2005
 */
public class JAXWSMetaDataBuilderEJB3
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(JAXWSMetaDataBuilderEJB3.class);
   // provide logging
   private final Logger log = Logger.getLogger(JAXWSMetaDataBuilderEJB3.class);

   protected Class<?> annotatedClass;

   /** Build from webservices.xml
    */
   public UnifiedMetaData buildMetaData(ArchiveDeployment dep)
   {
      if (log.isDebugEnabled())
         log.debug("START buildMetaData: [name=" + dep.getCanonicalName() + "]");
      try
      {
         UnifiedMetaData wsMetaData = dep.getAttachment(UnifiedMetaData.class);
         if (wsMetaData == null)
         {
            wsMetaData = new UnifiedMetaData(dep.getRootFile());
            wsMetaData.setDeploymentName(dep.getCanonicalName());

            ClassLoader runtimeClassLoader = dep.getRuntimeClassLoader();
            if (null == runtimeClassLoader)
               throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "RUNTIME_LOADER_CANNOT_BE_NULL"));
            wsMetaData.setClassLoader(new DelegateClassLoader(runtimeClassLoader, SecurityActions.getContextClassLoader()));
         } 

         // The container objects below provide access to all of the ejb metadata
         EJBArchiveMetaData apMetaData = dep.getAttachment(EJBArchiveMetaData.class);
         Iterator<EJBMetaData> it = apMetaData.getEnterpriseBeans();
         while (it.hasNext())
         {
            EJBMetaData beanMetaData = it.next();
            String ejbClassName = beanMetaData.getEjbClass();
            Class<?> beanClass = wsMetaData.getClassLoader().loadClass(ejbClassName);
            Service service = dep.getService();
            String ejbLink = beanMetaData.getEjbName();
            if (service.getEndpointByName(ejbLink) != null && (beanClass.isAnnotationPresent(WebService.class) || beanClass.isAnnotationPresent(WebServiceProvider.class)))
            {
               JAXWSServerMetaDataBuilder.setupProviderOrWebService(dep, wsMetaData, beanClass, ejbLink);

               /* Resolve dependency on @SecurityDomain
                * http://jira.jboss.org/jira/browse/JBWS-2107
               if (beanClass.isAnnotationPresent(SecurityDomain.class))
               {
                  SecurityDomain anSecurityDomain = (SecurityDomain)beanClass.getAnnotation(SecurityDomain.class);
                  String lastDomain = wsMetaData.getSecurityDomain();
                  String securityDomain = anSecurityDomain.value();
                  if (lastDomain != null && lastDomain.equals(securityDomain) == false)
                     throw new IllegalStateException(BundleUtils.getMessage(bundle, "MULTIPLE_SECURITY_DOMAINS_NOT_SUPPORTED",  securityDomain));

                  wsMetaData.setSecurityDomain(securityDomain);
               }
               */
            }
         }

         if (log.isDebugEnabled())
            log.debug("END buildMetaData: " + wsMetaData);
         return wsMetaData;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WSException(BundleUtils.getMessage(bundle, "CANNOT_BUILD_META_DATA",  ex.getMessage()),  ex);
      }
   }
}
