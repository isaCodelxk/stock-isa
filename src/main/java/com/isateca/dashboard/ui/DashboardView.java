package com.isateca.dashboard.ui;

import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.DataLabelsBuilder;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.github.appreciated.apexcharts.config.builder.PlotOptionsBuilder;
import com.github.appreciated.apexcharts.config.builder.ThemeBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.config.theme.Mode;
import com.github.appreciated.apexcharts.helper.Series;
import com.isateca.base.ui.ViewTitle;
import com.isateca.dashboard.DashboardService;
import com.isateca.dashboard.DashboardService.LowStockEntry;
import com.isateca.dashboard.DashboardService.NamedCount;
import com.isateca.dashboard.DashboardService.Summary;
import com.isateca.inventory.Movement;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Optional;

@Route("dashboard")
@PageTitle("Dashboard")
@Menu(order = -1, icon = "vaadin:dashboard", title = "Dashboard")
@PermitAll
class DashboardView extends VerticalLayout {

    DashboardView(DashboardService dashboardService) {
        var summary = dashboardService.getSummary();

        setSizeFull();
        setSpacing(true);
        add(new ViewTitle("Dashboard"), buildStatTiles(summary), buildCharts(summary),
                buildCustomerMovementsChart(summary), buildLowStockSection(summary),
                buildRecentMovementsSection(summary));
    }

    private Div buildStatTiles(Summary summary) {
        var row = new Div();
        row.addClassName("stat-tile-row");
        row.add(statTile("Productos activos", summary.activeProducts(), false),
                statTile("Bodegas activas", summary.activeWarehouses(), false),
                statTile("Renglones con existencia", summary.stockedItemCount(), false),
                statTile("Productos bajo el mínimo", summary.lowStock().size(), !summary.lowStock().isEmpty()));
        return row;
    }

    private Div statTile(String label, long value, boolean warning) {
        var tile = new Div();
        tile.addClassName("stat-tile");
        if (warning) {
            tile.addClassName("stat-tile-warning");
        }
        var valueSpan = new Span(String.valueOf(value));
        valueSpan.addClassName("stat-tile-value");
        var labelSpan = new Span(label);
        labelSpan.addClassName("stat-tile-label");
        tile.add(valueSpan, labelSpan);
        return tile;
    }

    private HorizontalLayout buildCharts(Summary summary) {
        var categoryChart = chartSection("Productos activos por categoría",
                buildCategoryPieChart(summary.productsByCategory()));
        var movementChart = chartSection("Movimientos recientes por tipo",
                buildBarChart(summary.recentMovementsByType()));

        var row = new HorizontalLayout(categoryChart, movementChart);
        row.setWidthFull();
        row.setFlexGrow(1, categoryChart, movementChart);
        return row;
    }

    private VerticalLayout buildCustomerMovementsChart(Summary summary) {
        return chartSection("Movimientos por cliente", buildBarChart(summary.movementsByCustomer()));
    }

    private VerticalLayout chartSection(String title, Div chart) {
        var section = new VerticalLayout(new H3(title), chart);
        section.setPadding(false);
        section.setSpacing(false);
        section.getElement().getStyle().set("gap", "var(--lumo-space-s)");
        section.setWidthFull();
        return section;
    }

    private Div buildCategoryPieChart(List<NamedCount> data) {
        if (data.isEmpty()) {
            return emptyChartPlaceholder();
        }
        var labels = data.stream().map(NamedCount::name).toArray(String[]::new);
        var values = data.stream().map(NamedCount::count).map(Double::valueOf).toArray(Double[]::new);
        var chart = ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withType(Type.PIE)
                        .withToolbar(ToolbarBuilder.get().withShow(false).build()).build())
                .withTheme(ThemeBuilder.get().withMode(Mode.DARK).build()).withLabels(labels).withSeries(values)
                .build();
        chart.setHeight("300px");
        var wrapper = new Div(chart);
        wrapper.setWidthFull();
        return wrapper;
    }

    private Div buildBarChart(List<NamedCount> data) {
        if (data.isEmpty()) {
            return emptyChartPlaceholder();
        }
        var categories = data.stream().map(NamedCount::name).toList();
        var values = data.stream().map(NamedCount::count).map(Double::valueOf).toArray(Double[]::new);
        var chart = ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withType(Type.BAR)
                        .withToolbar(ToolbarBuilder.get().withShow(false).build()).build())
                .withTheme(ThemeBuilder.get().withMode(Mode.DARK).build())
                .withPlotOptions(PlotOptionsBuilder.get()
                        .withBar(BarBuilder.get().withHorizontal(false).withColumnWidth("55%").build()).build())
                .withDataLabels(DataLabelsBuilder.get().withEnabled(false).build())
                .withLegend(LegendBuilder.get().withShow(false).build())
                .withXaxis(XAxisBuilder.get().withCategories(categories).build())
                .withSeries(new Series<>("Movimientos", values)).build();
        chart.setHeight("300px");
        var wrapper = new Div(chart);
        wrapper.setWidthFull();
        return wrapper;
    }

    private Div emptyChartPlaceholder() {
        var placeholder = new Div(new Span("Sin datos para mostrar"));
        placeholder.getElement().getStyle().set("color", "var(--lumo-secondary-text-color)");
        placeholder.getElement().getStyle().set("padding", "var(--lumo-space-m)");
        return placeholder;
    }

    private VerticalLayout buildLowStockSection(Summary summary) {
        var grid = new Grid<LowStockEntry>();
        grid.addColumn(e -> e.product().getSku()).setHeader("SKU").setAutoWidth(true);
        grid.addColumn(e -> e.product().getName()).setHeader("Producto").setAutoWidth(true);
        grid.addColumn(e -> e.currentQuantity().toPlainString()).setHeader("Stock actual").setAutoWidth(true);
        grid.addColumn(e -> Optional.ofNullable(e.product().getMinStock()).map(BigDecimal::toPlainString)
                .orElse("—")).setHeader("Stock mínimo").setAutoWidth(true);
        grid.setItems(summary.lowStock());
        grid.setEmptyStateText("Ningún producto está por debajo de su stock mínimo");
        grid.setAllRowsVisible(true);

        var section = new VerticalLayout(new H3("Productos bajo el stock mínimo"), grid);
        section.setPadding(false);
        section.setSpacing(false);
        section.getElement().getStyle().set("gap", "var(--lumo-space-s)");
        return section;
    }

    private VerticalLayout buildRecentMovementsSection(Summary summary) {
        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(getLocale())
                .withZone(ZoneId.systemDefault());

        var grid = new Grid<Movement>();
        grid.addColumn(m -> m.getProduct().getSku() + " — " + m.getProduct().getName()).setHeader("Producto")
                .setAutoWidth(true);
        grid.addColumn(m -> m.getMovementType().getName()).setHeader("Tipo").setAutoWidth(true);
        grid.addColumn(m -> m.getWarehouse().getName()).setHeader("Bodega").setAutoWidth(true);
        grid.addColumn(m -> m.getQuantity().toPlainString()).setHeader("Cantidad").setAutoWidth(true);
        grid.addColumn(m -> m.getUser().getUsername()).setHeader("Usuario").setAutoWidth(true);
        grid.addColumn(m -> dateTimeFormatter.format(m.getCreatedAt())).setHeader("Fecha").setAutoWidth(true);
        grid.setItems(summary.recentMovements());
        grid.setEmptyStateText("Todavía no hay movimientos registrados");
        grid.setAllRowsVisible(true);

        var section = new VerticalLayout(new H3("Movimientos recientes"), grid);
        section.setPadding(false);
        section.setSpacing(false);
        section.getElement().getStyle().set("gap", "var(--lumo-space-s)");
        return section;
    }
}
