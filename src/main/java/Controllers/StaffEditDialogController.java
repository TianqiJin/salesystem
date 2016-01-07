package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
    private TextField staffIdField;
    @FXML
    private TextField userNameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField positionField;
    @FXML
    private TextField locationField;


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
        staffIdField.setText(String.valueOf(staff.getStaffId()));
        //staffIdField.setEditable(false);
        userNameField.setText(staff.getUserName());
        passwordField.setText(staff.getPassword());
        fullNameField.setText(staff.getFullName());
        if (staff.getPosition()==null){
            positionField.setText("");
            locationField.setText("");
        }else{
            positionField.setText(staff.getPosition().name());
            locationField.setText(staff.getLocation().name());
        }

    }
    public void handleOk(){
        if(isInputValid()){
            staff.setStaffId(Integer.valueOf(staffIdField.getText()));
            staff.setUserName(userNameField.getText());
            staff.setPassword(passwordField.getText());
            staff.setFullName(fullNameField.getText());
            staff.setPosition(Staff.Position.getPosition(positionField.getText()));
            staff.setLocation(Staff.Location.getLocation(locationField.getText()));

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
        if(staffIdField.getText()==null | Integer.valueOf(staffIdField.getText())==0){
            errorMsg += "Unable to retrieve newest StaffId \n";
        }
        if(userNameField.getText() == null || userNameField.getText().length() == 0){
            errorMsg += "UserName should not be empty! \n";
        }
        if(passwordField.getText() == null || passwordField.getText().length() == 0){
            errorMsg += "Password should not be empty! \n";
        }
        if(fullNameField.getText() == null || fullNameField.getText().length() == 0){
            errorMsg += "FullName should not be empty! \n";
        }
        if(positionField.getText() == null || positionField.getText().length() == 0){
            errorMsg += "Position should not be empty! \n";
        }
        if(locationField.getText() == null || locationField.getText().length() == 0){
            errorMsg += "Location should not be empty! \n";
        }


        if(errorMsg.length() == 0){
            return true;
        }
        return false;
    }
}
