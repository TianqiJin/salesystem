//import Controllers.CustomerEditDialogController;
//import Controllers.CustomerOverviewController;
//import Controllers.OverviewController;
//import Controllers.ProductOverviewController;
//import javafx.application.Application;
//import javafx.beans.value.ChangeListener;
//import javafx.beans.value.ObservableValue;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Node;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.Tab;
//import javafx.scene.control.TabPane;
//import javafx.scene.layout.AnchorPane;
//import javafx.scene.layout.BorderPane;
//import javafx.stage.Modality;
//import javafx.stage.Stage;
//import model.*;
//import org.apache.log4j.Logger;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Created by tjin on 2015-11-21.
// */
//public class saleSystem extends Application{
//    private static Logger logger= Logger.getLogger(saleSystem.class);
//    private Stage primaryStage;
//    private BorderPane rootLayout;
//    @FXML
//    public TabPane tabPane;
//
//    private String[] tabList = {"Customer", "Product","Transaction"};
//    private Map<String, OverviewController> tabControllerMap = new HashMap<>();
//
//    public static void main(String []args){
//        launch(args);
//    }
//    @Override
//    public void start(Stage primaryStage) throws Exception {
//        this.primaryStage = primaryStage;
//        this.primaryStage.setTitle("Sales System");
//
//        initRootLayout();
//        initTab();
//        //showCustomerOverview();
//    }
//
//    public void initTab(){
//        for (String tab : tabList) {
//            tabPane.getTabs().add(new Tab(tab));
//        }
//
//        tabPane.getSelectionModel().clearSelection();
//        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
//            @Override
//            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
//                if (newValue.getContent() == null) {
//                    try {
//                        FXMLLoader fXMLLoader = new FXMLLoader();
//                        Parent root = fXMLLoader.load(this.getClass().getResource("/fxml/" + newValue.getText() + "Overview.fxml").openStream());
//                        newValue.setContent(root);
//                        OverviewController controller = fXMLLoader.getController();
//                        controller.loadDataFromDB();
//                        tabControllerMap.put(newValue.getText(), controller);
//
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    }
//                } else {
//                    //Parent root = (Parent)newValue.getContent();
//                    OverviewController controller = tabControllerMap.get(newValue.getText());
//                    controller.loadDataFromDB();
//                }
//            }
//        });
//        tabPane.getSelectionModel().selectFirst();
//
//    }
//
//    public void initRootLayout(){
//        try {
//            FXMLLoader loader = new FXMLLoader();
//            loader.setLocation(getClass().getResource("/fxml/RootLayout.fxml"));
//            rootLayout = loader.load();
//            Scene scene = new Scene(rootLayout);
//            primaryStage.setScene(scene);
//            primaryStage.show();
//            for (Node node: rootLayout.getChildren()){
//                if (node instanceof TabPane){
//                    tabPane = (TabPane)node;
//                    break;
//                }
//            }
//        }catch(IOException e){
//            logger.error(e.getMessage());
//        }
//    }
//    public void showCustomerOverview(){
//        try{
//            FXMLLoader loader = new FXMLLoader();
//            loader.setLocation(saleSystem.class.getResource("/fxml/CustomerOverview.fxml"));
//            AnchorPane customerOverview = loader.load();
//            rootLayout.setCenter(customerOverview);
//            CustomerOverviewController controller = loader.getController();
//            controller.loadDataFromDB();
//        }catch(IOException e){
//            logger.error(e.getMessage());
//        }
//    }
//
//    public boolean showCustomerEditDialog(Customer customer){
//        try{
//            FXMLLoader loader = new FXMLLoader();
//            loader.setLocation(saleSystem.class.getResource("/fxml/CustomerEditDialog.fxml"));
//            AnchorPane page = loader.load();
//
//            Stage dialogStage = new Stage();
//            dialogStage.setTitle("Edit Customer");
//            dialogStage.initModality(Modality.WINDOW_MODAL);
//            dialogStage.initOwner(primaryStage);
//            Scene scene = new Scene(page);
//            dialogStage.setScene(scene);
//
//            CustomerEditDialogController controller = loader.getController();
//            controller.setDialogStage(dialogStage);
//
//            controller.setTextField(customer);
//
//            dialogStage.showAndWait();
//            return controller.isOKClicked();
//        }catch(IOException e){
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//}
