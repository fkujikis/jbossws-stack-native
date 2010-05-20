package org.jboss.ws.eventing;

import java.net.URI;

import javax.xml.namespace.QName;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;

import org.jboss.ws.Constants;
import org.jboss.ws.eventing.mgmt.SubscriptionManagerFactory;
import org.jboss.ws.eventing.mgmt.SubscriptionManagerMBean;
import org.jboss.ws.soap.MessageContextAssociation;
import org.jboss.ws.soap.SOAPMessageContextImpl;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 13-Jan-2006
 */
public abstract class EventingEndpointBase
{

   private AddressingBuilder addrBuilder;

   /**
    * Retrieve the addressing properties associated with the request
    * and verify them.
    */
   protected static AddressingProperties getAddrProperties()
   {
      SOAPMessageContextImpl msgContext = MessageContextAssociation.peekMessageContext();
      AddressingProperties inProps = (AddressingProperties)msgContext.getProperty(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
      assertAddrProperties(inProps);
      return inProps;
   }

   protected void setReplyAction(URI replyAction)
   {

   }

   /**
    * Access local subscription manager service.
    */
   protected SubscriptionManagerMBean getSubscriptionManager()
   {
      SubscriptionManagerFactory factory = SubscriptionManagerFactory.getInstance();
      SubscriptionManagerMBean subscriptionManager = factory.getSubscriptionManager();
      return subscriptionManager;
   }

   protected AddressingBuilder getAddrBuilder()
   {
      if (null == addrBuilder)
         addrBuilder = AddressingBuilder.getAddressingBuilder();
      return addrBuilder;
   }

   /**
    * Ensure that all required inbound properties are supplied in request.
    * @param inProps
    * @throws javax.xml.rpc.soap.SOAPFaultException
    */
   protected static void assertAddrProperties(AddressingProperties inProps) throws SOAPFaultException
   {
      if (null == inProps)
         throw new SOAPFaultException(Constants.SOAP11_FAULT_CODE_CLIENT, "Addressing headers missing from request", "wse:InvalidMessage", null);
   }

   public QName buildFaultQName(String elementName)
   {
      return new QName(EventingConstants.NS_EVENTING, elementName, EventingConstants.PREFIX_EVENTING);
   }
}
