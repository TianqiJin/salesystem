package MainClass;

import Constants.Constant;
import Controllers.*;
import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;
import db.DBExecuteProperty;
import db.DBQueries;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import util.AlertBuilder;
import util.PropertiesSys;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
    private HttpClient client;

    @FXML
    public TabPane tabPane;
    @FXML
    public MenuBar menuBar;

    private String[] tabList = {"Transaction", "Product", "Customer"};
    private String[] tabList_High = {"Transaction", "Product", "Customer", "Staff"};
    private String[] tabList_Dist = {"Product"};
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
        if (state != 0){
            loadPropertyFromDB();
            showMainLayOut(primaryStage);
            executor.execute(refreshDB());
            Thread threadCheckUpgrade =  new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        checkNewVersion(false);
                        try {
                            Thread.sleep(60 * 1000 * 120);
                        } catch (InterruptedException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            });
            threadCheckUpgrade.setDaemon(true);
            threadCheckUpgrade.start();
        }
    }

    public SaleSystem(){
        dbExecuteProperty = new DBExecuteProperty();
    }

    public void showMainLayOut(Stage primaryStage){
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Sales System");
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        this.primaryStage.setX(bounds.getMinX());
        this.primaryStage.setY(bounds.getMinY());
        this.primaryStage.setWidth(bounds.getWidth());
        this.primaryStage.setHeight(bounds.getHeight());
        this.client = HttpClientBuilder.create().build();
        initRootLayout();
        initMenuBar();
        initTab();
    }

    public void initMenuBar(){
        Menu menuReport = new Menu("Report");
        Menu menuEdit = new Menu("Edit");
        Menu menuHelp = new Menu("Help");
        MenuItem generateRevenueReportMenuItem = new MenuItem("Revenue Report");
        MenuItem generateTransactionReportMenuItem = new MenuItem("Transaction Report");
        MenuItem settingsMenuItem = new MenuItem("Settings");
        MenuItem aboutMenuItem = new MenuItem("About");
        MenuItem checkUpdateMenuItem = new MenuItem("Check for Update");
        MenuItem logOutMenuItem = new MenuItem("Log out");
        menuReport.getItems().add(generateRevenueReportMenuItem);
        menuReport.getItems().add(generateTransactionReportMenuItem);
        menuEdit.getItems().add(settingsMenuItem);
        menuHelp.getItems().add(aboutMenuItem);
        menuHelp.getItems().add(checkUpdateMenuItem);
        menuHelp.getItems().add(new SeparatorMenuItem());
        menuHelp.getItems().add(logOutMenuItem);
        if(state < 3){
            menuBar.getMenus().add(menuReport);
            menuBar.getMenus().add(menuEdit);
        }
        menuBar.getMenus().add(menuHelp);

        generateRevenueReportMenuItem.setOnAction(event -> showGenerateRevenueReportDialog());
        generateTransactionReportMenuItem.setOnAction(event -> showGenerateTransactionReportDialog());
        settingsMenuItem.setOnAction(event -> showPropertySettingDialog());
        aboutMenuItem.setOnAction(event -> new AlertBuilder()
                .alertType(Alert.AlertType.INFORMATION)
                .alertContentText(Constant.CopyRight.copyRightConntent)
                .alertTitle("About")
                .build()
                .showAndWait());
        checkUpdateMenuItem.setOnAction(event -> checkNewVersion(true));
        logOutMenuItem.setOnAction(event -> {
            primaryStage.close();
            showLoginDialog();
            if (state!=0){
                loadPropertyFromDB();
                showMainLayOut(primaryStage);
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
            case 3:
                for (String tab : tabList_Dist) {
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
                        logger.error(ex.getMessage(), ex);
                    }
                } else {
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
            rootLayout.prefHeightProperty().bind(scene.heightProperty());
            rootLayout.prefWidthProperty().bind(scene.widthProperty());
            primaryStage.setScene(scene);
            primaryStage.show();
            for (Node node: rootLayout.getChildren()){
                if (node instanceof TabPane){
                    tabPane = (TabPane)node;
                    tabPane.prefHeightProperty().bind(rootLayout.heightProperty());
                    tabPane.prefWidthProperty().bind(rootLayout.widthProperty());
                }else if(node instanceof MenuBar){
                    menuBar = (MenuBar)node;
                }
            }
        }catch(IOException e){
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean showTransactionEditDialog(Transaction transaction, Transaction.TransactionType type){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/TransactionEditDialog.fxml"));
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
            controller.setMainClass(this);
            controller.setSelectedTransaction(transaction, type);
            controller.loadDataFromDB();
            dialogStage.showAndWait();
            return controller.isConfirmedClicked();
        }catch (IOException e){
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
        }
    }

    public void showGenerateTransactionReportDialog(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/GenerateTransactionReport.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.getIcons().add(new Image(SaleSystem.class.getResourceAsStream(Constant.Image.appIconPath)));
            dialogStage.setTitle("Transaction Report");
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.initOwner(primaryStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            TransactionReportController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.loadDataFromDB();
            dialogStage.showAndWait();

        }catch(IOException e){
            logger.error(e.getMessage(), e);
        }
    }

    public void showGenerateRevenueReportDialog(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/GeneratePDFReportDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.getIcons().add(new Image(SaleSystem.class.getResourceAsStream(Constant.Image.appIconPath)));
            dialogStage.setTitle("Revenue Report");
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.initOwner(primaryStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            GeneratePDFReportDialog controller = loader.getController();
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            loadPropertyFromDB();

        }catch(IOException e){
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public boolean showTransactionConfirmationPanel(Transaction transaction, Customer customer){
        Stage dialogStage = new Stage();
        TransactionConfirmationController controller = createDefaultTransactionConfirmationPanel(dialogStage, transaction, customer);
        dialogStage.showAndWait();
        if(controller != null){
            return controller.isConfirmedClicked();
        }
        return false;
    }

    public boolean showTransactionConfirmationPanel(Transaction transaction, Customer customer, ObservableList<ProductTransaction> productTransactionObservableList){
        Stage dialogStage = new Stage();
        TransactionConfirmationController controller = createDefaultTransactionConfirmationPanel(dialogStage, transaction, customer);
        if(controller != null){
            controller.setProductTransactionObservableList(productTransactionObservableList);
            dialogStage.showAndWait();
            return controller.isConfirmedClicked();
        }
        return false;
    }

    private TransactionConfirmationController createDefaultTransactionConfirmationPanel(Stage dialogStage, Transaction transaction, Customer customer){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SaleSystem.class.getResource("/fxml/TransactionConfirmation.fxml"));
            SplitPane page = loader.load();

            dialogStage.getIcons().add(new Image(SaleSystem.class.getResourceAsStream(Constant.Image.appIconPath)));
            dialogStage.setTitle("Transaction Confirmation");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            TransactionConfirmationController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setSelectedTransaction(transaction);
            controller.setCustomer(customer);
            return controller;
        }catch (IOException e){
            logger.error(e.getMessage(), e);
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

    public Property getProperty(){ return property; }



    private Task<Void> refreshDB(){
        Task<Void> refreshDB = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int count = 0;
                while(true){
                    dbExecuteProperty
                            .selectFirstFromDatabase(DBQueries.SelectQueries.Property.SELECT_ALL_PROPERTY);
                    Thread.sleep(60*1000*5);
                    count += 5;
                }
            }
        };
        return refreshDB;
    }

    private void checkNewVersion(boolean isManuallyTriggered){
        HttpGet request = new HttpGet("https://api.github.com/repos/t6jin/salesystem/releases/latest");
        request.addHeader("accept", "application/json");

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == 200){
                JSONObject jsonObject = new JSONObject(IOUtils.toString(response.getEntity().getContent()));
                String version = jsonObject.getString("tag_name");
                String changeLog = jsonObject.getString("body");
                JSONObject asset = (JSONObject) jsonObject.getJSONArray("assets").get(0);
                String url = asset.getString("browser_download_url");

                String currentVersion = IOUtils.toString(
                        getClass().getClassLoader().getResourceAsStream("version"), StandardCharsets.UTF_8);
                StringBuilder contentBuilder = new StringBuilder();
                contentBuilder.append("Milan Sale System Version " + version + " is available!")
                        .append("\n")
                        .append("The following is the change log")
                        .append("\n\n")
                        .append(changeLog)
                        .append("\n\n")
                        .append("Please click on the following link to download")
                        .append("\n");
                if(currentVersion.compareTo(version) < 0){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            FlowPane fp = new FlowPane();
                            Label lbl = new Label(contentBuilder.toString());
                            Hyperlink link = new Hyperlink(url);
                            fp.getChildren().addAll( lbl, link);
                            link.setOnAction(event -> {
                                if (Desktop.isDesktopSupported()){
                                    Desktop desktop = Desktop.getDesktop();
                                    if (desktop.isSupported(Desktop.Action.BROWSE)){
                                        try {
                                            desktop.browse(new URI(url));
                                        } catch (IOException | URISyntaxException e) {
                                            logger.info(e.getMessage(), e);
                                        }
                                    }
                                }else{
                                    new AlertBuilder().alertType(Alert.AlertType.WARNING)
                                            .alertTitle("Warning")
                                            .alertHeaderText("System Default Browser Error")
                                            .alertContentText("Unable to open system default browser. Please copy and paste the url manually")
                                            .build()
                                            .showAndWait();
                                }
                            });

                            Alert alert = new AlertBuilder().alertTitle("Version Upgrade")
                                    .alertType(Alert.AlertType.INFORMATION)
                                    .alertHeaderText("New Version is Available")
                                    .alertContentText(contentBuilder.toString())
                                    .build();
                            alert.getDialogPane().contentProperty().set(fp);
                            Optional<ButtonType> result = alert.showAndWait();
                            if(result.isPresent() && result.get() == ButtonType.OK){
                                System.exit(0);
                            }
                        }
                    });
                }else{
                    if(isManuallyTriggered){
                        new AlertBuilder().alertTitle("Upgrade Check")
                                .alertContentText("You've already had the latest version!")
                                .build()
                                .showAndWait();
                    }
                }
            }else{
                new AlertBuilder().alertType(Alert.AlertType.ERROR)
                        .alertTitle("Upgrade Error")
                        .alertHeaderText("Upgrade Check Error")
                        .alertContentText("We've encountered an issue when checking the version. Please the reason: "+ "\n\n" +
                                response.getStatusLine().getReasonPhrase())
                        .build()
                        .showAndWait();
            }
        } catch (IOException e) {
            logger.info(e.getMessage(), e);
        }
    }
}
