package com.czertainly.core.dao.repository;

import com.czertainly.core.dao.entity.CollectionSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface CollectionSourceRepository extends JpaRepository<CollectionSource, String> {

    Optional<CollectionSource> findByUuid(UUID uuid);

}
