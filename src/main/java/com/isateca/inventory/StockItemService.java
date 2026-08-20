package com.isateca.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StockItemService {

    private final StockItemRepository stockItemRepository;

    StockItemService(StockItemRepository stockItemRepository) {
        this.stockItemRepository = stockItemRepository;
    }

    @Transactional(readOnly = true)
    public List<StockItem> list() {
        return stockItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<StockItem> find(Long id) {
        return stockItemRepository.findById(id);
    }

    @Transactional
    public void delete(StockItem stockItem) {
        stockItemRepository.delete(stockItem);
    }
}
