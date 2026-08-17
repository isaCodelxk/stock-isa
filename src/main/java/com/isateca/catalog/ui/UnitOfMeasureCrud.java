package com.isateca.catalog.ui;

import com.isateca.catalog.UnitOfMeasure;
import com.isateca.catalog.UnitOfMeasureService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.util.List;

class UnitOfMeasureCrud extends AbstractCatalogCrud<UnitOfMeasure> {

    private final UnitOfMeasureService unitOfMeasureService;

    UnitOfMeasureCrud(UnitOfMeasureService unitOfMeasureService) {
        super("Unidad de medida");
        this.unitOfMeasureService = unitOfMeasureService;
        init();
    }

    @Override
    protected void buildColumns(Grid<UnitOfMeasure> grid) {
        grid.addColumn(UnitOfMeasure::getName).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(UnitOfMeasure::getAbbreviation).setHeader("Abreviatura").setAutoWidth(true);
        grid.addColumn(u -> u.isActive() ? "Sí" : "No").setHeader("Activo").setAutoWidth(true);
    }

    @Override
    protected void buildForm(FormLayout form, Binder<UnitOfMeasure> binder) {
        var nameField = new TextField("Nombre");
        var abbreviationField = new TextField("Abreviatura");
        var activeField = new Checkbox("Activo");

        binder.forField(nameField).asRequired("El nombre es obligatorio")
                .bind(UnitOfMeasure::getName, UnitOfMeasure::setName);
        binder.forField(abbreviationField).asRequired("La abreviatura es obligatoria")
                .bind(UnitOfMeasure::getAbbreviation, UnitOfMeasure::setAbbreviation);
        binder.forField(activeField).bind(UnitOfMeasure::isActive, UnitOfMeasure::setActive);

        form.add(nameField, abbreviationField, activeField);
    }

    @Override
    protected List<UnitOfMeasure> fetchAll() {
        return unitOfMeasureService.list();
    }

    @Override
    protected void persist(UnitOfMeasure item) {
        unitOfMeasureService.save(item);
    }

    @Override
    protected void delete(UnitOfMeasure item) {
        unitOfMeasureService.delete(item);
    }

    @Override
    protected UnitOfMeasure createNew() {
        return new UnitOfMeasure("", "");
    }
}
