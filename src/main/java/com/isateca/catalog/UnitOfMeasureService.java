package com.isateca.catalog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UnitOfMeasureService {

    private final UnitOfMeasureRepository unitOfMeasureRepository;

    UnitOfMeasureService(UnitOfMeasureRepository unitOfMeasureRepository) {
        this.unitOfMeasureRepository = unitOfMeasureRepository;
    }

    @Transactional(readOnly = true)
    public List<UnitOfMeasure> list() {
        return unitOfMeasureRepository.findAll();
    }

    @Transactional
    public UnitOfMeasure save(UnitOfMeasure unitOfMeasure) {
        return unitOfMeasureRepository.save(unitOfMeasure);
    }

    @Transactional
    public void delete(UnitOfMeasure unitOfMeasure) {
        unitOfMeasureRepository.delete(unitOfMeasure);
    }
}
