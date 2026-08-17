package com.isateca.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovementService {

    private final MovementRepository movementRepository;

    MovementService(MovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    @Transactional(readOnly = true)
    public List<Movement> list() {
        return movementRepository.findAll();
    }

    @Transactional
    public Movement save(Movement movement) {
        return movementRepository.save(movement);
    }

    @Transactional
    public void delete(Movement movement) {
        movementRepository.delete(movement);
    }
}
