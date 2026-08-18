package com.isateca.inventory.ui;

import com.isateca.base.ui.AbstractCrudView;
import com.isateca.catalog.AttributeDefinition;
import com.isateca.catalog.AttributeDefinitionService;
import com.isateca.catalog.Category;
import com.isateca.catalog.CategoryService;
import com.isateca.catalog.UnitOfMeasure;
import com.isateca.catalog.UnitOfMeasureService;
import com.isateca.inventory.Product;
import com.isateca.inventory.ProductService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.Binding;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.data.converter.StringToBigDecimalConverter;
import com.vaadin.flow.function.ValueProvider;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ProductCrud extends AbstractCrudView<Product> {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UnitOfMeasureService unitOfMeasureService;
    private final AttributeDefinitionService attributeDefinitionService;
    private final ComboBox<Category> categoryField = new ComboBox<>("Categoría");
    private final ComboBox<UnitOfMeasure> unitOfMeasureField = new ComboBox<>("Unidad de medida");
    // Only the OPTIONAL attributes eligible for the selected category are offered here - required
    // ones apply automatically (see rebuildDynamicValueFields) since they're not really optional.
    private final MultiSelectComboBox<AttributeDefinition> attributesField = new MultiSelectComboBox<>(
            "Atributos aplicables");
    private final Div dynamicValuesContainer = new Div();
    private final List<Binding<Product, ?>> dynamicValueBindings = new ArrayList<>();

    ProductCrud(ProductService productService, CategoryService categoryService,
            UnitOfMeasureService unitOfMeasureService, AttributeDefinitionService attributeDefinitionService) {
        super("Producto");
        this.productService = productService;
        this.categoryService = categoryService;
        this.unitOfMeasureService = unitOfMeasureService;
        this.attributeDefinitionService = attributeDefinitionService;
        init();
    }

    @Override
    protected void buildColumns(Grid<Product> grid) {
        grid.addColumn(Product::getSku).setHeader("SKU").setAutoWidth(true);
        grid.addColumn(Product::getName).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(p -> p.getCategory().getName()).setHeader("Categoría").setAutoWidth(true);
        grid.addColumn(p -> p.getUnitOfMeasure().getAbbreviation()).setHeader("Unidad").setAutoWidth(true);
        grid.addColumn(p -> Optional.ofNullable(p.getMinStock()).map(Object::toString).orElse("—"))
                .setHeader("Stock mínimo").setAutoWidth(true);
        grid.addColumn(p -> p.isActive() ? "Sí" : "No").setHeader("Activo").setAutoWidth(true);
    }

    @Override
    protected void buildForm(FormLayout form, Binder<Product> binder) {
        var skuField = new TextField("SKU");
        var nameField = new TextField("Nombre");
        var descriptionField = new TextField("Descripción");
        categoryField.setItemLabelGenerator(Category::getName);
        unitOfMeasureField.setItemLabelGenerator(UnitOfMeasure::getAbbreviation);
        var minStockField = new TextField("Stock mínimo");
        minStockField.setHelperText("Opcional, para alertas futuras");
        var activeField = new Checkbox("Activo");

        binder.forField(skuField).asRequired("El SKU es obligatorio").bind(Product::getSku, Product::setSku);
        binder.forField(nameField).asRequired("El nombre es obligatorio").bind(Product::getName, Product::setName);
        binder.forField(descriptionField).bind(Product::getDescription, Product::setDescription);
        binder.forField(categoryField).asRequired("La categoría es obligatoria")
                .bind(Product::getCategory, Product::setCategory);
        binder.forField(unitOfMeasureField).asRequired("La unidad de medida es obligatoria")
                .bind(Product::getUnitOfMeasure, Product::setUnitOfMeasure);
        binder.forField(minStockField).withNullRepresentation("")
                .withConverter(new StringToBigDecimalConverter("Debe ser un número"))
                .bind(Product::getMinStock, Product::setMinStock);
        binder.forField(activeField).bind(Product::isActive, Product::setActive);

        attributesField.setItemLabelGenerator(AttributeDefinition::getLabel);
        attributesField.setHelperText(
                "Los atributos obligatorios de la categoría se piden siempre; el resto es opcional.");
        binder.forField(attributesField).bind(this::selectedOptionalAttributes, this::applyAttributeSelection);

        // Both listeners funnel into the same idempotent rebuild: categoryField changing updates
        // which attributes are even offered (and may drop a now-ineligible selection, which
        // itself re-triggers the attributesField listener), while attributesField changing just
        // reflects the user's opt-in choice for the already-eligible optional attributes.
        categoryField.addValueChangeListener(event -> {
            updateAttributeCandidates();
            rebuildDynamicValueFields(binder);
        });
        attributesField.addValueChangeListener(event -> rebuildDynamicValueFields(binder));

        var dynamicSection = new Div(new H4("Atributos dinámicos"), attributesField, dynamicValuesContainer);
        form.add(skuField, nameField, descriptionField, categoryField, unitOfMeasureField, minStockField,
                activeField, dynamicSection);
        form.setColspan(dynamicSection, 2);
    }

    private List<AttributeDefinition> eligibleDefinitionsFor(@Nullable Category category) {
        return attributeDefinitionService.list().stream().filter(AttributeDefinition::isActive)
                .filter(definition -> definition.getCategory() == null || definition.getCategory().equals(category))
                .sorted(Comparator.comparing(AttributeDefinition::getLabel)).toList();
    }

    private void updateAttributeCandidates() {
        var optionalCandidates = eligibleDefinitionsFor(categoryField.getValue()).stream()
                .filter(definition -> !definition.isRequired()).toList();
        attributesField.setItems(optionalCandidates);
        var pruned = attributesField.getValue().stream().filter(optionalCandidates::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!pruned.equals(attributesField.getValue())) {
            attributesField.setValue(pruned);
        }
    }

    private void rebuildDynamicValueFields(Binder<Product> binder) {
        dynamicValueBindings.forEach(Binding::unbind);
        dynamicValueBindings.clear();
        dynamicValuesContainer.removeAll();

        var required = eligibleDefinitionsFor(categoryField.getValue()).stream()
                .filter(AttributeDefinition::isRequired);
        var toShow = Stream.concat(required, attributesField.getValue().stream())
                .sorted(Comparator.comparing(AttributeDefinition::getLabel)).toList();
        if (toShow.isEmpty()) {
            return;
        }

        var fieldsLayout = new FormLayout();
        toShow.forEach(definition -> fieldsLayout.add(buildDynamicField(binder, definition)));
        dynamicValuesContainer.add(fieldsLayout);
    }

    private LinkedHashSet<AttributeDefinition> selectedOptionalAttributes(Product product) {
        var optionalEligible = eligibleDefinitionsFor(product.getCategory()).stream()
                .filter(definition -> !definition.isRequired());
        return optionalEligible.filter(definition -> getAttribute(product, definition.getKey()) != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // Clears any stored value for an attribute that's neither required-for-this-category nor
    // selected as optional - covers both a deselected optional attribute and a leftover value
    // from a category the product no longer belongs to.
    private void applyAttributeSelection(Product product, Set<AttributeDefinition> selectedOptional) {
        var keep = new HashSet<>(selectedOptional);
        eligibleDefinitionsFor(product.getCategory()).stream().filter(AttributeDefinition::isRequired)
                .forEach(keep::add);
        attributeDefinitionService.list().forEach(definition -> {
            if (!keep.contains(definition)) {
                setAttribute(product, definition.getKey(), null);
            }
        });
    }

    private Component buildDynamicField(Binder<Product> binder, AttributeDefinition definition) {
        var key = definition.getKey();
        var item = getEditingItem();
        return switch (definition.getDataType()) {
            case TEXT -> {
                var field = new TextField(definition.getLabel());
                field.setValue(item == null ? "" : Objects.toString(getAttribute(item, key), ""));
                bindDynamicField(binder, definition, field, product -> asString(getAttribute(product, key)),
                        (product, value) -> setAttribute(product, key, value));
                yield field;
            }
            case NUMBER -> {
                var field = new NumberField(definition.getLabel());
                field.setValue(item == null ? null : asDouble(getAttribute(item, key)));
                bindDynamicField(binder, definition, field, product -> asDouble(getAttribute(product, key)),
                        (product, value) -> setAttribute(product, key, value));
                yield field;
            }
            case BOOLEAN -> {
                var field = new Checkbox(definition.getLabel());
                field.setValue(item != null && asBoolean(getAttribute(item, key)));
                bindDynamicField(binder, definition, field, product -> asBoolean(getAttribute(product, key)),
                        (product, value) -> setAttribute(product, key, value));
                yield field;
            }
            case DATE -> {
                var field = new DatePicker(definition.getLabel());
                field.setValue(item == null ? null : asLocalDate(getAttribute(item, key)));
                bindDynamicField(binder, definition, field, product -> asLocalDate(getAttribute(product, key)),
                        (product, value) -> setAttribute(product, key, value == null ? null : value.toString()));
                yield field;
            }
        };
    }

    private <V> void bindDynamicField(Binder<Product> binder, AttributeDefinition definition, HasValue<?, V> field,
            ValueProvider<Product, V> getter, Setter<Product, V> setter) {
        var builder = binder.forField(field);
        var boundBuilder = definition.isRequired() ? builder.asRequired(definition.getLabel() + " es obligatorio")
                : builder;
        dynamicValueBindings.add(boundBuilder.bind(getter, setter));
    }

    private static @Nullable Object getAttribute(Product product, String key) {
        var attributes = product.getCustomAttributes();
        return attributes == null ? null : attributes.get(key);
    }

    // Attribute values live in Product.customAttributes, keyed by AttributeDefinition.key, rather
    // than as real bean properties - so dynamic fields bind through this map access instead of a
    // getter/setter method reference.
    private static void setAttribute(Product product, String key, @Nullable Object value) {
        var attributes = product.getCustomAttributes();
        if (attributes == null) {
            attributes = new HashMap<>();
            product.setCustomAttributes(attributes);
        }
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    private static @Nullable String asString(@Nullable Object value) {
        return value == null ? null : value.toString();
    }

    private static @Nullable Double asDouble(@Nullable Object value) {
        return value instanceof Number number ? number.doubleValue() : null;
    }

    private static boolean asBoolean(@Nullable Object value) {
        return value instanceof Boolean bool && bool;
    }

    private static @Nullable LocalDate asLocalDate(@Nullable Object value) {
        return value == null ? null : LocalDate.parse(value.toString());
    }

    @Override
    protected void refresh() {
        super.refresh();
        categoryField.setItems(categoryService.list());
        unitOfMeasureField.setItems(unitOfMeasureService.list());
    }

    @Override
    protected List<Product> fetchAll() {
        return productService.list();
    }

    @Override
    protected void persist(Product item) {
        productService.save(item);
    }

    @Override
    protected void delete(Product item) {
        productService.delete(item);
    }

    @Override
    protected Product createNew() {
        var categories = categoryService.list();
        var units = unitOfMeasureService.list();
        if (categories.isEmpty() || units.isEmpty()) {
            throw new IllegalStateException("Primero crea al menos una categoría y una unidad de medida");
        }
        return new Product("", "", categories.get(0), units.get(0));
    }
}
