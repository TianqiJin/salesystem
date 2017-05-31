package model;

import org.apache.log4j.Logger;

/**
 * Created by jiawei.liu on 1/24/16.
 */
public class PaymentRecord {
    private static Logger logger = Logger.getLogger(PaymentRecord.class);
    private String date;
    private double paid;
    private String paymentType;
    private boolean isDeposit;

    public PaymentRecord(Object... params){
        this.date=(String)params[0];
        this.paid=(Double)params[1];
        this.paymentType = (String)params[2];
        this.isDeposit = (Boolean)params[3];
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getPaid() {
        return paid;
    }

    public void setPaid(double paid) {
        this.paid = paid;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public boolean isDeposit() {
        return isDeposit;
    }

    public void setDeposit(boolean deposit) {
        isDeposit = deposit;
    }
}
