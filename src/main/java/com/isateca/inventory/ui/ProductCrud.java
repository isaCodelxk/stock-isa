package com.isateca.inventory.ui;

import com.isateca.base.ui.AbstractCrudView;
import com.isateca.catalog.Category;
import com.isateca.catalog.CategoryService;
import com.isateca.catalog.UnitOfMeasure;
import com.isateca.catalog.UnitOfMeasureService;
import com.isateca.inventory.Product;
import com.isateca.inventory.ProductService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToBigDecimalConverter;

import java.util.List;
import java.util.Optional;

class ProductCrud extends AbstractCrudView<Product> {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UnitOfMeasureService unitOfMeasureService;
    private final ComboBox<Category> categoryField = new ComboBox<>("Categoría");
    private final ComboBox<UnitOfMeasure> unitOfMeasureField = new ComboBox<>("Unidad de medida");

    ProductCrud(ProductService productService, CategoryService categoryService,
            UnitOfMeasureService unitOfMeasureService) {
        super("Producto");
        this.productService = productService;
        this.categoryService = categoryService;
        this.unitOfMeasureService = unitOfMeasureService;
        init();
    }

    @Override
    protected void buildColumns(Grid<Product> grid) {
        grid.addColumn(Product::getSku).setHeader("SKU").setAutoWidth(true);
        grid.addColumn(Product::getName).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(p -> p.getCategory().getName()).setHeader("Categoría").setAutoWidth(true);
        grid.addColumn(p -> p.getUnitOfMeasure().getAbbreviation()).setHeader("Unidad").setAutoWidth(true);
        grid.addColumn(p -> Optional.ofNullable(p.getMinStock()).map(Object::toString).orElse("—"))
                .setHeader("Stock mínimo").setAutoWidth(true);
        grid.addColumn(p -> p.isActive() ? "Sí" : "No").setHeader("Activo").setAutoWidth(true);
    }

    @Override
    protected void buildForm(FormLayout form, Binder<Product> binder) {
        var skuField = new TextField("SKU");
        var nameField = new TextField("Nombre");
        var descriptionField = new TextField("Descripción");
        categoryField.setItemLabelGenerator(Category::getName);
        unitOfMeasureField.setItemLabelGenerator(UnitOfMeasure::getAbbreviation);
        var minStockField = new TextField("Stock mínimo");
        minStockField.setHelperText("Opcional, para alertas futuras");
        var activeField = new Checkbox("Activo");

        binder.forField(skuField).asRequired("El SKU es obligatorio").bind(Product::getSku, Product::setSku);
        binder.forField(nameField).asRequired("El nombre es obligatorio").bind(Product::getName, Product::setName);
        binder.forField(descriptionField).bind(Product::getDescription, Product::setDescription);
        binder.forField(categoryField).asRequired("La categoría es obligatoria")
                .bind(Product::getCategory, Product::setCategory);
        binder.forField(unitOfMeasureField).asRequired("La unidad de medida es obligatoria")
                .bind(Product::getUnitOfMeasure, Product::setUnitOfMeasure);
        binder.forField(minStockField).withNullRepresentation("")
                .withConverter(new StringToBigDecimalConverter("Debe ser un número"))
                .bind(Product::getMinStock, Product::setMinStock);
        binder.forField(activeField).bind(Product::isActive, Product::setActive);

        form.add(skuField, nameField, descriptionField, categoryField, unitOfMeasureField, minStockField,
                activeField);
    }

    @Override
    protected void refresh() {
        super.refresh();
        categoryField.setItems(categoryService.list());
        unitOfMeasureField.setItems(unitOfMeasureService.list());
    }

    @Override
    protected List<Product> fetchAll() {
        return productService.list();
    }

    @Override
    protected void persist(Product item) {
        productService.save(item);
    }

    @Override
    protected void delete(Product item) {
        productService.delete(item);
    }

    @Override
    protected Product createNew() {
        var categories = categoryService.list();
        var units = unitOfMeasureService.list();
        if (categories.isEmpty() || units.isEmpty()) {
            throw new IllegalStateException("Primero crea al menos una categoría y una unidad de medida");
        }
        return new Product("", "", categories.get(0), units.get(0));
    }
}
