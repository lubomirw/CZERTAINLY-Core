package com.czertainly.core.model.collection;

import com.czertainly.api.exception.AlreadyExistException;
import com.czertainly.api.model.common.attribute.v2.content.AttributeContentType;
import com.czertainly.api.model.core.collection.CollectionSourceType;
import com.czertainly.core.dao.entity.Collection;
import com.czertainly.core.dao.entity.CollectionSource;
import com.czertainly.core.dao.repository.CollectionRepository;
import com.czertainly.core.dao.repository.CollectionSourceRepository;
import com.czertainly.core.model.auth.ResourceSyncRequestDto;
import com.czertainly.core.model.auth.SyncResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CollectionEnumSynchronizer {

    private static final Logger logger = LoggerFactory.getLogger(CollectionEnumSynchronizer.class);

    private List<CollectionEnum> collectionEnums;

    private CollectionRepository collectionRepository;
    private CollectionSourceRepository collectionSourceRepository;

    @Autowired
    public void setCollectionEnums(List<CollectionEnum> collectionEnums) {
        this.collectionEnums = collectionEnums;
    }

    @Autowired
    public void setCollectionRepository(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    @Autowired
    public void setCollectionSourceRepository(CollectionSourceRepository collectionSourceRepository) {
        this.collectionSourceRepository = collectionSourceRepository;
    }

    @EventListener({ApplicationReadyEvent.class})
    public void register() throws AlreadyExistException {
        logger.info("Initiating Collection enums sync");
        Map<String, Collection> collections = collectionRepository.findBySourceType(CollectionSourceType.ENUM)
                .stream().collect(Collectors.toMap(Collection::getName, Function.identity()));

        for (CollectionEnum collectionEnum: collectionEnums) {
            Collection collection = collections.get(collectionEnum.getName());
            if(collection != null) {
                if(!collection.getSource().getName().equals(collectionEnum.getClass().getName())) {
                    logger.warn("Collection with name {} has different reference name in DB as is its class name ({} / {}).", collectionEnum.getName(), collection.getSource().getName(), collectionEnum.getClass().getName());
                    collection.getSource().setName(collectionEnum.getClass().getName());
                }
                collections.remove(collectionEnum.getName());
            }
            else {
                if (collectionRepository.findByName(collectionEnum.getName()).isPresent()) {
                    throw new AlreadyExistException(String.format("Collection with name '%s' already exists", collectionEnum.getName()));
                }

                logger.info("Adding new collection with name {}", collectionEnum.getName());

                collection = new Collection();
                collection.setName(collectionEnum.getName());
                collection.setDescription(collectionEnum.getDescription());
                collection.setContentType(collectionEnum.getContentType());
                collection.setSystemCollection(true);
                collectionRepository.save(collection);

                CollectionSource source = new CollectionSource();
                source.setType(CollectionSourceType.ENUM);
                source.setName(collectionEnum.getClass().getName());
                source.setCollection(collection);
                collection.setSource(source);

                collectionSourceRepository.save(source);
            }
        }

        // remove nonexisting collections
        for (Collection collection: collections.values()) {
            logger.info("Removing collection with name {}", collection.getName());
            collectionRepository.delete(collection);
        }

        logger.info("Collection enums synced");
    }
}
