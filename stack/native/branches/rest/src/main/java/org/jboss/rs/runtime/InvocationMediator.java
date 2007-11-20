package org.jboss.rs.runtime;

import org.jboss.rs.model.ResourceLocator;
import org.jboss.rs.model.StatefulResourceResolver;
import org.jboss.rs.model.ResourceMethod;
import org.jboss.rs.ResourceError;

import java.util.Stack;

/**
 * A resource invocation can go through many locator invocations
 * before invoking the final resource method. A mediator hides the implementation
 * details of this process from outermost components.
 *
 */
public class InvocationMediator
{
   private RuntimeContext runtimeContext;

   public InvocationMediator(RuntimeContext rt)
   {
      this.runtimeContext = rt;
   }

   public Object invoke() throws ResourceError
   {
      Object result = null;
      
      StatefulResourceResolver resolver = StatefulResourceResolver.newInstance(runtimeContext);
      ResourceMethod resourceMethod = resolver.resolve();

      // evaluate locator stack
      Object subResourceInstance = null;
      Stack<ResourceLocator> visitedLocators = resolver.getVisitedLocator();
      while(!visitedLocators.isEmpty())
      {
         ResourceLocator loc = visitedLocators.pop();
         runtimeContext.setWorkingPath(resolver.getLocatorWorkingPath(loc));

         InvocationBuilder builder = new DefaultInvocationBuilder();
         builder.addInvocationModel(loc.getParameterBinding());
         builder.addInvocationModel(loc.getOperationBinding());
         Invocation locatorInvocation = builder.build(runtimeContext);

         InvocationHandler bridgeInvoker = new DefaultInvocationHandler();
         subResourceInstance = bridgeInvoker.invoke(locatorInvocation);
         result = subResourceInstance; // best match
      }

      if(resourceMethod!=null)
      {
         // create an Invocation instance
         InvocationBuilder builder = new DefaultInvocationBuilder();
         runtimeContext.setWorkingPath(resolver.getMethodWorkingPath());

         if(subResourceInstance!=null)
            builder.addInvocationModel(new PresetInvocationTarget(subResourceInstance));

         builder.addInvocationModel( resourceMethod.getParameterBinding() );
         builder.addInvocationModel( resourceMethod.getOperationBinding() );
         Invocation invocation = builder.build(runtimeContext);

         // invoke it
         InvocationHandler invoker = new DefaultInvocationHandler();
         result = invoker.invoke(invocation); // more fine grained match
      }      

      return result;
   }
}
