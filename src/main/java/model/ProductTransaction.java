package model;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.FloatBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Arrays;

/**
 * Created by tjin on 12/12/2015.
 */
public class ProductTransaction extends ProductBase{
    private IntegerProperty quantity;
    private FloatProperty subTotal;

    public ProductTransaction(Object... params){
        super(Arrays.copyOfRange(params, 0, 3));
        this.quantity = new SimpleIntegerProperty((Integer)params[3]);
        this.subTotal = new SimpleFloatProperty((Float)params[4]);
        this.subTotal.bind(quantity.multiply(unitPriceProperty()));
    }

    public static class ProductTransactionBuilder{
        private int productId;
        private int totalNum;
        private float unitPrice;
        private int quantity = 0;
        private float subTotal = 0;

        public ProductTransactionBuilder productId(int productId){
            this.productId = productId;
            return this;
        }
        public ProductTransactionBuilder totalNum(int totalNum){
            this.totalNum = totalNum;
            return this;
        }
        public ProductTransactionBuilder unitPrice(float unitPrice){
            this.unitPrice = unitPrice;
            return this;
        }
        public ProductTransactionBuilder quantity(int quantity){
            this.quantity = quantity;
            return this;
        }
        public ProductTransactionBuilder subTotal(float subTotal){
            this.subTotal = subTotal;
            return this;
        }
        public ProductTransaction build(){
            return new ProductTransaction(productId, totalNum, unitPrice, quantity, subTotal);
        }
    }

    public int getQuantity() {
        return quantity.get();
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public void setQuantity(int quantity) {
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

    public String toString(){
        StringBuffer tmpString = new StringBuffer();
        tmpString
                .append("Product ID: " + this.getProductId() + " ")
                .append("Unit Price: " + this.getUnitPrice() + " ")
                .append("Quantity: " + this.getQuantity() + " ")
                .append("Sub Total: " + this.getSubTotal() + "\n");
        return tmpString.toString();
    }
}
