package com.czertainly.core.dao.entity;

import com.czertainly.api.model.common.attribute.v2.content.AttributeContentType;
import com.czertainly.api.model.core.collection.CollectionDto;
import com.czertainly.core.util.DtoMapper;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "collection")
public class Collection extends UniquelyIdentifiedAndAudited implements Serializable, DtoMapper<CollectionDto> {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "content_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AttributeContentType contentType;

    @Column(name = "system_collection", nullable = false)
    private boolean systemCollection;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL)
    private List<CollectionItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "collection", cascade = CascadeType.ALL)
    private CollectionSource source;

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AttributeContentType getContentType() {
        return contentType;
    }

    public void setContentType(AttributeContentType contentType) {
        this.contentType = contentType;
    }

    public boolean isSystemCollection() { return systemCollection; }

    public void setSystemCollection(boolean systemCollection) { this.systemCollection = systemCollection; }

    public CollectionSource getSource() { return source; }

    public void setSource(CollectionSource source) { this.source = source; }

    public List<CollectionItem> getItems() {
        return items;
    }

    public void setItems(List<CollectionItem> items) {
        this.items = items;
    }

    @Override
    public CollectionDto mapToDto() {
        CollectionDto dto = new CollectionDto();
        dto.setUuid(this.uuid.toString());
        dto.setName(this.name);
        dto.setDescription(this.description);
        dto.setContentType(this.contentType);
        dto.setSystemCollection(this.systemCollection);
        if(source != null) dto.setSource(source.mapToDto());

        return dto;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("uuid", uuid)
                .append("name", name)
                .append("description", description)
                .append("contentType", contentType)
                .append("systemCollection", systemCollection)
                .append("source", source)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Collection that = (Collection) o;
        return new EqualsBuilder().append(uuid, that.uuid).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(uuid).toHashCode();
    }
}
