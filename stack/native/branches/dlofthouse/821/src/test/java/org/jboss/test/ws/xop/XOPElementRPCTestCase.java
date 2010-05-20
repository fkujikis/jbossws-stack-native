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
package org.jboss.test.ws.xop;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.util.xml.DOMUtils;
import org.jboss.util.xml.DOMWriter;
import org.jboss.ws.Constants;
import org.jboss.ws.jaxrpc.Style;
import org.jboss.ws.metadata.OperationMetaData;
import org.jboss.ws.metadata.ParameterMetaData;
import org.jboss.ws.soap.*;
import org.jboss.ws.soap.attachment.MimeConstants;

import javax.mail.internet.ContentType;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Test the SOAPElement
 *
 * @author Thomas.Diesler@jboss.org
 * @since 17-Jan-2006
 */
public class XOPElementRPCTestCase extends JBossWSTest
{
   private OperationMetaData opMetaData;
   private ParameterMetaData paramMetaData;
   private MessageFactory msgFactory;
   private SOAPMessage soapMessage;
   private SOAPContentElement xopElement;

   protected void setUp() throws Exception
   {
      super.setUp();

      // Setup the opMetaData
      opMetaData = new OperationMetaData(new QName("http://somens", "myOperation", "ns1"), "myOperation");
      opMetaData.getEndpointMetaData().setStyle(Style.RPC);
      paramMetaData = new ParameterMetaData(opMetaData,
          new QName("xopParam"),
          new QName(Constants.NS_XML_MIME, "base64Binary", Constants.PREFIX_XSD),
          "java.lang.String"
      );
      opMetaData.addParameter(paramMetaData);

      // Setup XOP element
      msgFactory = MessageFactory.newInstance();
      soapMessage = msgFactory.createMessage();
      SOAPBodyElement bodyElement = new SOAPBodyElementRpc(new NameImpl(opMetaData.getXmlName()));
      soapMessage.getSOAPBody().addChildElement(bodyElement);
      xopElement = new SOAPContentElement(new NameImpl(paramMetaData.getXmlName()));
      xopElement.setParamMetaData(paramMetaData);
      bodyElement.addChildElement(xopElement);

      // Setup the message context
      SOAPMessageContextImpl msgContext = new SOAPMessageContextImpl();
      MessageContextAssociation.pushMessageContext(msgContext);
      msgContext.setOperationMetaData(opMetaData);
      msgContext.setMessage(soapMessage);

   }

   public void testSOAPMessageRoundTrip() throws Exception
   {
      /*String xopContent = "This is XOP content";
      xopElement.setXMimeContentType("text/plain");
      xopElement.setObjectValue(xopContent);

      // Write SOAP message as multipart/related
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      soapMessage.writeTo(baos);
      byte[] bytes = baos.toByteArray();

      //System.out.println(new String(bytes));

      // Verify mime headers
      MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
      ContentType contentType = new ContentType(mimeHeaders.getHeader(MimeConstants.CONTENT_TYPE)[0]);
      assertEquals(MimeConstants.TYPE_MULTIPART_RELATED, contentType.getBaseType());

      SOAPMessage soapMessage = msgFactory.createMessage(mimeHeaders, new ByteArrayInputStream(bytes));

      SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
      System.out.println(DOMWriter.printNode(soapEnvelope, true));

      SOAPBody soapBody = soapMessage.getSOAPBody();
      SOAPElement rpcElement = (SOAPElement)DOMUtils.getFirstChildElement(soapBody);
      assertEquals(new NameImpl(opMetaData.getXmlName()), rpcElement.getElementName());
      SOAPContentElement xopElement = (SOAPContentElement)DOMUtils.getFirstChildElement(rpcElement);
      assertEquals(new NameImpl(paramMetaData.getXmlName()), xopElement.getElementName());
      xopElement.setParamMetaData(paramMetaData);
      Object retObj = xopElement.getObjectValue();
      assertEquals(xopContent, retObj);
      */

      System.out.println("FIXME: JBXB-62");
   }
}