package Controllers;

import Constants.Constant;
import MainClass.SaleSystem;
import PDF.InvoiceGenerator;
import db.DBExecuteProperty;
import db.DBQueries;
import db.ObjectSerializer;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Property;
import org.apache.log4j.Logger;
import util.AlertBuilder;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by tjin on 1/14/2016.
 */
public class PropertySettingDialogController {
    private static Logger logger = Logger.getLogger(PropertySettingDialogController.class);
    private Stage dialogStage;
    private SaleSystem saleSystem;
    private StringBuffer errorMsg;
    private DBExecuteProperty dbExecuteProperty;

    @FXML
    private TextField productWarnLimitField;
    @FXML
    private TextField gstField;
    @FXML
    private TextField pstField;
    @FXML
    private TextField gstNumField;
    @FXML
    private TextField userClassAField;
    @FXML
    private TextField userClassBField;
    @FXML
    private TextField userClassCField;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;

    @FXML
    private void initialize(){}
    @FXML
    public void handleConfirmButton(){
        if(isInputValid()){
            Property tmpProperty = saleSystem.getProperty();
            if(!productWarnLimitField.getText().trim().isEmpty()){
                tmpProperty.setProductWarnLimit(Integer.valueOf(productWarnLimitField.getText()));
            }
            if(!pstField.getText().trim().isEmpty()){
                tmpProperty.setPstRate(Integer.valueOf(pstField.getText()));
            }
            if(!gstField.getText().trim().isEmpty()){
                tmpProperty.setGstRate(Integer.valueOf(gstField.getText()));
            }
            if(!gstNumField.getText().trim().isEmpty()){
                tmpProperty.setGstNumber(gstNumField.getText());
            }
            if(!userClassAField.getText().trim().isEmpty()){
                tmpProperty.getUserClass().setClassA(Integer.valueOf(userClassAField.getText()));
            }
            if(!userClassBField.getText().trim().isEmpty()){
                tmpProperty.getUserClass().setClassB(Integer.valueOf(userClassBField.getText()));
            }
            if(!userClassCField.getText().trim().isEmpty()){
                tmpProperty.getUserClass().setClassC(Integer.valueOf(userClassCField.getText()));
            }
            try{
                dbExecuteProperty.updateDatabase(DBQueries.UpdateQueries.Property.UPDATE_PROPERTY, ObjectSerializer.PROPERTY_OBJECT_SERIALIZER.serialize(tmpProperty));
            }catch(SQLException e){
                logger.error(e.getMessage());
                new AlertBuilder()
                        .alertType(Alert.AlertType.ERROR)
                        .alertContentText(Constant.DatabaseError.databaseUpdateError + e.toString())
                        .build()
                        .showAndWait();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
            dialogStage.close();
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR, errorMsg.toString());
            alert.showAndWait();
        }
    }

    @FXML
    public void handleCancelButton(){
        this.dialogStage.close();
    }

    public PropertySettingDialogController(){
        dbExecuteProperty = new DBExecuteProperty();
    }

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
        this.dialogStage.setResizable(false);
    }

    public void setMainClass(SaleSystem saleSystem) {
        this.saleSystem = saleSystem;
        productWarnLimitField.setText(String.valueOf(this.saleSystem.getProperty().getProductWarnLimit()));
        gstField.setText(String.valueOf(this.saleSystem.getProperty().getGstRate()));
        pstField.setText(String.valueOf(this.saleSystem.getProperty().getPstRate()));
        gstNumField.setText(this.saleSystem.getProperty().getGstNumber());
        userClassAField.setText(String.valueOf(this.saleSystem.getProperty().getUserClass().getClassA()));
        userClassBField.setText(String.valueOf(this.saleSystem.getProperty().getUserClass().getClassB()));
        userClassCField.setText(String.valueOf(this.saleSystem.getProperty().getUserClass().getClassC()));
    }

    private boolean isInputValid(){
        errorMsg = new StringBuffer();
        if(!productWarnLimitField.getText().trim().isEmpty()){
            if(!isProductWarnLimitValid()){
                errorMsg.append("Product Warn Limit must be an integer!\n");
            }
        }
        if(!gstField.getText().trim().isEmpty()){
            if(!isTaxRateValid(gstField.getText())){
                errorMsg.append("GST Tax Rate must be an integer!\n");
            }
        }
        if(!pstField.getText().trim().isEmpty()){
            if(!isTaxRateValid(pstField.getText())){
                errorMsg.append("PST Tax Rate must be an integer!\n");
            }
        }
        if(!userClassAField.getText().trim().isEmpty()){
            if(!isUserClassValid(userClassAField.getText())){
                errorMsg.append("User Class A value is invalid!\n");
            }
        }
        if(!userClassBField.getText().trim().isEmpty()){
            if(!isUserClassValid(userClassBField.getText())){
                errorMsg.append("User Class B value is invalid!\n");
            }
        }
        if(!userClassCField.getText().trim().isEmpty()){
            if(!isUserClassValid(userClassCField.getText())){
                errorMsg.append("User Class C value is invalid!\n");
            }
        }
        if(errorMsg.length() != 0){
            return false;
        }
        return true;
    }
    private boolean isProductWarnLimitValid(){
        try{
            Integer.parseInt(productWarnLimitField.getText().trim());
        }catch(NumberFormatException e){
            logger.error(e.getMessage());
            return false;
        }
        return true;
    }
    private boolean isTaxRateValid(String tax){
        try{
            Integer.parseInt(tax.trim());
        }catch(NumberFormatException e){
            logger.error(e.getMessage());
            return false;
        }
        return true;
    }
    private boolean isUserClassValid(String value){
        try{
            Integer.parseInt(value.trim());
        }catch(NumberFormatException e){
            logger.error(e.getMessage());
            return false;
        }
        if(Integer.parseInt(value.trim()) > 100 || Integer.parseInt(value.trim()) < 0){
            return false;
        }
        return true;
    }
    //TODO: Move setGSt and setPST into this class
}
