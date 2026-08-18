package com.isateca.customer;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "customer_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone")
    @Nullable
    private String phone;

    @Column(name = "email")
    @Nullable
    private String email;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected Customer() { // To keep Hibernate happy
    }

    public Customer(String name) {
        this.name = name;
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

    public @Nullable String getPhone() {
        return phone;
    }

    public void setPhone(@Nullable String phone) {
        this.phone = phone;
    }

    public @Nullable String getEmail() {
        return email;
    }

    public void setEmail(@Nullable String email) {
        this.email = email;
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

        Customer other = (Customer) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
