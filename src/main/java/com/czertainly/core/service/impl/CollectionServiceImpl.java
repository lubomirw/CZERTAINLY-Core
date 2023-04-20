package com.czertainly.core.service.impl;

import com.czertainly.api.exception.*;
import com.czertainly.api.model.client.collection.CollectionItemRequestDto;
import com.czertainly.api.model.client.collection.CollectionItemsImportRequestDto;
import com.czertainly.api.model.client.collection.CollectionRequestDto;
import com.czertainly.api.model.client.collection.CollectionUpdateRequestDto;
import com.czertainly.api.model.common.attribute.v2.content.BaseAttributeContent;
import com.czertainly.api.model.core.auth.Resource;
import com.czertainly.api.model.core.collection.*;
import com.czertainly.core.dao.entity.Collection;
import com.czertainly.core.dao.entity.CollectionItem;
import com.czertainly.core.dao.entity.CollectionSource;
import com.czertainly.core.dao.repository.CollectionItemRepository;
import com.czertainly.core.dao.repository.CollectionRepository;
import com.czertainly.core.dao.repository.CollectionSourceRepository;
import com.czertainly.core.model.auth.ResourceAction;
import com.czertainly.core.model.collection.CollectionEnum;
import com.czertainly.core.security.authz.ExternalAuthorization;
import com.czertainly.core.security.authz.SecuredUUID;
import com.czertainly.core.security.authz.SecurityFilter;
import com.czertainly.core.service.CollectionService;
import com.czertainly.core.util.AttributeDefinitionUtils;
import com.czertainly.core.util.collection.CollectionSourceProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CollectionServiceImpl implements CollectionService {
    private static final Logger logger = LoggerFactory.getLogger(CollectionServiceImpl.class);

    private CollectionRepository collectionRepository;
    private CollectionItemRepository collectionItemRepository;
    private CollectionSourceRepository collectionSourceRepository;
    private List<CollectionEnum> collectionEnums;

    private CollectionSourceProcessor collectionSourceProcessor;

    @Autowired
    public void setCollectionRepository(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    @Autowired
    public void setCollectionItemRepository(CollectionItemRepository collectionItemRepository) {
        this.collectionItemRepository = collectionItemRepository;
    }

    @Autowired
    public void setCollectionSourceRepository(CollectionSourceRepository collectionSourceRepository) {
        this.collectionSourceRepository = collectionSourceRepository;
    }

    @Autowired
    public void setCollectionEnums(List<CollectionEnum> collectionEnums) {
        this.collectionEnums = collectionEnums;
    }

    @Autowired
    public void setCollectionSourceProcessor(CollectionSourceProcessor collectionSourceProcessor) {
        this.collectionSourceProcessor = collectionSourceProcessor;
    }

    @Override
    @ExternalAuthorization(resource = Resource.COLLECTION, action = ResourceAction.LIST)
    public List<CollectionDto> listCollections(SecurityFilter filter) {
        return collectionRepository.findUsingSecurityFilter(filter)
                .stream()
                .map(Collection::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @ExternalAuthorization(resource = Resource.COLLECTION, action = ResourceAction.DETAIL)
    public CollectionDetailDto getCollection(SecuredUUID collectionUuid) throws NotFoundException {
        Collection collection = collectionRepository.findByUuid(collectionUuid)
                .orElseThrow(() -> new NotFoundException(Collection.class, collectionUuid));

        return getCollectionDetailDto(collection);
    }

    @Override
    @ExternalAuthorization(resource = Resource.COLLECTION, action = ResourceAction.CREATE)
    public CollectionDetailDto createCollection(CollectionRequestDto request) throws AlreadyExistException, NotFoundException {
        if (StringUtils.isBlank(request.getName())) {
            throw new ValidationException(ValidationError.create("Collection name must not be empty"));
        }

        if (collectionRepository.findByName(request.getName()).isPresent()) {
            throw new AlreadyExistException(String.format("Collection with name '%s' already exists", request.getName()));
        }

        Collection collection = new Collection();
        collection.setName(request.getName());
        collection.setDescription(request.getDescription());
        collection.setContentType(request.getContentType());
        collection.setSystemCollection(false);
        collectionRepository.save(collection);

        if(request.getSource() != null) {
            CollectionSource source = new CollectionSource();
            source.setType(request.getSource().getType());
            source.setName(request.getSource().getName());
            source.setUrl(request.getSource().getUrl());
            if(request.getSource().getCredentialUuid() != null) source.setCredentialUuid(UUID.fromString(request.getSource().getCredentialUuid()));
            source.setCollection(collection);
            collection.setSource(source);

            collectionSourceRepository.save(source);
        }

        return getCollectionDetailDto(collection);
    }

    @Override
    @ExternalAuthorization(resource = Resource.COLLECTION, action = ResourceAction.UPDATE)
    public CollectionDetailDto editCollection(SecuredUUID collectionUuid, CollectionUpdateRequestDto request) throws NotFoundException, ValidationException {
        Collection collection = collectionRepository.findByUuid(collectionUuid)
                .orElseThrow(() -> new NotFoundException(Collection.class, collectionUuid));

        if(collection.isSystemCollection()) {
            throw new ValidationException(ValidationError.create("System Collection cannot be edited"));
        }

        collection.setDescription(request.getDescription());
        if(request.getSource() == null) {
            if(collection.getSource() != null) {
                collectionSourceRepository.delete(collection.getSource());
                collection.setSource(null);
            }
        }
        else {
            if(collection.getSource() != null) {
                collection.getSource().setType(request.getSource().getType());
                collection.getSource().setName(request.getSource().getName());
                collection.getSource().setUrl(request.getSource().getUrl());
                collection.getSource().setCredentialUuid(UUID.fromString(request.getSource().getCredentialUuid()));
            }
            else {
                CollectionSource source = new CollectionSource();
                source.setType(request.getSource().getType());
                source.setName(request.getSource().getName());
                source.setUrl(request.getSource().getUrl());
                if(request.getSource().getCredentialUuid() != null) source.setCredentialUuid(UUID.fromString(request.getSource().getCredentialUuid()));
                source.setCollection(collection);
                collection.setSource(source);

                collectionSourceRepository.save(source);
            }
        }
        collectionRepository.save(collection);

        return getCollectionDetailDto(collection);
    }

    @Override
    @ExternalAuthorization(resource = Resource.COLLECTION, action = ResourceAction.DELETE)
    public void deleteCollection(SecuredUUID collectionUuid) throws NotFoundException, ValidationException {
        Collection collection = collectionRepository.findByUuid(collectionUuid)
                .orElseThrow(() -> new NotFoundException(Collection.class, collectionUuid));

        if(collection.isSystemCollection()) {
            throw new ValidationException(ValidationError.create("System Collection cannot be deleted"));
        }

        logger.info("Deleting the collection {} with UUID: {}", collection.getName(), collectionUuid);
        collectionRepository.delete(collection);
    }

    @Override
    @ExternalAuthorization(resource = Resource.COLLECTION, action = ResourceAction.DELETE)
    public void bulkDeleteCollections(List<SecuredUUID> collectionUuids) {
        for (SecuredUUID uuid : collectionUuids) {
            try {
                deleteCollection(uuid);
            } catch (NotFoundException e) {
                logger.warn("Unable to find Collection with UUID {}", uuid);
            } catch (ValidationException e) {
                logger.warn("Unable to delete system Collection with UUID {}", uuid);
            }
        }
    }

    @Override
    @ExternalAuthorization(resource = Resource.COLLECTION, action = ResourceAction.UPDATE)
    public CollectionItemDto addCollectionItem(SecuredUUID collectionUuid, CollectionItemRequestDto request) throws AlreadyExistException, NotFoundException, ValidationException {
        Collection collection = collectionRepository.findByUuid(collectionUuid)
                .orElseThrow(() -> new NotFoundException(Collection.class, collectionUuid));

        if(collection.isSystemCollection()) {
            throw new ValidationException(ValidationError.create("Cannot add items to system Collection"));
        }

        if(collection.getSource() != null) {
            throw new ValidationException(ValidationError.create("Cannot add items to collection with items source"));
        }

        if(request.getContent().getData() == null) {
            throw new ValidationException(ValidationError.create("Collection item content cannot be empty"));
        }

        StringBuilder validationMessage = new StringBuilder();
        if(!(AttributeDefinitionUtils.checkAttributeContentType(request.getContent(), collection.getContentType(), validationMessage))) {
            throw new ValidationException(ValidationError.create("Collection item content is not valid based on content type of collection: " + validationMessage.toString()));
        }

        // validate duplicity
        BaseAttributeContent<?> newItemContent = AttributeDefinitionUtils.getSingleAttributeContent(request.getContent(), collection.getContentType());
        for (CollectionItem item: collection.getItems()) {
            if(newItemContent.equals(item.getAttributeContent(collection.getContentType()))) throw new ValidationException(ValidationError.create("Collection item content is duplicate: " + newItemContent.getData().toString()));
        }

        CollectionItem collectionItem = new CollectionItem();
        collectionItem.setCollectionUuid(collectionUuid.getValue());
        collectionItem.setDescription(request.getDescription());
        collectionItem.setDefaultItem(request.isDefaultItem());
        collectionItem.setContent(AttributeDefinitionUtils.serializeSingleAttributeContent(request.getContent()));

        collectionItemRepository.save(collectionItem);
        return collectionItem.mapToDto(collection.getContentType());
    }

    @Override
    @ExternalAuthorization(resource = Resource.COLLECTION, action = ResourceAction.UPDATE)
    public CollectionDetailDto importCollectionItems(SecuredUUID collectionUuid, CollectionItemsImportRequestDto request) throws AlreadyExistException, NotFoundException {
        Collection collection = collectionRepository.findByUuid(collectionUuid)
                .orElseThrow(() -> new NotFoundException(Collection.class, collectionUuid));

        if(collection.isSystemCollection()) {
            throw new ValidationException(ValidationError.create("Cannot import items to system Collection"));
        }

        if(collection.getSource() != null) {
            throw new ValidationException(ValidationError.create("Cannot import items to collection with items source"));
        }

        // clean up existing items
        collectionItemRepository.deleteAll(collection.getItems());
        collection.getItems().clear();

        HashSet<String> importedItems = new HashSet<>();
        String csvContent = new String(Base64.getDecoder().decode(request.getContent()));
        csvContent = csvContent.replace("\r", "");
        String[] csvLines = csvContent.split("\n");
        for (String csvLine: csvLines) {
            String[] itemParts = csvLine.split(";");

            if(itemParts[0] == null || itemParts[0].length() == 0) continue;
            if(importedItems.contains(itemParts[0])) continue;
            importedItems.add(itemParts[0]);

            CollectionItem collectionItem = new CollectionItem();
            collectionItem.setCollectionUuid(collectionUuid.getValue());
            collectionItem.setContent(AttributeDefinitionUtils.serializeSingleAttributeContent(new BaseAttributeContent(itemParts[0], itemParts[0])));
            if(itemParts.length > 1) collectionItem.setDescription(itemParts[1]);

//            collectionItemRepository.save(collectionItem);
            collection.getItems().add(collectionItem);
        }

        collectionRepository.save(collection);

        return getCollectionDetailDto(collection);
    }

    @Override
    @ExternalAuthorization(resource = Resource.COLLECTION, action = ResourceAction.UPDATE)
    public CollectionItemDto editCollectionItem(SecuredUUID collectionUuid, String collectionItemUuid, CollectionItemRequestDto request) throws NotFoundException {
        Collection collection = collectionRepository.findByUuid(collectionUuid)
                .orElseThrow(() -> new NotFoundException(Collection.class, collectionUuid));

        CollectionItem collectionItem = collectionItemRepository.findByUuid(UUID.fromString(collectionItemUuid))
                .orElseThrow(() -> new NotFoundException(CollectionItem.class, collectionItemUuid));

        StringBuilder validationMessage = new StringBuilder();
        if(!(AttributeDefinitionUtils.checkAttributeContentType(request.getContent(), collection.getContentType(), validationMessage))) {
            throw new ValidationException(ValidationError.create("Collection item content is not valid based on content type of collection: " + validationMessage));
        }

        // validate duplicity
        BaseAttributeContent<?> newItemContent = AttributeDefinitionUtils.getSingleAttributeContent(request.getContent(), collection.getContentType());
        for (CollectionItem item: collection.getItems()) {
            if (item.getUuid().toString().equals(collectionItemUuid)) continue;
            if (newItemContent.equals(item.getAttributeContent(collection.getContentType()))) throw new ValidationException(ValidationError.create("Collection item content is duplicate: " + newItemContent.getData().toString()));
        }

        collectionItem.setDescription(request.getDescription());
        collectionItem.setDefaultItem(request.isDefaultItem());
        collectionItem.setContent(AttributeDefinitionUtils.serializeSingleAttributeContent(request.getContent()));

        collectionItemRepository.save(collectionItem);
        return collectionItem.mapToDto(collection.getContentType());
    }

    @Override
    @ExternalAuthorization(resource = Resource.COLLECTION, action = ResourceAction.UPDATE)
    public void deleteCollectionItem(SecuredUUID collectionUuid, String collectionItemUuid) throws NotFoundException {
        CollectionItem collectionItem = collectionItemRepository.findByUuid(UUID.fromString(collectionItemUuid))
                .orElseThrow(() -> new NotFoundException(CollectionItem.class, collectionItemUuid));

        logger.info("Deleting the collection item with UUID: {}", collectionItemUuid);
        collectionItemRepository.delete(collectionItem);
    }

    @Override
    @ExternalAuthorization(resource = Resource.COLLECTION, action = ResourceAction.UPDATE)
    public void bulkDeleteCollectionItems(SecuredUUID collectionUuid, List<String> collectionItemUuids) {
        for (String uuid : collectionItemUuids) {
            try {
                deleteCollectionItem(collectionUuid, uuid);
            } catch (NotFoundException e) {
                logger.warn("Unable to find Collection item with UUID {}", uuid);
            }
        }
    }

    private CollectionDetailDto getCollectionDetailDto(Collection collection) {
        CollectionDetailDto collectionDto = new CollectionDetailDto();
        collectionDto.setUuid(collection.getUuid().toString());
        collectionDto.setName(collection.getName());
        collectionDto.setDescription(collection.getDescription());
        collectionDto.setContentType(collection.getContentType());
        collectionDto.setSystemCollection(collection.isSystemCollection());
        if(collection.getSource() != null) {
            collectionDto.setSource(collection.getSource().mapToDto());
            collectionDto.setItems(collectionSourceProcessor.loadCollectionItems(collection.getSource(), collection.getContentType()));
        }
        else {
            for (CollectionItem item: collection.getItems()) {
                collectionDto.getItems().add(item.mapToDto(collection.getContentType()));
            }
        }

        return collectionDto;
    }

    private void retrieveCollectionItems(Collection collection) {

    }

}
