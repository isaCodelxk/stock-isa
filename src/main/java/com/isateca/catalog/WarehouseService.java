package com.isateca.catalog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public List<Warehouse> list() {
        return warehouseRepository.findAll();
    }

    @Transactional
    public Warehouse save(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    @Transactional
    public void delete(Warehouse warehouse) {
        warehouseRepository.delete(warehouse);
    }
}
