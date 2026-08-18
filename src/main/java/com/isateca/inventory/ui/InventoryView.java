package com.isateca.inventory.ui;

import com.isateca.base.ui.ViewTitle;
import com.isateca.catalog.AttributeDefinitionService;
import com.isateca.catalog.MovementTypeService;
import com.isateca.catalog.WarehouseService;
import com.isateca.catalog.CategoryService;
import com.isateca.catalog.UnitOfMeasureService;
import com.isateca.inventory.MovementService;
import com.isateca.inventory.ProductService;
import com.isateca.inventory.StockItemService;
import com.isateca.security.AppUserService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("inventario")
@PageTitle("Inventario")
@Menu(order = 2, icon = "vaadin:stock", title = "Inventario")
@PermitAll
class InventoryView extends VerticalLayout {

    InventoryView(ProductService productService, StockItemService stockItemService, MovementService movementService,
            CategoryService categoryService, UnitOfMeasureService unitOfMeasureService,
            WarehouseService warehouseService, MovementTypeService movementTypeService,
            AppUserService appUserService, AttributeDefinitionService attributeDefinitionService) {

        var stockItemCrud = new StockItemCrud(stockItemService, productService, warehouseService);

        var tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.add("Productos",
                new ProductCrud(productService, categoryService, unitOfMeasureService, attributeDefinitionService));
        tabs.add("Existencias", stockItemCrud);
        tabs.add("Movimientos", new MovementCrud(movementService, productService, warehouseService,
                movementTypeService, appUserService, stockItemCrud::refreshData));

        setSizeFull();
        add(new ViewTitle("Inventario"), tabs);
        setFlexGrow(1, tabs);
    }
}
