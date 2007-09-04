package org.jboss.test.ws.jaxws.wsrm.deserialization;

import java.io.ByteArrayInputStream;
import javax.xml.soap.SOAPMessage;
import org.jboss.ws.extensions.wsrm.spi.Provider;
import org.jboss.ws.extensions.wsrm.spi.MessageFactory;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.IncompleteSequenceBehavior;
import org.jboss.wsf.test.JBossWSTest;

public final class WSRMDeSerializationTestCase extends JBossWSTest
{
   private static final String WSRM_200702_NS = "http://docs.oasis-open.org/ws-rx/wsrm/200702";
   private static final MessageFactory WSRM_200702_FACTORY = Provider.getInstance(WSRM_200702_NS).getMessageFactory();
   
   private static final String CREATE_SEQUENCE_MESSAGE
      = "<soap:Envelope "
      + "   xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\""
      + "   xmlns:wsrm=\"http://docs.oasis-open.org/ws-rx/wsrm/200702\""
      + "   xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">"
      + "   <soap:Header>"
      + "      <wsa:MessageID>http://Business456.com/guid/0baaf88d-483b-4ecf-a6d8</wsa:MessageID>"
      + "      <wsa:To>http://example.com/serviceB/123</wsa:To>"
      + "      <wsa:Action>http://docs.oasis-open.org/ws-rx/wsrm/200702/CreateSequence</wsa:Action>"
      + "      <wsa:ReplyTo>"
      + "         <wsa:Address>http://Business456.com/serviceA/789</wsa:Address>"
      + "      </wsa:ReplyTo>"
      + "   </soap:Header>"
      + "   <soap:Body>"
      + "      <wsrm:CreateSequence>"
      + "         <wsrm:AcksTo>"
      + "            <wsa:Address>http://Business456.com/serviceA/789</wsa:Address>"
      + "         </wsrm:AcksTo>"
      + "         <wsrm:Expires>PT0S</wsrm:Expires>" 
      + "         <wsrm:Offer>"
      + "            <wsrm:Identifier>http://Business456.com/RM/ABC</wsrm:Identifier>"
      + "            <wsrm:Endpoint>http://Business456.com/serviceA/ASDF</wsrm:Endpoint>"
      + "            <wsrm:Expires>PT1S</wsrm:Expires>"
      + "            <wsrm:IncompleteSequenceBehavior>DiscardEntireSequence</wsrm:IncompleteSequenceBehavior>"
      + "         </wsrm:Offer>"
      + "      </wsrm:CreateSequence>"
      + "   </soap:Body>"
      + "</soap:Envelope>";
   
   private static final String createSequenceResponseMessage
      = "<soap:Envelope"
      + "   xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\""
      + "   xmlns:wsrm=\"http://docs.oasis-open.org/ws-rx/wsrm/200702\""
      + "   xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">"
      + "   <soap:Header>"
      + "      <wsa:To>http://Business456.com/serviceA/789</wsa:To>"
      + "      <wsa:RelatesTo>http://Business456.com/guid/0baaf88d-483b-4ecf-a6d8</wsa:RelatesTo>"
      + "      <wsa:Action>http://docs.oasis-open.org/ws-rx/wsrm/200702/CreateSequenceResponse</wsa:Action>"
      + "   </soap:Header>"
      + "   <soap:Body>"
      + "      <wsrm:CreateSequenceResponse>"
      + "         <wsrm:Identifier>http://Business456.com/RM/ABC</wsrm:Identifier>"
      + "      </wsrm:CreateSequenceResponse>"
      + "   </soap:Body>"
      + "</soap:Envelope>";
   
   private static final String sequencePlusAckRequestedMessage
      = "<soap:Envelope"
      + "   xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\""
      + "   xmlns:wsrm=\"http://docs.oasis-open.org/ws-rx/wsrm/200702\""
      + "   xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">"
      + "   <soap:Header>"
      + "      <wsa:MessageID>http://Business456.com/guid/71e0654e-5ce8-477b-bb9d</wsa:MessageID>"
      + "      <wsa:To>http://example.com/serviceB/123</wsa:To>"
      + "      <wsa:From>"
      + "         <wsa:Address>http://Business456.com/serviceA/789</wsa:Address>"
      + "      </wsa:From>"
      + "      <wsa:Action>http://example.com/serviceB/123/request</wsa:Action>"
      + "      <wsrm:Sequence>"
      + "         <wsrm:Identifier>http://Business456.com/RM/ABC</wsrm:Identifier>"
      + "         <wsrm:MessageNumber>1</wsrm:MessageNumber>"
      + "      </wsrm:Sequence>"
      + "      <wsrm:AckRequested>"
      + "         <wsrm:Identifier>http://Business456.com/RM/ABC</wsrm:Identifier>"
      + "      </wsrm:AckRequested>"
      + "   </soap:Header>"
      + "   <soap:Body><!-- Some Application Data --></soap:Body>"
      + "</soap:Envelope>";

   private static final String sequenceAcknowledgementMessage
      = "<soap:Envelope"
      + "   xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\""
      + "   xmlns:wsrm=\"http://docs.oasis-open.org/ws-rx/wsrm/200702\""
      + "   xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">"
      + "   <soap:Header>"
      + "      <wsa:MessageID>http://example.com/guid/0baaf88d-483b-4ecf-a6d8</wsa:MessageID>"
      + "      <wsa:To>http://Business456.com/serviceA/789</wsa:To>"
      + "      <wsa:From>"
      + "         <wsa:Address>http://example.com/serviceB/123</wsa:Address>"
      + "      </wsa:From>"
      + "      <wsa:Action>http://docs.oasis-open.org/ws-rx/wsrm/200702/SequenceAcknowledgement</wsa:Action>"
      + "      <wsrm:SequenceAcknowledgement>"
      + "         <wsrm:Identifier>http://Business456.com/RM/ABC</wsrm:Identifier>"
      + "         <wsrm:AcknowledgementRange Upper='1' Lower='1'/>"
      + "         <wsrm:AcknowledgementRange Upper='3' Lower='3'/>"
      + "      </wsrm:SequenceAcknowledgement>"
      + "   </soap:Header>"
      + "   <soap:Body/>"
      + "</soap:Envelope>";
   
   private static final String closeSequenceMessage
   = "<soap:Envelope"
   + "   xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\""
   + "   xmlns:wsrm=\"http://docs.oasis-open.org/ws-rx/wsrm/200702\""
   + "   xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">"
   + "   <soap:Header>"
   + "      <wsa:MessageID>http://Business456.com/guid/0baaf88d-483b-4ecf-a6d8</wsa:MessageID>"
   + "      <wsa:To>http://example.com/serviceB/123</wsa:To>"
   + "      <wsa:Action>http://docs.oasis-open.org/ws-rx/wsrm/200702/TerminateSequence</wsa:Action>"
   + "      <wsa:From>"
   + "         <wsa:Address>http://Business456.com/serviceA/789</wsa:Address>"
   + "      </wsa:From>"
   + "   </soap:Header>"
   + "   <soap:Body>"
   + "      <wsrm:CloseSequence>"
   + "         <wsrm:Identifier>http://Business456.com/RM/ABC</wsrm:Identifier>"
   + "         <wsrm:LastMsgNumber>3</wsrm:LastMsgNumber>"
   + "      </wsrm:CloseSequence>"
   + "   </soap:Body>"
   + "</soap:Envelope>";

   private static final String terminateSequenceMessage
      = "<soap:Envelope"
      + "   xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\""
      + "   xmlns:wsrm=\"http://docs.oasis-open.org/ws-rx/wsrm/200702\""
      + "   xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">"
      + "   <soap:Header>"
      + "      <wsa:MessageID>http://Business456.com/guid/0baaf88d-483b-4ecf-a6d8</wsa:MessageID>"
      + "      <wsa:To>http://example.com/serviceB/123</wsa:To>"
      + "      <wsa:Action>http://docs.oasis-open.org/ws-rx/wsrm/200702/TerminateSequence</wsa:Action>"
      + "      <wsa:From>"
      + "         <wsa:Address>http://Business456.com/serviceA/789</wsa:Address>"
      + "      </wsa:From>"
      + "   </soap:Header>"
      + "   <soap:Body>"
      + "      <wsrm:TerminateSequence>"
      + "         <wsrm:Identifier>http://Business456.com/RM/ABC</wsrm:Identifier>"
      + "         <wsrm:LastMsgNumber>3</wsrm:LastMsgNumber>"
      + "      </wsrm:TerminateSequence>"
      + "   </soap:Body>"
      + "</soap:Envelope>";
   
   private static final String closeSequenceResponseMessage
      = "<soap:Envelope"
      + "   xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\""
      + "   xmlns:wsrm=\"http://docs.oasis-open.org/ws-rx/wsrm/200702\""
      + "   xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">"
      + "   <soap:Header>"
      + "      <wsa:MessageID>http://Business456.com/guid/0baaf88d-483b-4ecf-a6d8</wsa:MessageID>"
      + "      <wsa:To>http://example.com/serviceA/789</wsa:To>"
      + "      <wsa:Action>http://docs.oasis-open.org/ws-rx/wsrm/200702/TerminateSequenceResponse</wsa:Action>"
      + "      <wsa:RelatesTo>http://Business456.com/guid/0baaf88d-483b-4ecf-a6d8</wsa:RelatesTo>"
      + "      <wsa:From>"
      + "         <wsa:Address>http://Business456.com/serviceA/789</wsa:Address>"
      + "      </wsa:From>"
      + "   </soap:Header>"
      + "   <soap:Body>"
      + "      <wsrm:CloseSequenceResponse>"
      + "         <wsrm:Identifier>http://Business456.com/RM/ABC</wsrm:Identifier>"
      + "      </wsrm:CloseSequenceResponse>"
      + "   </soap:Body>"
      + "</soap:Envelope>";
   
   private static final String terminateSequenceResponseMessage
   = "<soap:Envelope"
   + "   xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\""
   + "   xmlns:wsrm=\"http://docs.oasis-open.org/ws-rx/wsrm/200702\""
   + "   xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">"
   + "   <soap:Header>"
   + "      <wsa:MessageID>http://Business456.com/guid/0baaf88d-483b-4ecf-a6d8</wsa:MessageID>"
   + "      <wsa:To>http://example.com/serviceA/789</wsa:To>"
   + "      <wsa:Action>http://docs.oasis-open.org/ws-rx/wsrm/200702/TerminateSequenceResponse</wsa:Action>"
   + "      <wsa:RelatesTo>http://Business456.com/guid/0baaf88d-483b-4ecf-a6d8</wsa:RelatesTo>"
   + "      <wsa:From>"
   + "         <wsa:Address>http://Business456.com/serviceA/789</wsa:Address>"
   + "      </wsa:From>"
   + "   </soap:Header>"
   + "   <soap:Body>"
   + "      <wsrm:TerminateSequenceResponse>"
   + "         <wsrm:Identifier>http://Business456.com/RM/ABC</wsrm:Identifier>"
   + "      </wsrm:TerminateSequenceResponse>"
   + "   </soap:Body>"
   + "</soap:Envelope>";
   
   public void testCreateSequenceMessageDeserialization() throws Exception
   {
      System.out.println("FIXME [JBWS-515] Provide an initial implementation for WS-ReliableMessaging");
      if (true) return;
      CreateSequence createSequenceMessage = WSRM_200702_FACTORY.newCreateSequence();
      createSequenceMessage.deserializeFrom(toSOAPMessage(CREATE_SEQUENCE_MESSAGE));
      // perform assertion
      assertEquals(createSequenceMessage.getAcksTo(), "http://Business456.com/serviceA/789");
      assertEquals(createSequenceMessage.getExpires(), "PT0S");
      CreateSequence.Offer offer = createSequenceMessage.getOffer(); 
      assertEquals(offer.getIdentifier(), "http://Business456.com/RM/ABC");
      assertEquals(offer.getEndpoint(), "http://Business456.com/serviceA/ASDF");
      assertEquals(offer.getExpires(), "PT1S");
      assertEquals(offer.getIncompleteSequenceBehavior(), IncompleteSequenceBehavior.DISCARD_ENTIRE_SEQUENCE);
   }
   
   public void testCreateSequenceMessageSerialization() throws Exception
   {
      System.out.println("FIXME [JBWS-515] Provide an initial implementation for WS-ReliableMessaging");
      if (true) return;
      CreateSequence createSequenceMessage = WSRM_200702_FACTORY.newCreateSequence();
      // construct message
      createSequenceMessage.setAcksTo("http://Business456.com/serviceA/789");
      createSequenceMessage.setExpires("PT0S");
      CreateSequence.Offer offer = createSequenceMessage.newOffer();
      offer.setIdentifier("http://Business456.com/RM/ABC");
      offer.setEndpoint("http://Business456.com/serviceA/ASDF");
      offer.setExpires("PT1S");
      offer.setIncompleteSequenceBehavior(IncompleteSequenceBehavior.DISCARD_ENTIRE_SEQUENCE);
      createSequenceMessage.setOffer(offer);
      // serialize message
      SOAPMessage createdSOAPMessage = newEmptySOAPMessage();
      createSequenceMessage.serializeTo(createdSOAPMessage);
      // deserialize from constructed message
      CreateSequence createSequenceMessage1 = WSRM_200702_FACTORY.newCreateSequence();
      createSequenceMessage1.deserializeFrom(createdSOAPMessage);
      // deserialize from reference message
      CreateSequence createSequenceMessage2 = WSRM_200702_FACTORY.newCreateSequence();
      createSequenceMessage2.deserializeFrom(toSOAPMessage(CREATE_SEQUENCE_MESSAGE));
      // perform assertion
      assertEquals(createSequenceMessage1, createSequenceMessage2);
   }
   
   // TODO: implement other de/serializations
   
   private static SOAPMessage toSOAPMessage(String data) throws Exception
   {
      javax.xml.soap.MessageFactory factory = javax.xml.soap.MessageFactory.newInstance();
      return factory.createMessage(null, new ByteArrayInputStream(data.getBytes()));
   }
   
   private static SOAPMessage newEmptySOAPMessage() throws Exception
   {
      javax.xml.soap.MessageFactory factory = javax.xml.soap.MessageFactory.newInstance();
      return factory.createMessage();
   }

}
