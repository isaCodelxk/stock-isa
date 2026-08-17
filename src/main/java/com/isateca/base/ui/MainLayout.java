package com.isateca.base.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

@Layout
@PermitAll
public final class MainLayout extends AppLayout {

    private final transient AuthenticationContext authContext;

    MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;
        setPrimarySection(Section.DRAWER);
        addToDrawer(createApplicationHeader(), createApplicationDrawer(), createApplicationFooter());
    }

    private Component createApplicationHeader() {
        // TODO Replace with real application logo and name
        var appLogo = new Avatar("My Application");
        appLogo.addClassName("app-logo");
        appLogo.addThemeVariants(AvatarVariant.AURA_FILLED, AvatarVariant.XSMALL);

        var appName = new Span("My Application");
        appName.addClassName("app-name");

        var header = new HorizontalLayout(appLogo, appName);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(true);
        return header;
    }

    private Component createApplicationDrawer() {
        var scroller = new Scroller(createSideNav());
        scroller.addThemeVariants(ScrollerVariant.OVERFLOW_INDICATORS);
        return scroller;
    }

    private Component createApplicationFooter() {
        var footer = new VerticalLayout(new Span("Made with ❤️ with Vaadin"));
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.addClassName("app-footer");

        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            var logoutBtn = new Button("Cerrar sesión (" + user.getUsername() + ")",
                    event -> authContext.logout());
            logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            footer.add(logoutBtn);
        });

        return footer;
    }

    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.setMinWidth(200, Unit.PIXELS);
        MenuConfiguration.getMenuEntries().forEach(entry -> nav.addItem(createSideNavItem(entry)));
        return nav;
    }

    private SideNavItem createSideNavItem(MenuEntry menuEntry) {
        if (menuEntry.icon() != null) {
            Component icon = null;
            if (menuEntry.icon().contains(".svg")) {
                icon = new SvgIcon(menuEntry.icon());
            } else {
                icon = new Icon(menuEntry.icon());
            }
            return new SideNavItem(menuEntry.title(), menuEntry.menuClass(), icon);
        } else {
            return new SideNavItem(menuEntry.title(), menuEntry.menuClass());
        }
    }
}
