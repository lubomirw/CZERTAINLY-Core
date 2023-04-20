package com.czertainly.core.dao.entity;

import com.czertainly.api.model.core.collection.CollectionDto;
import com.czertainly.api.model.core.collection.CollectionSourceDto;
import com.czertainly.api.model.core.collection.CollectionSourceType;
import com.czertainly.core.util.DtoMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "collection_source")
public class CollectionSource extends UniquelyIdentified implements Serializable, DtoMapper<CollectionSourceDto> {
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CollectionSourceType type;

    @Column(name = "name")
    private String name;

    @Column(name = "url")
    private String url;

    @Column(name="collection_uuid")
    private UUID collectionUuid;

    @OneToOne
    @JoinColumn(name = "collection_uuid", insertable = false, updatable = false)
    private Collection collection;

    @Column(name = "credential_uuid")
    private UUID credentialUuid;

    @OneToOne
    @JoinColumn(name = "credential_uuid", insertable = false, updatable = false)
    private Credential credential;

    public CollectionSourceType getType() { return type; }

    public void setType(CollectionSourceType type) { this.type = type; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getUrl() { return url; }

    public void setUrl(String url) { this.url = url; }

    public UUID getCredentialUuid() { return credentialUuid; }

    public void setCredentialUuid(UUID credentialUuid) { this.credentialUuid = credentialUuid; }

    public Credential getCredential() { return credential; }

    public void setCredential(Credential credential) {
        this.credential = credential;
        if(credential != null) this.credentialUuid = credential.getUuid();
        else this.credentialUuid = null;
    }

    public UUID getCollectionUuid() {
        return collectionUuid;
    }

    public void setCollectionUuid(UUID collectionUuid) {
        this.collectionUuid = collectionUuid;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
        if(collection != null) this.collectionUuid = collection.getUuid();
        else this.collectionUuid = null;
    }

    @Override
    public CollectionSourceDto mapToDto() {
        CollectionSourceDto dto = new CollectionSourceDto();
        dto.setType(this.type);
        dto.setName(this.name);
        dto.setUrl(this.url);
        if (this.credentialUuid != null) dto.setCredentialUuid(this.credentialUuid.toString());

        return dto;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("uuid", uuid)
                .append("type", type)
                .append("name", name)
                .append("url", url)
                .append("collectionUuid", collectionUuid)
                .append("credentialUuid", credentialUuid)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollectionSource that = (CollectionSource) o;
        return new EqualsBuilder().append(uuid, that.uuid).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(uuid).toHashCode();
    }
}
