package Controllers;

import MainClass.SaleSystem;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Product;
import model.Staff;
import org.apache.commons.lang3.StringUtils;
import util.AlertBuilder;

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
    @FXML
    private TextField displayNameField;


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
            displayNameField.setText(product.getDisplayName());
        }
        else{
            productIdField.setText("");
            textureField.setText("");
            unitPriceField.setText("");
            piecesPerBoxField.setText("");
            lengthField.setText("");
            widthField.setText("");
            displayNameField.setText("");
        }

    }
    public void handleOk(){
        if(isInputValid()){
            product.setProductId(productIdField.getText());
            product.setTexture(textureField.getText());
            product.setUnitPrice(Float.valueOf(unitPriceField.getText()));
            product.setSize(lengthField.getText() + "*" + widthField.getText());
            product.setPiecesPerBox(Integer.parseInt(piecesPerBoxField.getText()));
            if(StringUtils.isBlank(displayNameField.getText())){
                product.setDisplayName(productIdField.getText());
            }else{
                product.setDisplayName(displayNameField.getText());
            }
            okClicked = true;
            dialogStage.close();
        }
        else{
            new AlertBuilder()
                    .alertType(Alert.AlertType.WARNING)
                    .alertHeaderText("Please correct the invalid fields")
                    .alertTitle("Invalid Fields")
                    .alertContentText(errorMsg)
                    .build()
                    .showAndWait();
        }
    }
    public void handleCancel(){
        dialogStage.close();
    }

    public boolean isOKClicked(){
        return okClicked;
    }

    public void setMainClass(SaleSystem saleSystem, boolean isEditClicked) {
        this.saleSystem = saleSystem;
        if(!this.saleSystem.getStaff().getPosition().equals(Staff.Position.MANAGER) && isEditClicked){
            unitPriceField.setDisable(true);
        }
        if(isEditClicked){
            productIdField.setDisable(true);
        }
    }

    private boolean isInputValid(){
        errorMsg  = "";
        if(productIdField.getText() == null || productIdField.getText().length() == 0){
            errorMsg += "ProductID should not be empty! \n";
        }
        try{
            Double.parseDouble(lengthField.getText());
        }catch (NumberFormatException e){
            errorMsg += "Product Length must be number! \n";
        }
        try{
            Double.parseDouble(widthField.getText());
        }catch (NumberFormatException e){
            errorMsg += "Product Width must be number! \n";
        }
        try{
            Float.parseFloat(unitPriceField.getText());
        }catch (NumberFormatException e){
            errorMsg += "Unit Price must be number! \n";
        }
        try{
            Integer.parseInt(piecesPerBoxField.getText());
        }catch (NumberFormatException e){
            errorMsg += "Pieces per box must be integer! \n";
        }
        if(errorMsg.length() == 0){
            return true;
        }
        return false;
    }
}
