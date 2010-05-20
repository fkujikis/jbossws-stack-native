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
package org.jboss.ws.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.util.xml.DOMUtils;
import org.jboss.util.xml.DOMWriter;
import org.jboss.ws.WSException;
import org.w3c.dom.Element;

/**
 * The publisher for web service endpoints
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-May-2006
 */
public abstract class ServiceEndpointPublisher
{
   // default bean name
   public static final String BEAN_NAME = "ServiceEndpointPublisher";

   // The servlet init param in web.xml that is the service endpoint class
   public static final String INIT_PARAM_SERVICE_ENDPOINT_IMPL = "ServiceEndpointImpl";

   // logging support
   private static Logger log = Logger.getLogger(ServiceEndpointPublisher.class);

   // The service endpoint deployer
   protected ServiceEndpointDeployer serviceEndpointDeployer;
   // The configured service endpoint servlet
   protected String servletName;

   public ServiceEndpointDeployer getServiceEndpointDeployer()
   {
      return serviceEndpointDeployer;
   }

   public void setServiceEndpointDeployer(ServiceEndpointDeployer serviceEndpointDeployer)
   {
      this.serviceEndpointDeployer = serviceEndpointDeployer;
   }

   public String getServiceEndpointServlet()
   {
      return servletName;
   }

   public void setServiceEndpointServlet(String servletName)
   {
      this.servletName = servletName;
   }

   public abstract String publishServiceEndpoint(URL warURL) throws Exception;

   public abstract String destroyServiceEndpoint(URL warURL) throws Exception;

   public abstract String publishServiceEndpoint(UnifiedDeploymentInfo udi) throws Exception;

   public abstract String destroyServiceEndpoint(UnifiedDeploymentInfo udi) throws Exception;

   public Map<String, String> rewriteWebXML(URL warURL)
   {
      File warFile = new File(warURL.getFile());
      if (warFile.isDirectory() == false)
         throw new WSException("Expected a war directory: " + warURL);

      File webXML = new File(warURL.getFile() + "/WEB-INF/web.xml");
      if (webXML.isFile() == false)
         throw new WSException("Cannot find web.xml: " + webXML);

      try
      {
         Element root = DOMUtils.parse(new FileInputStream(webXML));

         String warName = warFile.getName();
         Map<String, String> sepTargetMap = modifyServletConfig(root, warName);

         // After redeployment there might be a stale copy of the original web.xml.org, we delete it
         File orgWebXML = new File(webXML.getCanonicalPath() + ".org");
         orgWebXML.delete();

         // Rename the web.xml
         if (webXML.renameTo(orgWebXML) == false)
            throw new WSException("Cannot rename web.xml: " + orgWebXML);

         FileOutputStream fos = new FileOutputStream(webXML);
         new DOMWriter(fos).setPrettyprint(true).print(root);
         fos.close();

         return sepTargetMap;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception e)
      {
         throw new WSException(e);
      }
   }

   private Map<String, String> modifyServletConfig(Element root, String warName)
   {
      Map<String, String> sepTargetMap = new HashMap<String, String>();

      Iterator itServlets = DOMUtils.getChildElements(root, "servlet");
      while (itServlets.hasNext())
      {
         Element servletElement = (Element)itServlets.next();
         String linkName = DOMUtils.getTextContent(DOMUtils.getFirstChildElement(servletElement, "servlet-name"));

         // find the servlet-class
         Element classElement = DOMUtils.getFirstChildElement(servletElement, "servlet-class");
         if (classElement == null)
            throw new WSException("Cannot find <servlet-class> for servlet-name: " + linkName);

         // Get the servlet class
         String servletClassName = DOMUtils.getTextContent(classElement);

         String targetBeanName = null;

         // Nothing to do if we have an <init-param> 
         if (isAlreadyModified(servletElement) == false)
         {
            // Check if it is a real servlet that we can ignore
            if (servletClassName.endsWith("Servlet"))
            {
               log.info("Ignore <servlet-class> that ends with 'Servlet': " + servletClassName);
               continue;
            }
            
            // build a list of detached elements that come after <servlet-class>
            boolean startDetach = false;
            List<Element> detachedElements = new ArrayList<Element>();
            Iterator itDetached = DOMUtils.getChildElements(servletElement);
            while (itDetached.hasNext())
            {
               Element el = (Element)itDetached.next();
               if (startDetach == true)
               {
                  detachedElements.add(el);
                  servletElement.removeChild(el);
               }
               if (el.equals(classElement))
               {
                  servletElement.removeChild(el);
                  startDetach = true;
               }
            }

            // replace the class name
            classElement = (Element)DOMUtils.createElement("servlet-class");
            classElement.appendChild(DOMUtils.createTextNode(servletName));
            classElement = (Element)servletElement.getOwnerDocument().importNode(classElement, true);
            servletElement.appendChild(classElement);

            // add additional init params
            if (servletClassName.equals(servletName) == false)
            {
               Element paramElement = DOMUtils.createElement("init-param");
               paramElement.appendChild(DOMUtils.createElement("param-name")).appendChild(DOMUtils.createTextNode(INIT_PARAM_SERVICE_ENDPOINT_IMPL));
               paramElement.appendChild(DOMUtils.createElement("param-value")).appendChild(DOMUtils.createTextNode(servletClassName));
               paramElement = (Element)servletElement.getOwnerDocument().importNode(paramElement, true);
               servletElement.appendChild(paramElement);
               targetBeanName = servletClassName;
            }

            // reattach the elements
            itDetached = detachedElements.iterator();
            while (itDetached.hasNext())
            {
               Element el = (Element)itDetached.next();
               servletElement.appendChild(el);
            }
         }
         else
         {
            Iterator itParams = DOMUtils.getChildElements(servletElement, "init-param");
            while (itParams.hasNext())
            {
               Element elParam = (Element)itParams.next();
               Element elParamName = DOMUtils.getFirstChildElement(elParam, "param-name");
               Element elParamValue = DOMUtils.getFirstChildElement(elParam, "param-value");
               if (INIT_PARAM_SERVICE_ENDPOINT_IMPL.equals(DOMUtils.getTextContent(elParamName)))
               {
                  targetBeanName = DOMUtils.getTextContent(elParamValue);
               }
            }
         }

         if (targetBeanName == null)
            throw new IllegalStateException("Cannot obtain service endpoint bean for: " + linkName);
         
         sepTargetMap.put(linkName, targetBeanName.trim());
      }

      return sepTargetMap;
   }

   // Return true if the web.xml is already modified
   private boolean isAlreadyModified(Element servletElement)
   {
      Iterator itParams = DOMUtils.getChildElements(servletElement, "init-param");
      while (itParams.hasNext())
      {
         Element elParam = (Element)itParams.next();
         Element elParamName = DOMUtils.getFirstChildElement(elParam, "param-name");
         if (INIT_PARAM_SERVICE_ENDPOINT_IMPL.equals(DOMUtils.getTextContent(elParamName)))
            return true;
      }
      return false;
   }
}