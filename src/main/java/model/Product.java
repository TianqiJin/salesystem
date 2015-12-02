package model;

import org.apache.log4j.Logger;

/**
 * Created by tjin on 2015-11-22.
 */
public class Product{
    public static Logger logger= Logger.getLogger(Product.class);
    private int productId;
    private String texture;
    private String size;
    private int totalNum;


    public Product(Object... params){
        this.productId = (Integer)params[0];
        this.texture = (String) params[1];
        this.size = (String) params[2];
        this.totalNum= (Integer) params[3];
    }


    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }


}
