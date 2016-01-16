package Controllers;

import MainClass.SaleSystem;
import db.DBExecuteProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Created by tjin on 1/14/2016.
 */
public class PropertySettingDialogController {
    private Stage dialogStage;
    private SaleSystem saleSystem;
    private StringBuffer errorMsg;

    @FXML
    private TextField productWarnLimitField;
    @FXML
    private TextField taxRateField;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;

    @FXML
    private void initialize(){}
    @FXML
    public void handleConfirmButton(){
        if(isInputValid()){
            if(!productWarnLimitField.getText().trim().isEmpty()){
                saleSystem.setProductLimit(Integer.valueOf(productWarnLimitField.getText()));
            }
            if(!taxRateField.getText().trim().isEmpty()){
                saleSystem.setTaxRate(Integer.valueOf(taxRateField.getText()));
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

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }
    public void setMainClass(SaleSystem saleSystem) {
        this.saleSystem = saleSystem;
        productWarnLimitField.setText(String.valueOf(this.saleSystem.getProductWarnLimit()));
        taxRateField.setText(String.valueOf(this.saleSystem.getTaxRate()));
    }

    private boolean isInputValid(){
        errorMsg = new StringBuffer();
        if(!productWarnLimitField.getText().trim().isEmpty()){
            if(!isProductWarnLimitValid()){
                errorMsg.append("Product Warn Limit must be an integer!\n");
            }
        }
        if(!taxRateField.getText().trim().isEmpty()){
            if(!isTaxRateValid()){
                errorMsg.append("Tax Rate must be an integer!\n");
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

            return false;
        }
        return true;
    }
    private boolean isTaxRateValid(){
        try{
            Integer.parseInt(taxRateField.getText().trim());
        }catch(NumberFormatException e){

            return false;
        }
        return true;
    }
}
