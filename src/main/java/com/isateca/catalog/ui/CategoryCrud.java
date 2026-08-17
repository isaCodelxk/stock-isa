package com.isateca.catalog.ui;

import com.isateca.base.ui.AbstractCrudView;
import com.isateca.catalog.Category;
import com.isateca.catalog.CategoryService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.util.List;

class CategoryCrud extends AbstractCrudView<Category> {

    private final CategoryService categoryService;
    private final ComboBox<Category> parentField = new ComboBox<>("Categoría padre");

    CategoryCrud(CategoryService categoryService) {
        super("Categoría");
        this.categoryService = categoryService;
        init();
    }

    @Override
    protected void buildColumns(Grid<Category> grid) {
        grid.addColumn(Category::getName).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(c -> c.getParent() != null ? c.getParent().getName() : "—").setHeader("Categoría padre")
                .setAutoWidth(true);
        grid.addColumn(c -> c.isActive() ? "Sí" : "No").setHeader("Activo").setAutoWidth(true);
    }

    @Override
    protected void buildForm(FormLayout form, Binder<Category> binder) {
        var nameField = new TextField("Nombre");
        parentField.setItemLabelGenerator(Category::getName);
        parentField.setClearButtonVisible(true);
        var activeField = new Checkbox("Activo");

        binder.forField(nameField).asRequired("El nombre es obligatorio").bind(Category::getName, Category::setName);
        binder.forField(parentField).bind(Category::getParent, Category::setParent);
        binder.forField(activeField).bind(Category::isActive, Category::setActive);

        form.add(nameField, parentField, activeField);
    }

    @Override
    protected void refresh() {
        super.refresh();
        parentField.setItems(categoryService.list());
    }

    @Override
    protected List<Category> fetchAll() {
        return categoryService.list();
    }

    @Override
    protected void persist(Category item) {
        categoryService.save(item);
    }

    @Override
    protected void delete(Category item) {
        categoryService.delete(item);
    }

    @Override
    protected Category createNew() {
        return new Category("");
    }
}
