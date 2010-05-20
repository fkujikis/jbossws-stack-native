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
package org.jboss.ws.xop;

import org.apache.xerces.xs.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Scans complex type definitions for nested XOP type declarations.
 * A XOP type declaration is identified as a complex type
 * that derives from xsd:base64Binary, i.e:
 *
 * <code> <pre>
 * &lt;xs:complexType name="MyXOPElement" >
 *   &lt;xs:simpleContent>
 *       &lt;xs:extension base="xs:base64Binary" >
 *           &lt;xs:attribute ref="xmime:contentType" />
 *       &lt;/xs:extension>
 *   &lt;/xs:simpleContent>
 * &lt;/xs:complexType>
 * </pre></code>
 *
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since Jun 9, 2006
 * @version $Id$
 */
public class XOPScanner {

   // avoid circular scans
   private List<String> scannedItems = new ArrayList<String>();

   /**
    * Query a complex type for nested XOP type definitions.
    */
   public XSTypeDefinition findXOPTypeDef(XSTypeDefinition typeDef)
   {
      XSTypeDefinition result = null;

      if(typeDef instanceof XSComplexTypeDefinition)
      {
         XSComplexTypeDefinition complexTypeDef = (XSComplexTypeDefinition)typeDef;
         String name = complexTypeDef.getName();
         String namespace = complexTypeDef.getNamespace()!=null ? complexTypeDef.getNamespace():"";
         if(name!=null)
         {
            String typeKey = namespace+":"+name;

            if(scannedItems.contains(typeKey))
            {
               return null;
            }
            else
            {
               scannedItems.add(typeKey);
            }
         }

         //System.out.println("ct -> " + complexTypeDef);

         /*for(int x=0; x<complexTypeDef.getAttributeUses().getLength(); x++)
         {
            // TODO: access content type attribute value
            XSAttributeUseImpl att = (XSAttributeUseImpl)complexTypeDef.getAttributeUses().item(x);
            //System.out.println("! " + att.getAttrDeclaration().getName());
         }*/

         // An XOP parameter is detected if it is a complex type
         // that derives from xsd:base64Binary
         if (complexTypeDef.getSimpleType() != null)
         {
            String typeName = complexTypeDef.getSimpleType().getName();
            if ("base64Binary".equals(typeName))
               return complexTypeDef;
         }
         else
         {

            XSModelGroup xm = null;
            if(complexTypeDef.getContentType() != XSComplexTypeDefinition.CONTENTTYPE_EMPTY)
            {
               XSParticle xp = complexTypeDef.getParticle();
               if (xp != null)
               {
                  XSTerm xterm = xp.getTerm();
                  if(xterm instanceof XSModelGroup)
                  {
                     xm = (XSModelGroup)xterm;
                     //System.out.println("xm -> " + xm);

                     XSObjectList xo = xm.getParticles();

                     // interate over nested particles
                     for(int i=0; i<xm.getParticles().getLength(); i++ )
                     {
                        XSTerm xsterm = ((XSParticle)xo.item(i)).getTerm();

                        // Can be either XSModelGroup, XSWildcard, XSElementDeclaration
                        // We only proceed with XSElementDeclaration
                        if(xsterm instanceof XSElementDeclaration)
                        {
                           XSElementDeclaration xe = (XSElementDeclaration)xsterm;
                           XSTypeDefinition nestedTypeDef = xe.getTypeDefinition();

                           //System.out.println("Query nested -> " + xe.getName());
                           result = findXOPTypeDef(nestedTypeDef);
                        }
                     }
                  }
               }
            }

         }

         //System.out.println("result -> " + result);

      }

      return result;

   }

   public void reset()
   {
      scannedItems.clear();
   }

}