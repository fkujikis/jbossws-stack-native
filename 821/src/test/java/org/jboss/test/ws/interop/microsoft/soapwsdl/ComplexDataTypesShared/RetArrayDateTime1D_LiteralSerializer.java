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

public class RetArrayDateTime1D_LiteralSerializer extends LiteralObjectSerializerBase implements Initializable  {
    private static final javax.xml.namespace.QName ns3_inArrayDateTime1D_QNAME = new QName("http://tempuri.org/", "inArrayDateTime1D");
    private static final javax.xml.namespace.QName ns4_ArrayOfNullableOfdateTime_TYPE_QNAME = new QName("http://schemas.datacontract.org/2004/07/System", "ArrayOfNullableOfdateTime");
    private CombinedSerializer ns4_myArrayOfNullableOfdateTime_LiteralSerializer;
    
    public RetArrayDateTime1D_LiteralSerializer(javax.xml.namespace.QName type, java.lang.String encodingStyle) {
        this(type, encodingStyle, false);
    }
    
    public RetArrayDateTime1D_LiteralSerializer(javax.xml.namespace.QName type, java.lang.String encodingStyle, boolean encodeType) {
        super(type, true, encodingStyle, encodeType);
    }
    
    public void initialize(InternalTypeMappingRegistry registry) throws Exception {
        ns4_myArrayOfNullableOfdateTime_LiteralSerializer = (CombinedSerializer)registry.getSerializer("", org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.ArrayOfNullableOfdateTime.class, ns4_ArrayOfNullableOfdateTime_TYPE_QNAME);
    }
    
    public java.lang.Object doDeserialize(XMLReader reader,
        SOAPDeserializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.RetArrayDateTime1D instance = new org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.RetArrayDateTime1D();
        java.lang.Object member=null;
        javax.xml.namespace.QName elementName;
        java.util.List values;
        java.lang.Object value;
        
        reader.nextElementContent();
        elementName = reader.getName();
        if (reader.getState() == XMLReader.START) {
            if (elementName.equals(ns3_inArrayDateTime1D_QNAME)) {
                member = ns4_myArrayOfNullableOfdateTime_LiteralSerializer.deserialize(ns3_inArrayDateTime1D_QNAME, reader, context);
                instance.setInArrayDateTime1D((org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.ArrayOfNullableOfdateTime)member);
                reader.nextElementContent();
            }
        }
        
        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        return (java.lang.Object)instance;
    }
    
    public void doSerializeAttributes(java.lang.Object obj, XMLWriter writer, SOAPSerializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.RetArrayDateTime1D instance = (org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.RetArrayDateTime1D)obj;
        
    }
    public void doSerialize(java.lang.Object obj, XMLWriter writer, SOAPSerializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.RetArrayDateTime1D instance = (org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.RetArrayDateTime1D)obj;
        
        ns4_myArrayOfNullableOfdateTime_LiteralSerializer.serialize(instance.getInArrayDateTime1D(), ns3_inArrayDateTime1D_QNAME, null, writer, context);
    }
}
