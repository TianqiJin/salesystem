package PDF;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import model.Customer;
import model.ProductTransaction;
import model.Transaction;
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
 * Created by tjin on 12/28/15.
 */
public class PDFGenerator {
    private static Logger logger = Logger.getLogger(PDFGenerator.class);
    private static Font tableTitle = new Font(Font.FontFamily.COURIER, 12, Font.BOLD);
    private static Font totalFont = new Font(Font.FontFamily.COURIER, 9, Font.BOLD);
    private static Font tableTitleInner = new Font(Font.FontFamily.COURIER, 7);
    private static Font totalFontInner = new Font(Font.FontFamily.COURIER, 4);

    private Document document;
    private List<Transaction> transactionList;
    private final String destination;
    private String productId;
    private Integer staffId;
    private Customer customer;

    public PDFGenerator(List<Transaction> transactionList, String destination, String productId, Integer staffId, Customer customer){
        this.transactionList = transactionList;
        this.destination = destination;
        this.productId = productId;
        this.staffId = staffId;
        this.customer = customer;
    }

    public void generate() throws DocumentException, FileNotFoundException {

        this.transactionList = filterTransaction();
        document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(destination));
        TableHeader header = new TableHeader();
        writer.setPageEvent(header);
        document.setMargins(50, 45, 90, 40);
        document.open();
        header.setHeader("MILAN TILE CORPORATION REVENUE REPORT");
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{15,10,15,15,10,40});
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell(getCell("Date", Element.ALIGN_LEFT, tableTitle));
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell(getCell("Staff", Element.ALIGN_LEFT, tableTitle));
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell(getCell("Customer", Element.ALIGN_LEFT, tableTitle));
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell(getCell("Type", Element.ALIGN_LEFT, tableTitle));
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell(getCell("Total", Element.ALIGN_LEFT, tableTitle));
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell(getCell("Transaction Details", Element.ALIGN_LEFT, tableTitle));
        for(Transaction transaction : transactionList){
            table.addCell(getCell(DateUtil.format(transaction.getDate()), Element.ALIGN_LEFT, totalFont));
            table.addCell(getCell(String.valueOf(transaction.getStaffId()), Element.ALIGN_LEFT, totalFont));
            table.addCell(getCell(String.valueOf(transaction.getInfo()), Element.ALIGN_LEFT, totalFont));
            table.addCell(getCell(transaction.getType().toString(), Element.ALIGN_LEFT, totalFont));
            table.addCell(getCell(String.valueOf(transaction.getTotal()), Element.ALIGN_LEFT, totalFont));
            generateInnerTable(table, transaction);
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
        document.add(generateQuantityResult());
        document.close();
        openPDF(destination);
    }

    private void generateInnerTable(PdfPTable outerTable, Transaction transaction) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{25,25,25,25});
        table.addCell(getCell("Product ID", Element.ALIGN_LEFT, tableTitleInner));
        table.addCell(getCell("Unit Price", Element.ALIGN_LEFT, tableTitleInner));
        table.addCell(getCell("Quantity", Element.ALIGN_LEFT, tableTitleInner));
        table.addCell(getCell("Sub Total", Element.ALIGN_LEFT, tableTitleInner));
        for(ProductTransaction productTransaction: transaction.getProductTransactionList()){
            table.addCell(getCell(String.valueOf(productTransaction.getProductId()), Element.ALIGN_LEFT, totalFontInner));
            table.addCell(getCell(String.valueOf(productTransaction.getUnitPrice()), Element.ALIGN_LEFT, totalFontInner));
            table.addCell(getCell(String.valueOf(productTransaction.getQuantity()), Element.ALIGN_LEFT, totalFontInner));
            table.addCell(getCell(String.valueOf(productTransaction.getSubTotal()), Element.ALIGN_LEFT, totalFontInner));
        }
        table.setSpacingAfter(10);
        outerTable.addCell(table);
    }

    private PdfPTable generateResult() throws DocumentException{
        PdfPTable table = new PdfPTable(3);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setWidthPercentage(70);
        table.setSpacingBefore(50);
        table.setSpacingAfter(10);
        table.setWidths(new int[]{35,35,35});
        table.addCell(getCell("Total OUT", Element.ALIGN_LEFT, tableTitle));
        table.addCell(getCell("Total IN", Element.ALIGN_LEFT, tableTitle));
        table.addCell(getCell("Total RETURN", Element.ALIGN_LEFT, tableTitle));
        double totalOut = 0;
        double totalIn = 0;
        double totalReturn = 0;
        for(Transaction transaction : transactionList){
            if(transaction.getType().equals(Transaction.TransactionType.IN)){
                totalIn += transaction.getTotal();
            }else if(transaction.getType().equals(Transaction.TransactionType.IN.OUT)){
                totalOut += transaction.getTotal();
            }else if(transaction.getType().equals(Transaction.TransactionType.RETURN)){
                totalReturn += transaction.getTotal();
            }
        }
        table.addCell(getCell(new BigDecimal(totalOut).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString(), Element.ALIGN_LEFT, totalFont));
        table.addCell(getCell(new BigDecimal(totalIn).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString(), Element.ALIGN_LEFT, totalFont));
        table.addCell(getCell(new BigDecimal(totalReturn).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString(), Element.ALIGN_LEFT, totalFont));
        return table;
    }

    private PdfPTable generateQuantityResult() throws DocumentException{
        PdfPTable table = new PdfPTable(3);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setWidthPercentage(70);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        table.setWidths(new int[]{35,35,35});
        table.addCell(getCell("Total OUT Quantity", Element.ALIGN_LEFT, tableTitle));
        table.addCell(getCell("Total IN Quantity", Element.ALIGN_LEFT, tableTitle));
        table.addCell(getCell("Total RETURN Quantity", Element.ALIGN_LEFT, tableTitle));
        double totalOut = 0;
        double totalIn = 0;
        double totalReturn = 0;
        for(Transaction transaction : transactionList){
            if(transaction.getType().equals(Transaction.TransactionType.IN)){
                for(ProductTransaction p: transaction.getProductTransactionList()){
                    totalIn += p.getQuantity();
                }
            }else if(transaction.getType().equals(Transaction.TransactionType.OUT) || transaction.getType().equals(Transaction.TransactionType.QUOTATION)){
                for(ProductTransaction p: transaction.getProductTransactionList()){
                    totalOut += p.getQuantity();
                }
            }else if(transaction.getType().equals(Transaction.TransactionType.RETURN)){
                for(ProductTransaction p: transaction.getProductTransactionList()){
                    totalReturn += p.getQuantity();
                }
            }
        }
        table.addCell(getCell(new BigDecimal(totalOut).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString(), Element.ALIGN_LEFT, totalFont));
        table.addCell(getCell(new BigDecimal(totalIn).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString(), Element.ALIGN_LEFT, totalFont));
        table.addCell(getCell(new BigDecimal(totalReturn).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString(), Element.ALIGN_LEFT, totalFont));
        return table;
    }

    public static class PDFGeneratorBuilder{
        private String destination;
        private String productId;
        private Customer customer;
        private List<Transaction> transactionList;
        private Integer staffId;

        public PDFGeneratorBuilder destination(String destination){
            this.destination =
                    new File(destination, "Report_" + new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss").format(new Date()) + ".pdf").getPath();
            return this;
        }
        public PDFGeneratorBuilder productId(String productId){
            this.productId = productId;
            return this;
        }
        public PDFGeneratorBuilder customer(Customer customer){
            this.customer = customer;
            return this;
        }
        public PDFGeneratorBuilder staffId(Integer staffId){
            this.staffId = staffId;
            return this;
        }
        public PDFGeneratorBuilder transactionList(List<Transaction> transactionList){
            this.transactionList = transactionList;
            return this;
        }
        public PDFGenerator build(){
            return new PDFGenerator(transactionList, destination, productId, staffId, customer);
        }
    }

    private List<Transaction> filterTransaction(){
        System.out.println(productId);
        if((productId == null) && (staffId == null) && (customer == null)){
            return transactionList;
        }
        if(staffId != null){
            transactionList.removeIf(transaction -> transaction.getStaffId() != staffId.intValue());
        }
        if(customer != null){
            transactionList.removeIf(transaction -> !transaction.getInfo().equals(customer.getUserName()));
        }
        if(productId != null){
            for(Transaction transaction: transactionList){
                transaction.getProductTransactionList().removeIf(productTransaction ->
                        !productTransaction.getProductId().equals(String.valueOf(productId)));
            }
            transactionList.removeIf(transaction -> transaction.getProductTransactionList().size() == 0);
        }
        return transactionList;
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
}

class TableHeader extends PdfPageEventHelper {
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
