package model;

import javafx.beans.property.*;

import java.math.BigDecimal;

/**
 * Created by tjin on 12/13/2015.
 */
public abstract class ProductBase {
    private IntegerProperty productId;
    private IntegerProperty totalNum;
    private FloatProperty unitPrice;

    public ProductBase(Object... params){
        if (params[0]!=null) {
            this.productId = new SimpleIntegerProperty((Integer) params[0]);
            this.totalNum = new SimpleIntegerProperty((Integer) params[1]);
            this.unitPrice = new SimpleFloatProperty((Float) params[2]);
        }else{
            this.productId = new SimpleIntegerProperty(0) ;
            this.totalNum = new SimpleIntegerProperty(0) ;
            this.unitPrice = new SimpleFloatProperty(0);
        }
    }

    public int getProductId() {
        return productId.get();
    }

    public void setProductId(int productId) {
        this.productId.set(productId);
    }

    public float getUnitPrice() {
        return unitPrice.get();
    }

    public FloatProperty unitPriceProperty() {
        return unitPrice;
    }

    public void setUnitPrice(float unitPrice) {
        this.unitPrice.set(unitPrice);
    }

    public int getTotalNum() {
        return totalNum.get();
    }

    public void setTotalNum(int totalNum) {
        this.totalNum.set(totalNum);
    }



}
