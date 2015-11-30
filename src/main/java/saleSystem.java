import Controllers.CustomerOverviewController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by tjin on 2015-11-21.
 */
public class SaleSystem extends Application{
    private static Logger logger= Logger.getLogger(SaleSystem.class);
    private Stage primaryStage;
    private BorderPane rootLayout;

    public static void main(String []args){
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Sales System");

        initRootLayout();
        showCustomerOverview();
    }
    public void initRootLayout(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/RootLayout.fxml"));
            rootLayout = loader.load();
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
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
}
