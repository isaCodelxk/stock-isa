package com.isateca.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional
    public StockItem save(StockItem stockItem) {
        return stockItemRepository.save(stockItem);
    }

    @Transactional
    public void delete(StockItem stockItem) {
        stockItemRepository.delete(stockItem);
    }
}
