package com.isateca.catalog.ui;

import com.isateca.base.ui.ViewTitle;
import com.isateca.catalog.AttributeDefinitionService;
import com.isateca.catalog.CategoryService;
import com.isateca.catalog.MovementTypeService;
import com.isateca.catalog.UnitOfMeasureService;
import com.isateca.catalog.WarehouseService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("catalogos")
@PageTitle("Catálogos")
@Menu(order = 1, icon = "vaadin:folder-o", title = "Catálogos")
class CatalogView extends VerticalLayout {

    CatalogView(CategoryService categoryService, UnitOfMeasureService unitOfMeasureService,
            MovementTypeService movementTypeService, WarehouseService warehouseService,
            AttributeDefinitionService attributeDefinitionService) {

        var tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.add("Categorías", new CategoryCrud(categoryService));
        tabs.add("Unidades de medida", new UnitOfMeasureCrud(unitOfMeasureService));
        tabs.add("Tipos de movimiento", new MovementTypeCrud(movementTypeService));
        tabs.add("Bodegas", new WarehouseCrud(warehouseService));
        tabs.add("Atributos dinámicos", new AttributeDefinitionCrud(attributeDefinitionService, categoryService));

        setSizeFull();
        add(new ViewTitle("Catálogos"), tabs);
        setFlexGrow(1, tabs);
    }
}
