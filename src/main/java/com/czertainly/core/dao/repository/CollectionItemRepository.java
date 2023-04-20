package com.czertainly.core.dao.repository;

import com.czertainly.core.dao.entity.CollectionItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface CollectionItemRepository extends JpaRepository<CollectionItem, String> {

    Optional<CollectionItem> findByUuid(UUID uuid);

}
