package model;

import com.fasterxml.jackson.annotation.JsonView;
import javafx.beans.property.*;
import org.apache.log4j.Logger;
import util.DateUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by jiawei.liu on 12/2/15.
 */
public class Transaction{
    private static Logger logger = Logger.getLogger(Transaction.class);

    private IntegerProperty transactionId;
    private ObjectProperty<LocalDate> date;
    private DoubleProperty payment;
    private StringProperty paymentType;
    private DoubleProperty storeCredit;
    private IntegerProperty staffId;
    private TransactionType type;
    private StringProperty info;
    private List<ProductTransaction> productTransactionList;
    private DoubleProperty total;
    private List<PaymentRecord> payinfo;
    private DoubleProperty gstTax;
    private DoubleProperty pstTax;
    private StringProperty note;
    private BooleanProperty deleted;


    public Transaction(TransactionBuilder builder){
        this.transactionId = new SimpleIntegerProperty(builder.transactionId);
        this.productTransactionList = builder.productTransactionList;
        this.date = new SimpleObjectProperty<>(LocalDate.parse(builder.date));
        this.payment = new SimpleDoubleProperty(builder.payment);
        this.paymentType = new SimpleStringProperty(builder.paymentType);
        this.storeCredit = new SimpleDoubleProperty(builder.storeCredit);
        this.staffId = new SimpleIntegerProperty(builder.staffId);
        this.type = builder.type;
        this.info = new SimpleStringProperty(builder.info);
        this.total = new SimpleDoubleProperty(builder.total);
        this.payinfo = builder.payinfo;
        this.gstTax = new SimpleDoubleProperty(builder.gstTax);
        this.pstTax = new SimpleDoubleProperty(builder.pstTax);
        this.note = new SimpleStringProperty(builder.note);
        this.deleted = new SimpleBooleanProperty(builder.deleted);
    }

    public static class TransactionBuilder {
        private int transactionId = 0;
        private List<ProductTransaction> productTransactionList;
        private String date = null;
        private double payment = 0.0;
        private String paymentType = null;
        private int staffId = 0;
        private double storeCredit = 0.0;
        private TransactionType type = null;
        private String info = null;
        private double total;
        private List<PaymentRecord> payinfo;
        private double gstTax;
        private double pstTax;
        private String note = null;
        private boolean deleted = false;

        public TransactionBuilder transactionId(int transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public TransactionBuilder productInfoList(List<ProductTransaction> productTransactionList){
            this.productTransactionList = productTransactionList;
            return this;
        }

        public TransactionBuilder date(String date) {
            this.date = date;
            return this;
        }

        public TransactionBuilder payment(double payment) {

            this.payment = payment;
            return this;
        }

        public TransactionBuilder paymentType(String paymentType) {
            this.paymentType = paymentType;
            return this;
        }

        public TransactionBuilder staffId(int staffId) {
            this.staffId = staffId;
            return this;
        }

        public TransactionBuilder type(TransactionType type) {
            this.type = type;
            return this;
        }

        public TransactionBuilder info(String info) {
            this.info = info;
            return this;
        }

        public TransactionBuilder storeCredit(double storeCredit){
            this.storeCredit = storeCredit;
            return this;
        }

        public TransactionBuilder total (double total){
            this.total = total;
            return this;
        }

        public TransactionBuilder payinfo (List<PaymentRecord> payinfo) {
            this.payinfo=payinfo;
            return this;
        }

        public TransactionBuilder gstTax(double gstTax){
            this.gstTax = gstTax;
            return this;
        }

        public TransactionBuilder pstTax(double pstTax){
            this.pstTax = pstTax;
            return this;
        }

        public TransactionBuilder note(String note){
            this.note = note;
            return this;
        }

        public TransactionBuilder deleted(boolean deleted){
            this.deleted = deleted;
            return this;
        }

        public Transaction build() {
            return new Transaction(this);
        }
    }

    public int getTransactionId() {
        return transactionId.get();
    }

    public IntegerProperty transactionIdProperty() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId.set(transactionId);
    }

    public LocalDate getDate() {
        return date.get();
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public double getPayment() {
        return payment.get();
    }

    public DoubleProperty paymentProperty() {
        return payment;
    }

    public void setPayment(double payment) {
        this.payment.set(payment);
    }

    public String getPaymentType() {
        return paymentType.get();
    }

    public StringProperty paymentTypeProperty() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType.set(paymentType);
    }

    public int getStaffId() {
        return staffId.get();
    }

    public IntegerProperty staffIdProperty() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId.set(staffId);
    }

    public String getInfo() {
        return info.get();
    }

    public StringProperty infoProperty() {
        return info;
    }

    public void setInfo(String info) {
        this.info.set(info);
    }
    public TransactionType getType() {
        return type;
    }

    public List<ProductTransaction> getProductTransactionList() {
        return productTransactionList;
    }

    public double getStoreCredit() {
        return storeCredit.get();
    }

    public DoubleProperty storeCreditProperty() {
        return storeCredit;
    }

    public void setStoreCredit(double storeCredit) {
        this.storeCredit.set(storeCredit);
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public double getTotal() {
        return total.get();
    }

    public DoubleProperty totalProperty() {
        return total;
    }

    public void setTotal(double total) {
        this.total.set(total);
    }

    public List<PaymentRecord> getPayinfo() {
        return payinfo;
    }

    public void setPayinfo(List<PaymentRecord> payinfo) {
        this.payinfo = payinfo;
    }

    public double getGstTax() {
        return gstTax.get();
    }

    public DoubleProperty gstTaxProperty() {
        return gstTax;
    }

    public void setGstTax(double gstTax) {
        this.gstTax.set(gstTax);
    }

    public double getPstTax() {
        return pstTax.get();
    }

    public DoubleProperty pstTaxProperty() {
        return pstTax;
    }

    public void setPstTax(double pstTax) {
        this.pstTax.set(pstTax);
    }

    public String getNote() {
        return note.get();
    }

    public StringProperty noteProperty() {
        return note;
    }

    public void setNote(String note) {
        this.note.set(note);
    }

    public boolean isDeleted() {
        return deleted.get();
    }

    public BooleanProperty deletedProperty() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted.set(deleted);
    }

    public List<String> getProductTransactionListRevised(){
        List<String> list = new ArrayList<>();
        for(ProductTransaction productTransaction: this.productTransactionList){
            StringBuffer string = new StringBuffer();
            list.add(string.append("Product ID: ").append(productTransaction.getProductId())
                            .append(" Total Piece: ").append(productTransaction.getTotalNum())
                            .append(" Quantity: ").append(productTransaction.getQuantity())
                            .append(" Sub-Total: ").append(productTransaction.getSubTotal())
                            .toString());
        }
        return list;
    }

    public void setProductTransactionList(List<ProductTransaction> productTransactionList) {
        this.productTransactionList = productTransactionList;
    }

    public Object[] getPropertiesForDeleteUpdate(){
        return new Object[]{isDeleted(), getTransactionId()};
    }


    public enum TransactionType {
        IN("in"), OUT("out"), RETURN("return"), QUOTATION("quotation");
        private String type;

        private TransactionType(String type) {
            this.type = type;
        }

        public static TransactionType getType(String type) {
            for (TransactionType ttype : TransactionType.values()) {
                if (ttype.name().equalsIgnoreCase(type)) {
                    return ttype;
                }
            }
            return null;
        }
    }
}
