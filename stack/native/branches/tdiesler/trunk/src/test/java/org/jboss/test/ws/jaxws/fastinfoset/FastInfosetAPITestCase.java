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
package org.jboss.test.ws.jaxws.fastinfoset;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.jboss.wsf.common.DOMUtils;
import org.jboss.wsf.common.DOMWriter;
import org.jboss.wsf.test.JBossWSTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.xml.fastinfoset.dom.DOMDocumentParser;
import com.sun.xml.fastinfoset.dom.DOMDocumentSerializer;

/**
 * Test FastInfoset functionality
 *
 * @author Thomas.Diesler@jboss.com
 * @since 12-Mar-2008
 */
public class FastInfosetAPITestCase extends JBossWSTest
{
   public void testSimple() throws Exception
   {
      DOMDocumentSerializer serializer = new DOMDocumentSerializer();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      serializer.setOutputStream(baos);
      
      String srcXML = "<root>hello world</root>";
      Element srcRoot = DOMUtils.parse(srcXML);
      serializer.serialize(srcRoot);

      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      DOMDocumentParser parser = new DOMDocumentParser();
      Document resDoc = DOMUtils.getDocumentBuilder().newDocument();
      parser.parse(resDoc, bais);
      
      String resXML = DOMWriter.printNode(resDoc, false);
      assertEquals(srcXML, resXML);
   }
   
   public void testSimpleNamespace() throws Exception
   {
      DOMDocumentSerializer serializer = new DOMDocumentSerializer();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      serializer.setOutputStream(baos);
      
      String srcXML = "<root xmlns='http://somens'>hello world</root>";
      Element srcRoot = DOMUtils.parse(srcXML);
      serializer.serialize(srcRoot);

      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      DOMDocumentParser parser = new DOMDocumentParser();
      Document resDoc = DOMUtils.getDocumentBuilder().newDocument();
      parser.parse(resDoc, bais);
      
      String resXML = DOMWriter.printNode(resDoc, false);
      assertEquals(srcXML, resXML);
   }
   
   public void testPrefixedNamespace() throws Exception
   {
      DOMDocumentSerializer serializer = new DOMDocumentSerializer();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      serializer.setOutputStream(baos);
      
      String srcXML = "<ns1:root xmlns:ns1='http://somens'>hello world</ns1:root>";
      Element srcRoot = DOMUtils.parse(srcXML);
      serializer.serialize(srcRoot);

      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      DOMDocumentParser parser = new DOMDocumentParser();
      Document resDoc = DOMUtils.getDocumentBuilder().newDocument();
      parser.parse(resDoc, bais);
      
      String resXML = DOMWriter.printNode(resDoc, false);
      assertEquals(srcXML, resXML);
   }
}
