/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
package org.jboss.test.ws.outparam;

import javax.xml.namespace.QName;
import javax.xml.rpc.holders.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Calendar;

/**
 * A service endpoint interface for the HolderTestCase
 *
 * @author Thomas.Diesler@jboss.org
 * @since 22-Dec-2004
 */
public interface OutParamTestService extends Remote
{
   void echoBigDecimal(BigDecimal in, BigDecimalHolder out) throws RemoteException;

   void echoBigInteger(BigInteger in, BigIntegerHolder out) throws RemoteException;

   void echoBoolean(boolean in, BooleanHolder out) throws RemoteException;

   void echoBooleanWrapper(Boolean in, BooleanWrapperHolder out) throws RemoteException;

   void echoByteArray(byte[] in, ByteArrayHolder out) throws RemoteException;

   void echoByte(byte in, ByteHolder out) throws RemoteException;

   void echoByteWrapper(Byte in, ByteWrapperHolder out) throws RemoteException;

   void echoCalendar(Calendar in, CalendarHolder out) throws RemoteException;

   void echoDouble(double in, DoubleHolder out) throws RemoteException;

   void echoDoubleWrapper(Double in, DoubleWrapperHolder out) throws RemoteException;

   void echoFloat(float in, FloatHolder out) throws RemoteException;

   void echoFloatWrapper(Float in, FloatWrapperHolder out) throws RemoteException;

   void echoInt(int in, IntHolder out) throws RemoteException;

   void echoIntegerWrapper(Integer in, IntegerWrapperHolder out) throws RemoteException;

   void echoLong(long in, LongHolder out) throws RemoteException;

   void echoLongWrapper(Long in, LongWrapperHolder out) throws RemoteException;

   /*
   void echoObject(Object in, ObjectHolder out) throws RemoteException;
   */

   void echoQName(QName in, QNameHolder out) throws RemoteException;

   void echoShort(short in, ShortHolder out) throws RemoteException;

   void echoShortWrapper(Short in, ShortWrapperHolder out) throws RemoteException;

   void echoString(String in, StringHolder out) throws RemoteException;
}
