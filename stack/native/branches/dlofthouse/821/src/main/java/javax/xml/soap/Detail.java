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

/** A container for DetailEntry objects. DetailEntry objects give detailed
 * error information that is application-specific and related to the SOAPBody
 * object that contains it. 
 * 
 * A Detail object, which is part of a SOAPFault object, can be retrieved using
 * the method SOAPFault.getDetail. The Detail interface provides two methods.
 * One creates a new DetailEntry object and also automatically adds it to the
 * Detail object. The second method gets a list of the DetailEntry objects
 * contained in a Detail object. 
 * 
 * The following code fragment, in which sf is a SOAPFault object, gets its
 * Detail object (d), adds a new DetailEntry object to d, and then gets a list
 * of all the DetailEntry objects in d. The code also creates a Name object to
 * pass to the method addDetailEntry. The variable se, used to create the Name
 * object, is a SOAPEnvelope object. 
    Detail d = sf.getDetail();
    Name name = se.createName("GetLastTradePrice", "WOMBAT",
                                "http://www.wombat.org/trader");
    d.addDetailEntry(name);
    Iterator it = d.getDetailEntries();

 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface Detail
   extends SOAPFaultElement
{
   public DetailEntry addDetailEntry(Name name) throws SOAPException;
   public Iterator getDetailEntries();
}
