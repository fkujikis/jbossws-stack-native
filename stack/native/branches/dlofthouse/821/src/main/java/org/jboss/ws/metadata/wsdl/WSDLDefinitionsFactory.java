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
package org.jboss.ws.metadata.wsdl;

// $Id$

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.parsers.DocumentBuilder;

import org.jboss.logging.Logger;
import org.jboss.util.xml.DOMUtils;
import org.jboss.ws.Constants;
import org.jboss.ws.utils.JBossWSEntityResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;

import com.ibm.wsdl.xml.WSDLReaderImpl;

/**
 * A factory that creates a <code>WSDLDefinitions</code> object from an URL.
 *
 * This implementations deals with different WSDL versions so that clients of this
 * factory do need to know about WSDL version specifics. The Java object view of the
 * WSDL document (WSDLDefinitions) is modeled on WSDL-2.0
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-Oct-2004
 */
public class WSDLDefinitionsFactory
{
   // provide logging
   private static final Logger log = Logger.getLogger(WSDLDefinitionsFactory.class);

   // This feature is set by default in wsdl4j, it means the object structure contains the imported arguments
   public static final String FEATURE_IMPORT_DOCUMENTS = "javax.wsdl.importDocuments";
   // Set this feature for additional debugging output
   public static final String FEATURE_VERBOSE = "javax.wsdl.verbose";

   // The WSDL reader features
   private Map features = new HashMap();

   // Hide constructor
   private WSDLDefinitionsFactory()
   {
   }

   /**
    * Create a new instance of a wsdl factory
    */
   public static WSDLDefinitionsFactory newInstance()
   {
      return new WSDLDefinitionsFactory();
   }

   /**
    * Set a feature on the underlying reader
    */
   public void setFeature(String name, boolean value) throws IllegalArgumentException
   {
      features.put(name, new Boolean(value));
   }

   /**
    * Read the wsdl document from the given URL
    */
   public WSDLDefinitions parse(URL wsdlLocation) throws WSDLException
   {
      if (wsdlLocation == null)
         throw new IllegalArgumentException("URL cannot be null");

      log.debug("parse: " + wsdlLocation.toExternalForm());
      
      EntityResolver entityResolver = new JBossWSEntityResolver();
      WSDLDefinitions wsdlDefinitions = null;
      try
      {
         Document wsdlDoc = getDocument(wsdlLocation);
         String defaultNamespace = getDefaultNamespace(wsdlDoc);
         if (Constants.NS_WSDL20.equals(defaultNamespace))
         {
            WSDL20Reader wsdlReader = new WSDL20Reader();

            // Setup reader features
            Iterator it = features.entrySet().iterator();
            while (it.hasNext())
            {
               Map.Entry entry = (Map.Entry)it.next();
               String key = (String)entry.getKey();
               Boolean flag = (Boolean)entry.getValue();
               wsdlReader.setFeature(key, flag.booleanValue());
            }

            wsdlDefinitions = wsdlReader.readWSDL(new WSDLLocatorImpl(entityResolver, wsdlLocation));
            wsdlDefinitions.setWsdlDocument(wsdlDoc);
         }
         else if (Constants.NS_WSDL11.equals(defaultNamespace))
         {
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
            WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
            wsdlReader.setFeature("javax.wsdl.verbose", false);

            // Setup reader features
            Iterator it = features.entrySet().iterator();
            while (it.hasNext())
            {
               Map.Entry entry = (Map.Entry)it.next();
               String key = (String)entry.getKey();
               Boolean flag = (Boolean)entry.getValue();
               wsdlReader.setFeature(key, flag.booleanValue());
            }

            // Set EntityResolver in patched version of wsdl4j-1.5.2jboss
            ((WSDLReaderImpl)wsdlReader).setEntityResolver(entityResolver);            

            Definition definition = wsdlReader.readWSDL(new WSDLLocatorImpl(entityResolver, wsdlLocation));
            wsdlDefinitions = new WSDL11Reader().processDefinition(definition, wsdlLocation);
            wsdlDefinitions.setWsdlDocument(wsdlDoc);
         }
         else
            throw new WSDLException("Invalid default namespace: " + defaultNamespace);

         if (log.isTraceEnabled())
         {
            StringWriter stwr = new StringWriter();
            WSDL20Writer wsdl20Writer = new WSDL20Writer(wsdlDefinitions);
            wsdl20Writer.write(stwr);
            log.trace("\n" + stwr.toString());
         }
      }
      catch (WSDLException e)
      {
         throw e;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception e)
      {
         throw new WSDLException(e);
      }

      return wsdlDefinitions;
   }

   /** Get the WSDL document.
    */
   private Document getDocument(URL wsdlLocation) throws WSDLException
   {
      try
      {
         InputStream wsdlInputStream = wsdlLocation.openStream();
         try
         {
            DocumentBuilder builder = DOMUtils.getDocumentBuilder();
            return builder.parse(wsdlInputStream);
         }
         finally
         {
            wsdlInputStream.close();
         }
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception e)
      {
         throw new WSDLException(e);
      }
   }

   /** Get the default namespace for the given WSDL
    */
   private String getDefaultNamespace(Document wsdlDoc) throws WSDLException
   {
      Element root = wsdlDoc.getDocumentElement();
      String defaultNamespace = root.getNamespaceURI();
      return defaultNamespace;
   }
}
