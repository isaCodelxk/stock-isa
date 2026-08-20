package com.isateca.inventory;

import com.isateca.catalog.MovementType;
import com.isateca.catalog.MovementTypeService;
import com.isateca.catalog.Warehouse;
import com.isateca.customer.Customer;
import com.isateca.security.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class MovementService {

    private final MovementRepository movementRepository;
    private final StockItemRepository stockItemRepository;
    private final MovementTypeService movementTypeService;

    MovementService(MovementRepository movementRepository, StockItemRepository stockItemRepository,
            MovementTypeService movementTypeService) {
        this.movementRepository = movementRepository;
        this.stockItemRepository = stockItemRepository;
        this.movementTypeService = movementTypeService;
    }

    @Transactional(readOnly = true)
    public List<Movement> list() {
        return movementRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Movement> listRecent() {
        return movementRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Movement> listByCustomer(Customer customer) {
        return movementRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
    }

    @Transactional(readOnly = true)
    public List<Movement> listByProduct(Product product) {
        return movementRepository.findByProductIdOrderByCreatedAtAsc(product.getId());
    }

    /**
     * Persists a new movement and applies its effect to the relevant stock item(s). Movements are
     * append-only (see {@code MovementCrud.isEditable()}), so this is only ever used for creation.
     */
    @Transactional
    public Movement save(Movement movement) {
        applyToStock(movement, false);
        return movementRepository.save(movement);
    }

    /**
     * Deletes a movement after reverting its effect on stock. Fails if reverting would drive a stock
     * item negative, e.g. deleting an old "in" movement whose quantity has since been consumed by a
     * later movement.
     */
    @Transactional
    public void delete(Movement movement) {
        applyToStock(movement, true);
        movementRepository.delete(movement);
    }

    /**
     * Sets a stock item's quantity to {@code newQuantity} by recording an inventory-adjustment
     * movement for the difference (IN if it went up, OUT if it went down), instead of writing
     * stock_item directly - so a manual correction from the Existencias view still shows up in the
     * product's kardex rather than silently diverging from its movement history. No-op if the
     * quantity doesn't actually change.
     */
    @Transactional
    public void adjustStockItem(Product product, Warehouse warehouse, BigDecimal newQuantity, AppUser user) {
        var previousQuantity = stockItemRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
                .map(StockItem::getQuantity).orElse(BigDecimal.ZERO);
        var delta = newQuantity.subtract(previousQuantity);
        if (delta.signum() == 0) {
            return;
        }

        var direction = delta.signum() > 0 ? MovementType.Direction.IN : MovementType.Direction.OUT;
        var movementType = movementTypeService.getOrCreateAdjustmentType(direction);
        var movement = new Movement(product, warehouse, movementType, delta.abs(), user, Instant.now());
        movement.setReferenceNote("Ajuste manual de existencias");
        save(movement);
    }

    private void applyToStock(Movement movement, boolean reverse) {
        var direction = movement.getMovementType().getDirection();
        var quantity = reverse ? movement.getQuantity().negate() : movement.getQuantity();
        var product = movement.getProduct();
        var warehouse = movement.getWarehouse();

        switch (direction) {
            case IN -> adjust(product, warehouse, quantity);
            case OUT -> adjust(product, warehouse, quantity.negate());
            case NEUTRAL -> {
                var target = movement.getTargetWarehouse();
                if (target == null) {
                    throw new IllegalStateException("Las transferencias requieren una bodega destino");
                }
                if (target.equals(warehouse)) {
                    throw new IllegalStateException("La bodega destino debe ser diferente a la bodega de origen");
                }
                adjust(product, warehouse, quantity.negate());
                adjust(product, target, quantity);
            }
        }
    }

    private void adjust(Product product, Warehouse warehouse, BigDecimal delta) {
        var stockItem = stockItemRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
                .orElseGet(() -> new StockItem(product, warehouse));
        var newQuantity = stockItem.getQuantity().add(delta);
        if (newQuantity.signum() < 0) {
            throw new IllegalStateException(
                    "Stock insuficiente de \"" + product.getName() + "\" en \"" + warehouse.getName() + "\"");
        }
        stockItem.setQuantity(newQuantity);
        stockItemRepository.save(stockItem);
    }
}
