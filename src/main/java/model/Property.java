package model;

/**
 * Created by tjin on 1/13/2016.
 */
public class Property {
    private Integer productWarnLimit;
    private Integer gstRate;
    private Integer pstRate;
    private String gstNumber;
    private UserClass userClass;

    public Property(int productWarnLimit, int gstRate, int pstRate, String gstNumber, UserClass userClass){
        this.productWarnLimit = productWarnLimit;
        this.gstRate = gstRate;
        this.pstRate = pstRate;
        this.gstNumber = gstNumber;
        this.userClass = userClass;
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

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public UserClass getUserClass() {
        return userClass;
    }

    public void setUserClass(UserClass userClass) {
        this.userClass = userClass;
    }

}
