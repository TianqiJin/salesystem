package PDF;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import model.Customer;
import model.ProductTransaction;
import model.Transaction;
import util.DateUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    private Integer productId;
    private Integer staffId;
    private Customer customer;

    public PDFGenerator(List<Transaction> transactionList, String destination, Integer productId, Integer staffId, Customer customer){
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
        header.setHeader("Customer Report");
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{20,10,20,50});
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell("Date");
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell("Staff");
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell("Customer");
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell("Transaction Details");
        for(Transaction transaction : transactionList){
            table.addCell(DateUtil.format(transaction.getDate()));
            table.addCell(String.valueOf(transaction.getStaffId()));
            table.addCell(String.valueOf(transaction.getInfo()));
            generateInnerTable(table, transaction);
        }
        document.add(table);
        document.close();
    }

    private void generateInnerTable(PdfPTable outterTable, Transaction transaction) throws DocumentException {
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
        outterTable.addCell(table);
    }

    public static class PDFGeneratorBuilder{
        private String destination;
        private Integer productId;
        private Customer customer;
        private List<Transaction> transactionList;
        private Integer staffId;

        public PDFGeneratorBuilder destination(String destination){
            this.destination =
                    new File(destination, "Customer_Report_" + new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss").format(new Date()) + ".pdf").getPath();
            return this;
        }
        public PDFGeneratorBuilder productId(Integer productId){
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

        if((productId == null) && (staffId == null) && (customer == null)){
            System.out.println("No Filter Info is provided");
            return transactionList;
        }
        if(staffId != null){
            transactionList.removeIf(transaction -> transaction.getStaffId() != staffId.intValue());
        }
        if(customer != null){
            transactionList.removeIf(transaction -> transaction.getInfo() != customer.getUserName());
        }
        if(productId != null){
            for(Transaction transaction: transactionList){
                transaction.getProductTransactionList().removeIf(productTransaction -> productTransaction.getProductId() != productId.intValue());
            }
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
