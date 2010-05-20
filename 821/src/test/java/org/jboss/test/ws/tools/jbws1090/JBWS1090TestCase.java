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
package org.jboss.test.ws.tools.jbws1090;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.util.xml.DOMUtils;
import org.jboss.ws.tools.WSTools;
import org.w3c.dom.Element;

/**
 * Test append web service descriptions to existing webservices.xml (JBWS-1090)
 * 
 * @author <a href="mailto:alex.guizar@jboss.com">Alejandro Guizar</a>
 * @version $Revision$
 */
public class JBWS1090TestCase extends JBossWSTest
{
   public void testWebServicesAppend() throws Exception
   {
      // create output dir
      File outputDir = new File("tools/jbws1090");
      outputDir.mkdirs();
      // copy webservices.xml fixture to output folder 
      FileChannel src = new FileInputStream("resources/tools/metadatafixture/webservices.xml").getChannel();
      FileChannel dest = new FileOutputStream("tools/jbws1090/webservices.xml").getChannel();
      src.transferTo(0L, src.size(), dest);
      src.close();
      dest.close();

      // run wstools
      String[] args = { "-dest", "tools/jbws1090", "-config", "resources/tools/jbws1090/wstools-config.xml" };
      new WSTools().generate(args);

      Element expected = DOMUtils.parse(new FileInputStream("resources/tools/jbws1090/webservices.xml"));
      Element was = DOMUtils.parse(new FileInputStream("tools/jbws1090/webservices.xml"));
      assertEquals(expected, was);
   }
}
