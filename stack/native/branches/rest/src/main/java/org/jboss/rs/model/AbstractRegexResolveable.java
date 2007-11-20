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

import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
abstract class AbstractRegexResolveable<T>
{
   public final String URI_PARAM_PATTERN        = "(.*?)";
   public final String CHILD_SUFFIX_PATTERN     = "(/.*)?";
   public final String CHILDLESS_SUFFIX_PATTERN = "(/)?";

   protected Pattern regexPattern;
   private boolean isCompiled;

   protected T parent = null;   

   protected void initFromUriTemplate(String uriTemplate)
   {
      setupRegexPatterns(uriTemplate, null);
   }

   protected void setupRegexPatterns(String uriTemplate, UriParamHandler handler)
   {
      assert uriTemplate!=null;
      assert !uriTemplate.startsWith("/");
      
      StringTokenizer tokenizer = new StringTokenizer(uriTemplate, "/");
      StringBuffer patternBuffer = new StringBuffer();
      int groupIndex = 1; // matching regex groups start with 1
      while(tokenizer.hasMoreTokens())
      {
         String tok = tokenizer.nextToken();         
         if(isUriParam(tok))
         {

            if(groupIndex>1)
            {
               // i.e. 'spec/{name}'
               patternBuffer.append("(/)");
               groupIndex++;
            }

            if(handler != null)
            {
               // register uri param callback
               String paramName = tok.substring(1, tok.length()-1);
               handler.newUriParam(groupIndex, paramName);

            }

            patternBuffer.append( regexFromUriParam(tok) );
         }
         else
         {
            patternBuffer.append( regexFromPathSegment(tok) );
         }

         groupIndex++;
      }

      if(hasChildren())
         patternBuffer.append(CHILD_SUFFIX_PATTERN);
      else
         patternBuffer.append(CHILDLESS_SUFFIX_PATTERN);


      String patternString = patternBuffer.toString();
      
      this.regexPattern = Pattern.compile(patternString);
      this.isCompiled = true;
   }

   /**
    *  
    * @param input a URI string
    * @return a comparable or <code>null</code> if no match
    */
   public RegexQualifier resolve(String input)
   {      
      assert isCompiled;
      assert input != null;
      assert !input.startsWith("/");

      RegexQualifier qualifier = null;

      Matcher matcher = regexPattern.matcher(input);
      if(! matcher.matches() )
         return qualifier;

      String lastGroup = matcher.group( matcher.groupCount() );
      if(null == lastGroup)
      {
         lastGroup = "";
      }
      else if(lastGroup.startsWith("/"))
      {
         lastGroup = lastGroup.substring(1);         
      }
      
      qualifier = new RegexQualifier(
        getMatchingGroups(matcher),
        regexPattern.pattern().length(),
        lastGroup
      );

      return qualifier;
   }

   private static int getMatchingGroups(Matcher m)
   {
      int matchingGroups = 0;
      for(int i=1; i<=m.groupCount(); i++)
      {
         String s = m.group(i);
         if(s!=null && !"".equals(s)) matchingGroups++;
      }
      return matchingGroups;
   }

   private String regexFromPathSegment(String tok)
   {
      return "(\\b"+tok+"\\b)";  
   }

   private String regexFromUriParam(String tok)
   {
      return URI_PARAM_PATTERN;
   }

   private boolean isUriParam(String token)
   {
      return token.startsWith("{") && token.endsWith("}");
   }

   public T getParent() {
      return parent;
   }

   abstract boolean hasChildren();

   abstract void freeze();

   public interface UriParamHandler
   {
      void newUriParam(int regexGroup, String paramName);
   }

}
