/*
* JBoss, Home of Professional Open Source.
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
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
package org.jboss.ws.extensions.security.operation;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping;
import org.jboss.ws.WSException;
import org.jboss.ws.metadata.wsse.Authorize;

/**
 * Operation to authenticate and check the authorisation of the
 * current user.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 * @since December 23rd 2008
 */
public class AuthorizeOperation
{

   private static final Logger log = Logger.getLogger(AuthorizeOperation.class);

   private Authorize authorize;

   private AuthenticationManager am;

   private RealmMapping rm;

   public AuthorizeOperation(Authorize authorize)
   {
      this.authorize = authorize;

      try
      {
         Context ctx = new InitialContext();
         Object obj = ctx.lookup("java:comp/env/security/securityMgr");
         am = (AuthenticationManager)obj;
         rm = (RealmMapping)am;
      }
      catch (NamingException ne)
      {
         throw new WSException("Unable to lookup AuthenticationManager", ne);
      }

   }

   public void process()
   {
      log.trace("About to check authorization, using security domain '" + am.getSecurityDomain() + "'");
      // Step 1 - Authenticate using currently associated principals.

      // Step 2 - If unchecked all ok so return.

      // Step 3 - If roles specified check user in role. 

   }

}
