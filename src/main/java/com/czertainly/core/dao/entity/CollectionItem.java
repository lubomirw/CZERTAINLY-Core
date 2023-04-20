package com.czertainly.core.dao.entity;

import com.czertainly.api.model.common.attribute.v2.content.AttributeContentType;
import com.czertainly.api.model.common.attribute.v2.content.BaseAttributeContent;
import com.czertainly.api.model.core.collection.CollectionItemDto;
import com.czertainly.core.util.AttributeDefinitionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "collection_item")
public class CollectionItem extends UniquelyIdentified implements Serializable {

    @Column(name = "description")
    private String description;

    @Column(name = "default_item", nullable = false)
    private boolean defaultItem;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "collection_uuid", nullable = false)
    private UUID collectionUuid;

    @ManyToOne
    @JoinColumn(name = "collection_uuid", insertable = false, updatable = false)
    private Collection collection;

    public UUID getCollectionUuid() { return collectionUuid; }

    public void setCollectionUuid(UUID collectionUuid) {
        this.collectionUuid = collectionUuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDefaultItem() {
        return defaultItem;
    }

    public void setDefaultItem(boolean defaultItem) {
        this.defaultItem = defaultItem;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
        if(collection != null) this.collectionUuid = collection.getUuid();
        else this.collectionUuid = null;
    }

    public BaseAttributeContent<?> getAttributeContent(AttributeContentType collectionContentType) {
        return deserializeAttributeContent(AttributeContentType.getClass(collectionContentType));
    }

    private <T extends BaseAttributeContent> T deserializeAttributeContent(Class<T> clazz) {
        return AttributeDefinitionUtils.deserializeSingleAttributeContent(content, clazz);
    }

    public CollectionItemDto mapToDto(AttributeContentType collectionContentType) {
        CollectionItemDto dto = new CollectionItemDto();
        dto.setUuid(this.uuid.toString());
        dto.setCollectionUuid(this.collectionUuid.toString());
        dto.setDescription(this.description);
        dto.setDefaultItem(this.defaultItem);
        dto.setContent(getAttributeContent(collectionContentType));

        return dto;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("uuid", uuid)
                .append("collectionUuid", collectionUuid)
                .append("description", description)
                .append("defaultItem", defaultItem)
                .append("content", content)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollectionItem that = (CollectionItem) o;
        return new EqualsBuilder().append(uuid, that.uuid).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(uuid).toHashCode();
    }
}
