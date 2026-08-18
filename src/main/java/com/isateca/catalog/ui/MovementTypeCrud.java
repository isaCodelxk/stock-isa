package com.isateca.catalog.ui;

import com.isateca.catalog.MovementType;
import com.isateca.catalog.MovementType.Direction;
import com.isateca.base.ui.AbstractCrudView;
import com.isateca.catalog.MovementTypeService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.util.List;

class MovementTypeCrud extends AbstractCrudView<MovementType> {

    private final MovementTypeService movementTypeService;

    MovementTypeCrud(MovementTypeService movementTypeService) {
        super("Tipo de movimiento");
        this.movementTypeService = movementTypeService;
        init();
    }

    private static String directionLabel(Direction direction) {
        return switch (direction) {
            case IN -> "Entrada";
            case OUT -> "Salida";
            case NEUTRAL -> "Neutro";
        };
    }

    @Override
    protected void buildColumns(Grid<MovementType> grid) {
        grid.addColumn(MovementType::getName).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(mt -> directionLabel(mt.getDirection())).setHeader("Dirección").setAutoWidth(true);
        grid.addColumn(mt -> mt.isRequiresCustomer() ? "Sí" : "No").setHeader("Requiere cliente").setAutoWidth(true);
        grid.addColumn(mt -> mt.isActive() ? "Sí" : "No").setHeader("Activo").setAutoWidth(true);
    }

    @Override
    protected void buildForm(FormLayout form, Binder<MovementType> binder) {
        var nameField = new TextField("Nombre");
        var directionField = new ComboBox<Direction>("Dirección");
        directionField.setItems(Direction.values());
        directionField.setItemLabelGenerator(MovementTypeCrud::directionLabel);
        var requiresCustomerField = new Checkbox("Requiere cliente");
        requiresCustomerField.setHelperText("Para movimientos que representan una venta");
        var activeField = new Checkbox("Activo");

        binder.forField(nameField).asRequired("El nombre es obligatorio")
                .bind(MovementType::getName, MovementType::setName);
        binder.forField(directionField).asRequired("La dirección es obligatoria")
                .bind(MovementType::getDirection, MovementType::setDirection);
        binder.forField(requiresCustomerField)
                .bind(MovementType::isRequiresCustomer, MovementType::setRequiresCustomer);
        binder.forField(activeField).bind(MovementType::isActive, MovementType::setActive);

        form.add(nameField, directionField, requiresCustomerField, activeField);
    }

    @Override
    protected List<MovementType> fetchAll() {
        return movementTypeService.list();
    }

    @Override
    protected void persist(MovementType item) {
        movementTypeService.save(item);
    }

    @Override
    protected void delete(MovementType item) {
        movementTypeService.delete(item);
    }

    @Override
    protected MovementType createNew() {
        return new MovementType("", Direction.IN);
    }
}
