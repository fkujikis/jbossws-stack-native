package org.jboss.ws.soap;

import org.jboss.ws.jaxrpc.Style;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPException;
import java.io.InputStream;
import java.io.IOException;

/**
 * @author Heiko Braun, <heiko.braun@jboss.com>
 * @since 19-Apr-2006
 */
public interface SAAJEnvelopeBuilder {
   void setSOAPMessage(SOAPMessage soapMessage);
   void setStyle(Style style);
   SOAPEnvelope build(InputStream in) throws IOException, SOAPException;
   void setIgnoreParseException(boolean b);
}
