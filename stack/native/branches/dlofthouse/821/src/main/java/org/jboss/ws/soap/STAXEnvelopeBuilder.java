package org.jboss.ws.soap;

import com.ctc.wstx.stax.WstxInputFactory;
import org.jboss.util.xml.DOMWriter;
import org.jboss.ws.jaxrpc.Style;
import org.jboss.xb.binding.NamespaceRegistry;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;

/**
 * @author Heiko Braun, <heiko.braun@jboss.com>
 * @since 15-Apr-2006
 */
public class STAXEnvelopeBuilder implements SAAJEnvelopeBuilder {

   private static final String END_ELEMENT_BRACKET = "</";
   private static final String EMPTY_STRING = "";
   private static final String CLOSING_BRACKET = ">";
   private static final String START_ELEMENT_BRACKET = "<";
   private static final String HEADER_ELEMENT_NAME = "Header";
   private static final String BODY_ELEMENT_NAME = "Body";
   private static final String FAULT_ELEMENT_NAME = "Fault";

   private static enum Part { ENVELOPE, HEADER, BODY, FAULT, RPC_PAYLOAD, DOC_PAYLOAD, BARE_PAYLOAD}

   private Part currentPart = Part.ENVELOPE;
   private Part previousPart = null;

   private boolean ignoreParseException = false;
   private Style style;

   // saaj
   private SOAPMessage soapMessage;
   private SOAPPartImpl soapPart;
   private SOAPEnvelopeImpl soapEnv;
   private NamespaceRegistry namespaceRegistry;

   private StringBuffer fragmentBuffer;
   private QName fragmentRootCursor = null;
   private QName currentRootElement = null;
   private XMLStreamReader reader;

   private static XMLInputFactory factory;

   public STAXEnvelopeBuilder() {
      resetFragmentBuffer();
   }

   private void resetFragmentBuffer() {
      this.fragmentBuffer = new StringBuffer();
      this.fragmentBuffer.ensureCapacity(2048);
   }

   public static void main(String[] args) throws Exception {

      for(int i=0; i<1; i++)
      {
         SAAJEnvelopeBuilder builder = new STAXEnvelopeBuilder();
         builder.setSOAPMessage(new SOAPMessageImpl());
         builder.setStyle(Style.DOCUMENT);
         File source = new File("C:/dev/prj/Stax_JBoss/resource/req8.xml");
         InputStream in = new BufferedInputStream(new FileInputStream(source));

         long start = System.currentTimeMillis();
         SOAPEnvelope soapEnv = builder.build(in);
         System.out.println( (System.currentTimeMillis()-start) + " ms");
         System.out.println(DOMWriter.printNode(soapEnv, true));
         
         in.close();
      }
   }

   public void setStyle(Style style) {
      this.style = style;
   }

   public void setSOAPMessage(SOAPMessage soapMessage) {
      this.soapMessage = soapMessage;
   }

   public void setIgnoreParseException(boolean b) {
      this.ignoreParseException = b;
   }

   public SOAPEnvelope build(InputStream in) throws IOException, SOAPException {

      try
      {
         reader = getFactoryInstance().createXMLStreamReader(in);
      }
      catch (XMLStreamException e)
      {
         throw new IOException("Failed to create stream reader:" + e.getMessage());
      }

      try
      {
         soapPart = (SOAPPartImpl)soapMessage.getSOAPPart();

         while (reader.hasNext()) {

            if(reader.isStartElement())
            {
               processStartElement();
            }
            else if(reader.isCharacters())
            {
               processCharacters();
            }
            else if(reader.isEndElement())
            {
               processEndElement();
            }

            reader.next();
         }

      }
      catch(XMLStreamException e)
      {
         if(!ignoreParseException)
            throw new IOException("Failed to parse stream: " + e.getMessage());
      }
      catch(SOAPException e)
      {
         e.printStackTrace();
         throw e;
      }
      finally{
         try
         {
            if(reader!=null)
               reader.close();
         }
         catch (XMLStreamException e)
         {
            // ignore
         }
      }

      return soapEnv;
   }

   private static synchronized XMLInputFactory getFactoryInstance() {
      if(null == factory)
      {
         System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
         //System.setProperty("javax.xml.stream.XMLInputFactory", "com.sun.xml.stream.ZephyrParserFactory");
         factory = XMLInputFactory.newInstance();
         factory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
         ((WstxInputFactory)factory).configureForSpeed();
      }
      return factory;

   }

   private void processCharacters() throws SOAPException {
      if(fragmentRootCursor != null)
         consumeCharacters();
   }

   private void consumeCharacters() throws SOAPException {

      String text = normalize( reader.getText() );

      if(!atPartMargin() && !reader.isWhiteSpace()) {

         fragmentBuffer.append(text);

         if(Part.FAULT == currentPart)
         {
            String localName = currentRootElement.getLocalPart();
            SOAPFault fault = soapEnv.getBody().getFault();
            if("faultcode".equalsIgnoreCase(localName))
               fault.setFaultCode(text);
            else if("faultactor".equalsIgnoreCase(localName))
               fault.setFaultActor(text);
            else if("faultstring".equalsIgnoreCase(localName))
               fault.setFaultString(text);
         }
      }
   }

   private void processEndElement() throws SOAPException {
      if(fragmentRootCursor != null)
         consumeEndElement();
   }

   private void consumeEndElement() throws SOAPException {

      QName qName = reader.getName();

      fragmentBuffer.append(END_ELEMENT_BRACKET);
      fragmentBuffer.append(getFQElementName(qName));
      fragmentBuffer.append(CLOSING_BRACKET);

      if(fragmentRootCursor != null && fragmentRootCursor.equals(qName))
      {
         flushBuffer();
         fragmentRootCursor = null;
      }
   }

   private void flushBuffer() throws SOAPException {
      if(Part.HEADER == currentPart)
      {
         SOAPHeader soapHeader = soapEnv.getHeader();
         SOAPContentElement lastHeaderElement = (SOAPContentElement)
             soapHeader.getChildNodes().item(
                 soapHeader.getChildNodes().getLength()-1
             );

         lastHeaderElement.setXMLFragment(fragmentBuffer.toString());
      }
      else if(Part.BODY == currentPart)
      {
         SOAPBody soapBody = soapEnv.getBody();
         SOAPContentElement lastBodyElement = (SOAPContentElement)
             soapBody.getChildNodes().item(
                 soapBody.getChildNodes().getLength()-1
             );
         lastBodyElement.setXMLFragment(fragmentBuffer.toString());
      }
      else if(Part.FAULT == currentPart)
      {
         SOAPBody soapBody = soapEnv.getBody();
         SOAPContentElement faultElement = (SOAPContentElement)soapBody.getFault();
         faultElement.setXMLFragment(fragmentBuffer.toString());
      }

      System.out.println(fragmentBuffer.toString());
      resetFragmentBuffer();
   }

   private void processStartElement() throws SOAPException{

      QName qName = reader.getName();
      currentRootElement = qName;

      // identify current envelope part
      togglePartMargin(qName);

      // toggle current element
      Element destElement = null;
      if(Part.ENVELOPE == currentPart)
      {
         // setup envelope impl
         soapEnv = new SOAPEnvelopeImpl(soapPart, qName.getNamespaceURI());
         namespaceRegistry = soapEnv.getNamespaceRegistry();
         destElement = soapEnv; // soapEnv becomes current
      }
      else if(Part.HEADER == currentPart)
      {
         if(atPartMargin())
         {
            // the env:Header element itself
            SOAPHeader soapHeader = soapEnv.getHeader();
            destElement = soapHeader; // header becomes current
            previousPart = Part.HEADER;
         }
         else
         {
            // child element of env:Header
            if(fragmentRootCursor == null)
            {
               Name name = new NameImpl(qName.getLocalPart(), qName.getPrefix(), qName.getNamespaceURI());
               SOAPContentElement headerElement = new SOAPHeaderElementImpl(name);
               soapEnv.getHeader().addChildElement(headerElement);

               destElement = headerElement; // headerElement becomes current
               fragmentRootCursor = qName;
            }

            consumeStartElement();
         }
      }
      else if(Part.BODY == currentPart)
      {

         SOAPBody soapBody = soapEnv.getBody();

         if(atPartMargin())
         {
            // the env:Body element
            destElement = soapBody;
            previousPart = Part.BODY;
         }
         else
         {
            // payload not fault
            Name bodyElementName = new NameImpl(qName.getLocalPart(), qName.getPrefix(), qName.getNamespaceURI());

            if(fragmentRootCursor == null)
            {
               SOAPBodyElementDoc docBodyElement = new SOAPBodyElementDoc(bodyElementName);
               docBodyElement = (SOAPBodyElementDoc)soapBody.addChildElement(docBodyElement);

               destElement = docBodyElement;
               fragmentRootCursor = qName;
            }

            consumeStartElement();
         }
      }
      else if (Part.FAULT == currentPart)
      {
         // payload is fault
         if(atPartMargin())
         {
            SOAPBody soapBody = soapEnv.getBody();
            SOAPFaultImpl soapFault = new SOAPFaultImpl( soapEnv.getNamespaceURI() );
            soapBody.addChildElement(soapFault);
            destElement = soapFault;
            previousPart = Part.FAULT;
         }

         if(fragmentRootCursor == null)
         {
            fragmentRootCursor = qName;
         }

         consumeStartElement();
      }

      if(fragmentRootCursor == null) // constructing soap elements
      {
         copyAttributes(destElement);
         registerNameSpaces(namespaceRegistry);
      }
   }

   private void togglePartMargin(QName qName) {
      // identify the current part
      if(qName.getLocalPart().equalsIgnoreCase(HEADER_ELEMENT_NAME))
      {
         previousPart = currentPart;
         currentPart = Part.HEADER;
      }
      else if(qName.getLocalPart().equalsIgnoreCase(BODY_ELEMENT_NAME))
      {
         previousPart = currentPart;
         currentPart = Part.BODY;
      }
      else if (qName.getLocalPart().equalsIgnoreCase(FAULT_ELEMENT_NAME))
      {
         previousPart = currentPart;
         currentPart = Part.FAULT;
      }
   }

   private void consumeStartElement() {

      QName qName = reader.getName();

      // element
      fragmentBuffer.append(START_ELEMENT_BRACKET);
      fragmentBuffer.append( getFQElementName(qName) );

      // local namespaces
      for(int x=0; x<reader.getNamespaceCount();x++)
      {
         if(reader.getNamespacePrefix(x)!=null)
         {
            fragmentBuffer.append(" xmlns:");
            fragmentBuffer.append(reader.getNamespacePrefix(x)).append("='");
            fragmentBuffer.append(reader.getNamespaceURI(x)).append("'");
         }
         else if(reader.getNamespaceURI(x)!=null)
         {
            fragmentBuffer.append(" xmlns='");
            fragmentBuffer.append(reader.getNamespaceURI(x)).append("'");
         }
      }

      // attributes
      if(reader.getAttributeCount()>0)
      {
         for(int i=0; i<reader.getAttributeCount(); i++)
         {
            QName attQName = reader.getAttributeName(i);
            fragmentBuffer.append(" ").append( getFQElementName(attQName) );
            fragmentBuffer.append("='").append(reader.getAttributeValue(i)).append("'");
         }
      }

      fragmentBuffer.append(CLOSING_BRACKET);
   }

   private String getFQElementName(QName qName) {
      return !qName.getPrefix().equals(EMPTY_STRING) ? qName.getPrefix()+":"+qName.getLocalPart() : qName.getLocalPart();
   }

   private void registerNameSpaces(NamespaceRegistry reg) {

      for(int i=0; i<reader.getNamespaceCount(); i++)
      {
         String prefix = reader.getNamespacePrefix(i);
         String uri = reader.getNamespaceURI(i);
         reg.registerURI(uri,prefix);

         soapEnv.addNamespaceDeclaration(prefix, uri);
      }
   }

   private void copyAttributes(Element destElement) {

      if(reader.getAttributeCount()==0)
         return;

      for(int i=0; i<reader.getAttributeCount(); i++)
      {
         destElement.setAttributeNS(
             reader.getAttributeNamespace(i),
             reader.getAttributeLocalName(i),
             reader.getAttributeValue(i)
         );
      }
   }

   private boolean atPartMargin() {
      return previousPart !=currentPart;
   }

   private static String normalize(String valueStr)
   {
      // We assume most strings will not contain characters that need "escaping",
      // and optimize for this case.
      boolean found = false;
      int i = 0;

      outer: for (; i < valueStr.length(); i++)
      {
         switch (valueStr.charAt(i))
         {
            case '<':
            case '>':
            case '&':
            case '"':
               found = true;
               break outer;
         }
      }

      if (!found)
         return valueStr;

      // Resume where we left off
      StringBuilder builder = new StringBuilder();
      builder.append(valueStr.substring(0, i));
      for (; i < valueStr.length(); i++)
      {
         char c = valueStr.charAt(i);
         switch (c)
         {
            case '<':
               builder.append("&lt;");
               break;
            case '>':
               builder.append("&gt;");
               break;
            case '&':
               builder.append("&amp;");
               break;
            case '"':
               builder.append("&quot;");
               break;
            default:
               builder.append(c);
         }
      }

      return builder.toString();
   }
}
