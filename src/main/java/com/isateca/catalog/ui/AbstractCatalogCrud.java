package com.isateca.catalog.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Grid + Dialog form CRUD shell shared by the catalog views. Subclasses provide the entity-specific
 * columns, form fields and persistence calls; this class owns the toolbar, dialog wiring and delete
 * confirmation that are otherwise identical across every catalog.
 *
 * Subclasses must call {@link #init()} as the last statement of their constructor, once their own
 * fields (used by {@link #buildColumns} / {@link #buildForm}) are assigned.
 */
abstract class AbstractCatalogCrud<T> extends VerticalLayout {

    protected final Grid<T> grid = new Grid<>();
    protected final Binder<T> binder = new Binder<>();

    private final String entityName;
    private final Dialog dialog = new Dialog();
    private final Button deleteButton = new Button("Eliminar");
    private @Nullable T editingItem;

    protected AbstractCatalogCrud(String entityName) {
        this.entityName = entityName;
        setSizeFull();
        setPadding(false);
    }

    protected final void init() {
        var newButton = new Button("Nuevo", new Icon(VaadinIcon.PLUS), event -> openForCreate());
        newButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        var toolbar = new HorizontalLayout(newButton);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        buildColumns(grid);
        grid.addComponentColumn(this::createRowActions).setHeader("").setFlexGrow(0).setAutoWidth(true);
        grid.setSizeFull();

        var form = new FormLayout();
        buildForm(form, binder);
        dialog.add(form);

        var saveButton = new Button("Guardar", event -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        var cancelButton = new Button("Cancelar", event -> dialog.close());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(event -> {
            var item = editingItem;
            if (item != null) {
                confirmDelete(item, dialog::close);
            }
        });
        dialog.getFooter().add(deleteButton, cancelButton, saveButton);

        add(toolbar, grid);
        setFlexGrow(1, grid);

        refresh();
    }

    protected void refresh() {
        grid.setItems(fetchAll());
    }

    protected @Nullable T getEditingItem() {
        return editingItem;
    }

    private Component createRowActions(T item) {
        var editButton = new Button(new Icon(VaadinIcon.EDIT), event -> openForEdit(item));
        editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        var deleteRowButton = new Button(new Icon(VaadinIcon.TRASH), event -> confirmDelete(item, () -> {
        }));
        deleteRowButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
        return new HorizontalLayout(editButton, deleteRowButton);
    }

    private void openForCreate() {
        editingItem = createNew();
        binder.readBean(editingItem);
        deleteButton.setVisible(false);
        dialog.setHeaderTitle("Nuevo: " + entityName);
        dialog.open();
    }

    private void openForEdit(T item) {
        editingItem = item;
        binder.readBean(item);
        deleteButton.setVisible(true);
        dialog.setHeaderTitle("Editar: " + entityName);
        dialog.open();
    }

    private void save() {
        var item = editingItem;
        if (item == null) {
            return;
        }
        try {
            binder.writeBean(item);
        } catch (ValidationException e) {
            return;
        }
        persist(item);
        refresh();
        dialog.close();
        Notification.show(entityName + " guardado", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void confirmDelete(T item, Runnable afterDelete) {
        var confirm = new ConfirmDialog();
        confirm.setHeader("Eliminar " + entityName.toLowerCase());
        confirm.setText("¿Seguro que quieres eliminar este registro? Esta acción no se puede deshacer.");
        confirm.setCancelable(true);
        confirm.setCancelText("Cancelar");
        confirm.setConfirmText("Eliminar");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(event -> {
            delete(item);
            refresh();
            afterDelete.run();
            Notification.show(entityName + " eliminado", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        confirm.open();
    }

    protected abstract List<T> fetchAll();

    protected abstract void persist(T item);

    protected abstract void delete(T item);

    protected abstract T createNew();

    protected abstract void buildColumns(Grid<T> grid);

    protected abstract void buildForm(FormLayout form, Binder<T> binder);
}
