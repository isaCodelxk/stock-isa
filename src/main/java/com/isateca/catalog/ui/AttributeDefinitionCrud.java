package com.isateca.catalog.ui;

import com.isateca.catalog.AttributeDefinition;
import com.isateca.catalog.AttributeDefinition.DataType;
import com.isateca.catalog.AttributeDefinitionService;
import com.isateca.catalog.CategoryService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.util.List;
import java.util.Optional;

class AttributeDefinitionCrud extends AbstractCatalogCrud<AttributeDefinition> {

    private final AttributeDefinitionService attributeDefinitionService;
    private final CategoryService categoryService;
    private final ComboBox<com.isateca.catalog.Category> categoryField = new ComboBox<>("Categoría (opcional)");

    AttributeDefinitionCrud(AttributeDefinitionService attributeDefinitionService, CategoryService categoryService) {
        super("Atributo dinámico");
        this.attributeDefinitionService = attributeDefinitionService;
        this.categoryService = categoryService;
        init();
    }

    private static String dataTypeLabel(DataType dataType) {
        return switch (dataType) {
            case TEXT -> "Texto";
            case NUMBER -> "Número";
            case DATE -> "Fecha";
            case BOOLEAN -> "Booleano";
        };
    }

    @Override
    protected void buildColumns(Grid<AttributeDefinition> grid) {
        grid.addColumn(AttributeDefinition::getKey).setHeader("Clave").setAutoWidth(true);
        grid.addColumn(AttributeDefinition::getLabel).setHeader("Etiqueta").setAutoWidth(true);
        grid.addColumn(a -> dataTypeLabel(a.getDataType())).setHeader("Tipo de dato").setAutoWidth(true);
        grid.addColumn(a -> Optional.ofNullable(a.getCategory()).map(com.isateca.catalog.Category::getName)
                .orElse("Todas")).setHeader("Categoría").setAutoWidth(true);
        grid.addColumn(a -> a.isRequired() ? "Sí" : "No").setHeader("Obligatorio").setAutoWidth(true);
        grid.addColumn(a -> a.isActive() ? "Sí" : "No").setHeader("Activo").setAutoWidth(true);
    }

    @Override
    protected void buildForm(FormLayout form, Binder<AttributeDefinition> binder) {
        var keyField = new TextField("Clave");
        keyField.setHelperText("Identificador interno, p. ej. \"fecha_caducidad\"");
        var labelField = new TextField("Etiqueta");
        labelField.setHelperText("Lo que ve el usuario, p. ej. \"Fecha de caducidad\"");
        var dataTypeField = new ComboBox<DataType>("Tipo de dato");
        dataTypeField.setItems(DataType.values());
        dataTypeField.setItemLabelGenerator(AttributeDefinitionCrud::dataTypeLabel);
        categoryField.setItemLabelGenerator(com.isateca.catalog.Category::getName);
        categoryField.setClearButtonVisible(true);
        var requiredField = new Checkbox("Obligatorio");
        var activeField = new Checkbox("Activo");

        binder.forField(keyField).asRequired("La clave es obligatoria")
                .bind(AttributeDefinition::getKey, AttributeDefinition::setKey);
        binder.forField(labelField).asRequired("La etiqueta es obligatoria")
                .bind(AttributeDefinition::getLabel, AttributeDefinition::setLabel);
        binder.forField(dataTypeField).asRequired("El tipo de dato es obligatorio")
                .bind(AttributeDefinition::getDataType, AttributeDefinition::setDataType);
        binder.forField(categoryField).bind(AttributeDefinition::getCategory, AttributeDefinition::setCategory);
        binder.forField(requiredField).bind(AttributeDefinition::isRequired, AttributeDefinition::setRequired);
        binder.forField(activeField).bind(AttributeDefinition::isActive, AttributeDefinition::setActive);

        form.add(keyField, labelField, dataTypeField, categoryField, requiredField, activeField);
    }

    @Override
    protected void refresh() {
        super.refresh();
        categoryField.setItems(categoryService.list());
    }

    @Override
    protected List<AttributeDefinition> fetchAll() {
        return attributeDefinitionService.list();
    }

    @Override
    protected void persist(AttributeDefinition item) {
        attributeDefinitionService.save(item);
    }

    @Override
    protected void delete(AttributeDefinition item) {
        attributeDefinitionService.delete(item);
    }

    @Override
    protected AttributeDefinition createNew() {
        return new AttributeDefinition("", "", DataType.TEXT);
    }
}
