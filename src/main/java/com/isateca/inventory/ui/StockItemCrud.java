package com.isateca.inventory.ui;

import com.isateca.base.ui.AbstractCrudView;
import com.isateca.catalog.Warehouse;
import com.isateca.catalog.WarehouseService;
import com.isateca.inventory.MovementService;
import com.isateca.inventory.Product;
import com.isateca.inventory.ProductService;
import com.isateca.inventory.StockItem;
import com.isateca.inventory.StockItemService;
import com.isateca.security.AppUser;
import com.isateca.security.AppUserService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToBigDecimalConverter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

class StockItemCrud extends AbstractCrudView<StockItem> {

    private final StockItemService stockItemService;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final MovementService movementService;
    private final AppUserService appUserService;
    private final ComboBox<Product> productField = new ComboBox<>("Producto");
    private final ComboBox<Warehouse> warehouseField = new ComboBox<>("Bodega");
    private final ComboBox<AppUser> userField = new ComboBox<>("Usuario");

    StockItemCrud(StockItemService stockItemService, ProductService productService,
            WarehouseService warehouseService, MovementService movementService, AppUserService appUserService) {
        super("Existencia");
        this.stockItemService = stockItemService;
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.movementService = movementService;
        this.appUserService = appUserService;
        init();
    }

    @Override
    protected void buildColumns(Grid<StockItem> grid) {
        grid.addColumn(si -> si.getProduct().getSku() + " — " + si.getProduct().getName()).setHeader("Producto")
                .setAutoWidth(true);
        grid.addColumn(si -> si.getWarehouse().getName()).setHeader("Bodega").setAutoWidth(true);
        grid.addColumn(si -> si.getQuantity().toPlainString()).setHeader("Cantidad").setAutoWidth(true);
    }

    @Override
    protected void buildForm(FormLayout form, Binder<StockItem> binder) {
        productField.setItemLabelGenerator(p -> p.getSku() + " — " + p.getName());
        warehouseField.setItemLabelGenerator(Warehouse::getName);
        userField.setItemLabelGenerator(AppUser::getUsername);
        var quantityField = new TextField("Cantidad");
        quantityField.setHelperText("Un cambio aquí se registra como un movimiento de ajuste de inventario");

        binder.forField(productField).asRequired("El producto es obligatorio")
                .bind(StockItem::getProduct, StockItem::setProduct);
        binder.forField(warehouseField).asRequired("La bodega es obligatoria")
                .bind(StockItem::getWarehouse, StockItem::setWarehouse);
        binder.forField(quantityField).withConverter(new StringToBigDecimalConverter("Debe ser un número"))
                .withValidator(qty -> qty.signum() >= 0, "La cantidad no puede ser negativa")
                .bind(StockItem::getQuantity, StockItem::setQuantity);
        // Not part of the StockItem bean itself - just who to attribute the resulting adjustment
        // movement to, validated by hand in persist() since it isn't bound to the binder.
        userField.setRequiredIndicatorVisible(true);

        form.add(productField, warehouseField, quantityField, userField);
    }

    @Override
    protected void refresh() {
        super.refresh();
        productField.setItems(productService.list());
        warehouseField.setItems(warehouseService.list());
        userField.setItems(appUserService.list());
    }

    @Override
    protected List<StockItem> fetchAll() {
        return stockItemService.list();
    }

    @Override
    protected void persist(StockItem item) {
        var user = userField.getValue();
        if (user == null) {
            throw new IllegalStateException("El usuario es obligatorio");
        }

        // Existing quantities are never overwritten directly - the difference is recorded as an
        // adjustment movement instead, so the product's kardex stays in sync with stock_item. Read
        // the previous state fresh from the DB rather than from `item`, since the binder has already
        // overwritten it in place with the form's new values by this point.
        var previous = item.getId() == null ? Optional.<StockItem>empty() : stockItemService.find(item.getId());
        previous.filter(p -> !p.getProduct().equals(item.getProduct()) || !p.getWarehouse().equals(item.getWarehouse()))
                .ifPresent(p -> movementService.adjustStockItem(p.getProduct(), p.getWarehouse(), BigDecimal.ZERO,
                        user));
        movementService.adjustStockItem(item.getProduct(), item.getWarehouse(), item.getQuantity(), user);
    }

    @Override
    protected void delete(StockItem item) {
        if (item.getQuantity().signum() != 0) {
            throw new IllegalStateException(
                    "Ajusta la cantidad a 0 antes de eliminar este registro, para que quede en el kardex");
        }
        stockItemService.delete(item);
    }

    @Override
    protected StockItem createNew() {
        var products = productService.list();
        var warehouses = warehouseService.list();
        if (products.isEmpty() || warehouses.isEmpty()) {
            throw new IllegalStateException("Primero crea al menos un producto y una bodega");
        }
        var item = new StockItem(products.get(0), warehouses.get(0));
        item.setQuantity(BigDecimal.ZERO);
        return item;
    }
}
