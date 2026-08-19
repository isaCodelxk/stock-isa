package com.isateca.inventory;

import com.isateca.catalog.Warehouse;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Builds a kardex (product ledger) PDF: the product's own data followed by every movement it has
 * ever had, oldest first, with a running stock balance computed the same way
 * {@link MovementService#save} applies movements to stock (IN adds, OUT subtracts, NEUTRAL/transfer
 * nets to zero since it only moves quantity between warehouses of the same product).
 */
@Service
public class KardexPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.forLanguageTag("es-MX"))
            .withZone(ZoneId.systemDefault());

    private final MovementService movementService;

    KardexPdfService(MovementService movementService) {
        this.movementService = movementService;
    }

    public byte[] generateKardex(Product product) {
        var movements = movementService.listByProduct(product);
        var out = new ByteArrayOutputStream();
        var boldFont = boldFont();

        try (var document = new Document(new PdfDocument(new PdfWriter(out)))) {
            document.add(new Paragraph("Kardex de producto").setFont(boldFont).setFontSize(16));
            document.add(new Paragraph(product.getSku() + " — " + product.getName()).setFont(boldFont));
            document.add(new Paragraph("Categoría: " + product.getCategory().getName()));
            document.add(new Paragraph("Unidad de medida: " + product.getUnitOfMeasure().getAbbreviation()));
            document.add(new Paragraph("Costo de compra: " + product.getPurchasePrice().toPlainString()));
            document.add(new Paragraph("Precio de venta: " + product.getSalePrice().toPlainString()));
            document.add(new Paragraph(" "));
            document.add(buildMovementsTable(movements, boldFont));
        }

        return out.toByteArray();
    }

    private Table buildMovementsTable(List<Movement> movements, PdfFont boldFont) {
        var table = new Table(UnitValue.createPercentArray(new float[] { 13, 13, 10, 10, 8, 8, 8, 10, 10, 10 }))
                .useAllAvailableWidth();
        Stream.of("Fecha", "Tipo", "Bodega", "Bodega destino", "Entrada", "Salida", "Saldo", "Precio unitario",
                "Usuario", "Referencia").forEach(header -> table.addHeaderCell(headerCell(header, boldFont)));

        var balance = BigDecimal.ZERO;
        for (var movement : movements) {
            var entrada = "—";
            var salida = "—";
            switch (movement.getMovementType().getDirection()) {
                case IN -> {
                    balance = balance.add(movement.getQuantity());
                    entrada = movement.getQuantity().toPlainString();
                }
                case OUT -> {
                    balance = balance.subtract(movement.getQuantity());
                    salida = movement.getQuantity().toPlainString();
                }
                case NEUTRAL -> {
                    // Transfer between warehouses of the same product: shown as both an exit and an
                    // entry so the moved quantity is visible, but it nets to zero on the running
                    // total since nothing actually entered or left the product's overall stock.
                    entrada = movement.getQuantity().toPlainString();
                    salida = movement.getQuantity().toPlainString();
                }
            }
            table.addCell(cell(DATE_TIME_FORMATTER.format(movement.getCreatedAt())));
            table.addCell(cell(movement.getMovementType().getName()));
            table.addCell(cell(movement.getWarehouse().getName()));
            table.addCell(cell(Optional.ofNullable(movement.getTargetWarehouse()).map(Warehouse::getName)
                    .orElse("—")));
            table.addCell(cell(entrada));
            table.addCell(cell(salida));
            table.addCell(cell(balance.toPlainString()));
            table.addCell(cell(Optional.ofNullable(movement.getUnitPrice()).map(BigDecimal::toPlainString)
                    .orElse("—")));
            table.addCell(cell(movement.getUser().getUsername()));
            table.addCell(cell(Optional.ofNullable(movement.getReferenceNote()).orElse("—")));
        }
        return table;
    }

    // Base-14 standard font, loaded from AFM metrics only (no embedded font file) - this never
    // actually fails, so a checked IOException here is wrapped rather than declared on every caller.
    private static PdfFont boldFont() {
        try {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Cell headerCell(String text, PdfFont boldFont) {
        return new Cell().add(new Paragraph(text).setFont(boldFont));
    }

    private static Cell cell(String text) {
        return new Cell().add(new Paragraph(text));
    }
}
