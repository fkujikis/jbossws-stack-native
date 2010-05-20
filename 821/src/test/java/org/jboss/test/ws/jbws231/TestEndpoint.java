package org.jboss.test.ws.jbws231;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 07-Apr-2006
 */
public interface TestEndpoint extends Remote
{
   EyeColorType echoSimple(EyeColorType eyeColor) throws RemoteException;
   
   EyeColorType echoAnonymous(EyeColorType eyeColor) throws RemoteException;
}
