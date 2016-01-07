package MainClass;

import Controllers.*;
import db.DBExecuteStaff;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import model.Product;
import model.Staff;
import model.Transaction;
import org.apache.log4j.Logger;
import util.PropertiesSys;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by tjin on 2015-11-21.
 */
public class SaleSystem extends Application{
    private static Logger logger= Logger.getLogger(SaleSystem.class);
    private static String productLimit = PropertiesSys.properties.getProperty("product_limit");
    private Stage primaryStage;
    private BorderPane rootLayout;
    private static int state=0;
    private static int staffId;
    @FXML
    public TabPane tabPane;
    @FXML
    public MenuBar menuBar;

    private String[] tabList = {"Customer", "Product","Transaction"};
    private String[] tabList_High = {"Customer", "Product","Transaction","Staff"};
    private Map<String, OverviewController> tabControllerMap = new HashMap<>();

    public static void main(String []args){
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        showLoginDialog();
        if (state!=0){
            showMainLayOut(primaryStage);
        }
    }

    public void showMainLayOut(Stage primaryStage){
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Sales System");
        initRootLayout();
        initMenuBar();
        initTab();
    }

    public void initMenuBar(){
        Menu menuReport = new Menu("Report");
        Menu menuEdit = new Menu("Edit");
        Menu menuHelp = new Menu("Help");
        MenuItem generateReportMenuItem = new MenuItem("Generate Report");
        MenuItem settingsMenuItem = new MenuItem("Settings");
        MenuItem aboutMenuItem = new MenuItem("About");
        menuReport.getItems().add(generateReportMenuItem);
        menuEdit.getItems().add(settingsMenuItem);
        menuHelp.getItems().add(aboutMenuItem);
        menuBar.getMenus().add(menuReport);
        menuBar.getMenus().add(menuEdit);
        menuBar.getMenus().add(menuHelp);

        generateReportMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showPDFGenerateDialog();
            }
        });
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
                }else if(node instanceof MenuBar){
                    menuBar = (MenuBar)node;
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

    public boolean showProductEditDialog(Product product){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/ProductEditDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Product");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            ProductEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            controller.setTextField(product);

            dialogStage.showAndWait();
            return controller.isOKClicked();
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean showStaffEditDialog(Staff staff){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/StaffEditDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Staff");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            StaffEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            controller.setTextField(staff);

            dialogStage.showAndWait();
            return controller.isOKClicked();
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }

    public void showLoginDialog(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/LoginDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Login Dialog");
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            LoginDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            this.state = controller.returnState();
            this.staffId = controller.returnStaffId();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void showPDFGenerateDialog(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/GeneratePDFReportDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Generate Report");
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.initOwner(primaryStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            GeneratePDFReportDialog controller = loader.getController();
            System.out.println(controller);
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public Transaction showGenerateCustomerTransactionDialog(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/GenerateCustomerTransaction.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Create Customer Transaction");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            GenerateCustomerTransactController controller = loader.getController();
            controller.setMainClass(SaleSystem.this);
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            if(controller.isConfirmedClicked()){
                return controller.returnNewTrasaction();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public int getStaffId(){
        return this.staffId;
    }

    public static String getProductLimit() {
        return productLimit;
    }

    public static void setProductLimit(String productLimit) {
        SaleSystem.productLimit = productLimit;
        PropertiesSys.properties.setProperty("product_limit", SaleSystem.productLimit);
        //TODO: write property into file
    }
    //TODO: set DialogStage for each tab?

}
