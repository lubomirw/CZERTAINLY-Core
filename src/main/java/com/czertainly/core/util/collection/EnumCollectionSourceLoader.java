package com.czertainly.core.util.collection;

import com.czertainly.api.model.common.attribute.v2.content.AttributeContentType;
import com.czertainly.api.model.common.attribute.v2.content.BaseAttributeContent;
import com.czertainly.api.model.core.collection.CollectionItemDto;
import com.czertainly.core.dao.entity.CollectionSource;
import com.czertainly.core.model.collection.CollectionEnum;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Component
public class EnumCollectionSourceLoader implements CollectionSourceLoader {

    @Override
    public List<CollectionItemDto> loadCollectionItems(CollectionSource collectionSource, AttributeContentType contentType) {
        CollectionEnum<?,?> collection;
        ArrayList<CollectionItemDto> items = new ArrayList<>();

        try {
            Class<?> clazz = Class.forName(collectionSource.getName());
            Constructor<?> constructor = clazz.getConstructor(new Class[] {} );
            collection = (CollectionEnum<?,?>) constructor.newInstance(new Object[] { });
        } catch (Exception ex) {
            return items;
        }

        if(collection == null) return items;

        EnumSet<? extends Enum<?>> enumCodes = collection.getCodes();
        for (Enum<?> item: enumCodes) {
            CollectionItemDto collectionItem = new CollectionItemDto() {{
               setUuid("");
               setCollectionUuid(collectionSource.getCollectionUuid().toString());
               setDefaultItem(collection.isDefaultItem(item));
               setContent(new BaseAttributeContent(collection.getCode(item).toString(), collection.getCode(item)));
            }};

            items.add(collectionItem);
        }

        return items;
    }
}
