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
package org.jboss.test.rs.model;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class RunRegex
{
   public static void main(String[] args)
   {
      String s = "spec";
      System.out.println("> " +s);
      Pattern p = Pattern.compile("(\\bspec\\b)(.*?)(/)?");
      //Pattern p = Pattern.compile("(\\bspec\\b)(/)?");
      Matcher m = p.matcher(s);

      System.out.println("? " + m.matches());            

      int matchingGroups = 0;
      for(int i=1; i<=m.groupCount(); i++)
      {
         String s1 = m.group(i);
         System.out.println("g '" + s1 + "'");
         if(s1!=null && "".equals(s1)==false)
            matchingGroups++;
      }

      System.out.println("! "+matchingGroups);
      System.out.println("< "+m.group(m.groupCount()));
      System.out.println("---");


   }
}
