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
package org.jboss.rs.model;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class RegexQualifier implements Comparable
{
   public final int numGroups;
   public final int patternLength;
   public final String nextUriToken;

   public static RegexQualifier NONE = new RegexQualifier(0,0,"");
   
   RegexQualifier(int matchingGroups, int patternLenght, String uriToken)
   {
      this.numGroups = matchingGroups;
      this.patternLength = patternLenght;

      if(null == uriToken)
         throw new IllegalArgumentException("Null UriToken");
      
      this.nextUriToken = uriToken;
   }

   public int compareTo(Object o)
   {
      if(! (o instanceof RegexQualifier) )
         throw new IllegalArgumentException("Cannot compare RegexQualifier.class to " + o.getClass());

      RegexQualifier to = (RegexQualifier)o;
      if(to.numGroups <this.numGroups)
         return -1;
      else if(to.numGroups >this.numGroups)
         return 1;
      else
      {
         // bigger pattern but same number of matching groups
         // means it's less accurate
         if(to.patternLength<this.patternLength)
            return 1;
         else if(to.patternLength>this.patternLength)
            return -1;
         else
            return 0;
      }

   }

   public String toString()
   {
      return "RegexQualifier{groups="+ numGroups +", patternLength="+patternLength+"}";
   }
}