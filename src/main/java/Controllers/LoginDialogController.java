package Controllers;

import Constants.Constant;
import db.DBExecute;
import db.DBExecuteStaff;
import db.DBQueries;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Staff;
import org.apache.log4j.Logger;
import util.AlertBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by tjin on 12/8/2015.
 */
public class LoginDialogController {
    private static Logger logger= Logger.getLogger(LoginDialogController.class);
    private Stage dialogStage;
    private int state;
    private List<Staff> returnedStaff;
    private DBExecuteStaff dbExecute;
    private Executor executor;

    @FXML
    private TextField userNameField;
    @FXML
    private PasswordField passwordField;
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

        executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    @FXML
    public void handleLogin(){
        loginButton.setDisable(true);
        String userName = userNameField.getText();
        String password = passwordField.getText();

        Task<boolean[]> staffVerifyTask = new Task<boolean[]>() {
            @Override
            protected boolean[] call() throws Exception {
                return DBExecuteStaff.verification(userName,password);
            }
        };

        final boolean[][] auth_result = new boolean[1][1];
        staffVerifyTask.setOnSucceeded(event -> {
            auth_result[0] = staffVerifyTask.getValue();
            if (!auth_result[0][0]){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("You Shall NOT Pass!");
                alert.setHeaderText("Login Failed");
                alert.setContentText("UserName/Password is incorrect!!");
                alert.showAndWait();
                loginButton.setDisable(false);
            }else{
                if (auth_result[0][1]){
                    this.state = 1;
                }else {
                    this.state = 2;
                }
                try{
                    returnedStaff = dbExecute.selectFromDatabase(DBQueries.SelectQueries.Staff.SELECT_USERNAME_STAFF,
                            userName);
                }catch(SQLException e){
                    logger.error(e.getMessage(), e);
                    new AlertBuilder()
                            .alertType(Alert.AlertType.ERROR)
                            .alertContentText("Unable to grab data from database!\n" + e.getMessage())
                            .alertHeaderText(Constant.DatabaseError.databaseErrorAlertTitle)
                            .alertTitle("Database Error")
                            .build()
                            .showAndWait();
                }
                dialogStage.close();
            }}
        );
        staffVerifyTask.setOnFailed(event -> new AlertBuilder()
                .alertType(Alert.AlertType.ERROR)
                .alertContentText(event.getSource().exceptionProperty().getValue().toString())
                .alertHeaderText(Constant.DatabaseError.databaseErrorAlertTitle)
                .build()
                .showAndWait());

        executor.execute(staffVerifyTask);
    }

    @FXML
    public void handleCancel(){
        dialogStage.close();
    }

    public LoginDialogController() {
        this.state = 0;
        this.dbExecute = new DBExecuteStaff();
    }

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

    public int returnState(){
        return this.state;
    }

    public Staff returnStaff(){
        if(returnedStaff != null){
            return this.returnedStaff.get(0);
        }
        return null;
    }
}
