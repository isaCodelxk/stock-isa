package com.isateca.inventory.ui;

import com.isateca.base.ui.AbstractCrudView;
import com.isateca.catalog.Warehouse;
import com.isateca.catalog.WarehouseService;
import com.isateca.inventory.Product;
import com.isateca.inventory.ProductService;
import com.isateca.inventory.StockItem;
import com.isateca.inventory.StockItemService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToBigDecimalConverter;

import java.math.BigDecimal;
import java.util.List;

class StockItemCrud extends AbstractCrudView<StockItem> {

    private final StockItemService stockItemService;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final ComboBox<Product> productField = new ComboBox<>("Producto");
    private final ComboBox<Warehouse> warehouseField = new ComboBox<>("Bodega");

    StockItemCrud(StockItemService stockItemService, ProductService productService,
            WarehouseService warehouseService) {
        super("Existencia");
        this.stockItemService = stockItemService;
        this.productService = productService;
        this.warehouseService = warehouseService;
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
        var quantityField = new TextField("Cantidad");

        binder.forField(productField).asRequired("El producto es obligatorio")
                .bind(StockItem::getProduct, StockItem::setProduct);
        binder.forField(warehouseField).asRequired("La bodega es obligatoria")
                .bind(StockItem::getWarehouse, StockItem::setWarehouse);
        binder.forField(quantityField).withConverter(new StringToBigDecimalConverter("Debe ser un número"))
                .withValidator(qty -> qty.signum() >= 0, "La cantidad no puede ser negativa")
                .bind(StockItem::getQuantity, StockItem::setQuantity);

        form.add(productField, warehouseField, quantityField);
    }

    @Override
    protected void refresh() {
        super.refresh();
        productField.setItems(productService.list());
        warehouseField.setItems(warehouseService.list());
    }

    @Override
    protected List<StockItem> fetchAll() {
        return stockItemService.list();
    }

    @Override
    protected void persist(StockItem item) {
        stockItemService.save(item);
    }

    @Override
    protected void delete(StockItem item) {
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
