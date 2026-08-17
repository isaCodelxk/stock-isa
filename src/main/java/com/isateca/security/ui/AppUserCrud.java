package com.isateca.security.ui;

import com.isateca.base.ui.AbstractCrudView;
import com.isateca.security.AppUser;
import com.isateca.security.AppUserService;
import com.isateca.security.Role;
import com.isateca.security.RoleService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.util.List;

class AppUserCrud extends AbstractCrudView<AppUser> {

    private final AppUserService appUserService;
    private final RoleService roleService;
    private final ComboBox<Role> roleField = new ComboBox<>("Rol");

    AppUserCrud(AppUserService appUserService, RoleService roleService) {
        super("Usuario");
        this.appUserService = appUserService;
        this.roleService = roleService;
        init();
    }

    @Override
    protected void buildColumns(Grid<AppUser> grid) {
        grid.addColumn(AppUser::getUsername).setHeader("Usuario").setAutoWidth(true);
        grid.addColumn(AppUser::getFullName).setHeader("Nombre completo").setAutoWidth(true);
        grid.addColumn(u -> u.getRole().getName()).setHeader("Rol").setAutoWidth(true);
        grid.addColumn(u -> u.isActive() ? "Sí" : "No").setHeader("Activo").setAutoWidth(true);
    }

    @Override
    protected void buildForm(FormLayout form, Binder<AppUser> binder) {
        var usernameField = new TextField("Usuario");
        var fullNameField = new TextField("Nombre completo");
        var passwordField = new PasswordField("Contraseña");
        passwordField.setHelperText("Sin hashear todavía: pendiente de integrar Spring Security");
        roleField.setItemLabelGenerator(Role::getName);
        var activeField = new Checkbox("Activo");

        binder.forField(usernameField).asRequired("El usuario es obligatorio")
                .bind(AppUser::getUsername, AppUser::setUsername);
        binder.forField(fullNameField).asRequired("El nombre es obligatorio")
                .bind(AppUser::getFullName, AppUser::setFullName);
        binder.forField(passwordField).asRequired("La contraseña es obligatoria")
                .bind(AppUser::getPasswordHash, AppUser::setPasswordHash);
        binder.forField(roleField).asRequired("El rol es obligatorio").bind(AppUser::getRole, AppUser::setRole);
        binder.forField(activeField).bind(AppUser::isActive, AppUser::setActive);

        form.add(usernameField, fullNameField, passwordField, roleField, activeField);
    }

    @Override
    protected void refresh() {
        super.refresh();
        roleField.setItems(roleService.list());
    }

    @Override
    protected List<AppUser> fetchAll() {
        return appUserService.list();
    }

    @Override
    protected void persist(AppUser item) {
        appUserService.save(item);
    }

    @Override
    protected void delete(AppUser item) {
        appUserService.delete(item);
    }

    @Override
    protected AppUser createNew() {
        var roles = roleService.list();
        if (roles.isEmpty()) {
            throw new IllegalStateException("Primero crea al menos un rol");
        }
        return new AppUser("", "", "", roles.get(0));
    }
}
