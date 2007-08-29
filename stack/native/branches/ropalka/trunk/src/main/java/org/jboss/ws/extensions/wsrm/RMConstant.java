package org.jboss.ws.extensions.wsrm;

final class RMConstant
{
   private static final String PREFIX = RMConstant.class.getName();
   static final String TARGET_ADDRESS = PREFIX + ".targetAddress";
   static final String ONE_WAY_OPERATION = PREFIX + ".oneWayOperation";
   static final String INVOCATION_CONTEXT = PREFIX + ".invocationContext";
   static final String MARSHALLER = PREFIX + ".marshaller";
   static final String UNMARSHALLER = PREFIX + ".unmarshaller";
   static final String SERIALIZATION_CONTEXT = PREFIX + ".serializationContext";
   static final String REMOTING_INVOCATION_CONTEXT = PREFIX + ".remotingInvocationContext";
   static final String REMOTING_CONFIGURATION_CONTEXT = PREFIX + ".remotingConfigurationContext";
   
   private RMConstant()
   {
      // no instances
   }
}
