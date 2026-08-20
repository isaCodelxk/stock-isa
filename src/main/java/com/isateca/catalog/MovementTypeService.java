package com.isateca.catalog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovementTypeService {

    private static final String ADJUSTMENT_IN_NAME = "Ajuste de inventario (entrada)";
    private static final String ADJUSTMENT_OUT_NAME = "Ajuste de inventario (salida)";

    private final MovementTypeRepository movementTypeRepository;

    MovementTypeService(MovementTypeRepository movementTypeRepository) {
        this.movementTypeRepository = movementTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<MovementType> list() {
        return movementTypeRepository.findAll();
    }

    /**
     * The system-managed movement type used to record a manual stock correction (e.g. from the
     * Existencias view) as a proper movement, created on first use since movement types are
     * otherwise entirely user-managed catalog data.
     */
    @Transactional
    public MovementType getOrCreateAdjustmentType(MovementType.Direction direction) {
        var name = direction == MovementType.Direction.IN ? ADJUSTMENT_IN_NAME : ADJUSTMENT_OUT_NAME;
        return movementTypeRepository.findByName(name)
                .orElseGet(() -> movementTypeRepository.save(new MovementType(name, direction)));
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
