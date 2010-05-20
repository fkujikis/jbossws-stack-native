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
package org.jboss.ws.handler;

// $Id$

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.metadata.EndpointMetaData;
import org.jboss.ws.metadata.OperationMetaData;
import org.jboss.ws.metadata.ParameterMetaData;
import org.jboss.ws.soap.SAAJElementWriter;
import org.jboss.ws.soap.SOAPEnvelopeImpl;
import org.jboss.ws.soap.SOAPMessageContextImpl;
import org.jboss.ws.soap.SOAPMessageImpl;
import org.jboss.ws.xop.XOPContext;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.HandlerChain;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.*;
import java.util.*;


/**
 * Represents a list of handlers. All elements in the
 * HandlerChain are of the type javax.xml.rpc.handler.Handler.
 * <p/>
 * Abstracts the policy and mechanism for the invocation of the registered handlers.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 06-May-2004
 */
public abstract class HandlerChainBaseImpl implements HandlerChain
{
   private static Logger log = Logger.getLogger(HandlerChainBaseImpl.class);

   public static final int STATE_DOES_NOT_EXIST = 0;
   public static final int STATE_CREATED = 1;
   public static final int STATE_READY = 2;
   public static final int STATE_DESTROYED = 3;

   // The List<Entry> objects
   protected List<HandlerEntry> handlers = new ArrayList<HandlerEntry>();
   // The roles associated with the handler chain
   protected Set<String> roles = new HashSet<String>();
   // The index of the first handler that returned false during processing
   protected int falseIndex = -1;
   // The state of this handler chain
   protected int state;

   /**
    * Constructs a handler chain with the given handlers infos
    */
   public HandlerChainBaseImpl(List infos, Set roles)
   {
      log.debug("Create a handler chain for roles: " + roles);
      addHandlersToChain(infos, roles);
   }

   /** Get the list of handler infos
    */
   public List<HandlerInfo> getHandlerInfos()
   {
      List<HandlerInfo> list = new ArrayList<HandlerInfo>();
      for (int i = 0; i < handlers.size(); i++)
      {
         HandlerEntry entry = (HandlerEntry)handlers.get(i);
         list.add(entry.info);
      }
      return list;
   }

   /**
    * Initialize the a handler chain with the given handlers infos
    *
    * @throws javax.xml.rpc.JAXRPCException If any error during initialization
    */
   private void addHandlersToChain(List infos, Set roleSet)
   {
      try
      {
         if (infos != null)
         {
            for (int i = 0; i < infos.size(); i++)
            {
               HandlerInfo info = (HandlerInfo)infos.get(i);
               HandlerWrapper handler = new HandlerWrapper((Handler)info.getHandlerClass().newInstance());
               handlers.add(new HandlerEntry(handler, info));
            }
         }
         if (roleSet != null)
         {
            roles.addAll(roleSet);
         }
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new JAXRPCException("Cannot initialize handler chain", ex);
      }

      // set state to created
      state = STATE_CREATED;
   }

   /**
    * Get the state of this handler chain
    */
   public int getState()
   {
      return state;
   }

   /**
    * Initializes the configuration for a HandlerChain.
    *
    * @param config Configuration for the initialization of this handler chain
    * @throws javax.xml.rpc.JAXRPCException If any error during initialization
    */
   public void init(Map config)
   {
      log.debug("init: [config=" + config + "]");
      for (int i = 0; i < handlers.size(); i++)
      {
         HandlerEntry entry = (HandlerEntry)handlers.get(i);
         entry.handler.init(entry.info);
      }

      // set state to ready
      state = STATE_READY;
   }

   /**
    * Indicates the end of lifecycle for a HandlerChain.
    *
    * @throws javax.xml.rpc.JAXRPCException If any error during destroy
    */
   public void destroy()
   {
      log.debug("destroy");
      for (int i = 0; i < handlers.size(); i++)
      {
         HandlerEntry entry = (HandlerEntry)handlers.get(i);
         entry.handler.destroy();
      }
      handlers.clear();

      // set state to destroyed
      state = STATE_DESTROYED;
   }

   /**
    * Gets SOAP actor roles registered for this HandlerChain at this SOAP node. The returned array includes the
    * special SOAP actor next.
    *
    * @return SOAP Actor roles as URIs
    */
   public String[] getRoles()
   {
      String[] arr = new String[roles.size()];
      roles.toArray(arr);
      return arr;
   }

   /**
    * Sets SOAP Actor roles for this HandlerChain. This specifies the set of roles in which this HandlerChain is to act
    * for the SOAP message processing at this SOAP node. These roles assumed by a HandlerChain must be invariant during
    * the processing of an individual SOAP message through the HandlerChain.
    * <p/>
    * A HandlerChain always acts in the role of the special SOAP actor next. Refer to the SOAP specification for the
    * URI name for this special SOAP actor. There is no need to set this special role using this method.
    *
    * @param soapActorNames URIs for SOAP actor name
    */
   public void setRoles(String[] soapActorNames)
   {
      List<String> newRoles = Arrays.asList(soapActorNames);
      log.debug("setRoles: " + newRoles);

      roles.clear();
      roles.addAll(newRoles);
   }

   /**
    * Initiates the request processing for this handler chain.
    *
    * @param msgContext MessageContext parameter provides access to the request SOAP message.
    * @return Returns true if all handlers in chain have been processed. Returns false if a handler in the chain returned false from its handleRequest method.
    * @throws javax.xml.rpc.JAXRPCException if any processing error happens
    */
   public boolean handleRequest(MessageContext msgContext)
   {
      boolean doNext = true;

      if (handlers.size() > 0)
      {
         log.debug("Enter: handleRequest");

         // Replace handlers that did not survive the previous call
         replaceDirtyHandlers();

         int handlerIndex = 0;
         Handler currHandler = null;
         try
         {
            for (; doNext && handlerIndex < handlers.size(); handlerIndex++)
            {
               String lastMessageTrace = null;
               if (log.isTraceEnabled())
               {
                  SOAPMessageContextImpl msgCtx = (SOAPMessageContextImpl)msgContext;
                  SOAPPart soapPart = msgCtx.getMessage().getSOAPPart();
                  lastMessageTrace = traceSOAPPart(soapPart, lastMessageTrace);
               }

               currHandler = ((HandlerEntry)handlers.get(handlerIndex)).getHandler();
               log.debug("Handle request: " + currHandler);
               doNext = currHandler.handleRequest(msgContext);

               if (log.isTraceEnabled())
               {
                  SOAPMessageContextImpl msgCtx = (SOAPMessageContextImpl)msgContext;
                  SOAPPart soapPart = msgCtx.getMessage().getSOAPPart();
                  lastMessageTrace = traceSOAPPart(soapPart, lastMessageTrace);
               }
            }
         }
         catch (RuntimeException e)
         {
            log.error("RuntimeException in request handler", e);
            doNext = false;
            throw e;
         }
         finally
         {
            // we start at this index in the response chain
            if (doNext == false)
               falseIndex = (handlerIndex - 1);

            log.debug("Exit: handleRequest with status: " + doNext);
         }
      }

      return doNext;
   }

   /**
    * Initiates the response processing for this handler chain.
    * <p/>
    * In this implementation, the response handler chain starts processing from the same Handler
    * instance (that returned false) and goes backward in the execution sequence.
    *
    * @return Returns true if all handlers in chain have been processed.
    *         Returns false if a handler in the chain returned false from its handleResponse method.
    * @throws javax.xml.rpc.JAXRPCException if any processing error happens
    */
   public boolean handleResponse(MessageContext msgContext)
   {
      boolean doNext = true;

      if (handlers.size() > 0)
      {
         log.debug("Enter: handleResponse");

         int handlerIndex = handlers.size() - 1;
         if (falseIndex != -1)
            handlerIndex = falseIndex;

         Handler currHandler = null;
         try
         {
            for (; doNext && handlerIndex >= 0; handlerIndex--)
            {
               String lastMessageTrace = null;
               if (log.isTraceEnabled())
               {
                  SOAPMessageContextImpl msgCtx = (SOAPMessageContextImpl)msgContext;
                  SOAPPart soapPart = msgCtx.getMessage().getSOAPPart();
                  lastMessageTrace = traceSOAPPart(soapPart, lastMessageTrace);
               }

               currHandler = ((HandlerEntry)handlers.get(handlerIndex)).getHandler();
               log.debug("Handle response: " + currHandler);
               doNext = currHandler.handleResponse(msgContext);

               if (log.isTraceEnabled())
               {
                  SOAPMessageContextImpl msgCtx = (SOAPMessageContextImpl)msgContext;
                  SOAPPart soapPart = msgCtx.getMessage().getSOAPPart();
                  lastMessageTrace = traceSOAPPart(soapPart, lastMessageTrace);
               }
            }
         }
         catch (RuntimeException rte)
         {
            log.error("RuntimeException in response handler", rte);
            doNext = false;
            throw rte;
         }
         finally
         {
            // we start at this index in the fault chain
            if (doNext == false)
               falseIndex = (handlerIndex - 1);

            log.debug("Exit: handleResponse with status: " + doNext);
         }
      }

      return doNext;
   }

   /**
    * Initiates the SOAP fault processing for this handler chain.
    * <p/>
    * In this implementation, the fault handler chain starts processing from the same Handler
    * instance (that returned false) and goes backward in the execution sequence.
    *
    * @return Returns true if all handlers in chain have been processed.
    *         Returns false if a handler in the chain returned false from its handleFault method.
    * @throws javax.xml.rpc.JAXRPCException if any processing error happens
    */
   public boolean handleFault(MessageContext msgContext)
   {
      boolean doNext = true;

      if (handlers.size() > 0)
      {
         log.debug("Enter: handleFault");
         
         try
         {
            int handlerIndex = handlers.size() - 1;
            if (falseIndex != -1)
               handlerIndex = falseIndex;

            Handler currHandler = null;
            for (; doNext && handlerIndex >= 0; handlerIndex--)
            {
               currHandler = ((HandlerEntry)handlers.get(handlerIndex)).getHandler();
               log.debug("Handle fault: " + currHandler);
               doNext = currHandler.handleFault(msgContext);
            }
         }
         finally
         {
            log.debug("Exit: handleFault with status: " + doNext);
         }
      }

      return doNext;
   }

   /**
    * Trace the SOAPPart, do nothing if the String representation is equal to the last one.
    */
   protected String traceSOAPPart(SOAPPart soapPart, String lastMessageTrace)
   {
      try
      {
         SOAPEnvelopeImpl soapEnv = (SOAPEnvelopeImpl)soapPart.getEnvelope();
         String envString = SAAJElementWriter.printSOAPElement(soapEnv, true);
         if (envString.equals(lastMessageTrace) == false)
         {
            log.debug(envString);
            lastMessageTrace = envString;
         }
         return lastMessageTrace;
      }
      catch (SOAPException e)
      {
         log.error("Cannot get SOAPEnvelope", e);
         return null;
      }
   }

   /**
    * Replace handlers that did not survive the previous call
    */
   protected void replaceDirtyHandlers()
   {
      for (int i = 0; i < handlers.size(); i++)
      {
         HandlerEntry entry = (HandlerEntry)handlers.get(i);
         if (entry.handler.getState() == HandlerWrapper.DOES_NOT_EXIST)
         {
            log.debug("Replacing dirty handler: " + entry.handler);
            try
            {
               HandlerWrapper handler = new HandlerWrapper((Handler)entry.info.getHandlerClass().newInstance());
               entry.handler = handler;
               handler.init(entry.info);
            }
            catch (RuntimeException rte)
            {
               throw rte;
            }
            catch (Exception ex)
            {
               log.error("Cannot create handler instance for: " + entry.info, ex);
            }
         }
      }
   }

   /**
    * Get the handler at the requested position
    */
   protected Handler getHandlerAt(int pos)
   {
      if (pos < 0 || handlers.size() <= pos)
         throw new IllegalArgumentException("No handler at position: " + pos);

      HandlerEntry entry = (HandlerEntry)handlers.get(pos);
      return entry.handler;
   }

   /**
    * R1027 A RECEIVER MUST generate a "soap:MustUnderstand" fault when a
    * message contains a mandatory header block (i.e., one that has a
    * soap:mustUnderstand attribute with the value "1") targeted at the
    * receiver (via soap:actor) that the receiver does not understand.
    */
   public static void checkMustUnderstand(SOAPMessageContextImpl msgContext, String[] roles)
   {
      SOAPHeaderElement mustUnderstandHeaderElement = null;
      List roleList = (roles != null ? Arrays.asList(roles) : new ArrayList());
      try
      {
         SOAPMessageImpl soapMessage = (SOAPMessageImpl)msgContext.getMessage();
         
         // A SOAPHeaderElement is possibly bound to the endpoint operation
         // in order to check that we need a the opMetaData
         OperationMetaData opMetaData = msgContext.getOperationMetaData();
         if (opMetaData == null)
         {
            // The security handler must have decrypted the incomming message
            // before the dispatch target operation can be known 
            EndpointMetaData epMetaData = msgContext.getEndpointMetaData();
            opMetaData = soapMessage.getOperationMetaData(epMetaData);
         }         
         
         SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
         if (soapEnvelope != null && soapEnvelope.getHeader() != null)
         {
            Iterator it = soapEnvelope.getHeader().examineAllHeaderElements();
            while (it.hasNext() && mustUnderstandHeaderElement == null)
            {
               SOAPHeaderElement soapHeaderElement = (SOAPHeaderElement)it.next();
               Name name = soapHeaderElement.getElementName();
               QName xmlName = new QName(name.getURI(), name.getLocalName());
               
               ParameterMetaData paramMetaData = (opMetaData != null ? opMetaData.getParameter(xmlName) : null);
               boolean isBoundHeader = (paramMetaData != null && paramMetaData.isInHeader());
               
               if (soapHeaderElement.getMustUnderstand() && isBoundHeader == false)
               {
                  String actor = soapHeaderElement.getActor();
                  boolean noActor = (actor == null || actor.length() == 0);
                  boolean nextActor = Constants.URI_SOAP11_NEXT_ACTOR.equals(actor);
                  if (noActor || nextActor || roleList.contains(actor))
                  {
                     mustUnderstandHeaderElement = soapHeaderElement;
                  }
               }
            }
         }
      }
      catch (SOAPException ex)
      {
         log.error("Cannot check mustUnderstand for headers", ex);
      }

      if (mustUnderstandHeaderElement != null)
      {
         QName faultCode = Constants.SOAP11_FAULT_CODE_MUST_UNDERSTAND;
         String faultString = "Unprocessed 'mustUnderstand' header element: " + mustUnderstandHeaderElement.getElementName();
         throw new SOAPFaultException(faultCode, faultString, null, null);
      }
   }

   /**
    * An entry in the handler list
    */
   private class HandlerEntry
   {
      private HandlerWrapper handler;
      private HandlerInfo info;

      public HandlerEntry(HandlerWrapper handler, HandlerInfo info)
      {
         this.handler = handler;
         this.info = info;
      }

      public Handler getHandler()
      {
         return handler;
      }

      public HandlerInfo getInfo()
      {
         return info;
      }
   }

   // java.util.List interface ****************************************************************************************

   public boolean remove(Object o)
   {
      return handlers.remove(o);
   }

   public boolean containsAll(Collection c)
   {
      return handlers.containsAll(c);
   }

   public boolean removeAll(Collection c)
   {
      return handlers.removeAll(c);
   }

   public boolean retainAll(Collection c)
   {
      return handlers.retainAll(c);
   }

   public int hashCode()
   {
      return handlers.hashCode();
   }

   public boolean equals(Object o)
   {
      return handlers.equals(o);
   }

   public Iterator iterator()
   {
      return handlers.iterator();
   }

   public List subList(int fromIndex, int toIndex)
   {
      return handlers.subList(fromIndex, toIndex);
   }

   public ListIterator listIterator()
   {
      return handlers.listIterator();
   }

   public ListIterator listIterator(int index)
   {
      return handlers.listIterator(index);
   }

   public int size()
   {
      return handlers.size();
   }

   public void clear()
   {
      handlers.clear();
   }

   public boolean isEmpty()
   {
      return handlers.isEmpty();
   }

   public Object[] toArray()
   {
      return handlers.toArray();
   }

   public Object get(int index)
   {
      return handlers.get(index);
   }

   public Object remove(int index)
   {
      return handlers.remove(index);
   }

   public void add(int index, Object element)
   {
      handlers.add(index, (HandlerEntry)element);
   }

   public int indexOf(Object elem)
   {
      return handlers.indexOf(elem);
   }

   public int lastIndexOf(Object elem)
   {
      return handlers.lastIndexOf(elem);
   }

   public boolean add(Object o)
   {
      return handlers.add((HandlerEntry)o);
   }

   public boolean contains(Object elem)
   {
      return handlers.contains(elem);
   }

   public boolean addAll(int index, Collection c)
   {
      return handlers.addAll(index, c);
   }

   public boolean addAll(Collection c)
   {
      return handlers.addAll(c);
   }

   public Object set(int index, Object element)
   {
      return handlers.set(index, (HandlerEntry)element);
   }

   public Object[] toArray(Object[] a)
   {
      return handlers.toArray(a);
   }
}
