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
package org.jboss.test.ws.tools.jbws_211.tests;

import org.jboss.test.ws.tools.WSToolsTest;
import org.jboss.ws.tools.WSTools;

/**
 *  Base Class for the JBWS-211 Tests
 *  JBWS-211: Comprehensive Java -> WSDL 1.1 Test Collection
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since  Sep 24, 2005
 */
public abstract class JBWS211Test extends WSToolsTest
{  
   public abstract String getBase();
   public abstract String getWSDLName();
   public abstract String getFixMe();
   
   //Set up the test
   protected void setUp()
   {  
      String out_dir = "tools/jbws-211/jbossws/" + getBase();
    // createDir(out_dir + "/server");  
      createDir(out_dir ); 
      createDir(out_dir + "/wsdl");
   } 
   
   public final void testJava2WSDL() throws Exception
   { 
      if(getFixMe() != null)
      {
         System.out.println(getFixMe());
         return;
      }
      String out_dir = "tools/jbws-211/jbossws/" + getBase();
      String wsdlFix = "resources/tools/jbws-211/wsdlFixture/" + getBase() + "/" + getWSDLName();
      String configStr = getBase().replaceAll("/","");
      String configloc = "resources/tools/jbws-211/jbosswsConfig/"+ getBase() + "/" + configStr + "Config.xml";

      String[] args= new String[]{"-dest",out_dir,"-config",configloc};      
      WSTools tools = new WSTools();
      tools.generate(args); 
      //semanticallyValidateWSDL(wsdlFix, out_dir+"/server/" + getWSDLName()); 
      semanticallyValidateWSDL(wsdlFix, out_dir + "/wsdl/" + getWSDLName()); 
   }
}
