// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.3, build R1)
// Generated source version: 1.1.3

package org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesRpcLit;

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

public class IBaseDataTypesRpcLit_RetQName_RequestStruct_LiteralSerializer extends LiteralObjectSerializerBase implements Initializable  {
    private static final javax.xml.namespace.QName ns1_inQName_QNAME = new QName("", "inQName");
    private static final javax.xml.namespace.QName ns2_QName_TYPE_QNAME = SchemaConstants.QNAME_TYPE_QNAME;
    private CombinedSerializer ns2_myns2_QName__javax_xml_namespace_QName_QName_Serializer;
    
    public IBaseDataTypesRpcLit_RetQName_RequestStruct_LiteralSerializer(javax.xml.namespace.QName type, java.lang.String encodingStyle) {
        this(type, encodingStyle, false);
    }
    
    public IBaseDataTypesRpcLit_RetQName_RequestStruct_LiteralSerializer(javax.xml.namespace.QName type, java.lang.String encodingStyle, boolean encodeType) {
        super(type, true, encodingStyle, encodeType);
    }
    
    public void initialize(InternalTypeMappingRegistry registry) throws Exception {
        ns2_myns2_QName__javax_xml_namespace_QName_QName_Serializer = (CombinedSerializer)registry.getSerializer("", javax.xml.namespace.QName.class, ns2_QName_TYPE_QNAME);
    }
    
    public java.lang.Object doDeserialize(XMLReader reader,
        SOAPDeserializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesRpcLit.IBaseDataTypesRpcLit_RetQName_RequestStruct instance = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesRpcLit.IBaseDataTypesRpcLit_RetQName_RequestStruct();
        java.lang.Object member=null;
        javax.xml.namespace.QName elementName;
        java.util.List values;
        java.lang.Object value;
        
        reader.nextElementContent();
        elementName = reader.getName();
        if (reader.getState() == XMLReader.START) {
            if (elementName.equals(ns1_inQName_QNAME)) {
                member = ns2_myns2_QName__javax_xml_namespace_QName_QName_Serializer.deserialize(ns1_inQName_QNAME, reader, context);
                if (member == null) {
                    throw new DeserializationException("literal.unexpectedNull");
                }
                instance.setInQName((javax.xml.namespace.QName)member);
                reader.nextElementContent();
            } else {
                throw new DeserializationException("literal.unexpectedElementName", new Object[] { ns1_inQName_QNAME, reader.getName() });
            }
        }
        else {
            throw new DeserializationException("literal.expectedElementName", reader.getName().toString());
        }
        
        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        return (java.lang.Object)instance;
    }
    
    public void doSerializeAttributes(java.lang.Object obj, XMLWriter writer, SOAPSerializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesRpcLit.IBaseDataTypesRpcLit_RetQName_RequestStruct instance = (org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesRpcLit.IBaseDataTypesRpcLit_RetQName_RequestStruct)obj;
        
    }
    public void doSerialize(java.lang.Object obj, XMLWriter writer, SOAPSerializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesRpcLit.IBaseDataTypesRpcLit_RetQName_RequestStruct instance = (org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesRpcLit.IBaseDataTypesRpcLit_RetQName_RequestStruct)obj;
        
        if (instance.getInQName() == null) {
            throw new SerializationException("literal.unexpectedNull");
        }
        ns2_myns2_QName__javax_xml_namespace_QName_QName_Serializer.serialize(instance.getInQName(), ns1_inQName_QNAME, null, writer, context);
    }
}
