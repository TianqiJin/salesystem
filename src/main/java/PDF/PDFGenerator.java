package PDF;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.scene.paint.Color;
import model.Customer;
import model.ProductTransaction;
import model.Transaction;
import util.DateUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by tjin on 12/28/15.
 */
public class PDFGenerator {
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
        table.addCell("Date");
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell("Staff");
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell("Customer");
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell("Type");
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell("Total");
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell("Transaction Details");
        for(Transaction transaction : transactionList){
            table.addCell(DateUtil.format(transaction.getDate()));
            table.addCell(String.valueOf(transaction.getStaffId()));
            table.addCell(String.valueOf(transaction.getInfo()));
            table.addCell(transaction.getType().toString());
            table.addCell(String.valueOf(transaction.getTotal()));
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
        document.close();
    }

    private void generateInnerTable(PdfPTable outerTable, Transaction transaction) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{25,25,25,25});
        table.addCell("Product ID");
        table.addCell("Unit Price");
        table.addCell("Quantity");
        table.addCell("Sub Total");
        for(ProductTransaction productTransaction: transaction.getProductTransactionList()){
            table.addCell(String.valueOf(productTransaction.getProductId()));
            table.addCell(String.valueOf(productTransaction.getUnitPrice()));
            table.addCell(String.valueOf(productTransaction.getQuantity()));
            table.addCell(String.valueOf(productTransaction.getSubTotal()));
        }
        table.setSpacingAfter(10);
        outerTable.addCell(table);
    }

    private PdfPTable generateResult() throws DocumentException{
        PdfPTable table = new PdfPTable(3);
        table.setSpacingBefore(100);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setWidthPercentage(50);
        table.setWidths(new int[]{35,35,35});
        table.addCell("Total OUT");
        table.addCell("Total IN");
        table.addCell("Total RETURN");
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
        table.addCell(new BigDecimal(totalOut).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
        table.addCell(new BigDecimal(totalIn).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
        table.addCell(new BigDecimal(totalReturn).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
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
