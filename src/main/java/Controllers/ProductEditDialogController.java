package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Customer;
import model.Product;

/**
 * Created by tjin on 12/2/2015.
 */
public class ProductEditDialogController {
    @FXML
    private TextField productIdField;
    @FXML
    private TextField textureField;
    @FXML
    private TextField sizeField;
    @FXML
    private TextField unitPriceField;


    private Stage dialogStage;
    private Product product;
    private String errorMsg;
    private boolean okClicked;

    @FXML
    private void initialize(){}

    public ProductEditDialogController(){
        errorMsg = "";
        okClicked = false;
    }
    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }
    public void setTextField(Product product){
        this.product = product;
        productIdField.setText(String.valueOf(product.getProductId()));
        textureField.setText(product.getTexture());
        sizeField.setText(product.getSize());
        unitPriceField.setText(String.valueOf(product.getUnitPrice()));

    }
    public void handleOk(){
        if(isInputValid()){
            product.setProductId(Integer.valueOf(productIdField.getText()));
            product.setTexture(textureField.getText());
            product.setSize(sizeField.getText());
            product.setUnitPrice(Float.valueOf(unitPriceField.getText()));

            okClicked = true;
            dialogStage.close();
        }
        else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct the invalid fields");
            alert.setContentText(errorMsg);
            alert.showAndWait();
        }
    }
    public void handleCancle(){
        dialogStage.close();
    }

    public boolean isOKClicked(){
        return okClicked;
    }

    private boolean isInputValid(){
        if(productIdField.getText() == null || productIdField.getText().length() == 0){
            errorMsg += "ProductID should not be empty! \n";
        }
        if(errorMsg.length() == 0){
            return true;
        }
        return false;
    }
}
