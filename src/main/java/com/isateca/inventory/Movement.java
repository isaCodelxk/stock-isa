package com.isateca.inventory;

import com.isateca.catalog.MovementType;
import com.isateca.catalog.Warehouse;
import com.isateca.security.AppUser;
import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "movement")
@Check(constraints = "quantity > 0")
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "movement_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Origin warehouse, or the only warehouse involved when this isn't a transfer
    @ManyToOne(optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    // Only set for transfers
    @ManyToOne
    @JoinColumn(name = "target_warehouse_id")
    @Nullable
    private Warehouse targetWarehouse;

    @ManyToOne(optional = false)
    @JoinColumn(name = "movement_type_id", nullable = false)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "reference_note")
    @Nullable
    private String referenceNote;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Movement() { // To keep Hibernate happy
    }

    public Movement(Product product, Warehouse warehouse, MovementType movementType, BigDecimal quantity,
            AppUser user, Instant createdAt) {
        this.product = product;
        this.warehouse = warehouse;
        this.movementType = movementType;
        this.quantity = quantity;
        this.user = user;
        this.createdAt = createdAt;
    }

    public @Nullable Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public @Nullable Warehouse getTargetWarehouse() {
        return targetWarehouse;
    }

    public void setTargetWarehouse(@Nullable Warehouse targetWarehouse) {
        this.targetWarehouse = targetWarehouse;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public @Nullable String getReferenceNote() {
        return referenceNote;
    }

    public void setReferenceNote(@Nullable String referenceNote) {
        this.referenceNote = referenceNote;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Movement other = (Movement) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
