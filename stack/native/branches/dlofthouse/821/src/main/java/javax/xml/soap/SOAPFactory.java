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
package javax.xml.soap;

// $Id$

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jboss.logging.Logger;

/** SOAPFactory is a factory for creating various objects that exist in the SOAP XML tree.
 *
 * SOAPFactory can be used to create XML fragments that will eventually end up in the SOAP part.
 * These fragments can be inserted as children of the SOAPHeaderElement or SOAPBodyElement or
 * SOAPEnvelope or other SOAPElement objects.
 *
 * SOAPFactory also has methods to create javax.xml.soap.Detail objects as well as java.xml.soap.Name objects.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public abstract class SOAPFactory
{
   // provide logging
   private static Logger log = Logger.getLogger(SOAPFactory.class);

   private static final String DEFAULT_SOAP_FACTORY = "org.jboss.ws.soap.SOAPFactoryImpl"; 
   private static final String[] alternativeFactories = new String[]{
      "org.jboss.axis.soap.SOAPFactoryImpl"
   };

   /** Creates a new instance of SOAPFactory.
    *
    * @return a new instance of a SOAPFactory
    * @throws SOAPException if there was an error creating the default SOAPFactory
    */
   public static SOAPFactory newInstance() throws SOAPException
   {
      PrivilegedAction action = new PropertyAccessAction(SOAPFactory.class.getName(), DEFAULT_SOAP_FACTORY);
      String factoryName = (String)AccessController.doPrivileged(action);

      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      try
      {
         try
         {
            Class factoryClass = loader.loadClass(factoryName);
            return (SOAPFactory)factoryClass.newInstance();
         }
         catch (ClassNotFoundException e)
         {
            // Throw the exception if the user asked for a specific factory
            if (factoryName.equals(DEFAULT_SOAP_FACTORY) == false)
               throw e;

            for (int i = 0; i < alternativeFactories.length; i++)
            {
               factoryName = alternativeFactories[i];
               try
               {
                  Class factoryClass = loader.loadClass(factoryName);
                  return (SOAPFactory)factoryClass.newInstance();
               }
               catch (ClassNotFoundException e1)
               {
                  log.debug("Cannot load factory: " + factoryName);
               }
            }
         }
      }
      catch (Throwable t)
      {
         throw new SOAPException("Failed to create SOAPFactory: " + factoryName, t);
      }

      throw new SOAPException("Cannot find SOAPFactory implementation");
   }

   /** Creates a new Detail object which serves as a container for DetailEntry objects.
    *
    *  This factory method creates Detail objects for use in situations where it is not practical to use the SOAPFault abstraction.
    *
    * @return a Detail object
    * @throws SOAPException if there is a SOAP error
    */
   public abstract Detail createDetail() throws SOAPException;

   /** Create a SOAPElement object initialized with the given local name.
    *
    * @param localName a String giving the local name for the new element
    * @return the new SOAPElement object that was created
    * @throws SOAPException if there is an error in creating the SOAPElement object
    */
   public abstract SOAPElement createElement(String localName) throws SOAPException;

   /** Create a new SOAPElement object with the given local name, prefix and uri.
    *
    * @param localName a String giving the local name for the new element
    * @param prefix the prefix for this SOAPElement
    * @param uri a String giving the URI of the namespace to which the new element belongs
    * @return the new SOAPElement object that was created
    * @throws SOAPException if there is an error in creating the SOAPElement object
    */
   public abstract SOAPElement createElement(String localName, String prefix, String uri) throws SOAPException;

   /** Create a SOAPElement object initialized with the given Name object.
    *
    * @param name a Name object with the XML name for the new element
    * @return the new SOAPElement object that was created
    * @throws SOAPException if there is an error in creating the SOAPElement object
    */
   public abstract SOAPElement createElement(Name name) throws SOAPException;

   /** Creates a new Name object initialized with the given local name.
    *
    * This factory method creates Name objects for use in situations where it is not practical to use the
    * SOAPEnvelope abstraction.
    * @return
    * @throws SOAPException
    */
   public abstract Name createName(String localName) throws SOAPException;

   /** Creates a new Name object initialized with the given local name, namespace prefix, and namespace URI.
    *
    *  This factory method creates Name objects for use in situations where it is not practical to use the SOAPEnvelope abstraction.
    * 
    * @param localName a String giving the local name
    * @param prefix a String giving the prefix of the namespace
    * @param uri a String giving the URI of the namespace
    * @return a Name object initialized with the given local name, namespace prefix, and namespace URI
    * @throws SOAPException  if there is a SOAP error
    */
   public abstract Name createName(String localName, String prefix, String uri) throws SOAPException;

   private static class PropertyAccessAction implements PrivilegedAction
   {
      private String name;
      private String defaultValue;

      PropertyAccessAction(String name, String defaultValue)
      {
         this.name = name;
         this.defaultValue = defaultValue;
      }

      public Object run()
      {
         return System.getProperty(name, defaultValue);
      }
   }
}
