package com.isateca.inventory;

import com.isateca.catalog.Category;
import com.isateca.catalog.UnitOfMeasure;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name = "product", uniqueConstraints = @UniqueConstraint(columnNames = "sku"))
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    @Nullable
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_of_measure_id", nullable = false)
    private UnitOfMeasure unitOfMeasure;

    @Column(name = "min_stock", precision = 14, scale = 3)
    @Nullable
    private BigDecimal minStock;

    // Dynamic attributes defined per category via AttributeDefinition.
    // No columnDefinition here on purpose: Hibernate maps SqlTypes.JSON to each
    // dialect's native JSON type (jsonb on PostgreSQL) so the schema stays
    // portable to the H2 database used in tests.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_attributes")
    @Nullable
    private Map<String, Object> customAttributes;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected Product() { // To keep Hibernate happy
    }

    public Product(String sku, String name, Category category, UnitOfMeasure unitOfMeasure) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.unitOfMeasure = unitOfMeasure;
    }

    public @Nullable Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public UnitOfMeasure getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public @Nullable BigDecimal getMinStock() {
        return minStock;
    }

    public void setMinStock(@Nullable BigDecimal minStock) {
        this.minStock = minStock;
    }

    public @Nullable Map<String, Object> getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(@Nullable Map<String, Object> customAttributes) {
        this.customAttributes = customAttributes;
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

        Product other = (Product) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
