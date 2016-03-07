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
    private TextField gstField;
    @FXML
    private TextField pstField;
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
            if(!pstField.getText().trim().isEmpty()){
                saleSystem.setPstRate(Integer.valueOf(pstField.getText()));
            }
            if(!gstField.getText().trim().isEmpty()){
                saleSystem.setGstRate(Integer.valueOf(gstField.getText()));
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
        gstField.setText(String.valueOf(this.saleSystem.getGstRate()));
        pstField.setText(String.valueOf(this.saleSystem.getPstRate()));
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
    private boolean isTaxRateValid(String tax){
        try{
            Integer.parseInt(tax.trim());
        }catch(NumberFormatException e){

            return false;
        }
        return true;
    }

    //TODO: Move setGSt and setPST into this class
}
