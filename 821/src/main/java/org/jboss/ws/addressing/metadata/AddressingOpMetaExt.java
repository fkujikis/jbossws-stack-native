package org.jboss.ws.addressing.metadata;

import org.jboss.ws.metadata.MetaDataExtension;

import javax.xml.namespace.QName;

/**
 * Addressing meta data extensions:
 * <ul>
 * <li>wsa:Action attribute
 * </ul>
 * @author Heiko Braun, <heiko@openj.net>
 * @since 17-Mar-2006
 */
public class AddressingOpMetaExt extends MetaDataExtension  {

   private String inboundAction;
   private String outboundAction;   

   public AddressingOpMetaExt(String extensionNameSpace) {
      super(extensionNameSpace);
   }

   public String getInboundAction() {
      return inboundAction;
   }

   public void setInboundAction(String inboundAction) {
      this.inboundAction = inboundAction;
   }

   public String getOutboundAction() {
      return outboundAction;
   }

   public void setOutboundAction(String outboundAction) {
      this.outboundAction = outboundAction;
   }

}
