package com.isateca.catalog;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "attribute_definition")
public class AttributeDefinition {

    public enum DataType {
        TEXT, NUMBER, DATE, BOOLEAN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "attribute_definition_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @Nullable
    private Category category;

    @Column(name = "attribute_key", nullable = false)
    private String key;

    @Column(name = "label", nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private DataType dataType;

    @Column(name = "required", nullable = false)
    private boolean required = false;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected AttributeDefinition() { // To keep Hibernate happy
    }

    public AttributeDefinition(String key, String label, DataType dataType) {
        this.key = key;
        this.label = label;
        this.dataType = dataType;
    }

    public @Nullable Long getId() {
        return id;
    }

    public @Nullable Category getCategory() {
        return category;
    }

    public void setCategory(@Nullable Category category) {
        this.category = category;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        AttributeDefinition other = (AttributeDefinition) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
