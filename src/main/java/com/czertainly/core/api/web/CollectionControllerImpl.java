package com.czertainly.core.api.web;

import com.czertainly.api.exception.AlreadyExistException;
import com.czertainly.api.exception.ConnectorException;
import com.czertainly.api.exception.NotFoundException;
import com.czertainly.api.interfaces.core.web.CollectionController;
import com.czertainly.api.model.client.collection.CollectionItemRequestDto;
import com.czertainly.api.model.client.collection.CollectionItemsImportRequestDto;
import com.czertainly.api.model.client.collection.CollectionRequestDto;
import com.czertainly.api.model.client.collection.CollectionUpdateRequestDto;
import com.czertainly.api.model.core.collection.CollectionDetailDto;
import com.czertainly.api.model.core.collection.CollectionDto;
import com.czertainly.api.model.core.collection.CollectionItemDto;
import com.czertainly.core.security.authz.SecuredUUID;
import com.czertainly.core.security.authz.SecurityFilter;
import com.czertainly.core.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CollectionControllerImpl implements CollectionController {

    @Autowired
    private CollectionService collectionService;

    @Override
    public List<CollectionDto> listCollections() {
        return collectionService.listCollections(SecurityFilter.create());
    }

    @Override
    public CollectionDetailDto getCollection(String uuid) throws NotFoundException {
        return collectionService.getCollection(SecuredUUID.fromString(uuid));
    }

    @Override
    public CollectionDetailDto createCollection(CollectionRequestDto request) throws AlreadyExistException, NotFoundException, ConnectorException {
        return collectionService.createCollection(request);
    }

    @Override
    public CollectionDetailDto editCollection(String uuid, CollectionUpdateRequestDto request) throws NotFoundException, ConnectorException {
        return collectionService.editCollection(SecuredUUID.fromString(uuid), request);
    }

    @Override
    public void deleteCollection(String uuid) throws NotFoundException {
        collectionService.deleteCollection(SecuredUUID.fromString(uuid));
    }

    @Override
    public void bulkDeleteCollections(List<String> collectionUuids) {
        collectionService.bulkDeleteCollections(SecuredUUID.fromList(collectionUuids));
    }

    @Override
    public CollectionItemDto addCollectionItem(String uuid, CollectionItemRequestDto request) throws AlreadyExistException, NotFoundException, ConnectorException {
        return collectionService.addCollectionItem(SecuredUUID.fromString(uuid), request);
    }

    @Override
    public CollectionDetailDto importCollectionItems(String uuid, CollectionItemsImportRequestDto request) throws AlreadyExistException, NotFoundException, ConnectorException {
        return collectionService.importCollectionItems(SecuredUUID.fromString(uuid), request);
    }

    @Override
    public CollectionItemDto editCollectionItem(String uuid, String itemUuid, CollectionItemRequestDto request) throws NotFoundException, ConnectorException {
        return collectionService.editCollectionItem(SecuredUUID.fromString(uuid), itemUuid, request);
    }

    @Override
    public void deleteCollectionItem(String uuid, String itemUuid) throws NotFoundException {
        collectionService.deleteCollectionItem(SecuredUUID.fromString(uuid), itemUuid);
    }

    @Override
    public void bulkDeleteCollectionItems(String uuid, List<String> collectionItemUuids) {
        collectionService.bulkDeleteCollectionItems(SecuredUUID.fromString(uuid), collectionItemUuids);
    }
}
