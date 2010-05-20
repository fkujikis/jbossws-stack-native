package org.jboss.test.ws.jbws231;

import org.jboss.logging.Logger;

public class TestEndpointImpl implements TestEndpoint
{
   private Logger log = Logger.getLogger(TestEndpointImpl.class);

   public EyeColorType echoSimple(EyeColorType eyeColor)
   {
      log.info("echoSimple: " + eyeColor);
      return eyeColor;
   }

   public EyeColorType echoAnonymous(EyeColorType eyeColor)
   {
      log.info("echoAnonymous: " + eyeColor);
      return eyeColor;
   }
}
