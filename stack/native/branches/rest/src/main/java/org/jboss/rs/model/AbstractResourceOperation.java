package org.jboss.rs.model;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;

/**
 * Common base class for {@link org.jboss.rs.model.ResourceLocator}
 * and {@link org.jboss.rs.model.ResourceMethod}
 */
abstract class AbstractResourceOperation extends AbstractRegexResolveable
{
   protected String uriTemplate;
   protected Method invocationTarget;
   protected ParameterBinding parameterBinding;
   protected boolean frozen;

   AbstractResourceOperation(String uriTemplate, Method invocationTarget)
   {
      this.uriTemplate = uriTemplate;
      this.invocationTarget = invocationTarget;
   }

   void freeze()
   {
      // We need to know which param belongs to what regex group
      final Map<String, Integer> regexInfo = new HashMap<String, Integer>();
      UriParamHandler collectRegexInfo = new UriParamHandler()
      {
         public void newUriParam(int regexGroup, String paramName)
         {
            regexInfo.put(paramName, regexGroup);
         }
      };

      setupRegexPatterns(this.uriTemplate, collectRegexInfo);

      setupParameterBinding(regexInfo);

      // Lock instance
      this.frozen = true;
   }

   private void setupParameterBinding(Map<String, Integer> regexInfo)
   {
      // Create ParameterBindig
      this.parameterBinding = new ParameterBinding(this.regexPattern);

      // Annotations on method parameters
      this.parameterBinding.registerParameterAnnotations(invocationTarget);

      // Additional info abpout the regex binding
      for(String paramName : regexInfo.keySet())
      {
         int group = regexInfo.get(paramName);
         this.parameterBinding.registerRegexGroupForParam(group, paramName);
      }
   }

   public ParameterBinding getParameterBinding()
   {
      assert frozen;
      return parameterBinding;
   }

   public OperationBinding getOperationBinding()
   {
      assert frozen;
      return new OperationBinding(this.invocationTarget);
   }
}
