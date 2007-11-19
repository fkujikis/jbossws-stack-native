package org.jboss.rs.runtime;

public class PresetInvocationTarget implements InvocationModel
{
   private Object invocationInstance;


   public PresetInvocationTarget(Object invocationInstance) {
      assert invocationInstance!=null;
      this.invocationInstance = invocationInstance;
   }

   public void accept(Invocation invocation) {

      invocation.setTargetInstance(invocationInstance);  
   }
}
