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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tjin on 2/5/2016.
 */
public class InvoiceGenerator {
    private static Logger logger = Logger.getLogger(InvoiceGenerator.class);
    static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
            Font.BOLD);
    static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.NORMAL, BaseColor.RED);
    static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16,
            Font.BOLD);
    static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.BOLD);
    static Font smallText = new Font(Font.FontFamily.TIMES_ROMAN, 8);
    static Font tinyBold = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.BOLDITALIC);
    private static String destination;

    public InvoiceGenerator(){
        this.destination =
                new File("C:/Users/tjin/Desktop", "Report_" + new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss").format(new Date()) + ".pdf").getPath();
    }
    public void buildInvoice(Transaction transaction, Customer customer) throws Exception {
        Invoice invoice = new Invoice(transaction, customer);
        createPdf(invoice);
    }

    public void createPdf(Invoice invoice) throws Exception {
        InvoiceData invoiceData = new InvoiceData();
        BasicProfile basic = invoiceData.createBasicProfileData(invoice);

        // step 1
        Document document = new Document();
        // step 2
        PdfAWriter writer = PdfAWriter.getInstance(document, new FileOutputStream(destination), PdfAConformanceLevel.ZUGFeRDBasic);
        writer.setPdfVersion(PdfWriter.VERSION_1_7);
        writer.createXmpMetadata();
        writer.getXmpWriter().setProperty(PdfAXmpWriter.zugferdSchemaNS, PdfAXmpWriter.zugferdDocumentFileName, "ZUGFeRD-invoice.xml");
        // step 3
        document.open();
        // step 4
//        ICC_Profile icc = ICC_Profile.getInstance(new FileInputStream(ICC));
//        writer.setOutputIntents("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", icc);

        // header
        Paragraph p;
        p = new Paragraph(basic.getName() + " " + basic.getId(), subFont);
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
        //table.addCell(seller);
        PdfPCell buyer = getPartyAddress("To:",
                basic.getBuyerName(),
                basic.getBuyerLineOne(),
                basic.getBuyerLineTwo(),
                basic.getBuyerCountryID(),
                basic.getBuyerPostcode(),
                basic.getBuyerCityName());
        table.addCell(buyer);
//        seller = getPartyTax(basic.getSellerTaxRegistrationID(),
//                basic.getSellerTaxRegistrationSchemeID());
        table.addCell(seller);
//        buyer = getPartyTax(basic.getBuyerTaxRegistrationID(),
//                basic.getBuyerTaxRegistrationSchemeID());
        //table.addCell(buyer);
        document.add(table);

        // line items
        table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        table.setWidths(new int[]{7, 2, 1, 2, 2, 2});
        table.addCell(getCell("Item:", Element.ALIGN_LEFT, smallText));
        table.addCell(getCell("Price:", Element.ALIGN_LEFT, smallText));
        table.addCell(getCell("Qty:", Element.ALIGN_LEFT, smallText));
        table.addCell(getCell("Subtotal:", Element.ALIGN_LEFT, smallText));

        for (ProductTransaction product : invoice.getProducts()) {
            table.addCell(getCell(product.getProductId(), Element.ALIGN_LEFT, smallBold));
            table.addCell(getCell(InvoiceData.format2dec(InvoiceData.round(product.getUnitPrice())), Element.ALIGN_RIGHT, smallBold));
            table.addCell(getCell(String.valueOf(product.getQuantity()), Element.ALIGN_RIGHT, smallBold));
            table.addCell(getCell(InvoiceData.format2dec(InvoiceData.round(product.getSubTotal())), Element.ALIGN_RIGHT, smallBold));
        }
        document.add(table);

        // grand totals
        document.add(getTotalsTable(
                basic.getTaxBasisTotalAmount(), basic.getTaxTotalAmount(), basic.getGrandTotalAmount(), basic.getGrandTotalAmountCurrencyID(),
                basic.getTaxTypeCode(), basic.getTaxApplicablePercent(),
                basic.getTaxBasisAmount(), basic.getTaxCalculatedAmount(), basic.getTaxCalculatedAmountCurrencyID()));

        // payment info
        document.add(getPaymentInfo(basic.getPaymentReference(), basic.getPaymentMeansPayeeFinancialInstitutionBIC(), basic.getPaymentMeansPayeeAccountIBAN()));

        // XML version
        InvoiceDOM dom = new InvoiceDOM(basic);
        PdfDictionary parameters = new PdfDictionary();
        parameters.put(PdfName.MODDATE, new PdfDate());
        PdfFileSpecification fileSpec = writer.addFileAttachment(
                "ZUGFeRD invoice", dom.toXML(), null,
                "ZUGFeRD-invoice.xml", "application/xml",
                AFRelationshipValue.Alternative, parameters);
        PdfArray array = new PdfArray();
        array.add(fileSpec.getReference());
        writer.getExtraCatalog().put(PdfName.AF, array);

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
        cell.addElement(new Paragraph(who, smallText));
        cell.addElement(new Paragraph(name, smallText));
        cell.addElement(new Paragraph(line1, smallText));
        cell.addElement(new Paragraph(line2, smallText));
        cell.addElement(new Paragraph(String.format("%s-%s %s", countryID, postcode, city), smallText));
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

}
