package model;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.FloatBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Arrays;

/**
 * Created by tjin on 12/12/2015.
 */
public class ProductTransaction extends ProductBase{
    private FloatProperty quantity;
    private FloatProperty subTotal;
    private IntegerProperty discount;
    private StringProperty remark;
    private BoxNum boxNum;

    public ProductTransaction(ProductTransactionBuilder builder){
        super(builder.productId, builder.totalNum, builder.unitPrice, builder.piecesPerBox, builder.size, builder.sizeNumeric);
        this.quantity = new SimpleFloatProperty(builder.quantity);
        this.discount = new SimpleIntegerProperty(builder.discount);
        this.subTotal = new SimpleFloatProperty(builder.subTotal);
        this.subTotal.bind(quantity.multiply(unitPriceProperty()).subtract(quantity.multiply(unitPriceProperty()).multiply(discountProperty()).divide(100)));
        this.boxNum = builder.boxNum;
        this.remark = new SimpleStringProperty(builder.remark);
    }

    public static class ProductTransactionBuilder{
        private String productId;
        private float totalNum = 0;
        private float unitPrice = 0;
        private int piecesPerBox = 0;
        private String size = null;
        private float sizeNumeric = 0;
        private float quantity = 0;
        private int discount = 0;
        private float subTotal = 0;
        private String remark = "";
        private BoxNum boxNum;

        public ProductTransactionBuilder productId(String productId){
            this.productId = productId;
            return this;
        }
        public ProductTransactionBuilder totalNum(float totalNum){
            this.totalNum = totalNum;
            return this;
        }
        public ProductTransactionBuilder unitPrice(float unitPrice){
            this.unitPrice = unitPrice;
            return this;
        }
        public ProductTransactionBuilder size(String size){
            this.size = size;
            return this;
        }
        public ProductTransactionBuilder sizeNumeric(float sizeNumeric){
            this.sizeNumeric = sizeNumeric;
            return this;
        }
        public ProductTransactionBuilder piecesPerBox(int piecesPerBox){
            this.piecesPerBox = piecesPerBox;
            return this;
        }
        public ProductTransactionBuilder quantity(float quantity){
            this.quantity = quantity;
            return this;
        }
        public ProductTransactionBuilder subTotal(float subTotal){
            this.subTotal = subTotal;
            return this;
        }
        public ProductTransactionBuilder discount(int discount){
            this.discount = discount;
            return this;
        }
        public ProductTransactionBuilder boxNum(BoxNum boxNum){
            this.boxNum = boxNum;
            return this;
        }
        public ProductTransactionBuilder remark(String remark){
            this.remark = remark;
            return this;
        }
        public ProductTransaction build(){
            return new ProductTransaction(this);
        }
    }

    public float getQuantity() {
        return quantity.get();
    }

    public FloatProperty quantityProperty() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity.set(quantity);
    }

    public float getSubTotal() {
        return subTotal.get();
    }

    public FloatProperty subTotalProperty() {
        return subTotal;
    }

    public void setSubTotal(float subTotal) {
        this.subTotal.set(subTotal);
    }

    public int getDiscount() {
        return discount.get();
    }

    public IntegerProperty discountProperty() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount.set(discount);
    }

    public BoxNum getBoxNum() {
        return boxNum;
    }

    public void setBoxNum(BoxNum boxNum) {
        this.boxNum = boxNum;
    }

    public String getRemark() {
        return remark.get();
    }

    public StringProperty remarkProperty() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark.set(remark);
    }

    public String toString(){
        StringBuffer tmpString = new StringBuffer();
        tmpString
                .append("Product ID: " + this.getProductId() + " ")
                .append("Unit Price: " + this.getUnitPrice() + " ")
                .append("Quantity: " + this.getQuantity() + " ")
                .append("Discount: " + new BigDecimal(this.getSubTotal()*this.getDiscount()/100).toString() + " ")
                .append("Sub Total: " + this.getSubTotal() + "\n");
        return tmpString.toString();
    }
}
