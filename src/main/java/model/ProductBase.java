package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.math.BigDecimal;

/**
 * Created by tjin on 12/13/2015.
 */
public abstract class ProductBase {
    private int productId;
    private int totalNum;
    private SimpleObjectProperty<BigDecimal> unitPrice;

    public ProductBase(Object... params){
        this.productId = (Integer)params[0];
        this.totalNum = (Integer)params[1];
        this.unitPrice = new SimpleObjectProperty<>((BigDecimal) params[2]);
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice.get();
    }

    public SimpleObjectProperty<BigDecimal> unitPriceProperty() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice.set(unitPrice);
    }
}
