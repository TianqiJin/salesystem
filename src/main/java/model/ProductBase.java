package model;

import com.sun.scenario.effect.FloatMap;
import com.sun.scenario.effect.Flood;
import javafx.beans.property.*;
import org.apache.log4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Created by tjin on 12/13/2015.
 */
public abstract class ProductBase {

    private static Logger logger= Logger.getLogger(ProductBase.class);
    private StringProperty productId;
    private FloatProperty totalNum;
    private FloatProperty unitPrice;
    private IntegerProperty piecesPerBox;
    private StringProperty size;
    private StringProperty displayName;
    private FloatProperty sizeNumeric;

    public ProductBase(Object... params){
        if (params[0] != null) {
            this.productId = new SimpleStringProperty((String) params[0]);
            this.totalNum = new SimpleFloatProperty((Float) params[1]);
            this.unitPrice = new SimpleFloatProperty((Float) params[2]);
            this.piecesPerBox = new SimpleIntegerProperty((Integer) params[3]);
            this.size = new SimpleStringProperty((String) params[4]);
            this.sizeNumeric = new SimpleFloatProperty((Float) params[5]);
            this.displayName = new SimpleStringProperty((String) params[6]);
        }else{
            this.productId = new SimpleStringProperty(null);
            this.totalNum = new SimpleFloatProperty(0) ;
            this.unitPrice = new SimpleFloatProperty(0);
            this.piecesPerBox = new SimpleIntegerProperty(0);
            this.size = new SimpleStringProperty(null);
            this.sizeNumeric = new SimpleFloatProperty(0);
            this.displayName = new SimpleStringProperty(null);
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

    public float getTotalNum() {
        return totalNum.get();
    }

    public void setTotalNum(float totalNum) {
        this.totalNum.set(totalNum);
    }


    public StringProperty productIdProperty() {
        return productId;
    }

    public FloatProperty totalNumProperty() {
        return totalNum;
    }

    public int getPiecesPerBox() {
        return piecesPerBox.get();
    }
 
    public IntegerProperty piecesPerBoxProperty() {
        return piecesPerBox;
    }

    public void setPiecesPerBox(int piecePerBox) {
        this.piecesPerBox.set(piecePerBox);
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
            this.sizeNumeric = new SimpleFloatProperty(new BigDecimal(engine.eval(size).toString()).setScale(2, RoundingMode.HALF_EVEN).floatValue());
        } catch (ScriptException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public String getDisplayName() {
        return displayName.get();
    }

    public StringProperty displayNameProperty() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName.set(displayName);
    }

    public float getSizeNumeric() {
        return sizeNumeric.get();
    }

    public FloatProperty sizeNumericProperty() {
        return sizeNumeric;
    }
}
