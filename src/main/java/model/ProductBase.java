package model;

import com.sun.scenario.effect.FloatMap;
import com.sun.scenario.effect.Flood;
import javafx.beans.property.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;

/**
 * Created by tjin on 12/13/2015.
 */
public abstract class ProductBase {
    private StringProperty productId;
    private IntegerProperty totalNum;
    private FloatProperty unitPrice;
    private IntegerProperty piecePerBox;
    private StringProperty size;
    private IntegerProperty sizeNumeric;

    public ProductBase(Object... params){
        if (params[0]!=null) {
            this.productId = new SimpleStringProperty((String) params[0]);
            this.totalNum = new SimpleIntegerProperty((Integer) params[1]);
            this.unitPrice = new SimpleFloatProperty((Float) params[2]);
            this.piecePerBox = new SimpleIntegerProperty((Integer) params[3]);
            this.size = new SimpleStringProperty((String) params[4]);
            this.sizeNumeric = new SimpleIntegerProperty((Integer) params[5]);
        }else{
            this.productId = new SimpleStringProperty(null);
            this.totalNum = new SimpleIntegerProperty(0) ;
            this.unitPrice = new SimpleFloatProperty(0);
            this.piecePerBox = new SimpleIntegerProperty(0);
            this.size = new SimpleStringProperty(null);
            this.sizeNumeric = new SimpleIntegerProperty(0);
        }
    }

    public String getProductId() {
        return productId.get();
    }

    public void setProductId(String productId) {
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


    public StringProperty productIdProperty() {
        return productId;
    }

    public IntegerProperty totalNumProperty() {
        return totalNum;
    }

    public int getPiecePerBox() {
        return piecePerBox.get();
    }
 
    public IntegerProperty piecePerBoxProperty() {
        return piecePerBox;
    }

    public void setPiecePerBox(int piecePerBox) {
        this.piecePerBox.set(piecePerBox);
    }

    public String getSize() {
        return size.get();
    }

    public StringProperty sizeProperty() {
        return size;
    }

    public void setSize(String size) {
        this.size.set(size);
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            this.sizeNumeric = new SimpleIntegerProperty(Integer.valueOf(engine.eval(size).toString()));
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public int getSizeNumeric() {
        return sizeNumeric.get();
    }

    public IntegerProperty sizeNumericProperty() {
        return sizeNumeric;
    }
}
