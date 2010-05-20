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

public class IComplexDataTypesRpcLit_RetStructS1_RequestStruct_LiteralSerializer extends LiteralObjectSerializerBase implements Initializable  {
    private static final javax.xml.namespace.QName ns3_inStructS1_QNAME = new QName("", "inStructS1");
    private static final javax.xml.namespace.QName ns5_Name_TYPE_QNAME = new QName("http://schemas.datacontract.org/2004/07/XwsInterop.SoapWsdl.ComplexDataTypes.XmlFormatter.Service.Indigo", "Name");
    private CombinedSerializer ns5_myName_LiteralSerializer;
    
    public IComplexDataTypesRpcLit_RetStructS1_RequestStruct_LiteralSerializer(javax.xml.namespace.QName type, java.lang.String encodingStyle) {
        this(type, encodingStyle, false);
    }
    
    public IComplexDataTypesRpcLit_RetStructS1_RequestStruct_LiteralSerializer(javax.xml.namespace.QName type, java.lang.String encodingStyle, boolean encodeType) {
        super(type, true, encodingStyle, encodeType);
    }
    
    public void initialize(InternalTypeMappingRegistry registry) throws Exception {
        ns5_myName_LiteralSerializer = (CombinedSerializer)registry.getSerializer("", org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.Name.class, ns5_Name_TYPE_QNAME);
    }
    
    public java.lang.Object doDeserialize(XMLReader reader,
        SOAPDeserializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.IComplexDataTypesRpcLit_RetStructS1_RequestStruct instance = new org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.IComplexDataTypesRpcLit_RetStructS1_RequestStruct();
        java.lang.Object member=null;
        javax.xml.namespace.QName elementName;
        java.util.List values;
        java.lang.Object value;
        
        reader.nextElementContent();
        elementName = reader.getName();
        if (reader.getState() == XMLReader.START) {
            if (elementName.equals(ns3_inStructS1_QNAME)) {
                member = ns5_myName_LiteralSerializer.deserialize(ns3_inStructS1_QNAME, reader, context);
                if (member == null) {
                    throw new DeserializationException("literal.unexpectedNull");
                }
                instance.setInStructS1((org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.Name)member);
                reader.nextElementContent();
            } else {
                throw new DeserializationException("literal.unexpectedElementName", new Object[] { ns3_inStructS1_QNAME, reader.getName() });
            }
        }
        else {
            throw new DeserializationException("literal.expectedElementName", reader.getName().toString());
        }
        
        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        return (java.lang.Object)instance;
    }
    
    public void doSerializeAttributes(java.lang.Object obj, XMLWriter writer, SOAPSerializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.IComplexDataTypesRpcLit_RetStructS1_RequestStruct instance = (org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.IComplexDataTypesRpcLit_RetStructS1_RequestStruct)obj;
        
    }
    public void doSerialize(java.lang.Object obj, XMLWriter writer, SOAPSerializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.IComplexDataTypesRpcLit_RetStructS1_RequestStruct instance = (org.jboss.test.ws.interop.microsoft.soapwsdl.ComplexDataTypesShared.IComplexDataTypesRpcLit_RetStructS1_RequestStruct)obj;
        
        if (instance.getInStructS1() == null) {
            throw new SerializationException("literal.unexpectedNull");
        }
        ns5_myName_LiteralSerializer.serialize(instance.getInStructS1(), ns3_inStructS1_QNAME, null, writer, context);
    }
}
