// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.3, build R1)
// Generated source version: 1.1.3

package org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared;

import com.sun.xml.rpc.encoding.*;
import com.sun.xml.rpc.encoding.xsd.XSDConstants;
import com.sun.xml.rpc.encoding.literal.*;
import com.sun.xml.rpc.encoding.literal.DetailFragmentDeserializer;
import com.sun.xml.rpc.encoding.simpletype.*;
import com.sun.xml.rpc.encoding.soap.SOAPConstants;
import com.sun.xml.rpc.encoding.soap.SOAP12Constants;
import com.sun.xml.rpc.streaming.*;
import com.sun.xml.rpc.wsdl.document.schema.SchemaConstants;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;

public class ArrayOfshort_LiteralSerializer extends LiteralObjectSerializerBase implements Initializable  {
    private static final javax.xml.namespace.QName ns1_short_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/Arrays", "short");
    private static final javax.xml.namespace.QName ns2_short_TYPE_QNAME = SchemaConstants.QNAME_TYPE_SHORT;
    private CombinedSerializer ns2_myns2__short__short_Short_Serializer;
    
    public ArrayOfshort_LiteralSerializer(javax.xml.namespace.QName type, java.lang.String encodingStyle) {
        this(type, encodingStyle, false);
    }
    
    public ArrayOfshort_LiteralSerializer(javax.xml.namespace.QName type, java.lang.String encodingStyle, boolean encodeType) {
        super(type, true, encodingStyle, encodeType);
    }
    
    public void initialize(InternalTypeMappingRegistry registry) throws Exception {
        ns2_myns2__short__short_Short_Serializer = (CombinedSerializer)registry.getSerializer("", short.class, ns2_short_TYPE_QNAME);
    }
    
    public java.lang.Object doDeserialize(XMLReader reader,
        SOAPDeserializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.ArrayOfshort instance = new org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.ArrayOfshort();
        java.lang.Object member=null;
        javax.xml.namespace.QName elementName;
        java.util.List values;
        java.lang.Object value;
        
        reader.nextElementContent();
        elementName = reader.getName();
        if ((reader.getState() == XMLReader.START) && (elementName.equals(ns1_short_QNAME))) {
            values = new ArrayList();
            for(;;) {
                elementName = reader.getName();
                if ((reader.getState() == XMLReader.START) && (elementName.equals(ns1_short_QNAME))) {
                    value = ns2_myns2__short__short_Short_Serializer.deserialize(ns1_short_QNAME, reader, context);
                    if (value == null) {
                        throw new DeserializationException("literal.unexpectedNull");
                    }
                    values.add(value);
                    reader.nextElementContent();
                } else {
                    break;
                }
            }
            member = new short[values.size()];
            for (int i = 0; i < values.size(); ++i) {
                ((short[]) member)[i] = ((Short)(values.get(i))).shortValue();
            }
            instance.set_short((short[])member);
        }
        else {
            instance.set_short(new short[0]);
        }
        
        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        return (java.lang.Object)instance;
    }
    
    public void doSerializeAttributes(java.lang.Object obj, XMLWriter writer, SOAPSerializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.ArrayOfshort instance = (org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.ArrayOfshort)obj;
        
    }
    public void doSerialize(java.lang.Object obj, XMLWriter writer, SOAPSerializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.ArrayOfshort instance = (org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.ArrayOfshort)obj;
        
        if (instance.get_short() != null) {
            for (int i = 0; i < instance.get_short().length; ++i) {
                ns2_myns2__short__short_Short_Serializer.serialize(new Short(instance.get_short()[i]), ns1_short_QNAME, null, writer, context);
            }
        }
    }
}
