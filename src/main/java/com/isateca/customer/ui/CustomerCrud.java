package com.isateca.customer.ui;

import com.isateca.base.ui.AbstractCrudView;
import com.isateca.customer.Customer;
import com.isateca.customer.CustomerService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;

import java.util.List;
import java.util.Optional;

class CustomerCrud extends AbstractCrudView<Customer> {

    private final CustomerService customerService;

    CustomerCrud(CustomerService customerService) {
        super("Cliente");
        this.customerService = customerService;
        init();
    }

    @Override
    protected void buildColumns(Grid<Customer> grid) {
        grid.addColumn(Customer::getName).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(c -> Optional.ofNullable(c.getPhone()).orElse("—")).setHeader("Teléfono").setAutoWidth(true);
        grid.addColumn(c -> Optional.ofNullable(c.getEmail()).orElse("—")).setHeader("Correo").setAutoWidth(true);
        grid.addColumn(c -> c.isActive() ? "Sí" : "No").setHeader("Activo").setAutoWidth(true);
    }

    @Override
    protected void buildForm(FormLayout form, Binder<Customer> binder) {
        var nameField = new TextField("Nombre");
        var phoneField = new TextField("Teléfono");
        var emailField = new TextField("Correo");
        var activeField = new Checkbox("Activo");

        binder.forField(nameField).asRequired("El nombre es obligatorio")
                .bind(Customer::getName, Customer::setName);
        binder.forField(phoneField).bind(Customer::getPhone, Customer::setPhone);
        binder.forField(emailField).withValidator(new EmailValidator("El correo no es válido", true))
                .bind(Customer::getEmail, Customer::setEmail);
        binder.forField(activeField).bind(Customer::isActive, Customer::setActive);

        form.add(nameField, phoneField, emailField, activeField);
    }

    @Override
    protected List<Customer> fetchAll() {
        return customerService.list();
    }

    @Override
    protected void persist(Customer item) {
        customerService.save(item);
    }

    @Override
    protected void delete(Customer item) {
        customerService.delete(item);
    }

    @Override
    protected Customer createNew() {
        return new Customer("");
    }
}
