package com.isateca.security.ui;

import com.isateca.base.ui.ViewTitle;
import com.isateca.security.AppUserService;
import com.isateca.security.RoleService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("usuarios")
@PageTitle("Usuarios")
@Menu(order = 3, icon = "vaadin:user-heart", title = "Usuarios")
class SecurityView extends VerticalLayout {

    SecurityView(RoleService roleService, AppUserService appUserService) {
        var tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.add("Usuarios", new AppUserCrud(appUserService, roleService));
        tabs.add("Roles", new RoleCrud(roleService));

        setSizeFull();
        add(new ViewTitle("Usuarios"), tabs);
        setFlexGrow(1, tabs);
    }
}
