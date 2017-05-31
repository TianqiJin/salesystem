/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package util;

import Constants.Constant;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

/**
 * Created by tjin on 2/9/2016.
 */
public class AlertBuilder{
    private Logger logger = Logger.getLogger(AlertBuilder.class);
    private static Alert alert;

    public AlertBuilder(){
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle(null);
        alert.setContentText(null);
        alert.setTitle(null);
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(AlertBuilder.class.getResourceAsStream(Constant.Image.appIconPath)));
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(this.getClass().getResource("/css/bootstrap3.css").toExternalForm());
    }

    public AlertBuilder alertType(Alert.AlertType type){
        alert.setAlertType(type);
        return this;
    }
    public AlertBuilder alertHeaderText(String headText){
        alert.setHeaderText(headText);
        return this;
    }
    public AlertBuilder alertContentText(String contentText){
        alert.setContentText(contentText);
        return this;
    }
    public AlertBuilder alertTitle(String title){
        alert.setTitle(title);
        return this;
    }
    public AlertBuilder alertButton(ButtonType...buttonTypes){
        alert.getButtonTypes().setAll(buttonTypes);
        return this;
    }

    public Alert build(){
        return alert;
    }
}
