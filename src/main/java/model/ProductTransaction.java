package model;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Created by tjin on 12/12/2015.
 */
public class ProductTransaction extends ProductBase{
    private IntegerProperty quantity;
    private ObjectProperty<BigDecimal> subTotal;

    public ProductTransaction(Object... params){
        super(Arrays.copyOfRange(params, 0, 3));
        this.quantity = new SimpleIntegerProperty((Integer) params[3]);
        this.subTotal = new SimpleObjectProperty<>((BigDecimal)params[4]);
        ObjectBinding<BigDecimal> subTotalBinding = new ObjectBinding<BigDecimal>() {
            @Override
            protected BigDecimal computeValue() {
                return getUnitPrice().multiply(new BigDecimal(getQuantity()));
            }
        };
        this.subTotal.bind(subTotalBinding);
    }

    public static class ProductTransactionBuilder{
        private int productId;
        private int totalNum;
        private BigDecimal unitPrice;
        private int quantity = 0;
        private BigDecimal subTotal = new BigDecimal("0.00").setScale(2, BigDecimal.ROUND_HALF_EVEN);

        public ProductTransactionBuilder productId(int productId){
            this.productId = productId;
            return this;
        }
        public ProductTransactionBuilder totalNum(int totalNum){
            this.totalNum = totalNum;
            return this;
        }
        public ProductTransactionBuilder unitPrice(BigDecimal unitPrice){
            this.unitPrice = unitPrice;
            return this;
        }
        public ProductTransactionBuilder quantity(int quantity){
            this.quantity = quantity;
            return this;
        }
        public ProductTransactionBuilder subTotal(BigDecimal subTotal){
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

    public BigDecimal getSubTotal() {
        return subTotal.get();
    }

    public ObjectProperty<BigDecimal> subTotalProperty() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal.set(subTotal);
    }

    public boolean isEnoughStock(){
        if(getTotalNum() >= getQuantity()){
            return true;
        }
        return false;
    }
}
