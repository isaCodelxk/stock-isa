package com.isateca.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovementTypeRepository extends JpaRepository<MovementType, Long> {

    Optional<MovementType> findByName(String name);
}
