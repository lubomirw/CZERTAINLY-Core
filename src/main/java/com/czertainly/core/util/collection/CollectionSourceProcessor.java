package com.czertainly.core.util.collection;

import com.czertainly.api.model.common.attribute.v2.content.AttributeContentType;
import com.czertainly.api.model.common.attribute.v2.content.BaseAttributeContent;
import com.czertainly.api.model.core.collection.CollectionItemDto;
import com.czertainly.api.model.core.collection.CollectionSourceType;
import com.czertainly.core.dao.entity.Collection;
import com.czertainly.core.dao.entity.CollectionItem;
import com.czertainly.core.dao.entity.CollectionSource;
import com.czertainly.core.dao.repository.CollectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CollectionSourceProcessor {

    private CollectionRepository collectionRepository;
    private EnumCollectionSourceLoader enumCollectionSourceLoader;

    private Map<CollectionSourceType, CollectionSourceLoader> loaders = new HashMap<>() {{
        put(CollectionSourceType.ENUM, enumCollectionSourceLoader);
    }};

    @Autowired
    public void setCollectionRepository(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    @Autowired
    public void setEnumCollectionSourceLoader(EnumCollectionSourceLoader enumCollectionSourceLoader) {
        this.enumCollectionSourceLoader = enumCollectionSourceLoader;
    }

    public List<BaseAttributeContent> loadCollectionItemsContent(String collectionName) {
        ArrayList<BaseAttributeContent> collectionItemsContent = new ArrayList<>();

        Optional<Collection> collection = collectionRepository.findByName(collectionName);
        if(!collection.isPresent()) return collectionItemsContent;

        if(collection.get().getSource() == null) {
            for (CollectionItem item: collection.get().getItems()) {
                collectionItemsContent.add(item.getAttributeContent(collection.get().getContentType()));
            }
        }
        else {
            var collectionItems = loadCollectionItems(collection.get().getSource(), collection.get().getContentType());
            for (CollectionItemDto item: collectionItems) {
                collectionItemsContent.add(item.getContent());
            }
        }

        return collectionItemsContent;
    }

    public List<CollectionItemDto> loadCollectionItems(CollectionSource collectionSource, AttributeContentType contentType) {
        CollectionSourceLoader collectionSourceLoader = null;
        switch (collectionSource.getType()) {
            case ENUM:
                collectionSourceLoader = enumCollectionSourceLoader;
                break;
            case REST_API:
                break;
            case CSV_FILE:
                break;
        }

        return collectionSourceLoader.loadCollectionItems(collectionSource, contentType);
    }

}
