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

import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A factory for creating SOAPMessage objects.
 *
 * A SAAJ client can create a MessageFactory object using the method newInstance, as shown in the following line of code.
 *
 *    MessageFactory mf = MessageFactory.newInstance();
 *
 * A standalone client (a client that is not running in a container) can use the newInstance method to create a MessageFactory object.
 *
 * All MessageFactory objects, regardless of how they are created, will produce SOAPMessage objects that have the following elements by default:
 *
 *    A SOAPPart object
 *    A SOAPEnvelope object
 *    A SOAPBody object
 *    A SOAPHeader object
 *
 * MessageFactory objects can be initialized with a JAXM profile. In such a case it will produce messages that also
 * come prepopulated with additional entries in the SOAPHeader object and the SOAPBody object. The content of a new
 * SOAPMessage object depends on which of the two MessageFactory methods is used to create it.
 *
 *    createMessage() -- message has no content
 *    This is the method clients would normally use to create a request message.
 *
 *    createMessage(MimeHeaders, java.io.InputStream) -- message has content from the InputStream object and headers from the MimeHeaders object
 *    This method can be used internally by a service implementation to create a message that is a response to a request.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public abstract class MessageFactory
{
   // provide logging
   private static Logger log = Logger.getLogger(MessageFactory.class);

   private static final String DEFAULT_MESSAGE_FACTORY = "org.jboss.ws.soap.MessageFactoryImpl";
   private static final String[] alternativeFactories = new String[]{
      "org.jboss.axis.soap.MessageFactoryImpl"
   };

   /** Creates a new MessageFactory object that is an instance of the default implementation.
    */
   public static MessageFactory newInstance() throws SOAPException
   {
      PrivilegedAction action = new PropertyAccessAction(MessageFactory.class.getName(), DEFAULT_MESSAGE_FACTORY);
      String factoryName = (String)AccessController.doPrivileged(action);

      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      try
      {
         try
         {
            Class factoryClass = loader.loadClass(factoryName);
            return (MessageFactory)factoryClass.newInstance();
         }
         catch (ClassNotFoundException e)
         {
            // Throw the exception if the user asked for a specific factory
            if (factoryName.equals(DEFAULT_MESSAGE_FACTORY) == false)
               throw e;

            for (int i = 0; i < alternativeFactories.length; i++)
            {
               factoryName = alternativeFactories[i];
               try
               {
                  Class factoryClass = loader.loadClass(factoryName);
                  return (MessageFactory)factoryClass.newInstance();
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
         throw new SOAPException("Failed to create MessageFactory: " + factoryName, t);
      }

      throw new SOAPException("Cannot find MessageFactory implementation");
   }

   /**
    * Creates a new SOAPMessage object with the default SOAPPart, SOAPEnvelope, SOAPBody, and SOAPHeader objects.
    * Profile-specific message factories can choose to prepopulate the SOAPMessage object with profile-specific headers.
    *
    * Content can be added to this message's SOAPPart object, and the message can be sent "as is" when a message
    * containing only a SOAP part is sufficient. Otherwise, the SOAPMessage object needs to create one or more
    * AttachmentPart objects and add them to itself. Any content that is not in XML format must be in an AttachmentPart object.
    *
    * @return a new SOAPMessage object
    * @throws SOAPException if a SOAP error occurs
    */
   public abstract SOAPMessage createMessage() throws SOAPException;

   /**
    * Internalizes the contents of the given InputStream object into a new SOAPMessage object and returns the SOAPMessage object.
    *
    * @param headers the transport-specific headers passed to the message in a transport-independent fashion for creation of the message
    * @param in the InputStream object that contains the data for a message
    * @return a new SOAPMessage object containing the data from the given InputStream object
    * @throws IOException if there is a problem in reading data from the input stream
    * @throws SOAPException  if the message is invalid
    */
   public abstract SOAPMessage createMessage(MimeHeaders headers, InputStream in) throws IOException, SOAPException;

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
