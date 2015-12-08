package MainClass;

import Controllers.CustomerEditDialogController;
import Controllers.CustomerOverviewController;
import Controllers.OverviewController;
import db.DBExecuteStaff;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.Customer;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by tjin on 2015-11-21.
 */
public class SaleSystem extends Application{
    private static Logger logger= Logger.getLogger(SaleSystem.class);
    private Stage primaryStage;
    private BorderPane rootLayout;
    private static int state=0;
    @FXML
    public TabPane tabPane;

    private String[] tabList = {"Customer", "Product","Transaction"};
    private String[] tabList_High = {"Customer", "Product","Transaction","Staff"};
    private Map<String, OverviewController> tabControllerMap = new HashMap<>();

    public static void main(String []args){
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        loginPage();
        if (state!=0){
        showMainLayOut(primaryStage);
        }
    }

    public void showMainLayOut(Stage primaryStage){
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Sales System");
        initRootLayout();
        initTab();
    }


    public void initTab(){
        switch (state){
            case 1:
                for (String tab : tabList_High) {
                    tabPane.getTabs().add(new Tab(tab));
                }
                break;
            case 2:
                for (String tab : tabList) {
                    tabPane.getTabs().add(new Tab(tab));
                }
                break;
        }

        tabPane.getSelectionModel().clearSelection();
        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                if (newValue.getContent() == null) {
                    try {
                        FXMLLoader fXMLLoader = new FXMLLoader();
                        Parent root = fXMLLoader.load(this.getClass().getResource("/fxml/" + newValue.getText() + "Overview.fxml").openStream());
                        newValue.setContent(root);
                        OverviewController controller = fXMLLoader.getController();
                        controller.loadDataFromDB();
                        controller.setMainClass(SaleSystem.this);
                        tabControllerMap.put(newValue.getText(), controller);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    //Parent root = (Parent)newValue.getContent();
                    OverviewController controller = tabControllerMap.get(newValue.getText());
                    controller.loadDataFromDB();
                }
            }
        });
        tabPane.getSelectionModel().selectFirst();

    }

    public void initRootLayout(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/RootLayout.fxml"));
            rootLayout = loader.load();
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
            for (Node node: rootLayout.getChildren()){
                if (node instanceof TabPane){
                    tabPane = (TabPane)node;
                    break;
                }
            }
        }catch(IOException e){
            logger.error(e.getMessage());
        }
    }
    public void showCustomerOverview(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/CustomerOverview.fxml"));
            AnchorPane customerOverview = loader.load();
            rootLayout.setCenter(customerOverview);
            CustomerOverviewController controller = loader.getController();
            controller.loadDataFromDB();
        }catch(IOException e){
            logger.error(e.getMessage());
        }
    }

    public boolean showCustomerEditDialog(Customer customer){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/CustomerEditDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Customer");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            CustomerEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            controller.setTextField(customer);

            dialogStage.showAndWait();
            return controller.isOKClicked();
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }


    public void loginPage(){
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        dialog.setHeaderText("Welcome!!!Dumbass!!");
        dialog.setGraphic(new ImageView(this.getClass().getResource("/pics/login.jpg").toString()));

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> username.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();



        result.ifPresent(usernamePassword -> {
            System.out.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());
            boolean[] auth_result = DBExecuteStaff.verification(usernamePassword.getKey(),usernamePassword.getValue());
            if (!auth_result[0]){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("You Shall NOT Pass!");
                alert.setHeaderText("Login Failed");
                alert.setContentText("UserName/Password is incorrect!!");
                alert.showAndWait();
                loginPage();
            }else{
                if (auth_result[1]){
                    this.state = 1;
                }else {
                    this.state = 2;
                }
                System.out.println(state);
            }
        });

    }

}
