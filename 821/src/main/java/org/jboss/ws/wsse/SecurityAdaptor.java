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
package org.jboss.ws.wsse;

// $Id: SecurityAdaptor.java 314 2006-05-11 10:57:59Z thomas.diesler@jboss.com $

import java.security.Principal;

/**
 * A container idependent security adaptor
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-May-2005
 */
public interface SecurityAdaptor 
{
   void setPrincipal(Principal pricipal);
   void setCredential(Object credential);
}
