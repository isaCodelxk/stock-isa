package com.isateca.customer.ui;

import com.isateca.base.ui.ViewTitle;
import com.isateca.customer.CustomerService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("clientes")
@PageTitle("Clientes")
@Menu(order = 3, icon = "vaadin:group", title = "Clientes")
@PermitAll
class CustomerView extends VerticalLayout {

    CustomerView(CustomerService customerService) {
        var customerCrud = new CustomerCrud(customerService);

        setSizeFull();
        add(new ViewTitle("Clientes"), customerCrud);
        setFlexGrow(1, customerCrud);
    }
}
