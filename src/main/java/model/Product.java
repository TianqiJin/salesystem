package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Created by tjin on 2015-11-22.
 */
public class Product extends ProductBase{
    public static Logger logger= Logger.getLogger(Product.class);

    private StringProperty texture;
    private StringProperty size;


    public Product(Object... params){
        super(Arrays.copyOfRange(params, 0, 3));
        if (params.length==5) {
            this.texture = new SimpleStringProperty((String) params[3]);
            this.size = new SimpleStringProperty((String) params[4]);
        }else{
            this.texture = new SimpleStringProperty(null);
            this.size = new SimpleStringProperty(null);
        }
    }

    public static class ProductBuilder{
        private int productId;
        private int totalNum;
        private float unitPrice;
        private String texture = null;
        private String size = null;

        public ProductBuilder productId(int productId){
            this.productId = productId;
            return this;
        }
        public ProductBuilder totalNum(int totalNum){
            this.totalNum = totalNum;
            return this;
        }
        public ProductBuilder unitPrice(float unitPrice){
            this.unitPrice = unitPrice;
            return this;
        }
        public ProductBuilder texture(String texture){
            this.texture = texture;
            return this;
        }
        public ProductBuilder size(String size){
            this.size = size;
            return this;
        }
        public Product build(){
            return new Product(productId, totalNum, unitPrice, texture, size);
        }
    }
    public String getTexture() {
        return texture.get();
    }

    public void setTexture(String texture) {
        this.texture.set(texture);
    }

    public String getSize() {
        return size.get();
    }

    public void setSize(String size) {
        this.size.set(size);
    }


    public Object[] getAllProperties(){
        return (new Object[]{getProductId(), getTexture(), getSize(), getTotalNum(), getUnitPrice()});
    }

    public Object[] getAllPropertiesForUpdate(){
        return (new Object[]{getTexture(), getSize(), getTotalNum(), getUnitPrice(),getProductId()});
    }

}
