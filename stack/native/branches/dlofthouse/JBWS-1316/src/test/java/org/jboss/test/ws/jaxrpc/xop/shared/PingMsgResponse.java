/*
 * JBoss, the OpenSource EJB server
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
//Auto Generated by jbossws - Please do not edit!!!


package org.jboss.test.ws.jaxrpc.xop.shared;

public class  PingMsgResponse
{
   protected byte[] xopContent;

   public PingMsgResponse(){
   }

   public PingMsgResponse(byte[] dataHandler) {
      this.xopContent = dataHandler;
   }

   public byte[] getXopContent() {
      return xopContent;
   }

   public void setXopContent(byte[] xopContent) {
      this.xopContent = xopContent;
   }
}
