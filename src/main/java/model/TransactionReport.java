package model;

import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * Created by Tjin on 5/26/2017.
 */
public class TransactionReport {
    public enum ReportType{
        GST,
        PST,
        Cash,
        Debit,
        Credit,
        Cheque,
        Deposit,
        Broken;
    }

    public static class TransactionReportBuilder {
        private String paymentDate;
        private int transactionId;
        private String staffName;
        private String info;
        private double transactionTotal;
        private Transaction.TransactionType transactionType;
        private ReportType reportType;
        private double reportValue;

        public TransactionReportBuilder paymentDate(String paymentDate) {
            this.paymentDate = paymentDate;
            return this;
        }

        public TransactionReportBuilder transactionId(int transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public TransactionReportBuilder staffName(String staffName){
            this.staffName = staffName;
            return this;
        }

        public TransactionReportBuilder transactionType(Transaction.TransactionType type){
            this.transactionType = type;
            return this;
        }

        public TransactionReportBuilder reportType(ReportType type){
            this.reportType = type;
            return this;
        }

        public TransactionReportBuilder info(String info) {
            this.info = info;
            return this;
        }

        public TransactionReportBuilder transactionTotal(double transactionTotal) {
            this.transactionTotal = transactionTotal;
            return this;
        }

        public TransactionReportBuilder reportValue(double reportValue) {
            this.reportValue = reportValue;
            return this;
        }

        public TransactionReport build(){
            return new TransactionReport(this);
        }
    }

    private StringProperty paymentDate;
    private IntegerProperty transactionId;
    private StringProperty staffName;
    private StringProperty info;
    private DoubleProperty transactionTotal;
    private Transaction.TransactionType transactionType;
    private ReportType reportType;
    private DoubleProperty reportValue;

    public TransactionReport(TransactionReportBuilder builder){
        this.paymentDate = new SimpleStringProperty(builder.paymentDate);
        this.transactionId = new SimpleIntegerProperty(builder.transactionId);
        this.info = new SimpleStringProperty(builder.info);
        this.transactionTotal = new SimpleDoubleProperty(builder.transactionTotal);
        this.reportValue = new SimpleDoubleProperty(builder.reportValue);
        this.staffName = new SimpleStringProperty(builder.staffName);
        this.transactionType = builder.transactionType;
        this.reportType = builder.reportType;
    }

    public String getPaymentDate() {
        return paymentDate.get();
    }

    public StringProperty paymentDateProperty() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate.set(paymentDate);
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

    public String getInfo() {
        return info.get();
    }

    public StringProperty infoProperty() {
        return info;
    }

    public void setInfo(String info) {
        this.info.set(info);
    }

    public double getTransactionTotal() {
        return transactionTotal.get();
    }

    public DoubleProperty transactionTotalProperty() {
        return transactionTotal;
    }

    public void setTransactionTotal(double transactionTotal) {
        this.transactionTotal.set(transactionTotal);
    }

    public double getReportValue() {
        return reportValue.get();
    }

    public DoubleProperty reportValueProperty() {
        return reportValue;
    }

    public void setReportValue(double reportValue) {
        this.reportValue.set(reportValue);
    }

    public String getStaffName() {
        return staffName.get();
    }

    public StringProperty staffNameProperty() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName.set(staffName);
    }

    public Transaction.TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(Transaction.TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }
}
