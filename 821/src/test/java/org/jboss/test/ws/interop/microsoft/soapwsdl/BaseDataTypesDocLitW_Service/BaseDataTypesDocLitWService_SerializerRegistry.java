// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.3, build R1)
// Generated source version: 1.1.3

package org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service;

import com.sun.xml.rpc.client.BasicService;
import com.sun.xml.rpc.encoding.*;
import com.sun.xml.rpc.encoding.simpletype.*;
import com.sun.xml.rpc.encoding.soap.*;
import com.sun.xml.rpc.encoding.literal.*;
import com.sun.xml.rpc.soap.SOAPVersion;
import com.sun.xml.rpc.wsdl.document.schema.SchemaConstants;
import javax.xml.rpc.*;
import javax.xml.rpc.encoding.*;
import javax.xml.namespace.QName;

public class BaseDataTypesDocLitWService_SerializerRegistry implements SerializerConstants {
    public BaseDataTypesDocLitWService_SerializerRegistry() {
    }
    
    public TypeMappingRegistry getRegistry() {
        
        TypeMappingRegistry registry = BasicService.createStandardTypeMappingRegistry();
        TypeMapping mapping12 = registry.getTypeMapping(SOAP12Constants.NS_SOAP_ENCODING);
        TypeMapping mapping = registry.getTypeMapping(SOAPConstants.NS_SOAP_ENCODING);
        TypeMapping mapping2 = registry.getTypeMapping("");
        {
            QName type = new QName("http://tempuri.org/", "RetObject");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetObject_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetObject.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetDecimal");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDecimal_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDecimal.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetSByte");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetSByte_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetSByte.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetBool");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetBool_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetBool.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetShort");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetShort_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetShort.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetByteArrayResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetByteArrayResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetByteArrayResponse.class, type, serializer);
        }
        {
            CombinedSerializer serializer = new LiteralSimpleTypeSerializer(SchemaConstants.QNAME_TYPE_BASE64_BINARY,
                "", XSDBase64BinaryEncoder.getInstance());
            registerSerializer(mapping2,byte[].class, SchemaConstants.QNAME_TYPE_BASE64_BINARY, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetDateTimeResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDateTimeResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDateTimeResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetULong");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetULong_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetULong.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetUriResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetUriResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetUriResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetTimeSpanResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetTimeSpanResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetTimeSpanResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetBoolResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetBoolResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetBoolResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetQNameResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetQNameResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetQNameResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetDouble");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDouble_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDouble.class, type, serializer);
        }
        {
            CombinedSerializer serializer = new LiteralFragmentSerializer(SchemaConstants.QNAME_TYPE_URTYPE, NOT_NULLABLE, "");
            registerSerializer(mapping2,javax.xml.soap.SOAPElement.class, SchemaConstants.QNAME_TYPE_URTYPE, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetDateTime");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDateTime_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDateTime.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetSingle");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetSingle_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetSingle.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetStringResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetStringResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetStringResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetGuidResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetGuidResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetGuidResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetLong");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetLong_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetLong.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetLongResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetLongResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetLongResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetUInt");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetUInt_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetUInt.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetULongResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetULongResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetULongResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetUIntResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetUIntResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetUIntResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetChar");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetChar_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetChar.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetByte");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetByte_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetByte.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetUShortResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetUShortResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetUShortResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetCharResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetCharResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetCharResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetSByteResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetSByteResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetSByteResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetTimeSpan");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetTimeSpan_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetTimeSpan.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetByteArray");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetByteArray_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetByteArray.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetString");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetString_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetString.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetObjectResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetObjectResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetObjectResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetUri");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetUri_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetUri.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetQName");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetQName_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetQName.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetFloatResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetFloatResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetFloatResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetUShort");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetUShort_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetUShort.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetDoubleResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDoubleResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDoubleResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetByteResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetByteResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetByteResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetFloat");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetFloat_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetFloat.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetSingleResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetSingleResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetSingleResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetIntResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetIntResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetIntResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetShortResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetShortResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetShortResponse.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetInt");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetInt_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetInt.class, type, serializer);
        }
        {
            CombinedSerializer serializer = new LiteralSimpleTypeSerializer(SchemaConstants.QNAME_TYPE_ANY_URI,
                "", XSDAnyURIEncoder.getInstance());
            registerSerializer(mapping2,java.net.URI.class, SchemaConstants.QNAME_TYPE_ANY_URI, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetGuid");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetGuid_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetGuid.class, type, serializer);
        }
        {
            QName type = new QName("http://tempuri.org/", "RetDecimalResponse");
            CombinedSerializer serializer = new org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDecimalResponse_LiteralSerializer(type, "", DONT_ENCODE_TYPE);
            registerSerializer(mapping2,org.jboss.test.ws.interop.microsoft.soapwsdl.BaseDataTypesDocLitW_Service.RetDecimalResponse.class, type, serializer);
        }
        return registry;
    }
    
    private static void registerSerializer(TypeMapping mapping, java.lang.Class javaType, javax.xml.namespace.QName xmlType,
        Serializer ser) {
        mapping.register(javaType, xmlType, new SingletonSerializerFactory(ser),
            new SingletonDeserializerFactory((Deserializer)ser));
    }
    
}
