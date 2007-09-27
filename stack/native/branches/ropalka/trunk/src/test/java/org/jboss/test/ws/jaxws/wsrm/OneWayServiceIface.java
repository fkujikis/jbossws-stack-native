package org.jboss.test.ws.jaxws.wsrm;

import javax.jws.WebService;

import org.jboss.ws.annotation.EndpointConfig;

@WebService
@EndpointConfig(configName = "Standard WSRM Client", configFile = "META-INF/wsrm-jaxws-client-config.xml")
public interface OneWayServiceIface
{
   void method1();
   
   void method2(String s);
   
   void method3(String[] sa);
}
