package org.jboss.ws.metadata;

import java.util.Map;
import java.util.HashMap;

/**
 * Base class for UMD elements that are extensible.
 * 
 * @author Heiko Braun, <heiko@openj.net>
 * @since 21-Mar-2006
 */
public abstract class ExtensibleMetaData {

   private Map<String, MetaDataExtension> extensions = new HashMap<String, MetaDataExtension>();

   public Map<String, MetaDataExtension> getExtensions() {
      return extensions;
   }

   public void addExtension(MetaDataExtension ext) {
      getExtensions().put(ext.getExtensionNameSpace(), ext);
   }

   public MetaDataExtension getExtension(String namespace) {
      return getExtensions().get(namespace);
   }

}
