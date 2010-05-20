package org.jboss.test.ws.xop.doclit;

import org.jboss.logging.Logger;
import org.jboss.ws.soap.NameImpl;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayOutputStream;

/**
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since Jun 16, 2006
 */
public class InlineHandler extends GenericHandler {

   private static Logger log = Logger.getLogger(InlineHandler.class);

   public QName[] getHeaders() {
      return new QName[0];
   }

   public boolean handleRequest(MessageContext messageContext) {
      dumpMessage(messageContext);
      return true;
   }

   public boolean handleResponse(MessageContext messageContext) {
      dumpMessage(messageContext);
      return true;
   }

   private void dumpMessage(MessageContext messageContext) {
      try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)messageContext).getMessage();
         SOAPBody soapBody = soapMessage.getSOAPBody();

         SOAPElement response = (SOAPElement) soapBody.getChildElements().next();
         SOAPElement xopElement = (SOAPElement)response.getChildElements(new NameImpl("xopContent")).next();
         messageContext.setProperty("xop.inline.value", xopElement.getFirstChild().getNodeValue());
      }
      catch (Exception e)
      {
         log.error(e);
      }
   }
}
