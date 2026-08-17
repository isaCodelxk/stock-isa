package com.isateca.inventory;

import com.isateca.catalog.Warehouse;
import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_item", uniqueConstraints = @UniqueConstraint(columnNames = { "product_id", "warehouse_id" }))
@Check(constraints = "quantity >= 0")
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "stock_item_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "quantity", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantity = BigDecimal.ZERO;

    protected StockItem() { // To keep Hibernate happy
    }

    public StockItem(Product product, Warehouse warehouse) {
        this.product = product;
        this.warehouse = warehouse;
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        StockItem other = (StockItem) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
