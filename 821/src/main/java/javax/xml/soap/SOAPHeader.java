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

import java.util.Iterator;

/**
 * A representation of the SOAP header element. A SOAP header element consists
 * of XML data that affects the way the application-specific content is
 * processed by the message provider. For example, transaction semantics,
 * authentication information, and so on, can be specified as the content of a
 * SOAPHeader object.
 * 
 * A SOAPEnvelope object contains an empty SOAPHeader object by default. If the
 * SOAPHeader object, which is optional, is not needed, it can be retrieved and
 * deleted with the following line of code. The variable se is a SOAPEnvelope
 * object.
 *
 *    se.getHeader().detachNode();
 *
 * A SOAPHeader object is created with the SOAPEnvelope method addHeader.
 * This method, which creates a new header and adds it to the envelope, may be
 * called only after the existing header has been removed.
 *
 *    se.getHeader().detachNode();
 *    SOAPHeader sh = se.addHeader();
 *
 * A SOAPHeader object can have only SOAPHeaderElement objects as its
 * immediate children. The method addHeaderElement creates a new HeaderElement
 * object and adds it to the SOAPHeader object. In the following line of code,
 * the argument to the method addHeaderElement is a Name object that is the
 * name for the new HeaderElement object.
 *
 *    SOAPHeaderElement shElement = sh.addHeaderElement(name);
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface SOAPHeader extends SOAPElement
{
   /**
    * Creates a new SOAPHeaderElement object initialized with the specified name and adds it to this SOAPHeader object.
    *
    * @param name a Name object with the name of the new SOAPHeaderElement object
    * @return the new SOAPHeaderElement object that was inserted into this SOAPHeader object
    * @throws SOAPException if a SOAP error occurs
    */
   public SOAPHeaderElement addHeaderElement(Name name) throws SOAPException;

   /**
    * Returns an Iterator over all the SOAPHeaderElement objects in this SOAPHeader object.
    *
    * @return an Iterator object over all the SOAPHeaderElement objects contained by this SOAPHeader
    */
   public Iterator examineAllHeaderElements();

   /**
    * Returns an Iterator over all the SOAPHeaderElement objects in this SOAPHeader object that have the specified actor.
    * An actor is a global attribute that indicates the intermediate parties that should process a message before it
    * reaches its ultimate receiver. An actor receives the message and processes it before sending it on to the next actor.
    * The default actor is the ultimate intended recipient for the message, so if no actor attribute is included in a
    * SOAPHeader object, it is sent to the ultimate receiver along with the message body.
    *
    * @param actor a String giving the URI of the actor for which to search
    * @return an Iterator object over all the SOAPHeaderElement objects that contain the specified actor and are marked as MustUnderstand
    */
   public Iterator examineHeaderElements(String actor);

   /**
    * Returns an Iterator over all the SOAPHeaderElement objects in this SOAPHeader object that have the specified
    * actor and that have a MustUnderstand attribute whose value is equivalent to true.
    *
    * @param actor a String giving the URI of the actor for which to search
    * @return an Iterator object over all the SOAPHeaderElement objects that contain the specified actor and are marked as MustUnderstand
    */
   public Iterator examineMustUnderstandHeaderElements(String actor);

   /**
    * Returns an Iterator over all the SOAPHeaderElement objects in this SOAPHeader object and detaches
    * them from this SOAPHeader object.
    *
    * @return an Iterator object over all the SOAPHeaderElement objects contained by this SOAPHeader
    */
   public Iterator extractAllHeaderElements();

   /**
    * Returns an Iterator over all the SOAPHeaderElement objects in this SOAPHeader object that have the specified actor
    * and detaches them from this SOAPHeader object.
    *
    * This method allows an actor to process the parts of the SOAPHeader object that apply to it and to remove them
    * before passing the message on to the next actor.
    *
    * @param actor a String giving the URI of the actor for which to search
    * @return an Iterator object over all the SOAPHeaderElement objects that contain the specified actor and are marked as MustUnderstand
    */
   public Iterator extractHeaderElements(String actor);
}
