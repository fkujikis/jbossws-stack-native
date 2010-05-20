package org.jboss.ws.soap;

import org.w3c.dom.*;
import org.jboss.util.xml.DOMUtils;
import org.jboss.util.xml.DOMWriter;
import org.jboss.xb.binding.NamespaceRegistry;
import org.jboss.ws.jaxrpc.Style;
import org.jboss.ws.WSException;

import javax.xml.soap.*;
import javax.xml.namespace.QName;
import java.io.InputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Heiko Braun, <heiko.braun@jboss.com>
 * @since 19-Apr-2006
 */
public class DOMEnvelopeBuilder implements SAAJEnvelopeBuilder {

   private SOAPMessage soapMessage;
   private boolean ignoreParseException = false;
   private Style style = Style.DOCUMENT;

   public void setSOAPMessage(SOAPMessage soapMessage) {
      this.soapMessage = soapMessage;
   }

   public void setIgnoreParseException(boolean ignoreParseException) {
      this.ignoreParseException = ignoreParseException;
   }

   public void setStyle(Style style) {
      this.style = style;
   }

   public SOAPEnvelope build(InputStream ins) throws IOException, SOAPException {
      // Parse the XML input stream
      Element domEnv = null;
      try
      {
         domEnv = DOMUtils.parse(ins);
      }
      catch (IOException ex)
      {
         if (ignoreParseException)
         {
            return null;
         }
         throw ex;
      }

      String envNS = domEnv.getNamespaceURI();
      String envPrefix = domEnv.getPrefix();

      // Construct the envelope
      SOAPFactoryImpl soapFactory = new SOAPFactoryImpl();
      SOAPPartImpl soapPart = (SOAPPartImpl)soapMessage.getSOAPPart();
      SOAPEnvelopeImpl soapEnv = new SOAPEnvelopeImpl(soapPart, soapFactory.createElement(domEnv, false));
      NamespaceRegistry namespaceRegistry = soapEnv.getNamespaceRegistry();

      DOMUtils.copyAttributes(soapEnv, domEnv);
      registerNamespaces(namespaceRegistry, soapEnv);

      // Add the header elements
      Element domHeader = DOMUtils.getFirstChildElement(domEnv, new QName(envNS, "Header"));
      if (domHeader != null)
      {
         SOAPHeader soapHeader = soapEnv.getHeader();

         DOMUtils.copyAttributes(soapHeader, domHeader);
         registerNamespaces(namespaceRegistry, soapHeader);

         Iterator it = DOMUtils.getChildElements(domHeader);
         while (it.hasNext())
         {
            Element srcElement = (Element)it.next();
            //registerNamespacesLocally(srcElement);
            String xmlFragment = DOMWriter.printNode(srcElement, false);

            Name name = new NameImpl(srcElement.getLocalName(), srcElement.getPrefix(), srcElement.getNamespaceURI());
            SOAPContentElement destElement = new SOAPHeaderElementImpl(name);
            soapHeader.addChildElement(destElement);

            //DOMUtils.copyAttributes(destElement, srcElement);
            destElement.setXMLFragment(xmlFragment);
         }
      }

      // Add the body elements
      Element domBody = DOMUtils.getFirstChildElement(domEnv, new QName(envNS, "Body"));
      SOAPBody soapBody = soapEnv.getBody();

      DOMUtils.copyAttributes(soapBody, domBody);
      registerNamespaces(namespaceRegistry, soapBody);

      Iterator itBody = DOMUtils.getChildElements(domBody);
      if (itBody.hasNext())
      {
         Element domBodyElement = (Element)itBody.next();

         String localName = domBodyElement.getLocalName();
         String prefix = domBodyElement.getPrefix();
         String nsURI = domBodyElement.getNamespaceURI();
         Name beName = new NameImpl(localName, prefix, nsURI);

         // Process a <env:Fault> message
         if (beName.equals(new NameImpl("Fault", envPrefix, envNS)))
         {
            SOAPFaultImpl soapFault = new SOAPFaultImpl(envNS);
            soapBody.addChildElement(soapFault);

            DOMUtils.copyAttributes(soapFault, domBodyElement);

            Element domFaultCode = DOMUtils.getFirstChildElement(domBodyElement, new QName("faultcode"));
            if (domFaultCode == null)
               throw new SOAPException("SOAPFault does not contain a <faultcode> element");

            Element domFaultString = DOMUtils.getFirstChildElement(domBodyElement, new QName("faultstring"));
            if (domFaultString == null)
               throw new SOAPException("SOAPFault does not contain a <faultstring> element");

            String faultCode = DOMUtils.getTextContent(domFaultCode);
            soapFault.setFaultCode(faultCode);

            String faultString = DOMUtils.getTextContent(domFaultString);
            soapFault.setFaultString(faultString);

            Element domFaultActor = DOMUtils.getFirstChildElement(domBodyElement, new QName("faultactor"));
            if (domFaultActor != null)
            {
               String faultActor = DOMUtils.getTextContent(domFaultActor);
               soapFault.setFaultActor(faultActor);
            }

            // Add the fault detail
            Element domFaultDetail = DOMUtils.getFirstChildElement(domBodyElement, "detail");
            if (domFaultDetail != null)
            {
               Detail detail = soapFault.addDetail();
               Iterator it = DOMUtils.getChildElements(domFaultDetail);
               while (it.hasNext())
               {
                  Element domElement = (Element)it.next();
                  SOAPElement detailEntry = new DetailEntryImpl(soapFactory.createElement(domElement, true));
                  detailEntry = detail.addChildElement(detailEntry);
               }
            }
         }

         // Process and RPC or DOCUMENT style message
         else
         {

            if (style == Style.RPC)
            {
               SOAPBodyElementRpc soapBodyElement = new SOAPBodyElementRpc(beName);
               soapBodyElement = (SOAPBodyElementRpc)soapBody.addChildElement(soapBodyElement);

               DOMUtils.copyAttributes(soapBodyElement, domBodyElement);

               Iterator itBodyElement = DOMUtils.getChildElements(domBodyElement);
               while (itBodyElement.hasNext())
               {
                  Element srcElement = (Element)itBodyElement.next();
                  registerNamespacesLocally(srcElement);

                  Name name = new NameImpl(srcElement.getLocalName(), srcElement.getPrefix(), srcElement.getNamespaceURI());
                  SOAPContentElement destElement = new SOAPContentElement(name);
                  soapBodyElement.addChildElement(destElement);
                  // handle SOAPEncoding namespaces
                  /*NamedNodeMap attribs = srcElement.getAttributes();
                  for (int i = 0; i < attribs.getLength(); i++)
                  {
                     Attr attr = (Attr)attribs.item(i);
                     if(attr.getNamespaceURI()!=null &&
                         attr.getNamespaceURI().equals(Constants.URI_SOAP11_ENC)  &&
                         attr.getValue().indexOf(':') != -1
                         ) // nested soap11-enc namespaces
                     {
                        String nestedPrefix = attr.getValue().substring(0, attr.getValue().indexOf(':'));
                        String nestedNS = soapBodyElement.getNamespaceURI(nestedPrefix);
                        if(nestedNS!=null)
                           soapBodyElement.addNamespaceDeclaration(nestedPrefix, nestedNS);
                     }
                  } */

                  String xmlFragment = DOMWriter.printNode(srcElement, false);
                  destElement.setXMLFragment(xmlFragment);
               }
            }
            else if (style == Style.DOCUMENT)
            {
               Element srcElement = (Element)domBodyElement;
               registerNamespacesLocally(srcElement);

               SOAPBodyElementDoc destElement = new SOAPBodyElementDoc(beName);
               destElement = (SOAPBodyElementDoc)soapBody.addChildElement(destElement);

               String xmlFragment = DOMWriter.printNode(srcElement, false);
               destElement.setXMLFragment(xmlFragment);
            }
            else if (style == null)
            {
               SOAPBodyElementMessage soapBodyElement = new SOAPBodyElementMessage(beName);
               soapBodyElement = (SOAPBodyElementMessage)soapBody.addChildElement(soapBodyElement);

               DOMUtils.copyAttributes(soapBodyElement, domBodyElement);

               NodeList nlist = domBodyElement.getChildNodes();
               for (int i = 0; i < nlist.getLength(); i++)
               {
                  org.w3c.dom.Node child = nlist.item(i);
                  short childType = child.getNodeType();
                  if (childType == org.w3c.dom.Node.ELEMENT_NODE)
                  {
                     SOAPElement soapElement = soapFactory.createElement((Element)child, true);
                     soapBodyElement.addChildElement(soapElement);
                  }
                  else if (childType == org.w3c.dom.Node.TEXT_NODE)
                  {
                     String nodeValue = child.getNodeValue();
                     soapBodyElement.addTextNode(nodeValue);
                  }
                  else if (childType == org.w3c.dom.Node.CDATA_SECTION_NODE)
                  {
                     String nodeValue = child.getNodeValue();
                     soapBodyElement.addTextNode(nodeValue);
                  }
                  else
                  {
                     System.out.println("Ignore child type: " + childType);
                  }
               }
            }
            else
            {
               throw new WSException("Unsupported message style: " + style);
            }
         }
      }

      return soapEnv;
   }

   /**
    * Register globally available namespaces on element level.
    * This is necessary to ensure that each xml fragment is valid.    
    */
   private void registerNamespacesLocally(Element srcElement) {
      if(srcElement.getPrefix()== null)
      {
         srcElement.setAttribute("xmlns", srcElement.getNamespaceURI());
      }
      else
      {
         srcElement.setAttribute("xmlns:"+srcElement.getPrefix(), srcElement.getNamespaceURI());
      }
   }
   private void registerNamespaces(NamespaceRegistry namespaceRegistry, SOAPElement soapEl)
   {
      Iterator itNSPrefixes = soapEl.getNamespacePrefixes();
      while (itNSPrefixes.hasNext())
      {
         String prefix = (String)itNSPrefixes.next();
         String nsURI = soapEl.getNamespaceURI(prefix);
         namespaceRegistry.registerURI(nsURI, prefix);
      }
   }
}
