package org.jboss.test.ws.interop.microsoft.soapwsdl;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 20-Feb-2006
 */
public interface BaseDataTypesSEI extends java.rmi.Remote {
   public boolean retBool(boolean inBool) throws
         java.rmi.RemoteException;
    public short retByte(short inByte) throws
         java.rmi.RemoteException;
    public byte[] retByteArray(byte[] inByteArray) throws
         java.rmi.RemoteException;
    public int retChar(int inChar) throws
         java.rmi.RemoteException;
    public java.util.Calendar retDateTime(java.util.Calendar inDateTime) throws
         java.rmi.RemoteException;
    public java.math.BigDecimal retDecimal(java.math.BigDecimal inDecimal) throws
         java.rmi.RemoteException;
    public double retDouble(double inDouble) throws
         java.rmi.RemoteException;
    public float retFloat(float inFloat) throws
         java.rmi.RemoteException;
    public java.lang.String retGuid(java.lang.String inGuid) throws
         java.rmi.RemoteException;
    public int retInt(int inInt) throws
         java.rmi.RemoteException;
    public long retLong(long inLong) throws
         java.rmi.RemoteException;
    public javax.xml.soap.SOAPElement retObject(javax.xml.soap.SOAPElement inObject) throws
         java.rmi.RemoteException;
    public javax.xml.namespace.QName retQName(javax.xml.namespace.QName inQName) throws
         java.rmi.RemoteException;
    public byte retSByte(byte inSByte) throws
         java.rmi.RemoteException;
    public short retShort(short inShort) throws
         java.rmi.RemoteException;
    public float retSingle(float inSingle) throws
         java.rmi.RemoteException;
    public java.lang.String retString(java.lang.String inString) throws
         java.rmi.RemoteException;
    public java.lang.String retTimeSpan(java.lang.String inTimeSpan) throws
         java.rmi.RemoteException;
    public long retUInt(long inUInt) throws
         java.rmi.RemoteException;
    public java.math.BigInteger retULong(java.math.BigInteger inULong) throws
         java.rmi.RemoteException;
    public int retUShort(int inUShort) throws
         java.rmi.RemoteException;
    public java.net.URI retUri(java.net.URI inUri) throws
         java.rmi.RemoteException;
}
