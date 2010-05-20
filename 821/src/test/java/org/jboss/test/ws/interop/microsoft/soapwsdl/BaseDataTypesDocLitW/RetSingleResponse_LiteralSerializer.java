// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.3, build R1)
// Generated source version: 1.1.3

package org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW;

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

public class RetSingleResponse_LiteralSerializer extends LiteralObjectSerializerBase implements Initializable  {
    private static final javax.xml.namespace.QName ns1_RetSingleResult_QNAME = new QName("http://tempuri.org/", "RetSingleResult");
    private static final javax.xml.namespace.QName ns2_float_TYPE_QNAME = SchemaConstants.QNAME_TYPE_FLOAT;
    private CombinedSerializer ns2_myns2__float__java_lang_Float_Float_Serializer;
    
    public RetSingleResponse_LiteralSerializer(javax.xml.namespace.QName type, java.lang.String encodingStyle) {
        this(type, encodingStyle, false);
    }
    
    public RetSingleResponse_LiteralSerializer(javax.xml.namespace.QName type, java.lang.String encodingStyle, boolean encodeType) {
        super(type, true, encodingStyle, encodeType);
    }
    
    public void initialize(InternalTypeMappingRegistry registry) throws Exception {
        ns2_myns2__float__java_lang_Float_Float_Serializer = (CombinedSerializer)registry.getSerializer("", java.lang.Float.class, ns2_float_TYPE_QNAME);
    }
    
    public java.lang.Object doDeserialize(XMLReader reader,
        SOAPDeserializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW.RetSingleResponse instance = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW.RetSingleResponse();
        java.lang.Object member=null;
        javax.xml.namespace.QName elementName;
        java.util.List values;
        java.lang.Object value;
        
        reader.nextElementContent();
        elementName = reader.getName();
        if (reader.getState() == XMLReader.START) {
            if (elementName.equals(ns1_RetSingleResult_QNAME)) {
                member = ns2_myns2__float__java_lang_Float_Float_Serializer.deserialize(ns1_RetSingleResult_QNAME, reader, context);
                if (member == null) {
                    throw new DeserializationException("literal.unexpectedNull");
                }
                instance.setRetSingleResult((java.lang.Float)member);
                reader.nextElementContent();
            }
        }
        
        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        return (java.lang.Object)instance;
    }
    
    public void doSerializeAttributes(java.lang.Object obj, XMLWriter writer, SOAPSerializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW.RetSingleResponse instance = (org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW.RetSingleResponse)obj;
        
    }
    public void doSerialize(java.lang.Object obj, XMLWriter writer, SOAPSerializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW.RetSingleResponse instance = (org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW.RetSingleResponse)obj;
        
        if (instance.getRetSingleResult() != null) {
            ns2_myns2__float__java_lang_Float_Float_Serializer.serialize(instance.getRetSingleResult(), ns1_RetSingleResult_QNAME, null, writer, context);
        }
    }
}
