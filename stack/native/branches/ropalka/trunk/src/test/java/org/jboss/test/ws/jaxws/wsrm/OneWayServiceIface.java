package org.jboss.test.ws.jaxws.wsrm;

import javax.jws.WebService;

@WebService
public interface OneWayServiceIface
{
   void method1();
   
   void method2(String s);
   
   void method3(String[] sa);
}
