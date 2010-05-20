package org.jboss.test.ws.jbws807;

import org.jboss.ws.WSException;

import java.rmi.RemoteException;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 11-Apr-2006
 */
public class TestServiceImpl implements TestService_PortType {
    public PingMsgResponse ping(PingMsg pingMsg) throws RemoteException {
        throw new WSException("JBWS807 is supposed to fault");
    }
}
