package org.jboss.ws.utils;

import org.jboss.ws.soap.SOAPMessageContextImpl;
import org.jboss.ws.wsse.SecurityStore;

import java.util.Stack;

/**
 * Maintain thread locals at a single point.
 *
 * @author Heiko Braun, <heiko@openj.net>
 * @since 10-Apr-2006
 */
public class ThreadLocalAssociation {

   /**
    * Handles invocations on MDB endpoints.
    */
   private static ThreadLocal invokerMDBAssoc = new ThreadLocal();

   /**
    * SOAP message context
    * @see org.jboss.ws.soap.MessageContextAssociation
    */
   private static ThreadLocal<Stack<SOAPMessageContextImpl>> msgContextAssoc = new InheritableThreadLocal<Stack<SOAPMessageContextImpl>>();

   /**
    * @see org.jboss.ws.wsse.STRTransform
    */
   private static InheritableThreadLocal<SecurityStore> strTransformAssoc = new InheritableThreadLocal<SecurityStore>();

   private static ThreadLocal<Boolean> DOMExpansionAssoc = new ThreadLocal<Boolean>()
   {
      protected Boolean initialValue() {
         return Boolean.TRUE;
      }
   };
   public static ThreadLocal localInvokerMDBAssoc() {
      return invokerMDBAssoc;
   }

   public static ThreadLocal<Stack<SOAPMessageContextImpl>> localMsgContextAssoc() {
      return msgContextAssoc;
   }

   public static ThreadLocal<SecurityStore> localStrTransformAssoc() {
      return strTransformAssoc;
   }

   public static ThreadLocal<Boolean> localDomExpansion()
   {
      return DOMExpansionAssoc;
   }
   public static void clear() {
      invokerMDBAssoc.set(null);
      msgContextAssoc.set(null);
      strTransformAssoc.set(null);
      DOMExpansionAssoc.set(Boolean.FALSE);
   }
}
