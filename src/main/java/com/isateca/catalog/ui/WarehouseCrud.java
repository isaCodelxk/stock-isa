package com.isateca.catalog.ui;

import com.isateca.catalog.Warehouse;
import com.isateca.catalog.WarehouseService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.util.List;
import java.util.Optional;

class WarehouseCrud extends AbstractCatalogCrud<Warehouse> {

    private final WarehouseService warehouseService;

    WarehouseCrud(WarehouseService warehouseService) {
        super("Bodega");
        this.warehouseService = warehouseService;
        init();
    }

    @Override
    protected void buildColumns(Grid<Warehouse> grid) {
        grid.addColumn(Warehouse::getName).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(w -> Optional.ofNullable(w.getLocation()).orElse("—")).setHeader("Ubicación")
                .setAutoWidth(true);
        grid.addColumn(w -> w.isActive() ? "Sí" : "No").setHeader("Activo").setAutoWidth(true);
    }

    @Override
    protected void buildForm(FormLayout form, Binder<Warehouse> binder) {
        var nameField = new TextField("Nombre");
        var locationField = new TextField("Ubicación");
        var activeField = new Checkbox("Activo");

        binder.forField(nameField).asRequired("El nombre es obligatorio")
                .bind(Warehouse::getName, Warehouse::setName);
        binder.forField(locationField).bind(Warehouse::getLocation, Warehouse::setLocation);
        binder.forField(activeField).bind(Warehouse::isActive, Warehouse::setActive);

        form.add(nameField, locationField, activeField);
    }

    @Override
    protected List<Warehouse> fetchAll() {
        return warehouseService.list();
    }

    @Override
    protected void persist(Warehouse item) {
        warehouseService.save(item);
    }

    @Override
    protected void delete(Warehouse item) {
        warehouseService.delete(item);
    }

    @Override
    protected Warehouse createNew() {
        return new Warehouse("");
    }
}
