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

import java.util.Locale;

/** An element in the SOAPBody object that contains error and/or status
 * information. This information may relate to errors in the SOAPMessage
 * object or to problems that are not related to the content in the message
 * itself. Problems not related to the message itself are generally errors in
 * processing, such as the inability to communicate with an upstream server. 
 * 
 * The SOAPFault interface provides methods for retrieving the information
 * contained in a SOAPFault object and for setting the fault code, the fault
 * actor, and a string describing the fault. A fault code is one of the codes
 * defined in the SOAP 1.1 specification that describe the fault. An actor is
 * an intermediate recipient to whom a message was routed. The message path may
 * include one or more actors, or, if no actors are specified, the message goes
 * only to the default actor, which is the final intended recipient.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface SOAPFault extends SOAPBodyElement
{
   /**
    * Creates an optional Detail object and sets it as the Detail object for this SOAPFault  object.
    *
    * It is illegal to add a detail when the fault already contains a detail.
    * Therefore, this method should be called only after the existing detail has been removed.
    * @return the new Detail object
    * @throws SOAPException  if this SOAPFault object already contains a valid Detail object
    */
   public Detail addDetail() throws SOAPException;

   /**
    * Returns the optional detail element for this SOAPFault  object.
    *
    * A Detail object carries application-specific error information related to SOAPBodyElement objects.
    *
    * @return a Detail object with application-specific error information
    */
   public Detail getDetail();

   /**
    * Gets the fault actor for this SOAPFault object.
    * @return a String giving the actor in the message path that caused this SOAPFault object
    */
   public String getFaultActor();

   /**
    * Gets the fault code for this SOAPFault object.
    * @return a String with the fault code
    */
   public String getFaultCode();

   /**
    * Gets the mandatory SOAP 1.1 fault code for this SOAPFault object as a SAAJ Name object.
    * The SOAP 1.1 specification requires the value of the "faultcode" element to be of type QName.
    * This method returns the content of the element as a QName in the form of a SAAJ Name object.
    * This method should be used instead of the getFaultCode method since it allows applications to
    * easily access the namespace name without additional parsing.
    *
    * In the future, a QName object version of this method may also be added.
    *
    * @return a Name representing the faultcode
    */
   public Name getFaultCodeAsName();

   /**
    * Gets the fault string for this SOAPFault object.
    * @return a String giving an explanation of the fault
    */
   public String getFaultString();

   /**
    * Gets the locale of the fault string for this SOAPFault object.
    * @return a Locale object indicating the native language of the fault string or null if no locale was specified
    */
   public Locale getFaultStringLocale();

   /**
    * Sets this SOAPFault object with the given fault actor.
    *
    * The fault actor is the recipient in the message path who caused the fault to happen.
    *
    * @param faultActor  a String identifying the actor that caused this SOAPFault object
    * @throws SOAPException  if there was an error in adding the faultActor to the underlying XML tree.
    */
   public void setFaultActor(String faultActor) throws SOAPException;

   /**
    * Sets this SOAPFault object with the give fault code.
    *
    * Fault codes, which given information about the fault, are defined in the SOAP 1.1 specification.
    * This element is mandatory in SOAP 1.1. Because the fault code is required to be a QName
    * it is preferable to use the setFaultCode(Name) form of this method.
    *
    * @param faultCode a String giving the fault code to be set.
    * It must be of the form "prefix:localName" where the prefix has been defined in a namespace declaration.
    * @throws SOAPException if there was an error in adding the faultCode to the underlying XML tree.
    */
   public void setFaultCode(String faultCode) throws SOAPException;

   /**
    * Sets this SOAPFault object with the given fault code.
    *
    * Fault codes, which give information about the fault, are defined in the SOAP 1.1 specification.
    * A fault code is mandatory and must be of type QName. This method provides a convenient way to set a fault code.
    * For example,
    *
    *    SOAPEnvelope se = ...;
    *    //Create a qualified name in the SOAP namespace with a localName
    *    // of "Client".  Note that prefix parameter is optional and is null
    *    // here which causes the implementation to use an appropriate prefix.
    *    Name qname = se.createName("Client", null, SOAPConstants.URI_NS_SOAP_ENVELOPE);
    *    SOAPFault fault = ...;
    *    fault.setFaultCode(qname);
    *
    * It is preferable to use this method over setFaultCode(String).
    *
    * @param faultCodeQName  a Name object giving the fault code to be set. It must be namespace qualified.
    * @throws SOAPException if there was an error in adding the faultcode element to the underlying XML tree.
    */
   public void setFaultCode(Name faultCodeQName) throws SOAPException;

   /**
    * Sets the fault string for this SOAPFault object to the given string.
    *
    * @param faultString  a String giving an explanation of the fault
    * @throws SOAPException  if there was an error in adding the faultString to the underlying XML tree.
    */
   public void setFaultString(String faultString) throws SOAPException;


   /**
    * Sets the fault string for this SOAPFault object to the given string and localized to the given locale.
    *
    * @param faultString  a String giving an explanation of the fault
    * @param locale a Locale object indicating the native language of the faultString
    * @throws SOAPException
    */
   public void setFaultString(String faultString, Locale locale) throws SOAPException;
}
