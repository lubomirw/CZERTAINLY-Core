package com.czertainly.core.model.collection;

import com.czertainly.api.model.common.attribute.v2.content.AttributeContentType;
import com.czertainly.api.model.core.certificate.CertificateType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class CertificateTypeCollection implements CollectionEnum<CertificateType, String> {

    @Override
    public String getName() {
        return CertificateType.class.getSimpleName();
    }

    @Override
    public String getDescription() {
        return "Type of certificate";
    }

    @Override
    public AttributeContentType getContentType() {
        return AttributeContentType.STRING;
    }

    @Override
    public Class<CertificateType> getEnumClass() {
        return CertificateType.class;
    }

    @Override
    public String getCode(Enum<?> value) {
        return ((CertificateType)value).getCode();
    }

    @Override
    public CertificateType getFromCode(String code) {
        return CertificateType.fromCode(code);
    }

    @Override
    public EnumSet<CertificateType> getCodes() {
        return EnumSet.allOf(CertificateType.class);
    }

    @Override
    public boolean isDefaultItem(Enum<?> value) {
        return value == CertificateType.X509;
    }

    @Override
    public String getDescription(Enum<?> value) {
        return "Desc";
    }
}
