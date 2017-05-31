package PDF;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import javafx.beans.property.DoubleProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import model.Customer;
import model.ProductTransaction;
import model.Transaction;
import model.TransactionReport;
import org.apache.log4j.Logger;
import util.AlertBuilder;
import util.DateUtil;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by Tjin on 5/28/2017.
 */
public class ReportGenerator {
    private static Logger logger = Logger.getLogger(ReportGenerator.class);
    private static Font tableTitle = new Font(Font.FontFamily.COURIER, 7, Font.BOLD);
    private static Font totalFont = new Font(Font.FontFamily.COURIER, 4, Font.NORMAL);

    private Document document;
    private List<TransactionReport> transactionReports;
    private String title;
    private Double total;
    private final String destination;

    public ReportGenerator(List<TransactionReport> transactionReports, String destination, String title, double total){
        this.transactionReports = transactionReports;
        this.destination = destination;
        this.title = title;
        this.total = total;
    }

    public void generate() throws DocumentException, FileNotFoundException {
        document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(destination));
        PDFGenerator.TableHeader header = new PDFGenerator.TableHeader();
        writer.setPageEvent(header);
        document.setMargins(50, 45, 90, 40);
        document.open();
        header.setHeader(title);
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{10,14,14,14,10,14,10,10});
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell(getCell("Date", Element.ALIGN_LEFT, tableTitle));
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell(getCell("Transaction ID", Element.ALIGN_LEFT, tableTitle));
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell(getCell("Staff", Element.ALIGN_LEFT, tableTitle));
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell(getCell("Customer/Vendor", Element.ALIGN_LEFT, tableTitle));
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell(getCell("Total", Element.ALIGN_LEFT, tableTitle));
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell(getCell("Transaction Type", Element.ALIGN_LEFT, tableTitle));
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell(getCell("Report Type", Element.ALIGN_LEFT, tableTitle));
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell(getCell("Amount", Element.ALIGN_LEFT, tableTitle));

        for(TransactionReport report : transactionReports){
            table.addCell(getCell(report.getPaymentDate(), Element.ALIGN_LEFT, totalFont));
            table.addCell(getCell(String.valueOf(report.getTransactionId()), Element.ALIGN_LEFT, totalFont));
            table.addCell(getCell(report.getStaffName(), Element.ALIGN_LEFT, totalFont));
            table.addCell(getCell(report.getInfo(), Element.ALIGN_LEFT, totalFont));
            table.addCell(getCell(String.valueOf(report.getTransactionTotal()), Element.ALIGN_LEFT, totalFont));
            table.addCell(getCell(report.getTransactionType().name(), Element.ALIGN_LEFT, totalFont));
            table.addCell(getCell(report.getReportType().name(), Element.ALIGN_LEFT, totalFont));
            table.addCell(getCell(String.valueOf(report.getReportValue()), Element.ALIGN_LEFT, totalFont));
        }
        boolean flag = true;
        for(PdfPRow row: table.getRows().subList(1, table.getRows().size())) {
            for(PdfPCell cell: row.getCells()) {
                cell.setBackgroundColor(flag ? BaseColor.LIGHT_GRAY : BaseColor.WHITE);
            }
            flag = !flag;
        }
        document.add(table);
        document.add(generateResult());
        document.close();
        openPDF(destination);
    }


    private PdfPTable generateResult() throws DocumentException{
        PdfPTable table = new PdfPTable(2);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setWidthPercentage(30);
        table.setSpacingBefore(50);
        table.setSpacingAfter(10);
        table.setWidths(new int[]{50, 50});
        table.addCell(getCell("Total Amount", Element.ALIGN_LEFT, tableTitle));
        table.addCell(getCell(String.valueOf(total), Element.ALIGN_LEFT, totalFont));
        return table;
    }

    private PdfPCell getCell(String value, int alignment, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        Paragraph p = new Paragraph(value, font);
        p.setAlignment(alignment);
        cell.addElement(p);
        return cell;
    }

    private void openPDF(String path){
        StringBuilder errorMsg = new StringBuilder();
        ButtonType openType = new ButtonType("Open Report");
        ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new AlertBuilder()
                .alertType(Alert.AlertType.CONFIRMATION)
                .alertTitle("Report Generation")
                .alertHeaderText("Report Generation Successful!")
                .alertContentText("Report is successfully generated at\n" + path + "\n\nClick Open Report to open it")
                .alertButton(openType, cancelType)
                .build();

        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == openType){
            if(Desktop.isDesktopSupported()){
                try {
                    Desktop.getDesktop().open(new File(path));
                } catch (IOException e) {
                    logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
                    errorMsg.append(e.getMessage() + "\n");
                }
            }else{
                logger.error("AWT Desktop is not supported!");
                errorMsg.append("AWT Desktop is not supported!\n");
            }
        }else{
            alert.close();
        }
        if(errorMsg.length() != 0){
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertTitle("Invoice Generation")
                    .alertHeaderText("Invoice generation is failed!")
                    .alertContentText(errorMsg.toString())
                    .build()
                    .showAndWait();
        }
    }

    public static class TableHeader extends PdfPageEventHelper {
        String header;
        PdfTemplate total;

        public void setHeader(String header) {
            this.header = header;
        }

        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(30, 16);
        }

        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable table = new PdfPTable(3);
            try {
                table.setWidths(new int[]{24, 24, 24});
                table.setTotalWidth(527);
                table.setLockedWidth(true);
                table.getDefaultCell().setBorder(Rectangle.BOTTOM);
                table.addCell(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                table.getDefaultCell().setFixedHeight(20);
                table.getDefaultCell().setBorder(Rectangle.BOTTOM);
                table.addCell(header);
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(String.format("Page %d", writer.getPageNumber()));
                table.writeSelectedRows(0, -1, 34, 803, writer.getDirectContent());
            }
            catch(DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }

        public void onCloseDocument(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
                    new Phrase(String.valueOf(writer.getPageNumber() - 1)),
                    (document.left() + document.right())/2 , document.bottom()-20, 0);
        }
    }
}
