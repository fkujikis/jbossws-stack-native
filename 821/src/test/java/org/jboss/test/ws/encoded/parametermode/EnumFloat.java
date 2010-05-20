// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.2_01, build R40)
// Generated source version: 1.1.2

package org.jboss.test.ws.encoded.parametermode;


import java.util.Map;
import java.util.HashMap;

public class EnumFloat {
    private float value;
    private static Map valueMap = new HashMap();
    public static final float _value1 = (float)-1.00000000;
    public static final float _value2 = (float)3.00000000;
    
    public static final EnumFloat value1 = new EnumFloat(_value1);
    public static final EnumFloat value2 = new EnumFloat(_value2);
    
    protected EnumFloat(float value) {
        this.value = value;
        valueMap.put(this.toString(), this);
    }
    
    public float getValue() {
        return value;
    }
    
    public static EnumFloat fromValue(float value)
        throws java.lang.IllegalStateException {
        if (value1.value == value) {
            return value1;
        } else if (value2.value == value) {
            return value2;
        }
        throw new IllegalArgumentException();
    }
    
    public static EnumFloat fromString(String value)
        throws java.lang.IllegalStateException {
        EnumFloat ret = (EnumFloat)valueMap.get(value);
        if (ret != null) {
            return ret;
        }
        if (value.equals("-1.00000000")) {
            return value1;
        } else if (value.equals("3.00000000")) {
            return value2;
        }
        throw new IllegalArgumentException();
    }
    
    public String toString() {
        return new Float(value).toString();
    }
    
    private Object readResolve()
        throws java.io.ObjectStreamException {
        return fromValue(getValue());
    }
    
    public boolean equals(Object obj) {
        if (!(obj instanceof EnumFloat)) {
            return false;
        }
        return ((EnumFloat)obj).value == value;
    }
    
    public int hashCode() {
        return new Float(value).toString().hashCode();
    }
}
