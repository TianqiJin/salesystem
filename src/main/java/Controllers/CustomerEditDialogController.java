package Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Customer;
import util.AlertBuilder;

/**
 * Created by tjin on 12/2/2015.
 */
public class CustomerEditDialogController {
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField streetField;
    @FXML
    private TextField postalCodeField;
    @FXML
    private TextField cityField;
    @FXML
    private ComboBox classField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField storeCreditField;
    @FXML
    private TextField companyField;
    @FXML
    private TextField pstNumberField;

    private Stage dialogStage;
    private Customer customer;
    private String errorMsg;
    private boolean okClicked;

    private ObservableList<String> options = FXCollections.observableArrayList("A", "B", "C");

    @FXML
    private void initialize(){}

    public CustomerEditDialogController(){
        errorMsg = "";
        okClicked = false;
    }
    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
        this.dialogStage.setResizable(false);
    }
    public void setTextField(Customer customer){
        this.customer = customer;
        firstNameField.setText(customer.getFirstName());
        lastNameField.setText(customer.getLastName());
        streetField.setText(customer.getStreet());
        postalCodeField.setText(customer.getPostalCode());
        cityField.setText(customer.getCity());
        phoneField.setText(customer.getPhone());
        classField.setItems(options);
        classField.setValue(customer.getUserClass());
        emailField.setText(customer.getEmail());
        storeCreditField.setText(String.valueOf(customer.getStoreCredit()));
        companyField.setText(customer.getCompany());
        pstNumberField.setText(customer.getPstNumber());
    }
    public void handleOk(){
        if(isInputValid()){
            customer.setFirstName(firstNameField.getText());
            customer.setLastName(lastNameField.getText());
            customer.setCity(cityField.getText());
            customer.setPostalCode(postalCodeField.getText());
            customer.setPhone(phoneField.getText());
            customer.setStreet(streetField.getText());
            customer.setUserClass(String.valueOf(classField.getValue()));
            customer.setEmail(emailField.getText());
            customer.setStoreCredit(Double.valueOf(storeCreditField.getText()));
            customer.setCompany(companyField.getText());
            customer.setPstNumber(pstNumberField.getText());

            okClicked = true;
            dialogStage.close();
        }
        else{
            new AlertBuilder()
                    .alertHeaderText("Please correct the invalid fields")
                    .alertType(Alert.AlertType.WARNING)
                    .alertTitle("Invalid Customer Fields")
                    .alertContentText(errorMsg)
                    .build()
                    .showAndWait();
        }
    }
    public void handleCancle(){
        dialogStage.close();
    }

    public boolean isOKClicked(){
        return okClicked;
    }

    private boolean isInputValid(){
        if(firstNameField.getText() == null || firstNameField.getText().length() == 0){
            errorMsg += "First Name should not be empty! \n";
        }
        if(lastNameField.getText() == null || lastNameField.getText().length() == 0){
            errorMsg += "Last Name should not be empty! \n";
        }
        try{
            Double.parseDouble(storeCreditField.getText());
        }catch (NumberFormatException e){
            errorMsg += "Store Credit must be numbers! \n";
        }
        //TODO: inpsect the validation for emailField

        if(errorMsg.length() == 0){
            return true;
        }
        return false;
    }
}
