package com.isateca.customer.ui;

import com.isateca.base.ui.AbstractCrudView;
import com.isateca.customer.Customer;
import com.isateca.customer.CustomerService;
import com.isateca.inventory.Movement;
import com.isateca.inventory.MovementService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Optional;

class CustomerCrud extends AbstractCrudView<Customer> {

    private final CustomerService customerService;
    private final MovementService movementService;

    CustomerCrud(CustomerService customerService, MovementService movementService) {
        super("Cliente");
        this.customerService = customerService;
        this.movementService = movementService;
        init();
    }

    @Override
    protected void buildColumns(Grid<Customer> grid) {
        grid.addColumn(Customer::getName).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(c -> Optional.ofNullable(c.getPhone()).orElse("—")).setHeader("Teléfono").setAutoWidth(true);
        grid.addColumn(c -> Optional.ofNullable(c.getEmail()).orElse("—")).setHeader("Correo").setAutoWidth(true);
        grid.addColumn(c -> c.isActive() ? "Sí" : "No").setHeader("Activo").setAutoWidth(true);
        grid.addComponentColumn(this::historyButton).setHeader("").setFlexGrow(0).setAutoWidth(true);
    }

    private Button historyButton(Customer customer) {
        var button = new Button(new Icon(VaadinIcon.INVOICE), event -> openHistory(customer));
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        button.getElement().setAttribute("title", "Historial de compras");
        return button;
    }

    private void openHistory(Customer customer) {
        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(getLocale())
                .withZone(ZoneId.systemDefault());

        var grid = new Grid<Movement>();
        grid.addColumn(m -> m.getProduct().getSku() + " — " + m.getProduct().getName()).setHeader("Producto")
                .setAutoWidth(true);
        grid.addColumn(m -> m.getMovementType().getName()).setHeader("Tipo").setAutoWidth(true);
        grid.addColumn(m -> m.getQuantity().toPlainString()).setHeader("Cantidad").setAutoWidth(true);
        grid.addColumn(m -> dateTimeFormatter.format(m.getCreatedAt())).setHeader("Fecha").setAutoWidth(true);
        grid.addColumn(m -> Optional.ofNullable(m.getReferenceNote()).orElse("—")).setHeader("Referencia")
                .setAutoWidth(true);
        grid.setItems(movementService.listByCustomer(customer));
        grid.setEmptyStateText("Este cliente todavía no tiene compras registradas");
        grid.setAllRowsVisible(true);
        grid.setWidthFull();

        var closeButton = new Button("Cerrar");
        var dialog = new Dialog();
        dialog.setHeaderTitle("Historial de compras: " + customer.getName());
        dialog.setWidth("60em");
        dialog.add(grid);
        dialog.getFooter().add(closeButton);
        closeButton.addClickListener(event -> dialog.close());
        dialog.open();
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
