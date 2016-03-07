package model;

/**
 * Created by tjin on 1/13/2016.
 */
public class Property {
    private Integer productWarnLimit;
    private Integer gstRate;
    private Integer pstRate;

    public Property(int productWarnLimit, int gstRate, int pstRate){
        this.productWarnLimit = productWarnLimit;
        this.gstRate = gstRate;
        this.pstRate = pstRate;
    }

    public Integer getProductWarnLimit() {
        return productWarnLimit;
    }

    public void setProductWarnLimit(Integer productWarnLimit) {
        this.productWarnLimit = productWarnLimit;
    }

    public Integer getGstRate() {
        return gstRate;
    }

    public void setGstRate(Integer gstRate) {
        this.gstRate = gstRate;
    }

    public Integer getPstRate() {
        return pstRate;
    }

    public void setPstRate(Integer pstRate) {
        this.pstRate = pstRate;
    }
}
