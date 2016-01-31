package Controllers;

import MainClass.SaleSystem;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Customer;
import model.Product;
import model.Staff;
import model.Transaction;

/**
 * Created by tjin on 12/2/2015.
 */
public class ProductEditDialogController {
    @FXML
    private TextField productIdField;
    @FXML
    private TextField textureField;
    @FXML
    private TextField unitPriceField;
    @FXML
    private TextField lengthField;
    @FXML
    private TextField widthField;
    @FXML
    private TextField piecesPerBoxField;


    private Stage dialogStage;
    private Product product;
    private String errorMsg;
    private boolean okClicked;
    private SaleSystem saleSystem;

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
        if(product.getProductId() != null){
            productIdField.setText(String.valueOf(product.getProductId()));
            textureField.setText(product.getTexture());
            unitPriceField.setText(String.valueOf(product.getUnitPrice()));
            piecesPerBoxField.setText(String.valueOf(product.getPiecesPerBox()));
            String[] sizeArray = product.getSize().split("\\*");
            lengthField.setText(sizeArray[0]);
            widthField.setText(sizeArray[1]);
        }
        else{
            productIdField.setText("");
            textureField.setText("");
            unitPriceField.setText("");
            piecesPerBoxField.setText("");
            lengthField.setText("");
            widthField.setText("");
        }

    }
    public void handleOk(){
        if(isInputValid()){
            product.setProductId(productIdField.getText());
            product.setTexture(textureField.getText());
            product.setUnitPrice(Float.valueOf(unitPriceField.getText()));
            product.setSize(lengthField.getText() + "*" + widthField.getText());
            product.setPiecesPerBox(Integer.parseInt(piecesPerBoxField.getText()));
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
    public void handleCancel(){
        dialogStage.close();
    }

    public boolean isOKClicked(){
        return okClicked;
    }

    public void setMainClass(SaleSystem saleSystem) {
        this.saleSystem = saleSystem;
        if(!this.saleSystem.getStaff().getPosition().equals(Staff.Position.MANAGER)){
            unitPriceField.setDisable(true);
        }
    }

    private boolean isInputValid(){
        if(productIdField.getText() == null || productIdField.getText().length() == 0){
            errorMsg += "ProductID should not be empty! \n";
        }
        try{
            Integer.parseInt(lengthField.getText());
        }catch (NumberFormatException e){
            errorMsg += "Product Length must be integer!";
        }
        try{
            Integer.parseInt(widthField.getText());
        }catch (NumberFormatException e){
            errorMsg += "Product Width must be integer!";
        }
        if(errorMsg.length() == 0){
            return true;
        }
        return false;
    }
}
