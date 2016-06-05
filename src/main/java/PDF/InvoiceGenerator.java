package PDF;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.xml.xmp.PdfAXmpWriter;
import com.itextpdf.text.zugferd.InvoiceDOM;
import com.itextpdf.text.zugferd.profiles.BasicProfile;
import model.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tjin on 2/5/2016.
 */
public class InvoiceGenerator {
    private static Logger logger = Logger.getLogger(InvoiceGenerator.class);
    static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 20);
    static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 18);
    static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 14,Font.BOLD);
    static Font tableTitle = new Font(Font.FontFamily.COURIER, 12, Font.BOLD,BaseColor.WHITE);
    static Font totalFont = new Font(Font.FontFamily.COURIER, 12, Font.BOLD);
    static Font addressFont = new Font(Font.FontFamily.TIMES_ROMAN, 12);
    static Font smallText = new Font(Font.FontFamily.TIMES_ROMAN, 8);
    static Font tinyBold = new Font(Font.FontFamily.TIMES_ROMAN, 8,Font.BOLD);
    private static String destination_Invoice;
    private static String destination_Delivery;
    private Invoice invoice;

    public InvoiceGenerator(String destination_Folder){
        this.destination_Invoice =
                new File(destination_Folder, "Invoice_" + new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss").format(new Date()) + ".pdf").getPath();
        this.destination_Delivery =
                new File(destination_Folder, "Delivery_" + new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss").format(new Date()) + ".pdf").getPath();

    }
    public void buildInvoice(Transaction transaction, Customer customer, Staff staff) throws Exception {
        invoice = new Invoice(transaction, customer, staff);
        createPdf(invoice);
    }
    public void buildDelivery(Transaction transaction, Customer customer, Staff staff) throws Exception{
        invoice = new Invoice(transaction, customer, staff);
        createPdf_delivery(invoice, null);

    }

    public void buildDelivery(Transaction transaction, Customer customer, Staff staff, Address address) throws Exception{
        invoice = new Invoice(transaction, customer, staff);
        createPdf_delivery(invoice, address);

    }

    public void createPdf_delivery(Invoice invoice, Address address) throws Exception {
        InvoiceData invoiceData = new InvoiceData();
        BasicProfile basic = invoiceData.createBasicProfileData(invoice);

        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(destination_Delivery));
        document.open();

        Paragraph p;
        p = new Paragraph("Milan Building Supply LTD.",catFont);
        p.setAlignment(Element.HEADER);
        document.add(p);
        p = new Paragraph("Delivery Order" + " " + String.format("D/%05d", invoice.getId()), subFont);
        p.setAlignment(Element.ALIGN_RIGHT);
        document.add(p);
        p = new Paragraph(convertDate(basic.getDateTime(), "MMM dd, yyyy"), smallBold);
        p.setAlignment(Element.ALIGN_RIGHT);
        document.add(p);

        // Address seller / buyer
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        PdfPCell seller = getPartyAddress("From:",
                basic.getSellerName(),
                basic.getSellerLineOne(),
                basic.getSellerLineTwo(),
                basic.getSellerCountryID(),
                basic.getSellerPostcode(),
                basic.getSellerCityName());
        table.addCell(seller);
        PdfPCell buyer;
        if(address!=null){
            buyer = getPartyAddress("To:",
                    basic.getBuyerName(),
                    address.getStreet(),
                    "",
                    "CA",
                    address.getPostalCode(),
                    address.getCity());

        }else{
            buyer = getPartyAddress("To:",
                    basic.getBuyerName(),
                    basic.getBuyerLineOne(),
                    basic.getBuyerLineTwo(),
                    basic.getBuyerCountryID(),
                    basic.getBuyerPostcode(),
                    basic.getBuyerCityName());

        }
        table.addCell(buyer);

        document.add(table);

        // line items
        table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        table.setWidths(new int[]{3, 2, 3});
        table.addCell(getCellTitle("Item", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Size", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Qty(Boxes)", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        int row=0;
        for (ProductTransaction product : invoice.getProducts()) {
            table.addCell(getCellwithBackground(product.getProductId(), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(product.getSize(), Element.ALIGN_LEFT, totalFont, row));
            int boxes = product.getBoxNum().getBox();
            int res = product.getBoxNum().getResidualTile();
            String displayNum = boxes+" boxes";
            if(res!=0){
                displayNum+=", "+res+" pieces";
            }
            table.addCell(getCellwithBackground(displayNum, Element.ALIGN_RIGHT, totalFont, row));
            row++;
        }

        document.add(table);
        p = new Paragraph("Payment Information:",addressFont);
        p.setAlignment(Element.ALIGN_LEFT);
        table = new PdfPTable(2);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(0);
        table.setSpacingBefore(10);
        table.setWidths(new int[]{5,5});
        table.addCell(getCellTitle("Date", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Type", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        row = 0;
        for (PaymentRecord paymentRecord : invoice.getPaymentRecords()){
            table.addCell(getCellwithBackground(paymentRecord.getDate(), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(paymentRecord.getPaymentType(), Element.ALIGN_LEFT, totalFont, row));
            row++;
        }
        p.add(table);
        document.add(p);

        document.newPage();
        table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        table.addCell(getCellNoWrapwithBack("THANK YOU FOR YOUR BUSINESS!", Element.ALIGN_CENTER, addressFont, BaseColor.LIGHT_GRAY));
        document.add(table);

        p = new Paragraph("[Disclaimer:]",smallText);
        p.setAlignment(Element.ALIGN_LEFT);
        document.add(p);
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
                "-  We are unable to provide you with specific delivery times – only that it will be a morning or \n" +
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
                "installation issues.",smallText);
        p.setAlignment(Element.ALIGN_LEFT);
        document.add(p);
        // step 5
        document.close();
    }


    public void createPdf(Invoice invoice) throws Exception {
        InvoiceData invoiceData = new InvoiceData();
        BasicProfile basic = invoiceData.createBasicProfileData(invoice);

        // step 1
        Document document = new Document();
        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(destination_Invoice));
        // step 3
        document.open();
        // step 4

        // header
        Paragraph p;
        p = new Paragraph("Milan Building Supply LTD.",catFont);
        p.setAlignment(Element.HEADER);
        document.add(p);
        p = new Paragraph(basic.getName() + " " + basic.getId(), subFont);
        p.setAlignment(Element.ALIGN_RIGHT);
        document.add(p);
        p = new Paragraph(convertDate(basic.getDateTime(), "MMM dd, yyyy"), smallBold);
        p.setAlignment(Element.ALIGN_RIGHT);
        document.add(p);

        // Address seller / buyer
        PdfPTable table = new PdfPTable(2);
        if (invoice.getTransaction().getType()== Transaction.TransactionType.RETURN){
            table.setWidthPercentage(100);
            PdfPCell buyer = getPartyAddress("From:",
                    basic.getBuyerName(),
                    basic.getBuyerLineOne(),
                    basic.getBuyerLineTwo(),
                    basic.getBuyerCountryID(),
                    basic.getBuyerPostcode(),
                    basic.getBuyerCityName());
            table.addCell(buyer);
            PdfPCell seller = getPartyAddress("To:",
                    basic.getSellerName(),
                    "N/A",
                    "N/A",
                    "N/A",
                    "N/A",
                    "N/A");
            table.addCell(seller);

        }else if (invoice.getTransaction().getType() == Transaction.TransactionType.OUT){
            table.setWidthPercentage(100);
            PdfPCell seller = getPartyAddress("From:",
                    basic.getSellerName(),
                    basic.getSellerLineOne(),
                    basic.getSellerLineTwo(),
                    basic.getSellerCountryID(),
                    basic.getSellerPostcode(),
                    basic.getSellerCityName());
            table.addCell(seller);
            PdfPCell buyer = getPartyAddress("To:",
                    basic.getBuyerName(),
                    "N/A",
                    "N/A",
                    "N/A",
                    "N/A",
                    "N/A");
            table.addCell(buyer);

        }
        document.add(table);

        // line items
        table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        table.setWidths(new int[]{3, 2, 2, 2, 2});
        table.addCell(getCellTitle("Item", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Size", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Price", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Qty(feet)", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Subtotal", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        int row=0;
        double total=0;
        for (ProductTransaction product : invoice.getProducts()) {
            total+=product.getUnitPrice()*product.getQuantity();
            table.addCell(getCellwithBackground(product.getProductId(), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(product.getSize(), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(InvoiceData.format2dec(InvoiceData.round(product.getUnitPrice())), Element.ALIGN_RIGHT, totalFont, row));
            table.addCell(getCellwithBackground(String.valueOf(product.getQuantity()), Element.ALIGN_RIGHT, totalFont, row));
            table.addCell(getCellwithBackground(InvoiceData.format2dec(InvoiceData.round(product.getSubTotal())), Element.ALIGN_RIGHT, totalFont, row));
            row++;
        }
        table.addCell(getCellHolder());
        table.addCell(getCellHolder());
        table.addCell(getCellHolder());
        table.addCell(getCellNoWrap("Subtotal:", Element.ALIGN_LEFT, tinyBold));
        table.addCell(getCellNoWrap("$CAD   " + new BigDecimal(total).setScale(2, BigDecimal.ROUND_HALF_EVEN), Element.ALIGN_JUSTIFIED_ALL, smallText));

        double discount = invoice.getTotal()-invoice.getTransaction().getGstTax()-invoice.getTransaction().getPstTax()-total;
        if(discount>0){
            System.out.print("Something went wrong!!");
        }
        table.addCell(getCellHolder());
        table.addCell(getCellHolder());
        table.addCell(getCellHolder());
        table.addCell(getCellNoWrap("Discount:", Element.ALIGN_LEFT, tinyBold));
        table.addCell(getCellNoWrap("$CAD   " + new BigDecimal(discount).setScale(2, BigDecimal.ROUND_HALF_EVEN), Element.ALIGN_JUSTIFIED_ALL, smallText));

        table.addCell(getCellHolder());
        table.addCell(getCellHolder());
        table.addCell(getCellHolder());
        table.addCell(getCellNoWrap("GST:", Element.ALIGN_LEFT, tinyBold));
        table.addCell(getCellNoWrap("$CAD   " + (invoice.getTransaction().getGstTax()), Element.ALIGN_JUSTIFIED_ALL, smallText));

        table.addCell(getCellHolder());
        table.addCell(getCellHolder());
        table.addCell(getCellHolder());
        table.addCell(getCellUnder("PST:", Element.ALIGN_LEFT, tinyBold));
        table.addCell(getCellUnder("$CAD   " + (invoice.getTransaction().getPstTax()), Element.ALIGN_JUSTIFIED_ALL, smallText));

        table.addCell(getCellHolder());
        table.addCell(getCellHolder());
        table.addCell(getCellHolder());
        table.addCell(getCellTop("Total:", Element.ALIGN_LEFT, totalFont));
        table.addCell(getCellTop("$CAD  " + invoice.getTotal(), Element.ALIGN_JUSTIFIED_ALL, totalFont));

        double paid =0;
        for (PaymentRecord paymentRecord : invoice.getPaymentRecords()){
            paid+=paymentRecord.getPaid();
        }
        table.addCell(getCellHolder());
        table.addCell(getCellHolder());
        table.addCell(getCellHolder());
        table.addCell(getCellUnder("Paid:", Element.ALIGN_LEFT, totalFont));
        table.addCell(getCell("$CAD  " + paid, Element.ALIGN_JUSTIFIED_ALL, totalFont));

        table.addCell(getCellHolder());
        table.addCell(getCellHolder());
        table.addCell(getCellHolder());
        table.addCell(getCellTop("Total Due:", Element.ALIGN_LEFT, totalFont));
        table.addCell(getCellTop("$CAD  " + new BigDecimal((invoice.getTotal()-paid)).setScale(2, BigDecimal.ROUND_HALF_EVEN), Element.ALIGN_JUSTIFIED_ALL, totalFont));

        document.add(table);
        p = new Paragraph("Payment Information:",addressFont);
        p.setAlignment(Element.ALIGN_LEFT);
        table = new PdfPTable(3);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(0);
        table.setSpacingBefore(10);
        table.setWidths(new int[]{3, 3, 3});
        table.addCell(getCellTitle("Date", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Amount", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        table.addCell(getCellTitle("Type", Element.ALIGN_CENTER, tableTitle,BaseColor.BLACK));
        row = 0;
        for (PaymentRecord paymentRecord : invoice.getPaymentRecords()){
            table.addCell(getCellwithBackground(paymentRecord.getDate(), Element.ALIGN_LEFT, totalFont, row));
            table.addCell(getCellwithBackground(InvoiceData.format2dec(InvoiceData.round(paymentRecord.getPaid())), Element.ALIGN_RIGHT, totalFont, row));
            table.addCell(getCellwithBackground(paymentRecord.getPaymentType(), Element.ALIGN_LEFT, totalFont, row));
            row++;
        }
        p.add(table);
        document.add(p);

        document.newPage();
        table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        table.addCell(getCellNoWrapwithBack("THANK YOU FOR YOUR BUSINESS!", Element.ALIGN_CENTER, addressFont, BaseColor.LIGHT_GRAY));
        document.add(table);

        p = new Paragraph("[Disclaimer:]",smallText);
        p.setAlignment(Element.ALIGN_LEFT);
        document.add(p);
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
                "-  We are unable to provide you with specific delivery times – only that it will be a morning or \n" +
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
                "installation issues.",smallText);
        p.setAlignment(Element.ALIGN_LEFT);
        document.add(p);
        // step 5
        document.close();
    }

    public String convertDate(Date d, String newFormat) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(newFormat);
        return sdf.format(d);
    }

    public PdfPCell getPartyAddress(String who, String name, String line1, String line2, String countryID, String postcode, String city) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.addElement(new Paragraph(who, addressFont));
        cell.addElement(new Paragraph(name, addressFont));
        cell.addElement(new Paragraph(line1, addressFont));
        cell.addElement(new Paragraph(line2, addressFont));
        cell.addElement(new Paragraph(String.format("%s-%s %s", countryID, postcode, city), addressFont));
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

    public Paragraph getPaymentInfo(String ref, String[] bic, String[] iban) {
        Paragraph p = new Paragraph(String.format(
                "Please wire the amount due to our bank account using the following reference: %s",
                ref), smallText);
        int n = bic.length;
        for (int i = 0; i < n; i++) {
            p.add(Chunk.NEWLINE);
            p.add(String.format("BIC: %s - IBAN: %s", bic[i], iban[i]));
        }
        return p;
    }

    public PdfPTable getTotalsTable(String tBase, String tTax, String tTotal, String tCurrency,
                                    String[] type, String[] percentage, String base[], String tax[], String currency[]) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{1, 1, 3, 3, 3, 1});
        table.addCell(getCell("TAX", Element.ALIGN_LEFT, smallText));
        table.addCell(getCell("%", Element.ALIGN_RIGHT, smallText));
        table.addCell(getCell("Base amount:", Element.ALIGN_LEFT, smallText));
        table.addCell(getCell("Tax amount:", Element.ALIGN_LEFT, smallText));
        table.addCell(getCell("Total:", Element.ALIGN_LEFT, smallText));
        table.addCell(getCell("", Element.ALIGN_LEFT, smallText));
        int n = type.length;
        for (int i = 0; i < n; i++) {
            table.addCell(getCell(type[i], Element.ALIGN_RIGHT, smallText));
            table.addCell(getCell(percentage[i], Element.ALIGN_RIGHT, smallText));
            table.addCell(getCell(base[i], Element.ALIGN_RIGHT, smallText));
            table.addCell(getCell(tax[i], Element.ALIGN_RIGHT, smallText));
            double total = Double.parseDouble(base[i]) + Double.parseDouble(tax[i]);
            table.addCell(getCell(InvoiceData.format2dec(InvoiceData.round(total)), Element.ALIGN_RIGHT, smallText));
            table.addCell(getCell(currency[i], Element.ALIGN_LEFT, smallText));
        }
        PdfPCell cell = getCell("", Element.ALIGN_LEFT, smallText);
        cell.setColspan(2);
        cell.setBorder(PdfPCell.NO_BORDER);
        table.addCell(cell);
        table.addCell(getCell(tBase, Element.ALIGN_RIGHT, smallText));
        table.addCell(getCell(tTax, Element.ALIGN_RIGHT, smallText));
        table.addCell(getCell(tTotal, Element.ALIGN_RIGHT, smallText));
        table.addCell(getCell(tCurrency, Element.ALIGN_LEFT, smallText));
        return table;
    }


    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}