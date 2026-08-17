package com.isateca.security.ui;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "login", autoLayout = false)
@PageTitle("Iniciar sesión")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView() {
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        var i18n = LoginI18n.createDefault();
        var form = i18n.getForm();
        form.setTitle("Stock ISA");
        form.setUsername("Usuario");
        form.setPassword("Contraseña");
        form.setSubmit("Iniciar sesión");
        form.setForgotPassword("¿Olvidaste tu contraseña?");
        i18n.setForm(form);

        var errorMessage = i18n.getErrorMessage();
        errorMessage.setTitle("Usuario o contraseña incorrectos");
        errorMessage.setMessage("Verifica los datos e intenta de nuevo.");
        i18n.setErrorMessage(errorMessage);

        login.setI18n(i18n);
        login.setForgotPasswordButtonVisible(false);
        login.setAction("login");

        add(login);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            login.setError(true);
        }
    }
}
