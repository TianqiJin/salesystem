package PDF;

import MainClass.SaleSystem;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import model.*;
import org.apache.log4j.Logger;
import util.AlertBuilder;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

/**
 * Created by tjin on 2/5/2016.
 */
public class InvoiceGenerator {
    private static Logger logger = Logger.getLogger(InvoiceGenerator.class);

    static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 20);
    static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 18);
    static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 14,Font.BOLD);
    static Font tableTitle = new Font(Font.FontFamily.COURIER, 11, Font.BOLD,BaseColor.WHITE);
    static Font totalFont = new Font(Font.FontFamily.COURIER, 9, Font.BOLD);
    static Font addressFont = new Font(Font.FontFamily.TIMES_ROMAN, 12);
    static Font smallText = new Font(Font.FontFamily.TIMES_ROMAN, 8);
    static Font midText = new Font(Font.FontFamily.TIMES_ROMAN, 12);
    static Font largeText = new Font(Font.FontFamily.TIMES_ROMAN, 13);
    static Font tinyBold = new Font(Font.FontFamily.TIMES_ROMAN, 8,Font.BOLD);
    static final String CHINESE_FONT_LOCATION = "/fonts/Deng.ttf";
    Font chineseFont = new Font(BaseFont.createFont(CHINESE_FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.EMBEDDED), 9);

    private String destinationInvoice;
    private String destinationDelivery;
    private String destinationQuotation;
    private String destinationReturn;
    private String destinationPo;
    private Invoice invoice;
    private SaleSystem saleSystem;
    private StringBuilder errorMsg;

    public enum InvoiceType{
        INVOICE("Invoice"), QUOTATION("Quotation"), DELIVERY("Delivery"), PO("Purchase Order");

        private String type;
        InvoiceType(String type){
            this.type = type;
        }
    }

    public InvoiceGenerator(String destination_Folder, SaleSystem saleSystem) throws IOException, DocumentException{
        this.destinationInvoice =
                new File(destination_Folder, "Invoice_" + new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss").format(new Date()) + ".pdf").getPath();
        this.destinationDelivery =
                new File(destination_Folder, "Delivery_" + new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss").format(new Date()) + ".pdf").getPath();
        this.destinationReturn =
                new File(destination_Folder, "Return_" + new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss").format(new Date()) + ".pdf").getPath();
        this.destinationQuotation =
                new File(destination_Folder, "Quotation_" + new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss").format(new Date()) + ".pdf").getPath();
        this.destinationPo =
                new File(destination_Folder, "PurchaseOrder_" + new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss").format(new Date()) + ".pdf").getPath();
        this.saleSystem = saleSystem;
        this.errorMsg = new StringBuilder();
    }
    public void buildInvoice(Transaction transaction, Customer customer, Staff staff, Address address) throws Exception {
        invoice = new Invoice(transaction, customer, staff, address);
        createPdf(invoice);
    }

    public void buildDelivery(Transaction transaction, Customer customer, Staff staff, Address address) throws Exception{
        invoice = new Invoice(transaction, customer, staff, address);
        createPdf_delivery(invoice);

    }

    public void buildQuotation(Transaction transaction, Customer customer, Staff staff, Address address) throws Exception {
        invoice = new Invoice(transaction, customer, staff, address);
        createPdf_quotation(invoice);
    }

    public void buildPo(Transaction transaction, Customer customer, Staff staff, Address address) throws Exception {
        invoice = new Invoice(transaction, customer, staff, address);
        createPdfPo(invoice);
    }

    public void createPdfPo(Invoice invoice) throws Exception{
        InvoiceData invoiceData = new InvoiceData();
        InvoiceData.AdvancedProfileImp basic = invoiceData.createBasicProfileData(invoice);

        //init
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(destinationPo));
        document.open();
        //Create Header;
        createHeader(document, basic, InvoiceType.PO, invoice.getTransaction());
        //gstNum Field
        createGSTNo(document);
        // Address seller / buyer
        createPartyAddress(document, basic);

        // line items
        createItemList(document, InvoiceType.PO);
        //Add PaymentInfo
        createPaymentInfo(document, InvoiceType.PO);
        //Create CompanyClaims
        createCompanyClaims(document);
        document.close();
        openPDF(this.destinationPo);
    }

    public void createPdf_delivery(Invoice invoice) throws Exception {
        InvoiceData invoiceData = new InvoiceData();
        InvoiceData.AdvancedProfileImp basic = invoiceData.createBasicProfileData(invoice);

        //init
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(destinationDelivery));
        document.open();
        //Create Header;
        createHeader(document, basic, InvoiceType.DELIVERY, invoice.getTransaction());
        // Address seller / buyer
        createPartyAddress(document, basic);
        // line items
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        table.setWidths(new int[]{3, 3, 3, 3, 3});
        table.addCell(getCellTitle("Item", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Name", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Size", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Qty(Boxes)", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Remark", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        int row=0;
        for (ProductTransaction product : invoice.getProducts()) {
            table.addCell(getCellwithBackground(product.getProductId(), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(product.getDisplayName(), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(product.getSize(), Element.ALIGN_LEFT, totalFont, row));
            int boxes = product.getBoxNum().getBox();
            int res = product.getBoxNum().getResidualTile();
            String displayNum = boxes+" boxes";
            if(res!=0){
                displayNum+=", "+res+" pieces";
            }
            table.addCell(getCellwithBackground(displayNum, Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(product.getRemark(), Element.ALIGN_LEFT, chineseFont, row));
            row++;
        }
        document.add(table);
        //Add PaymentInfo
        createPaymentInfo(document, InvoiceType.DELIVERY);
        //Add Signature
        PdfPTable signTable = new PdfPTable(2);
        signTable.setWidthPercentage(86);
        signTable.addCell(getCellNoWrap("Packing by: _________________", Element.ALIGN_LEFT, largeText));
        signTable.addCell(getCellNoWrap("Customer Sign: ________________", Element.ALIGN_RIGHT, largeText));
        signTable.addCell(getCellNoWrap(" ", Element.ALIGN_CENTER,largeText ));
        signTable.addCell(getCellNoWrap(" ", Element.ALIGN_CENTER, largeText));
        signTable.addCell(getCellNoWrap("Date:         __________________", Element.ALIGN_LEFT, largeText));
        signTable.addCell(getCellNoWrap("Date:            ___________________", Element.ALIGN_RIGHT, largeText));
        if(signTable.getTotalWidth()==0)signTable.setTotalWidth((document.right()-document.left())*signTable.getWidthPercentage()/100f);
        signTable.writeSelectedRows(0, -1, (document.left()+document.right()-signTable.getTotalWidth())/2f, document.bottom() + signTable.getTotalHeight()*3f, writer.getDirectContent());

        //Create CompanyClaims
        createCompanyClaims(document);
        document.close();
        openPDF(this.destinationDelivery);
    }


    public void createPdf(Invoice invoice) throws Exception {
        InvoiceData invoiceData = new InvoiceData();
        InvoiceData.AdvancedProfileImp basic = invoiceData.createBasicProfileData(invoice);

        //init
        Document document = new Document();
        PdfWriter writer;
        if(invoice.getTransaction().getType().equals(Transaction.TransactionType.RETURN)){
            writer = PdfWriter.getInstance(document, new FileOutputStream(destinationReturn));
        }else{
            writer = PdfWriter.getInstance(document, new FileOutputStream(destinationInvoice));
        }
        document.open();
        // header
        createHeader(document, basic, InvoiceType.INVOICE, invoice.getTransaction());
        //gstNum Field
        createGSTNo(document);
        // Address seller / buyer
        createPartyAddress(document, basic);

        // line items
        createItemList(document, InvoiceType.INVOICE);
        //Add PaymentInfo
        createPaymentInfo(document, InvoiceType.INVOICE);
        //Add Company Claims
        createCompanyClaims(document);
        document.close();
        if(invoice.getTransaction().getType().equals(Transaction.TransactionType.OUT)){
            openPDF(this.destinationInvoice);
        }else if(invoice.getTransaction().getType().equals(Transaction.TransactionType.RETURN)){
            openPDF(this.destinationReturn);
        }
    }

    public void createPdf_quotation(Invoice invoice) throws Exception{
        InvoiceData invoiceData = new InvoiceData();
        InvoiceData.AdvancedProfileImp basic = invoiceData.createBasicProfileData(invoice);
        //init
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(destinationQuotation));
        document.open();
        // header
        createHeader(document, basic, InvoiceType.QUOTATION, invoice.getTransaction());
        //gstNum Field
        createGSTNo(document);
        // Address seller / buyer
        createPartyAddress(document, basic);
        // line items
        createItemList(document, InvoiceType.QUOTATION);
        //Create CompanyClaims
        createCompanyClaims(document);
        document.close();
        openPDF(this.destinationQuotation);
    }

    public String convertDate(Date d, String newFormat) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(newFormat);
        return sdf.format(d);
    }

    public PdfPCell getPartyAddress(String who, String name, String line1, String line2, String countryID, String postcode, String city, String company, String phone) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.addElement(new Paragraph(who, addressFont));
        cell.addElement(new Paragraph(name, addressFont));
        cell.addElement(new Paragraph(line1, addressFont));
        if(line2 != null){
            cell.addElement(new Paragraph(line2, addressFont));
        }
        cell.addElement(new Paragraph(String.format("%s %s", countryID, city), addressFont));
        cell.addElement(new Paragraph(postcode, addressFont));
        if(company != null){
            cell.addElement(new Paragraph(company, addressFont));
        }
        if(phone != null){
            cell.addElement(new Paragraph(phone, addressFont));
        }
        return cell;
    }

    public PdfPCell getCell(String value, int alignment, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        Paragraph p = new Paragraph(value, font);
        p.setAlignment(alignment);
        cell.addElement(p);
        return cell;
    }
    public PdfPCell getCellTitle(String value, int alignment, Font font, BaseColor color) {
        PdfPCell cell = new PdfPCell();
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        cell.setBackgroundColor(color);
        Paragraph p = new Paragraph(value, font);
        p.setAlignment(alignment);
        cell.addElement(p);
        return cell;
    }

    public PdfPCell getCellwithBackground(String value, int alignment, Font font, int i) {
        PdfPCell cell = new PdfPCell();
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        if (i%2==0){
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        }
        Paragraph p = new Paragraph(value, font);
        p.setAlignment(alignment);
        cell.addElement(p);
        return cell;
    }

    public PdfPCell getCellNoWrap(String value, int alignment, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        cell.setBorder(Rectangle.NO_BORDER);
        Paragraph p = new Paragraph(value, font);
        p.setAlignment(alignment);
        cell.addElement(p);
        return cell;
    }

    public PdfPCell getCellNoWrapwithBack(String value, int alignment, Font font, BaseColor color) {
        PdfPCell cell = new PdfPCell();
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(color);
        Paragraph p = new Paragraph(value, font);
        p.setAlignment(alignment);
        cell.addElement(p);
        return cell;
    }


    public PdfPCell getCellUnder(String value, int alignment, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        cell.setBorder(Rectangle.BOTTOM);
        Paragraph p = new Paragraph(value, font);
        p.setAlignment(alignment);
        cell.addElement(p);
        return cell;
    }
    public PdfPCell getCellTop(String value, int alignment, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        cell.setBorder(Rectangle.TOP);
        Paragraph p = new Paragraph(value, font);
        p.setAlignment(alignment);
        cell.addElement(p);
        return cell;
    }

    public PdfPCell getCellHolder() {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);

        return cell;
    }

    public void getEmptyCellHolder(PdfPTable table, int num){
        int i = 0;
        while(i < num){
            table.addCell(getCellHolder());
            i++;
        }
    }

    private void createHeader(Document document, InvoiceData.AdvancedProfileImp profileImp, InvoiceType type, Transaction transaction){
        Paragraph pCompany = new Paragraph("Milan Building Supply LTD.",catFont);
        pCompany.setAlignment(Element.HEADER);
        String typeString = null;
        String invoiceId = null;

        //Generate invoice type
        if(type.equals(InvoiceType.DELIVERY)){
            typeString = "Pick Up/Delivery Order";
        }else if(type.equals(InvoiceType.QUOTATION)){
            typeString = "Quotation Order";
        }else if(type.equals(InvoiceType.INVOICE)){
            if(this.invoice.getTransaction().getType().equals(Transaction.TransactionType.OUT)){
                typeString = profileImp.getName();
            }else if(this.invoice.getTransaction().getType().equals(Transaction.TransactionType.RETURN)){
                typeString = "RETURN";
            }
        }else if(type.equals(InvoiceType.PO)){
            typeString = "PO";
        }

        //Generate invoice Id
        if(type.equals(InvoiceType.PO)){
            invoiceId = String.format("D/%05d", invoice.getId()) + "-" + String.format("%02d", transaction.getPayinfo().size());
        }else{
            invoiceId = String.format("D/%05d", invoice.getId());
        }
        Paragraph pType = new Paragraph(typeString + " " + invoiceId, subFont);
        pType.setAlignment(Element.ALIGN_RIGHT);
        Paragraph pDate = null;
        try {
            pDate = new Paragraph(convertDate(profileImp.getDateTime(), "MMM dd, yyyy"), smallBold);
        } catch (Exception e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }
        pDate.setAlignment(Element.ALIGN_RIGHT);
        try {
            document.add(pCompany);
            document.add(pType);
            document.add(pDate);
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }

    }

    private void createItemList(Document document, InvoiceType type){
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        try {
            table.setWidths(new int[]{3, 3, 2, 2, 2, 2, 3});
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }
        table.addCell(getCellTitle("Item", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Name", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Size", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Price", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Qty(ft)", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Subtotal", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Remark", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        int row=0;
        double total=0;
        for (ProductTransaction product : invoice.getProducts()) {
            double tmpSubTotal = Double.valueOf(InvoiceData.format2dec(InvoiceData.round(product.getSubTotal()*100/(100-product.getDiscount()))));
            total+=tmpSubTotal;
            table.addCell(getCellwithBackground(product.getProductId(), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(product.getDisplayName(), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(product.getSize(), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(InvoiceData.format2dec(InvoiceData.round(product.getUnitPrice())), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(String.valueOf(product.getQuantity()), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(String.valueOf(tmpSubTotal), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(product.getRemark(), Element.ALIGN_LEFT, chineseFont, row));
            row++;
        }
        getEmptyCellHolder(table, 5);
        table.addCell(getCellNoWrap("Subtotal:", Element.ALIGN_LEFT, tinyBold));
        table.addCell(getCellNoWrap("$CAD   " + new BigDecimal(total).setScale(2, BigDecimal.ROUND_HALF_EVEN), Element.ALIGN_JUSTIFIED_ALL, smallText));

        double discount =  new BigDecimal(
                invoice.getTotal()-invoice.getTransaction().getGstTax()-invoice.getTransaction().getPstTax()-total)
                .setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();

        if(discount>0){
            String msg = "Discount is greater than 0";
            logger.error(msg);
            errorMsg.append(msg).append("\n");
        }
        getEmptyCellHolder(table, 5);
        table.addCell(getCellNoWrap("Discount:", Element.ALIGN_LEFT, tinyBold));
        table.addCell(getCellNoWrap("$CAD   " + new BigDecimal(discount).setScale(2, BigDecimal.ROUND_HALF_EVEN), Element.ALIGN_JUSTIFIED_ALL, smallText));


        getEmptyCellHolder(table, 5);
        table.addCell(getCellNoWrap("GST:", Element.ALIGN_LEFT, tinyBold));
        table.addCell(getCellNoWrap("$CAD   " + (invoice.getTransaction().getGstTax()), Element.ALIGN_JUSTIFIED_ALL, smallText));

        getEmptyCellHolder(table, 5);
        table.addCell(getCellUnder("PST:", Element.ALIGN_LEFT, tinyBold));
        table.addCell(getCellUnder("$CAD   " + (invoice.getTransaction().getPstTax()), Element.ALIGN_JUSTIFIED_ALL, smallText));

        getEmptyCellHolder(table, 5);
        table.addCell(getCellTop("Total:", Element.ALIGN_LEFT, totalFont));
        table.addCell(getCellTop("$CAD  " + invoice.getTotal(), Element.ALIGN_JUSTIFIED_ALL, totalFont));

        double paid = 0;
        for (PaymentRecord paymentRecord : invoice.getPaymentRecords()){
            paid+=paymentRecord.getPaid();
        }
        BigDecimal paidRoundEven = new BigDecimal(paid).setScale(2, BigDecimal.ROUND_HALF_EVEN);
        getEmptyCellHolder(table, 5);
        table.addCell(getCellUnder("Paid:", Element.ALIGN_LEFT, totalFont));
        table.addCell(getCell("$CAD  " + paidRoundEven.toString(), Element.ALIGN_JUSTIFIED_ALL, totalFont));

        getEmptyCellHolder(table, 5);
        table.addCell(getCellTop("Total Due:", Element.ALIGN_LEFT, totalFont));
        table.addCell(getCellTop("$CAD  " + new BigDecimal((invoice.getTotal()-paid)).setScale(2, BigDecimal.ROUND_HALF_EVEN), Element.ALIGN_JUSTIFIED_ALL, totalFont));

        try {
            document.add(table);
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }
    }

    private void createGSTNo(Document document){
        Paragraph pGst = new Paragraph("GST No. " + saleSystem.getProperty().getGstNumber(), tinyBold);
        pGst.setAlignment(Element.ALIGN_LEFT);
        try {
            document.add(pGst);
            document.add(new Paragraph());
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }
    }

    private void createPartyAddress(Document document, InvoiceData.AdvancedProfileImp basic){
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        PdfPCell buyer;
        PdfPCell seller;
        String buyerWho;
        String sellerWho;
        if(this.invoice.getTransaction().getType().equals(Transaction.TransactionType.RETURN)){
            buyerWho = "From:";
            sellerWho = "To:";
        }else{
            buyerWho = "To:";
            sellerWho = "From:";
        }
        buyer = getPartyAddress(buyerWho,
                    basic.getBuyerName(),
                    basic.getBuyerLineOne(),
                    basic.getBuyerLineTwo(),
                    basic.getBuyerCountryID(),
                    basic.getBuyerPostcode(),
                    basic.getBuyerCityName(),
                    basic.getBuyerCompanyName(),
                    basic.getBuyerPhoneNumber());
        seller = getPartyAddress(sellerWho,
                    basic.getSellerName(),
                    basic.getSellerLineOne(),
                    basic.getSellerLineTwo(),
                    basic.getSellerCountryID(),
                    basic.getSellerPostcode(),
                    basic.getSellerCityName(),
                    basic.getSellerCompanyName(),
                    basic.getSellerPhoneNumber());
        if(this.invoice.getTransaction().getType().equals(Transaction.TransactionType.RETURN)){
            table.addCell(buyer);
            table.addCell(seller);
        }else{
            table.addCell(seller);
            table.addCell(buyer);
        }
        try {
            document.add(table);
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }

    }

    private void createPaymentInfo(Document document, InvoiceType type){
        Paragraph p = new Paragraph("Payment Information:",addressFont);
        PdfPTable table = null;
        int row = 0;
        p.setAlignment(Element.ALIGN_LEFT);
        if(type.equals(InvoiceType.DELIVERY)){
            table = new PdfPTable(2);
            table.setWidthPercentage(50);
            table.setHorizontalAlignment(0);
            table.setSpacingBefore(10);
            try {
                table.setWidths(new int[]{5,5});
            } catch (DocumentException e) {
                logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
                errorMsg.append(e.getMessage()).append("\n");
            }
            table.addCell(getCellTitle("Date", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
            table.addCell(getCellTitle("Type", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
            row = 0;
            for (PaymentRecord paymentRecord : invoice.getPaymentRecords()){
                table.addCell(getCellwithBackground(paymentRecord.getDate(), Element.ALIGN_LEFT, totalFont, row));
                table.addCell(getCellwithBackground(paymentRecord.getPaymentType(), Element.ALIGN_LEFT, totalFont, row));
                row++;
            }
            p.add(table);
        }else if(type.equals(InvoiceType.INVOICE) || type.equals(InvoiceType.PO)){
            if(this.invoice.getTransaction().getType().equals(Transaction.TransactionType.OUT)){
                table = new PdfPTable(4);
                table.setWidthPercentage(50);
                table.setHorizontalAlignment(0);
                table.setSpacingBefore(10);
                try {
                    table.setWidths(new int[]{3, 2, 2, 4});
                } catch (DocumentException e) {
                    logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
                    errorMsg.append(e.getMessage()).append("\n");
                }
                table.addCell(getCellTitle("Date", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
                table.addCell(getCellTitle("Amount", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
                table.addCell(getCellTitle("Type", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
                table.addCell(getCellTitle("Deposit", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
                for (PaymentRecord paymentRecord : invoice.getPaymentRecords()){
                    table.addCell(getCellwithBackground(paymentRecord.getDate(), Element.ALIGN_LEFT, totalFont, row));
                    table.addCell(getCellwithBackground(InvoiceData.format2dec(InvoiceData.round(paymentRecord.getPaid())), Element.ALIGN_LEFT, totalFont, row));
                    table.addCell(getCellwithBackground(paymentRecord.getPaymentType(), Element.ALIGN_LEFT, totalFont, row));
                    table.addCell(getCellwithBackground(paymentRecord.isDeposit()? "YES" : "NO", Element.ALIGN_LEFT, totalFont, row));
                    row++;
                }
            }else if (this.invoice.getTransaction().getType().equals(Transaction.TransactionType.RETURN)){
                table = new PdfPTable(3);
                table.setWidthPercentage(50);
                table.setHorizontalAlignment(0);
                table.setSpacingBefore(10);
                try {
                    table.setWidths(new int[]{3, 3, 3});
                } catch (DocumentException e) {
                    logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
                    errorMsg.append(e.getMessage()).append("\n");
                }
                table.addCell(getCellTitle("Date", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
                table.addCell(getCellTitle("Amount", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
                table.addCell(getCellTitle("Type", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
                for (PaymentRecord paymentRecord : invoice.getPaymentRecords()){
                    table.addCell(getCellwithBackground(paymentRecord.getDate(), Element.ALIGN_LEFT, totalFont, row));
                    table.addCell(getCellwithBackground(InvoiceData.format2dec(InvoiceData.round(paymentRecord.getPaid())), Element.ALIGN_LEFT, totalFont, row));
                    table.addCell(getCellwithBackground(paymentRecord.getPaymentType(), Element.ALIGN_LEFT, totalFont, row));
                    row++;
                }
            }
            p.add(table);
        }
        try {
            document.add(p);
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }
    }

    private void createCompanyClaims(Document document){
        document.newPage();
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        table.addCell(getCellNoWrapwithBack("THANK YOU FOR YOUR BUSINESS!", Element.ALIGN_CENTER, addressFont, BaseColor.LIGHT_GRAY));
        try {
            document.add(table);
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }

        Paragraph p = new Paragraph("[Disclaimer:]",midText);
        p.setAlignment(Element.ALIGN_LEFT);
        try {
            document.add(p);
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }
        p = new Paragraph("1. Payments\n" +
                "-  A 50% deposit is required on all orders.\n" +
                "-  Full payment is required six days prior to delivery or pickup.\n" +
                "-  Credit card payments are accepted in sore and by phone.\n" +
                "-  Milan Tiles does not accept payment upon delivery\n" +
                "\n" +
                "2. Return and Cancellation\n" +
                "-  A refund will be issued for full boxes of current tile returned in good box condition within 2 \n" +
                "months of the date product were received.\n" +
                "-  No returns or refund allowed on discontinued or special order items.\n" +
                "-  Milan Tiles Ltd is under no obligation to accept the cancellation of any special order items.\n" +
                "-  All returns must be in their original packaging and in the condition in which it was received.\n" +
                "\n" +
                "3. Delivery\n" +
                "-  All deliveries should be arranged through the store of purchase.\n" +
                "-  We are unable to provide you with specific delivery times �?only that it will be a morning or \n" +
                "afternoon delivery.\n" +
                "-  Delivery on stocked products will be charge from $50 delivery fee which depends on the location.\n" +
                "-  Special orders will be take more than 6 weeks to delivery.\n" +
                "\n" +
                "4. Pick-Ups\n" +
                "This is generally arranged through our Milan Tile warehouse. You must check in at the reception \n" +
                "desk before goods can be released. Once your goods have left our facility, Milan will not accept \n" +
                "any claims of damage. If someone other than the name on the invoice is picking up yours tiles, \n" +
                "you must notify us in advance.\n"+
                "\n" +
                "5. Third-Party Pick-Ups\n" +
                "Milan will not accept any damage claims once a third-party carrier has deceived the goods. Please \n" +
                "note tile installers are considered a third party.\n" +
                "\n" +
                "6. Installations\n" +
                "Milan will not provide any tile installers for customers. Milan do not have responsibility on any \n" +
                "installation issues.",largeText);
        p.setAlignment(Element.ALIGN_LEFT);
        try {
            document.add(p);
        } catch (DocumentException e) {
            logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            errorMsg.append(e.getMessage()).append("\n");
        }
    }

    private void openPDF(String path){
        if(this.errorMsg.length() == 0){
            ButtonType openType = new ButtonType("Open Invoice");
            ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new AlertBuilder()
                    .alertType(Alert.AlertType.CONFIRMATION)
                    .alertTitle("Invoice Generation")
                    .alertHeaderText("Invoice Generation Successful!")
                    .alertContentText("Invoice is successfully generated at\n" + path + "\n\nClick Open Invoice to open it")
                    .alertButton(openType, cancelType)
                    .build();

            Optional<ButtonType> result = alert.showAndWait();
            if(result.isPresent() && result.get() == openType){
                if(Desktop.isDesktopSupported()){
                    try {
                        Desktop.getDesktop().open(new File(path));
                    } catch (IOException e) {
                        logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
                        errorMsg.append(e.getMessage()).append("\n");
                    }
                }else{
                    logger.error("AWT Desktop is not supported!");
                    errorMsg.append("AWT Desktop is not supported!").append("\n");
                }
            }else{
                alert.close();
            }
        }else{
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