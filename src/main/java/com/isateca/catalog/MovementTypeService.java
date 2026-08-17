package com.isateca.catalog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovementTypeService {

    private final MovementTypeRepository movementTypeRepository;

    MovementTypeService(MovementTypeRepository movementTypeRepository) {
        this.movementTypeRepository = movementTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<MovementType> list() {
        return movementTypeRepository.findAll();
    }

    @Transactional
    public MovementType save(MovementType movementType) {
        return movementTypeRepository.save(movementType);
    }

    @Transactional
    public void delete(MovementType movementType) {
        movementTypeRepository.delete(movementType);
    }
}
