package Controllers;

import db.DBExecuteStaff;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Created by tjin on 12/8/2015.
 */
public class LoginDialogController {
    private Stage dialogStage;
    private int state;

    @FXML
    private TextField userNameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Button loginButton;

    @FXML
    private void initialize(){
        userNameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!userNameField.getText().trim().isEmpty()){
                    loginButton.setDisable(false);
                }
                else{
                    loginButton.setDisable(true);
                }
            }
        });
    }

    @FXML
    public void handleLogin(){
        String userName = userNameField.getText();
        String password = passwordField.getText();
        System.out.println("Username=" + userName + ", Password=" + password);
        boolean[] auth_result = DBExecuteStaff.verification(userName,password);
        if (!auth_result[0]){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("You Shall NOT Pass!");
            alert.setHeaderText("Login Failed");
            alert.setContentText("UserName/Password is incorrect!!");
            alert.showAndWait();
        }else{
            if (auth_result[1]){
                this.state = 1;
            }else {
                this.state = 2;
            }
            dialogStage.close();
        }
    }

    @FXML
    public void handleCancel(){
        dialogStage.close();
    }

    public LoginDialogController() {
        this.state = 0;
    }

    public void settDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

    public int returnState(){
        return this.state;
    }
}
