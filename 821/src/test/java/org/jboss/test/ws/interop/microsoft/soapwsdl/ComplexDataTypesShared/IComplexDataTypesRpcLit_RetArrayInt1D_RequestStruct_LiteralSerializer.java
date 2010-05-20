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

public class IComplexDataTypesRpcLit_RetArrayInt1D_RequestStruct_LiteralSerializer extends LiteralObjectSerializerBase implements Initializable  {
    private static final javax.xml.namespace.QName ns3_inArrayInt1D_QNAME = new QName("", "inArrayInt1D");
    private static final javax.xml.namespace.QName ns1_ArrayOfint_TYPE_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/Arrays", "ArrayOfint");
    private CombinedSerializer ns1_myArrayOfint_LiteralSerializer;
    
    public IComplexDataTypesRpcLit_RetArrayInt1D_RequestStruct_LiteralSerializer(javax.xml.namespace.QName type, java.lang.String encodingStyle) {
        this(type, encodingStyle, false);
    }
    
    public IComplexDataTypesRpcLit_RetArrayInt1D_RequestStruct_LiteralSerializer(javax.xml.namespace.QName type, java.lang.String encodingStyle, boolean encodeType) {
        super(type, true, encodingStyle, encodeType);
    }
    
    public void initialize(InternalTypeMappingRegistry registry) throws Exception {
        ns1_myArrayOfint_LiteralSerializer = (CombinedSerializer)registry.getSerializer("", org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.ArrayOfint.class, ns1_ArrayOfint_TYPE_QNAME);
    }
    
    public java.lang.Object doDeserialize(XMLReader reader,
        SOAPDeserializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.IComplexDataTypesRpcLit_RetArrayInt1D_RequestStruct instance = new org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.IComplexDataTypesRpcLit_RetArrayInt1D_RequestStruct();
        java.lang.Object member=null;
        javax.xml.namespace.QName elementName;
        java.util.List values;
        java.lang.Object value;
        
        reader.nextElementContent();
        elementName = reader.getName();
        if (reader.getState() == XMLReader.START) {
            if (elementName.equals(ns3_inArrayInt1D_QNAME)) {
                member = ns1_myArrayOfint_LiteralSerializer.deserialize(ns3_inArrayInt1D_QNAME, reader, context);
                if (member == null) {
                    throw new DeserializationException("literal.unexpectedNull");
                }
                instance.setInArrayInt1D((org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.ArrayOfint)member);
                reader.nextElementContent();
            } else {
                throw new DeserializationException("literal.unexpectedElementName", new Object[] { ns3_inArrayInt1D_QNAME, reader.getName() });
            }
        }
        else {
            throw new DeserializationException("literal.expectedElementName", reader.getName().toString());
        }
        
        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        return (java.lang.Object)instance;
    }
    
    public void doSerializeAttributes(java.lang.Object obj, XMLWriter writer, SOAPSerializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.IComplexDataTypesRpcLit_RetArrayInt1D_RequestStruct instance = (org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.IComplexDataTypesRpcLit_RetArrayInt1D_RequestStruct)obj;
        
    }
    public void doSerialize(java.lang.Object obj, XMLWriter writer, SOAPSerializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.IComplexDataTypesRpcLit_RetArrayInt1D_RequestStruct instance = (org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.IComplexDataTypesRpcLit_RetArrayInt1D_RequestStruct)obj;
        
        if (instance.getInArrayInt1D() == null) {
            throw new SerializationException("literal.unexpectedNull");
        }
        ns1_myArrayOfint_LiteralSerializer.serialize(instance.getInArrayInt1D(), ns3_inArrayInt1D_QNAME, null, writer, context);
    }
}
