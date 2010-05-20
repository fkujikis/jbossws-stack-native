package org.jboss.test.ws.interop.microsoft.addressing.wsaTestService;

import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.namespace.QName;

public class WsaTestPortType_Impl implements org.jboss.test.ws.interop.microsoft.addressing.wsaTestService.WsaTestPortType, java.rmi.Remote {

   static String[] faultMessages = new String[] {"1133", "1134", "1135"};

   public void notify(java.lang.String wsaNotifyMessagePart) throws
         java.rmi.RemoteException {
      System.out.println("notify " +wsaNotifyMessagePart);
   }

   public java.lang.String echo(java.lang.String wsaEchoInPart) throws
         java.rmi.RemoteException {

      System.out.println("echo " +wsaEchoInPart);

      for(String s : faultMessages)
      {
         if(wsaEchoInPart.indexOf(s) != -1)
            throw new SOAPFaultException(
                  new QName("http://jboss.org/test/interop/wsa", "wsaTestServiceError", "jbwsa"),
                  "This is supposed to fault", null, null
            );
      }

      java.lang.String _retVal = wsaEchoInPart;
      return _retVal;
   }

   public void echoOut(java.lang.String wsaEchoOutPart) throws
         java.rmi.RemoteException {
      System.out.println("echOut " +wsaEchoOutPart);
   }
}
