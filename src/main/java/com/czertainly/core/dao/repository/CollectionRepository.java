package com.czertainly.core.dao.repository;

import com.czertainly.api.model.core.collection.CollectionSourceType;
import com.czertainly.api.model.core.connector.ConnectorStatus;
import com.czertainly.core.dao.entity.Collection;
import com.czertainly.core.dao.entity.Connector;
import com.czertainly.core.dao.entity.FunctionGroup;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface CollectionRepository extends SecurityFilterRepository<Collection, String> {

    Optional<Collection> findByUuid(UUID uuid);

    Optional<Collection> findByName(String name);

    List<Collection> findBySourceType(CollectionSourceType sourceType);

}
