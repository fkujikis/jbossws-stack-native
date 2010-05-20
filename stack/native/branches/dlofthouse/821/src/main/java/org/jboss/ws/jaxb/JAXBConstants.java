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
package org.jboss.ws.jaxb;


// $Id$

/** JBoss JAXB Constants
 *
 * @author Thomas.Diesler@jboss.org
 * @since 18-Oct-2004
 */
public interface JAXBConstants
{
   /** Set this property with a Reader to the xsdSchema */
   String JAXB_SCHEMA_READER = "org.jboss.jaxb.xsd.reader";
   /** Set this property with a the QName of the root element */
   String JAXB_ROOT_QNAME = "org.jboss.jaxb.root.qname";
   /** Set this property with a the QName of the root type */
   String JAXB_TYPE_QNAME = "org.jboss.jaxb.type.qname";
   /** Set this property with an instance of JavaWsdlMapping */
   String JAXB_JAVA_MAPPING = "org.jboss.jaxb.java.mapping";
   /** Set this property to the XSModel to pull schema info from */
   String JAXB_XS_MODEL = "org.jboss.jaxb.xsd.xsmodel";
}
