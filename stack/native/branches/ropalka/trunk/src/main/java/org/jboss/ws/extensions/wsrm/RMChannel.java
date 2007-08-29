package org.jboss.ws.extensions.wsrm;

import static org.jboss.ws.extensions.wsrm.RMConstant.*;

import org.jboss.remoting.Client;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.invocation.OnewayInvocation;
import org.jboss.remoting.marshal.Marshaller;
import org.jboss.remoting.marshal.UnMarshaller;
import org.jboss.ws.core.HTTPMessageImpl;
import org.jboss.ws.core.MessageAbstraction;
import org.jboss.ws.core.MessageTrace;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RM Channel Singleton
 * @author richard.opalka@jboss.com
 */
public class RMChannel
{
   private static final RMChannel INSTANCE = new RMChannel();
   
   private RMChannel()
   {
      super();
      // forbidden inheritance
   }

   public static RMChannel getInstance()
   {
      return INSTANCE;
   }

   private static final ExecutorService rmChannelPool = Executors.newFixedThreadPool(5, new RMThreadFactory());

   private static final class RMThreadFactory implements ThreadFactory
   {
      final ThreadGroup group;
      final AtomicInteger threadNumber = new AtomicInteger(1);
      final String namePrefix = "rm-channel-pool-thread-";
    
      private RMThreadFactory()
      {
         SecurityManager sm = System.getSecurityManager();
         group = (sm != null) ? sm.getThreadGroup() : Thread.currentThread().getThreadGroup();
      }
      
      public Thread newThread(Runnable r)
      {
         Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
         if (t.isDaemon())
            t.setDaemon(false);
         if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
         return t;
      }
   }
   
   private static final class RMMarshaller implements Marshaller
   {
      private static final Marshaller INSTANCE = new RMMarshaller();
      
      public Marshaller cloneMarshaller() throws CloneNotSupportedException
      {
         return getInstance();
      }
      
      public static Marshaller getInstance()
      {
         return INSTANCE;
      }
      
      public void write(Object dataObject, OutputStream output) throws IOException
      {
         if (dataObject instanceof InvocationRequest)
            dataObject = ((InvocationRequest)dataObject).getParameter();

         if (dataObject instanceof OnewayInvocation)
            dataObject = ((OnewayInvocation)dataObject).getParameters()[0];

         if ((dataObject instanceof byte[]) == false)
            throw new IllegalArgumentException("Not a byte array: " + dataObject);

         output.write((byte[])dataObject);
         output.flush();
      }
   }
   
   private static final class RMUnMarshaller implements UnMarshaller
   {
      private static final UnMarshaller INSTANCE = new RMUnMarshaller();

      public UnMarshaller cloneUnMarshaller() throws CloneNotSupportedException
      {
         return getInstance();
      }
      
      public static UnMarshaller getInstance()
      {
         return INSTANCE;
      }
      
      public Object read(InputStream inputStream, Map metadata) throws IOException, ClassNotFoundException
      {
         if (inputStream == null)
            return RMMessageFactory.newMessage(null, new RMMetadata(metadata)); // WSAddressing reply-to

         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         byte[] buffer = new byte[1024];
         int count = -1;
         count = inputStream.read(buffer);
         while (count != -1)
         {
            baos.write(buffer, 0, count);
            count = inputStream.read(buffer);
         }
         return RMMessageFactory.newMessage(baos.toByteArray(), new RMMetadata(metadata));
      }

      public void setClassLoader(ClassLoader classloader)
      {
         // do nothing
      }
   }

   private static final class RMChannelResponse
   {
      private final Throwable fault;
      private final RMMessage result;
      
      public RMChannelResponse(Throwable fault)
      {
         this(null, fault);
      }
      
      public RMChannelResponse(RMMessage result)
      {
         this(result, null);
      }
      
      private RMChannelResponse(RMMessage result, Throwable fault)
      {
         super();
         this.result = result;
         this.fault = fault;
      }
      
      public Throwable getFault()
      {
         return this.fault;
      }
      
      public RMMessage getResponse()
      {
         return this.result;
      }
   }
   
   private static final class RMChannelRequest implements Callable<RMChannelResponse>
   {
      private final RMMessage rmRequest;
      private static final String JBOSSWS_SUBSYSTEM = "jbossws";
      
      private RMChannelRequest(RMMessage rmRequest)
      {
         super();
         this.rmRequest = rmRequest;
      }
      
      public RMChannelResponse call()
      {
         InvokerLocator locator = null;
         try
         {
            locator = new InvokerLocator((String)rmRequest.getMetadata().getContext(INVOCATION_CONTEXT).get(TARGET_ADDRESS));
         }
         catch (MalformedURLException e)
         {
            return new RMChannelResponse(new IllegalArgumentException("Malformed endpoint address", e));
         }

         try
         {
            Client client = new Client(locator, JBOSSWS_SUBSYSTEM, rmRequest.getMetadata().getContext(REMOTING_CONFIGURATION_CONTEXT));
            client.connect();

            client.setMarshaller(RMMarshaller.getInstance());

            boolean oneWay = (Boolean)rmRequest.getMetadata().getContext(RMConstant.INVOCATION_CONTEXT).get(ONE_WAY_OPERATION);
            if (!oneWay)  
               client.setUnMarshaller(RMUnMarshaller.getInstance());
         
            //if (log.isDebugEnabled())
            //   log.debug("Remoting metadata: " + rmRequest.getMetadata());
            //System.out.println("Remoting metadata: " + rmRequest.getMetadata());

            // debug the outgoing message
            //MessageTrace.traceMessage("Outgoing Request Message", reqMessage);
            //System.out.println("Outgoing Request Message" + new String(rmRequest.getPayload()));

            RMMessage resMessage = null;
            Map<String, Object> remotingInvocationContext = rmRequest.getMetadata().getContext(REMOTING_INVOCATION_CONTEXT);
            if (oneWay)
            {
               client.invokeOneway(rmRequest.getPayload(), remotingInvocationContext, false);
            }
            else
            {
               resMessage = (RMMessage)client.invoke(rmRequest.getPayload(), remotingInvocationContext);
            }

            // Disconnect the remoting client
            client.disconnect();
 
            // trace the incomming response message
            //MessageTrace.traceMessage("Incoming Response Message", resMessage);
            //System.out.println("Incoming Response Message" + new String(resMessage.getPayload()));

            return new RMChannelResponse(resMessage);
         }
         catch (Throwable t)
         {
            return new RMChannelResponse(t);
         }
      }
   }

   private RMMessage createRMMessage(MessageAbstraction request, RMMetadata rmMetadata) throws Throwable
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Marshaller marshaller = (Marshaller)rmMetadata.getContext(SERIALIZATION_CONTEXT).get(MARSHALLER);
      // we have to serialize message before putting it to the rm pool
      //  * contextClassloader not serializable issue
      //  * DOMUtil threadlocal issue (if message is de/serialized in separate thread)
      marshaller.write(request, baos);
      RMMessage rmMessage = RMMessageFactory.newMessage(baos.toByteArray(), rmMetadata);
      return rmMessage;
   }
   
   private MessageAbstraction createResponse(RMMessage rmResponse, RMMetadata rmMetadata) throws Throwable
   {
      Map<String, Object> invocationContext = rmMetadata.getContext(INVOCATION_CONTEXT);
      boolean oneWay = (Boolean)rmMetadata.getContext(INVOCATION_CONTEXT).get(ONE_WAY_OPERATION);
      MessageAbstraction response = null;
      if (!oneWay)
      {
         byte[] payload = rmResponse.getPayload();
         InputStream is = payload == null ? null : new ByteArrayInputStream(rmResponse.getPayload()); 
         // we have to deserialize message after pick up from the rm pool
         //  * contextClassloader not serializable issue
         //  * DOMUtil threadlocal issue (if message is de/serialized in separate thread)
         UnMarshaller unmarshaller = (UnMarshaller)rmMetadata.getContext(SERIALIZATION_CONTEXT).get(UNMARSHALLER);
         response = (MessageAbstraction)unmarshaller.read(is, rmResponse.getMetadata().getContext(REMOTING_INVOCATION_CONTEXT));
      }
      invocationContext.clear();
      invocationContext.putAll(rmMetadata.getContext(REMOTING_INVOCATION_CONTEXT));
      return response;
   }
   
   public MessageAbstraction send(MessageAbstraction request, RMMetadata rmMetadata) throws Throwable
   {
      RMMessage rmRequest = createRMMessage(request, rmMetadata);
      RMMessage rmResponse = sendToChannel(rmRequest);
      return createResponse(rmResponse, rmMetadata);
   }
   
   private RMMessage sendToChannel(RMMessage request) throws Throwable
   {
      RMChannelResponse result = rmChannelPool.submit(new RMChannelRequest(request)).get();

      Throwable fault = result.getFault();
      if (fault != null)
      {
         throw fault;
      }
      else
      {
         return result.getResponse();
      }
   }
}
