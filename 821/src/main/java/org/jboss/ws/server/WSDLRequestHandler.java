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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.jboss.logging.Logger;
import org.jboss.util.xml.DOMUtils;
import org.jboss.ws.metadata.EndpointMetaData;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles the delivery of the WSDL and its included artifacts.
 * It rewrites the include URL's.
 *
 * http://www.jboss.org/index.html?module=bb&op=viewtopic&p=3871263#3871263
 *
 * For a discussion of this topic.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 23-Mar-2005
 */
public class WSDLRequestHandler
{
   // provide logging
   private Logger log = Logger.getLogger(WSDLRequestHandler.class);

   private EndpointMetaData epMetaData;

   public WSDLRequestHandler(EndpointMetaData epMetaData)
   {
      this.epMetaData = epMetaData;
   }

   /**
    * Get the WSDL resource for a given resource path
    * <p/>
    * Use path value of null to get the root document
    *
    * @param resourcePath The wsdl resource to get, can be null for the top level wsdl
    * @return A wsdl document, or null if it cannot be found
    */
   public Document getDocumentForPath(String hostPath, String requestURI, String resourcePath) throws IOException
   {
      String wsdlLocation = epMetaData.getServiceMetaData().getWsdlFile();
      if (wsdlLocation == null)
         throw new IllegalStateException("Cannot obtain wsdlFile from endpoint meta data");

      Document wsdlDoc;
      
      // The WSDLFilePublisher should set the location to an URL 
      URL wsdlURL = new URL(wsdlLocation);
      
      // get the root wsdl
      if (resourcePath == null)
      {
         Element wsdlElement = DOMUtils.parse(wsdlURL.openStream());
         wsdlDoc = wsdlElement.getOwnerDocument();
      }

      // get some imported resource
      else
      {
         String resPath = new File(wsdlURL.getPath()).getParent() + File.separatorChar + resourcePath;
         File resFile = new File(resPath);

         Element wsdlElement = DOMUtils.parse(resFile.toURL().openStream());
         wsdlDoc = wsdlElement.getOwnerDocument();
      }

      modifyImportLocations(hostPath, requestURI, resourcePath, wsdlDoc.getDocumentElement());
      return wsdlDoc;
   }

   /**
    * Modify the location of wsdl and schema imports
    */
   private void modifyImportLocations(String hostPath, String requestURI, String resourcePath, Element element)
   {
      // map wsdl definition imports
      NodeList nlist = element.getChildNodes();
      for (int i = 0; i < nlist.getLength(); i++)
      {
         Node childNode = nlist.item(i);
         if (childNode.getNodeType() == Node.ELEMENT_NODE)
         {
            Element childElement = (Element)childNode;
            String nodeName = childElement.getLocalName();
            if ("import".equals(nodeName) || "include".equals(nodeName))
            {
               Attr locationAttr = childElement.getAttributeNode("schemaLocation");
               if (locationAttr == null)
                  locationAttr = childElement.getAttributeNode("location");

               if (locationAttr != null)
               {
                  String orgLocation = locationAttr.getNodeValue();
                  boolean isAbsolute = orgLocation.startsWith("http://") || orgLocation.startsWith("https://");
                  if (isAbsolute == false && orgLocation.startsWith(requestURI) == false)
                  {
                     String newResourcePath = orgLocation;

                     if (resourcePath != null && resourcePath.indexOf("/") > 0)
                        newResourcePath = resourcePath.substring(0, resourcePath.lastIndexOf("/") + 1) + orgLocation;

                     String newLocation = hostPath + requestURI + "?wsdl&resource=" + newResourcePath;
                     locationAttr.setNodeValue(newLocation);

                     log.debug("Mapping import from '" + orgLocation + "' to '" + newLocation + "'");
                  }
               }
            }
            else
            {
               modifyImportLocations(hostPath, requestURI, resourcePath, childElement);
            }
         }
      }
   }

}
