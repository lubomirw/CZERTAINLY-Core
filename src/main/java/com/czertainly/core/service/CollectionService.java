package com.czertainly.core.service;

import com.czertainly.api.exception.AlreadyExistException;
import com.czertainly.api.exception.ConnectorException;
import com.czertainly.api.exception.NotFoundException;
import com.czertainly.api.exception.ValidationException;
import com.czertainly.api.model.client.attribute.custom.CustomAttributeCreateRequestDto;
import com.czertainly.api.model.client.collection.CollectionItemRequestDto;
import com.czertainly.api.model.client.collection.CollectionItemsImportRequestDto;
import com.czertainly.api.model.client.collection.CollectionRequestDto;
import com.czertainly.api.model.client.collection.CollectionUpdateRequestDto;
import com.czertainly.api.model.core.collection.CollectionDetailDto;
import com.czertainly.api.model.core.collection.CollectionDto;
import com.czertainly.api.model.core.collection.CollectionItemDto;
import com.czertainly.core.security.authz.SecuredUUID;
import com.czertainly.core.security.authz.SecurityFilter;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CollectionService {

    /**
     * Function to list the available collections stored in the database
     *
     * @return - List of Collections stored in the database
     */
    public List<CollectionDto> listCollections(SecurityFilter filter);

    /**
     * Function to get the detail of the collection by providing the UUID
     *
     * @param uuid UUID of collection
     * @return Collection detail
     */
    public CollectionDetailDto getCollection(SecuredUUID collectionUuid) throws NotFoundException;

    /**
     * Function to create the collection based on the user provided information
     *
     * @param request: {@link CollectionRequestDto} request information
     * @return Collection detail of newly created collection
     */
    public CollectionDetailDto createCollection(CollectionRequestDto request) throws AlreadyExistException, NotFoundException;

    /**
     * Function to update the collection
     *
     * @param uuid    UUID of the collection
     * @param request {@link CollectionUpdateRequestDto} request information
     * @return Collection detail of updated collection
     */
    public CollectionDetailDto editCollection(SecuredUUID collectionUuid, CollectionUpdateRequestDto request) throws NotFoundException, ValidationException;

    /**
     * Function to delete collection
     *
     * @param uuid  Collection UUID
     * @throws NotFoundException
     */
    public void deleteCollection(SecuredUUID collectionUuid) throws NotFoundException, ValidationException;

    /**
     * Delete multiple collections from the database
     *
     * @param collectionUuids UUIDs of the collections to be deleted
     */
    public void bulkDeleteCollections(List<SecuredUUID> collectionUuids);

    /**
     * Function to create the collection item based on the user provided information
     *
     * @param uuid  Collection UUID
     * @param request: {@link CollectionItemRequestDto} request information
     * @return Collection item detail of newly created item
     */
    public CollectionItemDto addCollectionItem(SecuredUUID collectionUuid, CollectionItemRequestDto request) throws AlreadyExistException, NotFoundException, ValidationException;

    /**
     * Function to import the collection items
     *
     * @param uuid  Collection UUID
     * @param request: {@link CollectionItemsImportRequestDto} request information
     * @return Collection detail
     */
    public CollectionDetailDto importCollectionItems(SecuredUUID collectionUuid, CollectionItemsImportRequestDto request) throws AlreadyExistException, NotFoundException, ValidationException;

    /**
     * Function to update the collection item based on the user provided information
     *
     * @param uuid  Collection UUID
     * @param itemUuid  Collection item UUID
     * @param request: {@link CollectionItemRequestDto} request information
     * @return Collection item detail of updated item
     */
    public CollectionItemDto editCollectionItem(SecuredUUID collectionUuid, String itemUuid, CollectionItemRequestDto request) throws NotFoundException;

    /**
     * Function to delete collection item
     *
     * @param uuid  Collection UUID
     * @param itemUuid  Collection item UUID
     * @throws NotFoundException
     */
    public void deleteCollectionItem(SecuredUUID collectionUuid, String itemUuid) throws NotFoundException;

    /**
     * Delete multiple collection items from the database
     *
     * @param uuid  Collection UUID
     * @param collectionItemUuids UUIDs of the collection items to be deleted
     */
    public void bulkDeleteCollectionItems(SecuredUUID collectionUuid, List<String> collectionItemUuids);

}
