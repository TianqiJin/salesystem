package MainClass;

import Constants.Constant;
import Controllers.*;
import db.DBExecuteProperty;
import db.DBQueries;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import org.apache.log4j.Logger;
import util.AlertBuilder;
import util.PropertiesSys;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by tjin on 2015-11-21.
 */
public class SaleSystem extends Application{
    private static Logger logger= Logger.getLogger(SaleSystem.class);
    private static String productLimit = PropertiesSys.properties.getProperty("product_limit");
    private Stage primaryStage;
    private BorderPane rootLayout;
    private static int state=0;
    private static Staff staff;
    private static Property property;
    private DBExecuteProperty dbExecuteProperty;
    private Executor executor;

    @FXML
    public TabPane tabPane;
    @FXML
    public MenuBar menuBar;

    private String[] tabList = {"Transaction", "Product", "Customer"};
    private String[] tabList_High = {"Transaction", "Product", "Customer", "Staff"};
    private Map<String, OverviewController> tabControllerMap = new HashMap<>();

    public static void main(String []args){
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.getIcons().add(new Image(SaleSystem.class.getResourceAsStream(Constant.Image.appIconPath)));
        executor = Executors.newCachedThreadPool(r->{
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        showLoginDialog();
        if (state!=0){
            loadPropertyFromDB();
            showMainLayOut(primaryStage);
            executor.execute(refreshDB());
        }
    }

    public SaleSystem(){
        dbExecuteProperty = new DBExecuteProperty();
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
        MenuItem logOutMenuItem = new MenuItem("Log out");
        menuReport.getItems().add(generateReportMenuItem);
        menuEdit.getItems().add(settingsMenuItem);
        menuHelp.getItems().add(aboutMenuItem);
        menuHelp.getItems().add(new SeparatorMenuItem());
        menuHelp.getItems().add(logOutMenuItem);
        menuBar.getMenus().add(menuReport);
        menuBar.getMenus().add(menuEdit);
        menuBar.getMenus().add(menuHelp);

        generateReportMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showPDFGenerateDialog();
            }
        });
        settingsMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showPropertySettingDialog();
            }
        });
        aboutMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                new AlertBuilder()
                        .alertType(Alert.AlertType.INFORMATION)
                        .alertContentText(Constant.CopyRight.copyRightConntent)
                        .alertTitle("About")
                        .build()
                        .showAndWait();
            }
        });
        logOutMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                primaryStage.close();
                showLoginDialog();
                if (state!=0){
                    loadPropertyFromDB();
                    showMainLayOut(primaryStage);
                }
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

    public boolean showCustomerEditDialog(Customer customer){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/CustomerEditDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.getIcons().add(new Image(SaleSystem.class.getResourceAsStream(Constant.Image.appIconPath)));
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
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean showTransactionEditDialog(Transaction transaction){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/EditTransactionDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.getIcons().add(new Image(SaleSystem.class.getResourceAsStream(Constant.Image.appIconPath)));
            dialogStage.setTitle("Edit Transaction");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            TransactionEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setSelectedTransaction(transaction);
            dialogStage.showAndWait();
            return controller.isConfirmedClicked();
        }catch (IOException e){
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean showProductEditDialog(Product product, boolean isEditClicked){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/ProductEditDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.getIcons().add(new Image(SaleSystem.class.getResourceAsStream(Constant.Image.appIconPath)));
            dialogStage.setTitle("Edit Product");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            ProductEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMainClass(SaleSystem.this, isEditClicked);
            controller.setTextField(product);

            dialogStage.showAndWait();
            return controller.isOKClicked();
        }catch(IOException e){
            logger.error(e.getMessage());
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
            dialogStage.getIcons().add(new Image(SaleSystem.class.getResourceAsStream(Constant.Image.appIconPath)));
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
            logger.error(e.getMessage());
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
            dialogStage.getIcons().add(new Image(SaleSystem.class.getResourceAsStream(Constant.Image.appIconPath)));
            dialogStage.setTitle("Login Dialog");
            page.getStylesheets().add(SaleSystem.class.getResource("/css/theme.css").toExternalForm());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            LoginDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
            this.state = controller.returnState();
            this.staff = controller.returnStaff();

        }catch(IOException e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public void showPDFGenerateDialog(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/GeneratePDFReportDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.getIcons().add(new Image(SaleSystem.class.getResourceAsStream(Constant.Image.appIconPath)));
            dialogStage.setTitle("Generate Report");
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.initOwner(primaryStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            GeneratePDFReportDialog controller = loader.getController();
            System.out.println(controller);
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            loadPropertyFromDB();

        }catch(IOException e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public void showPropertySettingDialog(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/PropertySettingDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.getIcons().add(new Image(SaleSystem.class.getResourceAsStream(Constant.Image.appIconPath)));
            dialogStage.setTitle("Property Setting");
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.initOwner(primaryStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            PropertySettingDialogController controller = loader.getController();
            controller.setMainClass(SaleSystem.this);
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

        }catch(IOException e){
            logger.error(e.getMessage());
        }
    }

    public void showInvoiceDirectoryEditDialog(Customer customer, Transaction transaction, Staff staff){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/InvoiceDirectoryEditDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.getIcons().add(new Image(SaleSystem.class.getResourceAsStream(Constant.Image.appIconPath)));
            dialogStage.setTitle("Generate Invoice");
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.initOwner(primaryStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            InvoiceDirectoryEditDialogController controller = loader.getController();
            controller.setMainClass(SaleSystem.this);
            controller.loadDataFromDB();
            controller.setCustomer(customer);
            controller.setTransaction(transaction);
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

        }catch(IOException e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public ContainerClass showGenerateCustomerTransactionDialog(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/GenerateCustomerTransaction.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.getIcons().add(new Image(SaleSystem.class.getResourceAsStream(Constant.Image.appIconPath)));
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
                return controller.returnContainer();
            }
        }catch(IOException e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Transaction showGenerateProductTransactionDialog(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/GenerateProductTransaction.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.getIcons().add(new Image(SaleSystem.class.getResourceAsStream(Constant.Image.appIconPath)));
            dialogStage.setTitle("Create Stock Transaction");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            GenerateProductTransactController controller = loader.getController();
            controller.setMainClass(SaleSystem.this);
            controller.setDialogStage(dialogStage);
            controller.loadDataFromDB();
            dialogStage.showAndWait();
            if(controller.isConfirmedClicked()){
                return controller.returnNewTrasaction();
            }
        }catch(IOException e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Transaction showGenerateReturnTransactionDialog(Transaction selectedTransaction){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/GenerateReturnTransactions.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.getIcons().add(new Image(SaleSystem.class.getResourceAsStream(Constant.Image.appIconPath)));
            dialogStage.setTitle("Create Return Transaction");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            GenerateReturnTransactController controller = loader.getController();
            controller.setMainClass(SaleSystem.this);
            controller.setDialogStage(dialogStage);
            controller.setSelectedTransaction(selectedTransaction);
            dialogStage.showAndWait();
            if(controller.isConfirmedClicked()){
                return controller.returnNewTrasaction();
            }
        }catch(IOException e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Staff getStaff(){
        return this.staff;
    }
    private void loadPropertyFromDB(){
        Task<Property> getPropertyTask = new Task<Property>() {
            @Override
            protected Property call() throws Exception {
                return dbExecuteProperty
                        .selectFirstFromDatabase(DBQueries.SelectQueries.Property.SELECT_ALL_PROPERTY);
            }
        };

        getPropertyTask.setOnFailed(event -> {
            logger.error(event.getSource().exceptionProperty().getValue());
            new AlertBuilder()
                    .alertType(Alert.AlertType.WARNING)
                    .alertContentText(Constant.DatabaseError.databaseReturnError +event.getSource().exceptionProperty().getValue())
                    .alertTitle(Constant.DatabaseError.databaseErrorAlertTitle)
                    .build()
                    .showAndWait();
        });

        getPropertyTask.setOnSucceeded(event -> {
            property = getPropertyTask.getValue();
        });
        executor.execute(getPropertyTask);
    }

//    public int getProductWarnLimit(){
//        return property.getProductWarnLimit();
//    }
//
//    public int getPstRate(){ return property.getPstRate();}
//
//    public int getGstRate(){ return property.getGstRate();}
//
//    public String getGstNum(){ return property.getGstNumber();}

    public Property getProperty(){ return property; }

//    public void setProductLimit(Integer productLimit){
//        try{
//            dbExecuteProperty.updateDatabase(DBQueries.UpdateQueries.Property.UPDATE_PRODUCT_WARN_LIMIT, productLimit);
//        }catch(SQLException e){
//            logger.error(e.getMessage());
//            new AlertBuilder()
//                    .alertType(Alert.AlertType.ERROR)
//                    .alertContentText(Constant.DatabaseError.databaseUpdateError + e.toString())
//                    .build()
//                    .showAndWait();
//        }
//        loadPropertyFromDB();
//    }

//    public void setPstRate(Integer pstRate){
//        try{
//            dbExecuteProperty.updateDatabase(DBQueries.UpdateQueries.Property.UPDATE_PST_RATE, pstRate);
//        }catch(SQLException e){
//            logger.error(e.getMessage());
//            new AlertBuilder()
//                    .alertType(Alert.AlertType.ERROR)
//                    .alertContentText(Constant.DatabaseError.databaseUpdateError + e.toString())
//                    .build()
//                    .showAndWait();
//        }
//        loadPropertyFromDB();
//    }
//
//    public void setGstRate(Integer gstRate){
//        try{
//            dbExecuteProperty.updateDatabase(DBQueries.UpdateQueries.Property.UPDATE_GST_RATE, gstRate);
//        }catch(SQLException e){
//            logger.error(e.getMessage());
//            new AlertBuilder()
//                    .alertType(Alert.AlertType.ERROR)
//                    .alertContentText(Constant.DatabaseError.databaseUpdateError + e.toString())
//                    .build()
//                    .showAndWait();
//        }
//        loadPropertyFromDB();
//    }
//
//    public void setGstNumber(String gstNumber){
//        try{
//            dbExecuteProperty.updateDatabase(DBQueries.UpdateQueries.Property.UPDATE_GST_NUMBER, gstNumber);
//        }catch(SQLException e){
//            logger.error(e.getMessage());
//            new AlertBuilder()
//                    .alertType(Alert.AlertType.ERROR)
//                    .alertContentText(Constant.DatabaseError.databaseUpdateError + e.toString())
//                    .build()
//                    .showAndWait();
//        }
//        loadPropertyFromDB();
//    }

    private Task<Void> refreshDB(){
        Task<Void> refreshDB = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int count = 0;
                while(true){
                    logger.info("Refresh DB at " + count + " minute");
                    dbExecuteProperty
                            .selectFirstFromDatabase(DBQueries.SelectQueries.Property.SELECT_ALL_PROPERTY);
                    Thread.sleep(60*1000*5);
                    count += 5;
                }
            }
        };
        return refreshDB;
    }

}
