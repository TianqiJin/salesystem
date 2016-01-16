package model;

/**
 * Created by tjin on 1/13/2016.
 */
public class Property {
    private Integer productWarnLimit;
    private Integer taxRate;

    public Property(int productWarnLimit, int taxRate){
        this.productWarnLimit = productWarnLimit;
        this.taxRate = taxRate;
    }

    public Integer getProductWarnLimit() {
        return productWarnLimit;
    }

    public void setProductWarnLimit(Integer productWarnLimit) {
        this.productWarnLimit = productWarnLimit;
    }

    public Integer getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(Integer taxRate) {
        this.taxRate = taxRate;
    }
}
