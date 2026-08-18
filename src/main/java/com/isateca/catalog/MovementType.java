package com.isateca.catalog;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "movement_type")
public class MovementType {

    public enum Direction {
        IN, OUT, NEUTRAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "movement_type_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 20)
    private Direction direction;

    @Column(name = "requires_customer", nullable = false)
    private boolean requiresCustomer = false;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected MovementType() { // To keep Hibernate happy
    }

    public MovementType(String name, Direction direction) {
        this.name = name;
        this.direction = direction;
    }

    public @Nullable Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public boolean isRequiresCustomer() {
        return requiresCustomer;
    }

    public void setRequiresCustomer(boolean requiresCustomer) {
        this.requiresCustomer = requiresCustomer;
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

        MovementType other = (MovementType) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
