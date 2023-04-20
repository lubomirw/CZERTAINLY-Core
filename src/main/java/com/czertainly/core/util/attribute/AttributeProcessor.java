package com.czertainly.core.util.attribute;

import com.czertainly.api.model.common.attribute.v2.BaseAttribute;
import com.czertainly.api.model.common.attribute.v2.DataAttribute;
import com.czertainly.core.util.collection.CollectionSourceProcessor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class AttributeProcessor {

    private CollectionSourceProcessor collectionSourceProcessor;

    @Autowired
    public void setCollectionSourceProcessor(CollectionSourceProcessor collectionSourceProcessor) {
        this.collectionSourceProcessor = collectionSourceProcessor;
    }

    public static List<BaseAttribute> loadAttributes(List<BaseAttribute> attributes) {
        for (BaseAttribute attribute: attributes) {
            if (attribute instanceof DataAttribute) {
                DataAttribute dataAttribute = (DataAttribute) attribute;
                if(StringUtils.isBlank(dataAttribute.getProperties().getCollection())) continue;

                // load collection

            }
        }

        return attributes;
    }

}
