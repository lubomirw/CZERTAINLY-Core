package com.czertainly.core.util.collection;

import com.czertainly.api.model.common.attribute.v2.content.AttributeContentType;
import com.czertainly.api.model.core.collection.CollectionItemDto;
import com.czertainly.core.dao.entity.CollectionSource;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface CollectionSourceLoader {

    List<CollectionItemDto> loadCollectionItems(CollectionSource collectionSource, AttributeContentType contentType);

}
