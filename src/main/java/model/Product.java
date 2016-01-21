package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.log4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Created by tjin on 2015-11-22.
 */
public class Product extends ProductBase{
    public static Logger logger= Logger.getLogger(Product.class);

    private StringProperty texture;
    private IntegerProperty totalFeet;


    public Product(ProductBuilder builder){
        super(builder.productId, builder.totalNum, builder.unitPrice, builder.piecesPerBox, builder.size, builder.sizeNumeric);
        this.texture = new SimpleStringProperty(builder.texture);
        this.totalFeet = new SimpleIntegerProperty(builder.totalFeet);
        this.totalFeet.bind(totalNumProperty().multiply(piecePerBoxProperty()).multiply(sizeNumericProperty()));
    }

    public static class ProductBuilder{
        private String productId;
        private int totalNum;
        private float unitPrice;
        private String texture = null;
        private String size = null;
        private int piecesPerBox;
        private int sizeNumeric;
        private int totalFeet;

        public ProductBuilder productId(String productId){
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
        public ProductBuilder sizeNumeric(int sizeNumeric){
            this.sizeNumeric = sizeNumeric;
            return this;
        }
        public ProductBuilder totalFeet(int totalFeet){
            this.totalFeet = totalFeet;
            return this;
        }
        public ProductBuilder piecesPerBox(int piecesPerBox){
            this.piecesPerBox = piecesPerBox;
            return this;
        }

        public Product build(){
            return new Product(this);
        }
    }
    public String getTexture() {
        return texture.get();
    }

    public void setTexture(String texture) {
        this.texture.set(texture);
    }

    public int getTotalFeet() {
        return totalFeet.get();
    }

    public void setTotalFeet(int totalFeet) {
        this.totalFeet.set(totalFeet);
    }

    public Object[] getAllProperties(){
        return (new Object[]{getProductId(), getTexture(), getTotalNum(), getUnitPrice(), getPiecePerBox(), getSize(), getSizeNumeric()});
    }

    public Object[] getAllPropertiesForUpdate(){
        return (new Object[]{ getTexture(), getTotalNum(), getUnitPrice(), getPiecePerBox(),getSize(), getSizeNumeric() ,getProductId()});
    }

}
