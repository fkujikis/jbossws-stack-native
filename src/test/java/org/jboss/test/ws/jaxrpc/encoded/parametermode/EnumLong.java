// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.2_01, build R40)
// Generated source version: 1.1.2

package org.jboss.test.ws.jaxrpc.encoded.parametermode;


import java.util.HashMap;
import java.util.Map;

public class EnumLong {
    private long value;
    private static Map valueMap = new HashMap();
    public static final long _value1 = -9223372036854775808L;
    public static final long _value2 = 9223372036854775807L;
    
    public static final EnumLong value1 = new EnumLong(_value1);
    public static final EnumLong value2 = new EnumLong(_value2);
    
    protected EnumLong(long value) {
        this.value = value;
        valueMap.put(this.toString(), this);
    }
    
    public long getValue() {
        return value;
    }
    
    public static EnumLong fromValue(long value)
        throws java.lang.IllegalStateException {
        if (value1.value == value) {
            return value1;
        } else if (value2.value == value) {
            return value2;
        }
        throw new IllegalArgumentException();
    }
    
    public static EnumLong fromString(String value)
        throws java.lang.IllegalStateException {
        EnumLong ret = (EnumLong)valueMap.get(value);
        if (ret != null) {
            return ret;
        }
        if (value.equals("-9223372036854775808")) {
            return value1;
        } else if (value.equals("9223372036854775807")) {
            return value2;
        }
        throw new IllegalArgumentException();
    }
    
    public String toString() {
        return new Long(value).toString();
    }
    
    private Object readResolve()
        throws java.io.ObjectStreamException {
        return fromValue(getValue());
    }
    
    public boolean equals(Object obj) {
        if (!(obj instanceof EnumLong)) {
            return false;
        }
        return ((EnumLong)obj).value == value;
    }
    
    public int hashCode() {
        return new Long(value).toString().hashCode();
    }
}
