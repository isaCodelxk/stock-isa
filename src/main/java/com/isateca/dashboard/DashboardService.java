package com.isateca.dashboard;

import com.isateca.catalog.WarehouseService;
import com.isateca.inventory.Movement;
import com.isateca.inventory.MovementService;
import com.isateca.inventory.Product;
import com.isateca.inventory.ProductService;
import com.isateca.inventory.StockItem;
import com.isateca.inventory.StockItemService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Aggregates data already owned by the catalog/inventory services into the numbers the dashboard
 * shows. Read-only: datasets are small enough that summing/grouping in memory over each service's
 * existing {@code list()} is simpler than adding bespoke aggregate queries per metric.
 */
@Service
public class DashboardService {

    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final StockItemService stockItemService;
    private final MovementService movementService;

    DashboardService(ProductService productService, WarehouseService warehouseService,
            StockItemService stockItemService, MovementService movementService) {
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.stockItemService = stockItemService;
        this.movementService = movementService;
    }

    public Summary getSummary() {
        var products = productService.list();
        var warehouses = warehouseService.list();
        var stockItems = stockItemService.list();

        var activeProducts = products.stream().filter(Product::isActive).count();
        var activeWarehouses = warehouses.stream().filter(w -> w.isActive()).count();
        var stockedItems = stockItems.stream().filter(si -> si.getQuantity().signum() > 0).count();

        var quantityByProduct = stockItems.stream().collect(Collectors.groupingBy(StockItem::getProduct,
                Collectors.reducing(BigDecimal.ZERO, StockItem::getQuantity, BigDecimal::add)));

        var lowStock = products.stream().filter(Product::isActive).filter(p -> p.getMinStock() != null)
                .map(p -> new LowStockEntry(p, quantityByProduct.getOrDefault(p, BigDecimal.ZERO)))
                .filter(entry -> entry.currentQuantity().compareTo(entry.product().getMinStock()) < 0).toList();

        var productsByCategory = products.stream().filter(Product::isActive)
                .collect(Collectors.groupingBy(p -> p.getCategory().getName(), Collectors.counting())).entrySet()
                .stream().map(e -> new NamedCount(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(NamedCount::name)).toList();

        var recentMovements = movementService.listRecent();
        var recentMovementsByType = recentMovements.stream()
                .collect(Collectors.groupingBy(m -> m.getMovementType().getName(), Collectors.counting()))
                .entrySet().stream().map(e -> new NamedCount(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(NamedCount::name)).toList();

        return new Summary(activeProducts, activeWarehouses, stockedItems, lowStock, recentMovements,
                productsByCategory, recentMovementsByType);
    }

    public record LowStockEntry(Product product, BigDecimal currentQuantity) {
    }

    public record NamedCount(String name, long count) {
    }

    public record Summary(long activeProducts, long activeWarehouses, long stockedItemCount,
            List<LowStockEntry> lowStock, List<Movement> recentMovements, List<NamedCount> productsByCategory,
            List<NamedCount> recentMovementsByType) {
    }
}
