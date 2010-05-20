package org.jboss.ws.metadata;

import javax.xml.namespace.QName;

/**
 * Operation metaData extension.
 *
 * @author Heiko Braun, <heiko@openj.net>
 * @since 17-Mar-2006
 */
public abstract class MetaDataExtension {

   private String extensionNameSpace;

   public MetaDataExtension(String extensionNameSpace) {
      this.extensionNameSpace = extensionNameSpace;
   }

   public String getExtensionNameSpace() {
      return extensionNameSpace;
   }
}
