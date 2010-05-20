// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.3, build R1)
// Generated source version: 1.1.3

package org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service;

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

public class RetDecimalResponse_LiteralSerializer extends LiteralObjectSerializerBase implements Initializable  {
    private static final javax.xml.namespace.QName ns1_RetDecimalResult_QNAME = new QName("http://tempuri.org/", "RetDecimalResult");
    private static final javax.xml.namespace.QName ns2_decimal_TYPE_QNAME = SchemaConstants.QNAME_TYPE_DECIMAL;
    private CombinedSerializer ns2_myns2_decimal__java_math_BigDecimal_Decimal_Serializer;
    
    public RetDecimalResponse_LiteralSerializer(javax.xml.namespace.QName type, java.lang.String encodingStyle) {
        this(type, encodingStyle, false);
    }
    
    public RetDecimalResponse_LiteralSerializer(javax.xml.namespace.QName type, java.lang.String encodingStyle, boolean encodeType) {
        super(type, true, encodingStyle, encodeType);
    }
    
    public void initialize(InternalTypeMappingRegistry registry) throws Exception {
        ns2_myns2_decimal__java_math_BigDecimal_Decimal_Serializer = (CombinedSerializer)registry.getSerializer("", java.math.BigDecimal.class, ns2_decimal_TYPE_QNAME);
    }
    
    public java.lang.Object doDeserialize(XMLReader reader,
        SOAPDeserializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDecimalResponse instance = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDecimalResponse();
        java.lang.Object member=null;
        javax.xml.namespace.QName elementName;
        java.util.List values;
        java.lang.Object value;
        
        reader.nextElementContent();
        elementName = reader.getName();
        if (reader.getState() == XMLReader.START) {
            if (elementName.equals(ns1_RetDecimalResult_QNAME)) {
                member = ns2_myns2_decimal__java_math_BigDecimal_Decimal_Serializer.deserialize(ns1_RetDecimalResult_QNAME, reader, context);
                if (member == null) {
                    throw new DeserializationException("literal.unexpectedNull");
                }
                instance.setRetDecimalResult((java.math.BigDecimal)member);
                reader.nextElementContent();
            }
        }
        
        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        return (java.lang.Object)instance;
    }
    
    public void doSerializeAttributes(java.lang.Object obj, XMLWriter writer, SOAPSerializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDecimalResponse instance = (org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDecimalResponse)obj;
        
    }
    public void doSerialize(java.lang.Object obj, XMLWriter writer, SOAPSerializationContext context) throws java.lang.Exception {
        org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDecimalResponse instance = (org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDecimalResponse)obj;
        
        if (instance.getRetDecimalResult() != null) {
            ns2_myns2_decimal__java_math_BigDecimal_Decimal_Serializer.serialize(instance.getRetDecimalResult(), ns1_RetDecimalResult_QNAME, null, writer, context);
        }
    }
}
