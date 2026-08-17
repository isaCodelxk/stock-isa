package com.isateca.inventory.ui;

import com.isateca.base.ui.AbstractCrudView;
import com.isateca.catalog.MovementType;
import com.isateca.catalog.MovementTypeService;
import com.isateca.catalog.Warehouse;
import com.isateca.catalog.WarehouseService;
import com.isateca.inventory.Movement;
import com.isateca.inventory.MovementService;
import com.isateca.inventory.Product;
import com.isateca.inventory.ProductService;
import com.isateca.security.AppUser;
import com.isateca.security.AppUserService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToBigDecimalConverter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Optional;

class MovementCrud extends AbstractCrudView<Movement> {

    private final MovementService movementService;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final MovementTypeService movementTypeService;
    private final AppUserService appUserService;
    private final Runnable onStockChanged;

    private final ComboBox<Product> productField = new ComboBox<>("Producto");
    private final ComboBox<Warehouse> warehouseField = new ComboBox<>("Bodega");
    private final ComboBox<Warehouse> targetWarehouseField = new ComboBox<>("Bodega destino (solo transferencias)");
    private final ComboBox<MovementType> movementTypeField = new ComboBox<>("Tipo de movimiento");
    private final ComboBox<AppUser> userField = new ComboBox<>("Usuario");

    MovementCrud(MovementService movementService, ProductService productService, WarehouseService warehouseService,
            MovementTypeService movementTypeService, AppUserService appUserService, Runnable onStockChanged) {
        super("Movimiento");
        this.movementService = movementService;
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.movementTypeService = movementTypeService;
        this.appUserService = appUserService;
        this.onStockChanged = onStockChanged;
        init();
    }

    @Override
    protected void buildColumns(Grid<Movement> grid) {
        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(getLocale())
                .withZone(ZoneId.systemDefault());

        grid.addColumn(m -> m.getProduct().getSku() + " — " + m.getProduct().getName()).setHeader("Producto")
                .setAutoWidth(true);
        grid.addColumn(m -> m.getMovementType().getName()).setHeader("Tipo").setAutoWidth(true);
        grid.addColumn(m -> m.getWarehouse().getName()).setHeader("Bodega").setAutoWidth(true);
        grid.addColumn(m -> Optional.ofNullable(m.getTargetWarehouse()).map(Warehouse::getName).orElse("—"))
                .setHeader("Bodega destino").setAutoWidth(true);
        grid.addColumn(m -> m.getQuantity().toPlainString()).setHeader("Cantidad").setAutoWidth(true);
        grid.addColumn(m -> m.getUser().getUsername()).setHeader("Usuario").setAutoWidth(true);
        grid.addColumn(m -> dateTimeFormatter.format(m.getCreatedAt())).setHeader("Fecha").setAutoWidth(true);
    }

    @Override
    protected void buildForm(FormLayout form, Binder<Movement> binder) {
        productField.setItemLabelGenerator(p -> p.getSku() + " — " + p.getName());
        warehouseField.setItemLabelGenerator(Warehouse::getName);
        targetWarehouseField.setItemLabelGenerator(Warehouse::getName);
        targetWarehouseField.setClearButtonVisible(true);
        movementTypeField.setItemLabelGenerator(MovementType::getName);
        userField.setItemLabelGenerator(AppUser::getUsername);
        var quantityField = new TextField("Cantidad");
        var referenceNoteField = new TextField("Referencia");
        referenceNoteField.setHelperText("Opcional, p. ej. \"Compra folio 123\"");

        binder.forField(productField).asRequired("El producto es obligatorio")
                .bind(Movement::getProduct, Movement::setProduct);
        binder.forField(movementTypeField).asRequired("El tipo de movimiento es obligatorio")
                .bind(Movement::getMovementType, Movement::setMovementType);
        binder.forField(warehouseField).asRequired("La bodega es obligatoria")
                .bind(Movement::getWarehouse, Movement::setWarehouse);
        binder.forField(targetWarehouseField).bind(Movement::getTargetWarehouse, Movement::setTargetWarehouse);
        binder.forField(quantityField).withConverter(new StringToBigDecimalConverter("Debe ser un número"))
                .withValidator(qty -> qty.signum() > 0, "La cantidad debe ser mayor a 0")
                .bind(Movement::getQuantity, Movement::setQuantity);
        binder.forField(userField).asRequired("El usuario es obligatorio")
                .bind(Movement::getUser, Movement::setUser);
        binder.forField(referenceNoteField).bind(Movement::getReferenceNote, Movement::setReferenceNote);

        form.add(productField, movementTypeField, warehouseField, targetWarehouseField, quantityField, userField,
                referenceNoteField);
    }

    @Override
    protected void refresh() {
        super.refresh();
        productField.setItems(productService.list());
        warehouseField.setItems(warehouseService.list());
        targetWarehouseField.setItems(warehouseService.list());
        movementTypeField.setItems(movementTypeService.list());
        userField.setItems(appUserService.list());
    }

    @Override
    protected void afterChange() {
        onStockChanged.run();
    }

    @Override
    protected boolean isEditable() {
        // Movements are an append-only ledger: editing one after the fact would desync the stock
        // adjustment its creation already applied. Only creation and deletion are allowed.
        return false;
    }

    @Override
    protected List<Movement> fetchAll() {
        return movementService.list();
    }

    @Override
    protected void persist(Movement item) {
        movementService.save(item);
    }

    @Override
    protected void delete(Movement item) {
        movementService.delete(item);
    }

    @Override
    protected Movement createNew() {
        var products = productService.list();
        var warehouses = warehouseService.list();
        var movementTypes = movementTypeService.list();
        var users = appUserService.list();
        if (products.isEmpty() || warehouses.isEmpty() || movementTypes.isEmpty() || users.isEmpty()) {
            throw new IllegalStateException(
                    "Primero crea al menos un producto, una bodega, un tipo de movimiento y un usuario");
        }
        return new Movement(products.get(0), warehouses.get(0), movementTypes.get(0), BigDecimal.ONE, users.get(0),
                Instant.now());
    }
}
