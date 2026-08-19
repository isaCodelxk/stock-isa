package com.isateca.inventory;

import com.isateca.catalog.Category;
import com.isateca.catalog.CategoryService;
import com.isateca.catalog.MovementType;
import com.isateca.catalog.MovementTypeService;
import com.isateca.catalog.UnitOfMeasure;
import com.isateca.catalog.UnitOfMeasureService;
import com.isateca.catalog.Warehouse;
import com.isateca.catalog.WarehouseService;
import com.isateca.security.AppUser;
import com.isateca.security.AppUserService;
import com.isateca.security.Role;
import com.isateca.security.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class MovementServiceTest {

    @Autowired
    MovementService movementService;
    @Autowired
    StockItemService stockItemService;
    @Autowired
    ProductService productService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    UnitOfMeasureService unitOfMeasureService;
    @Autowired
    WarehouseService warehouseService;
    @Autowired
    MovementTypeService movementTypeService;
    @Autowired
    RoleService roleService;
    @Autowired
    AppUserService appUserService;

    Product product;
    Warehouse warehouseA;
    Warehouse warehouseB;
    MovementType inType;
    MovementType outType;
    MovementType transferType;
    AppUser user;

    @BeforeEach
    void setUp() {
        var category = categoryService.save(new Category("Test category"));
        var unit = unitOfMeasureService.save(new UnitOfMeasure("Pieza", "pz"));
        product = productService
                .save(new Product("SKU-1", "Test product", category, unit, BigDecimal.TEN, BigDecimal.valueOf(20)));
        warehouseA = warehouseService.save(new Warehouse("Bodega A"));
        warehouseB = warehouseService.save(new Warehouse("Bodega B"));
        inType = movementTypeService.save(new MovementType("Entrada", MovementType.Direction.IN));
        outType = movementTypeService.save(new MovementType("Salida", MovementType.Direction.OUT));
        transferType = movementTypeService.save(new MovementType("Transferencia", MovementType.Direction.NEUTRAL));
        var role = roleService.save(new Role("Almacenista"));
        user = appUserService.save(new AppUser("tester", "x", "Tester", role));
    }

    private BigDecimal quantityAt(Warehouse warehouse) {
        return stockItemService.list().stream().filter(si -> si.getWarehouse().equals(warehouse)).findFirst()
                .map(StockItem::getQuantity).orElse(BigDecimal.ZERO);
    }

    @Test
    void in_movement_increases_stock() {
        movementService.save(new Movement(product, warehouseA, inType, BigDecimal.TEN, user, Instant.now()));

        assertThat(quantityAt(warehouseA)).isEqualByComparingTo("10");
    }

    @Test
    void out_movement_decreases_stock() {
        movementService.save(new Movement(product, warehouseA, inType, BigDecimal.TEN, user, Instant.now()));
        movementService.save(new Movement(product, warehouseA, outType, BigDecimal.valueOf(4), user, Instant.now()));

        assertThat(quantityAt(warehouseA)).isEqualByComparingTo("6");
    }

    @Test
    void out_movement_without_enough_stock_is_rejected() {
        assertThatThrownBy(() -> movementService
                .save(new Movement(product, warehouseA, outType, BigDecimal.ONE, user, Instant.now())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void transfer_moves_stock_between_warehouses() {
        movementService.save(new Movement(product, warehouseA, inType, BigDecimal.TEN, user, Instant.now()));

        var transfer = new Movement(product, warehouseA, transferType, BigDecimal.valueOf(3), user, Instant.now());
        transfer.setTargetWarehouse(warehouseB);
        movementService.save(transfer);

        assertThat(quantityAt(warehouseA)).isEqualByComparingTo("7");
        assertThat(quantityAt(warehouseB)).isEqualByComparingTo("3");
    }

    @Test
    void transfer_without_target_warehouse_is_rejected() {
        var transfer = new Movement(product, warehouseA, transferType, BigDecimal.ONE, user, Instant.now());

        assertThatThrownBy(() -> movementService.save(transfer)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deleting_a_movement_reverts_its_stock_effect() {
        var movement = movementService
                .save(new Movement(product, warehouseA, inType, BigDecimal.TEN, user, Instant.now()));

        movementService.delete(movement);

        assertThat(quantityAt(warehouseA)).isEqualByComparingTo("0");
    }

    @Test
    void deleting_an_in_movement_whose_stock_was_already_consumed_is_rejected() {
        var movement = movementService
                .save(new Movement(product, warehouseA, inType, BigDecimal.TEN, user, Instant.now()));
        movementService.save(new Movement(product, warehouseA, outType, BigDecimal.valueOf(8), user, Instant.now()));

        assertThatThrownBy(() -> movementService.delete(movement)).isInstanceOf(IllegalStateException.class);
    }
}
