package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Customer;
import model.Staff;

/**
 * Created by tjin on 12/2/2015.
 */
public class StaffEditDialogController {
    @FXML
    private TextField userNameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField fullNameField;
    @FXML
    private ChoiceBox<String> positionChoiceBox;
    @FXML
    private TextField streetField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField postalCodeField;
    @FXML
    private TextField phoneField;

    private Stage dialogStage;
    private Staff staff;
    private String errorMsg;
    private boolean okClicked;

    @FXML
    private void initialize(){}

    public StaffEditDialogController(){
        errorMsg = "";
        okClicked = false;
    }

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }
    public void setTextField(Staff staff){
        this.staff = staff;
        userNameField.setText(staff.getUserName());
        passwordField.setText(staff.getPassword());
        fullNameField.setText(staff.getFullName());
        streetField.setText(staff.getStreet());
        cityField.setText(staff.getCity());
        postalCodeField.setText(staff.getPostalCode());
        phoneField.setText(staff.getPhone());
        if (staff.getPosition()==null){
            positionChoiceBox.getSelectionModel().selectFirst();
        }else{
            positionChoiceBox.getSelectionModel().select(staff.getPosition().name());
        }
    }
    public void handleOk(){
        if(isInputValid()){
            staff.setUserName(userNameField.getText());
            staff.setPassword(passwordField.getText());
            staff.setFullName(fullNameField.getText());
            staff.setPosition(Staff.Position.getPosition(positionChoiceBox.getSelectionModel().getSelectedItem()));
            staff.setStreet(streetField.getText());
            staff.setCity(cityField.getText());
            staff.setPostalCode(postalCodeField.getText());
            staff.setPhone(phoneField.getText());

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
        errorMsg = "";
        if(userNameField.getText() == null || userNameField.getText().length() == 0){
            errorMsg += "UserName should not be empty! \n";
        }
        if(passwordField.getText() == null || passwordField.getText().length() == 0){
            errorMsg += "Password should not be empty! \n";
        }
        if(fullNameField.getText() == null || fullNameField.getText().length() == 0){
            errorMsg += "FullName should not be empty! \n";
        }
        if(streetField.getText() == null || streetField.getText().length() == 0){
            errorMsg += "Street should not be empty! \n";
        }
        if(cityField.getText() == null || cityField.getText().length() == 0){
            errorMsg += "City should not be empty! \n";
        }
        if(postalCodeField.getText() == null || postalCodeField.getText().length() == 0){
            errorMsg += "PostalCode should not be empty! \n";
        }
        if(phoneField.getText() == null || phoneField.getText().length() == 0){
            errorMsg += "Phone should not be empty! \n";
        }

        if(errorMsg.length() == 0){
            return true;
        }
        return false;
    }
}
