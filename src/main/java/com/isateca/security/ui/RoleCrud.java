package com.isateca.security.ui;

import com.isateca.base.ui.AbstractCrudView;
import com.isateca.security.Role;
import com.isateca.security.RoleService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.util.List;

class RoleCrud extends AbstractCrudView<Role> {

    private final RoleService roleService;

    RoleCrud(RoleService roleService) {
        super("Rol");
        this.roleService = roleService;
        init();
    }

    @Override
    protected void buildColumns(Grid<Role> grid) {
        grid.addColumn(Role::getName).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(r -> r.isActive() ? "Sí" : "No").setHeader("Activo").setAutoWidth(true);
    }

    @Override
    protected void buildForm(FormLayout form, Binder<Role> binder) {
        var nameField = new TextField("Nombre");
        var activeField = new Checkbox("Activo");

        binder.forField(nameField).asRequired("El nombre es obligatorio").bind(Role::getName, Role::setName);
        binder.forField(activeField).bind(Role::isActive, Role::setActive);

        form.add(nameField, activeField);
    }

    @Override
    protected List<Role> fetchAll() {
        return roleService.list();
    }

    @Override
    protected void persist(Role item) {
        roleService.save(item);
    }

    @Override
    protected void delete(Role item) {
        roleService.delete(item);
    }

    @Override
    protected Role createNew() {
        return new Role("");
    }
}
