package com.czertainly.core.model.collection;

import com.czertainly.api.model.common.attribute.v2.content.AttributeContentType;

import java.util.EnumSet;

public interface CollectionEnum<TEnum extends Enum<TEnum>, TCode> {

    String getName();

    String getDescription();

    AttributeContentType getContentType();

    Class<TEnum> getEnumClass();

    TCode getCode(Enum<? extends Enum<?>> value);

    TEnum getFromCode(TCode code);

    EnumSet<TEnum> getCodes();

    boolean isDefaultItem(Enum<? extends Enum<?>> value);

    String getDescription(Enum<? extends Enum<?>> value);

}
