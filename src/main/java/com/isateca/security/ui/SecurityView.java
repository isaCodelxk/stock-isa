package com.isateca.security.ui;

import com.isateca.base.ui.ViewTitle;
import com.isateca.security.AppUserService;
import com.isateca.security.RoleService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.annotation.security.RolesAllowed;

@Route("usuarios")
@PageTitle("Usuarios")
@Menu(order = 3, icon = "vaadin:user-heart", title = "Usuarios")
@RolesAllowed("ADMINISTRADOR")
class SecurityView extends VerticalLayout {

    SecurityView(RoleService roleService, AppUserService appUserService, PasswordEncoder passwordEncoder) {
        var tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.add("Usuarios", new AppUserCrud(appUserService, roleService, passwordEncoder));
        tabs.add("Roles", new RoleCrud(roleService));

        setSizeFull();
        add(new ViewTitle("Usuarios"), tabs);
        setFlexGrow(1, tabs);
    }
}
