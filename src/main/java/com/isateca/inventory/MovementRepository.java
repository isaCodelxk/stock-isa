package com.isateca.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovementRepository extends JpaRepository<Movement, Long> {

    List<Movement> findTop10ByOrderByCreatedAtDesc();

    List<Movement> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Movement> findByProductIdOrderByCreatedAtAsc(Long productId);
}
