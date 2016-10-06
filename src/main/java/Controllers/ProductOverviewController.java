package Controllers;


import MainClass.SaleSystem;
import db.DBExecuteProduct;
import db.DBExecuteTransaction;
import db.DBQueries;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Product;
import model.ProductTransaction;
import model.Staff;
import model.Transaction;
import org.apache.log4j.Logger;
import util.AlertBuilder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class ProductOverviewController implements OverviewController{

    private static Logger logger= Logger.getLogger(ProductOverviewController.class);
    private SaleSystem saleSystem;
    private ObservableList<Product> productList;
    private List<Transaction> transactionList;
    private Executor executor;
    private DBExecuteProduct dbExecute;
    private DBExecuteTransaction dbExecuteTransaction;
    private Stage dialogStage;

    @FXML
    private TableView<Product> productTable;
    @FXML
    private TextField filterField;
    @FXML
    private TableColumn<Product, String> productIdCol;
    @FXML
    private TableColumn<Product, Float> totalNumCol;
    @FXML
    private TableColumn<Product, String> sizeCol;
    @FXML
    private Label productIdLabel;
    @FXML
    private Label textualLabel;
    @FXML
    private Label sizeLabel;
    @FXML
    private Label totalNumLabel;
    @FXML
    private Label unitPriceLabel;
    @FXML
    private Label piecesPerBoxLabel;
    @FXML
    private TableView<Transaction> productTransactionTableView;
    @FXML
    private TableColumn<Transaction, LocalDate> productTransactionDateCol;
    @FXML
    private TableColumn<Transaction, Integer> productTransactionStaffIdCol;
    @FXML
    private TableColumn<Transaction, String> productTransactionInfoCol;
    @FXML
    private TableColumn<Transaction, Number> productTransactionBoxCol;
    @FXML
    private TableColumn<Transaction, Number> productTransactionResidualTileCol;
    @FXML
    private TableColumn<Transaction, Number> productTransactionUnitPriceCol;
    @FXML
    private TableColumn<Transaction, Number> productTransactionSubtotalCol;
    @FXML
    private TableColumn<Transaction, Number> productTransactionTotalFeetCol;
    @FXML
    private ProgressBar progressBar;

    @FXML
    private void initialize(){
        productIdCol.setCellValueFactory(new PropertyValueFactory<Product, String>("ProductId"));
        totalNumCol.setCellValueFactory(new PropertyValueFactory<Product, Float>("totalNum"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<Product, String>("size"));
        totalNumCol.setCellFactory(new Callback<TableColumn<Product, Float>, TableCell<Product, Float>>() {
            @Override
            public TableCell<Product, Float> call(TableColumn<Product, Float> param) {
                return new TableCell<Product, Float>(){
                    @Override
                    public void updateItem(Float item, boolean empty){
                        super.updateItem(item, empty);
                        if(item == null || empty){
                            setText(null);
                            getTableRow().setStyle("");
                        }else{
                            setText(String.valueOf(item));
                            if(item < saleSystem.getProperty().getProductWarnLimit()){
                                getTableRow().setStyle("-fx-background-color: lightcoral; -fx-border-color: cornsilk");
                            }
                            else{
                                getTableRow().setStyle("");
                            }
                        }
                    }
                };
            }
        });
        productTransactionDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        productTransactionStaffIdCol.setCellValueFactory(new PropertyValueFactory<>("staffId"));
        productTransactionInfoCol.setCellValueFactory(new PropertyValueFactory<>("info"));
        productTransactionBoxCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<Transaction, Number> param) {
                return new SimpleIntegerProperty(param.getValue().getProductTransactionList().get(0).getBoxNum().getBox());
            }
        });
        productTransactionResidualTileCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<Transaction, Number> param) {
                return new SimpleIntegerProperty(param.getValue().getProductTransactionList().get(0).getBoxNum().getResidualTile());
            }
        });
        productTransactionTotalFeetCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<Transaction, Number> param) {
                return new SimpleFloatProperty(param.getValue().getProductTransactionList().get(0).getQuantity());
            }
        });
        showProductDetail(null);
        productTable.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Product>() {
                    @Override
                    public void changed(ObservableValue<? extends Product> observable, Product oldValue, Product newValue) {
                        showProductDetail(newValue);
                    }
                }
        );
        executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    @FXML
    private void  handleDeleteProduct(){
        int selectedIndex = productTable.getSelectionModel().getSelectedIndex();
        if(selectedIndex >= 0){
            String tempID = productTable.getItems().get(selectedIndex).getProductId();
            float temptotalNum = productTable.getItems().get(selectedIndex).getTotalNum();
            Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete product - " + tempID);
            Optional<ButtonType> result =  alertConfirm.showAndWait();
            boolean flag = true;
            if(result.isPresent() && result.get() == ButtonType.OK){
                try{
                    dbExecute.deleteDatabase(DBQueries.DeleteQueries.Product.DELETE_FROM_PRODUCT,
                            productTable.getItems().get(selectedIndex).getProductId());
                }catch(SQLException e){
                    logger.error(e.getMessage());
                    flag = false;
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error When Deleting Product: "+tempID);
                    alert.setTitle("Delete Product Error");
                    alert.showAndWait();
                }finally{
                    if(flag){
                        productTable.getItems().remove(selectedIndex);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Delete Product Successfully");
                        alert.setHeaderText(null);
                        alert.setContentText("Successfully Deleted Product: "+tempID);
                        alert.showAndWait();
                    }
                }

            }
        }
        else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Product Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a product in the table.");
            alert.showAndWait();
        }

    }

    @FXML
    private void handleAddProduct(){
        Product newProduct = new Product(new Product.ProductBuilder());
        boolean okClicked = saleSystem.showProductEditDialog(newProduct, false);
        if(okClicked){
            try{
                dbExecute.insertIntoDatabase(DBQueries.InsertQueries.Product.INSERT_INTO_PRODUCT,
                        newProduct.getAllProperties());
            }catch(SQLException e){
                logger.error(e.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Unable To Add New Product.\n" + e.getMessage());
                alert.setHeaderText(null);
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }finally{
                loadDataFromDB();
            }
        }
    }

    @FXML
    private void handleEditProduct(){
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if(selectedProduct != null){
            boolean onClicked = saleSystem.showProductEditDialog(selectedProduct, true);
            if(onClicked){
                try{
                    dbExecute.updateDatabase(DBQueries.UpdateQueries.Product.UPDATE_PRODUCT,
                            selectedProduct.getAllPropertiesForUpdate());
                }catch(SQLException e){
                    logger.error(e.getMessage());
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Unable To Edit Product");
                    alert.setHeaderText(null);
                    alert.setContentText("Unable To Edit Product " + selectedProduct.getProductId() + "\n" + e.getMessage());
                    alert.showAndWait();
                }finally{
                    loadDataFromDB();
                }
            }
        }
        else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Product Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a product in the table.");
            alert.showAndWait();
        }
    }

    public ProductOverviewController(){
        dbExecute = new DBExecuteProduct();
        dbExecuteTransaction = new DBExecuteTransaction();
    }
    public void loadDataFromDB(){
        Task<List<Product>> productListTask = new Task<List<Product>>() {
            @Override
            protected List<Product> call() throws Exception {
                List<Product> tmpProductList = new ArrayList<>();
                for(int i = 0; i < 1; i++){
                    tmpProductList = dbExecute.selectFromDatabase(DBQueries.SelectQueries.Product.SELECT_ALL_PRODUCT);
                    updateProgress(i+1, 1);
                }
                return tmpProductList;
            }
        };
        Task<List<Transaction>> transactionListTask = new Task<List<Transaction>>() {
            @Override
            protected List<Transaction> call() throws Exception {
                List<Transaction> tmpTransactionList = new ArrayList<>();
                for(int i = 0; i < 1; i++){
                    tmpTransactionList = dbExecuteTransaction.selectFromDatabase(DBQueries.SelectQueries.Transaction.SELECT_ALL_TRANSACTION);
                    updateProgress(i+1, 1);
                }
                return tmpTransactionList;
            }
        };
        progressBar.progressProperty().bind(transactionListTask.progressProperty());
        transactionListTask.setOnSucceeded(event -> {
            transactionList = FXCollections.observableArrayList(transactionListTask.getValue());
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(productListTask.progressProperty());
            executor.execute(productListTask);
        });
        transactionListTask.setOnFailed(event -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Unable to grab data from database!\n" + event.getSource().exceptionProperty().getValue());
            alert.setTitle("Database Error");
            alert.showAndWait();
        });
        productListTask.setOnSucceeded(event -> {
            productList = FXCollections.observableArrayList(productListTask.getValue());
            productTable.setItems(productList);
            productTable.getSelectionModel().selectFirst();
            FilteredList<Product> filteredData = new FilteredList<Product>(productList,p->true);
            filterField.textProperty().addListener((observable,oldVal,newVal)->{
                filteredData.setPredicate(product -> {
                    if (newVal == null || newVal.isEmpty()){
                        return true;
                    }
                    String lowerCase = newVal.toLowerCase();
                    if (String.valueOf(product.getTotalNum()).toLowerCase().contains(lowerCase)){
                        return true;
                    }else if (String.valueOf(product.getProductId()).toLowerCase().contains(lowerCase)){
                        return true;
                    }else if(product.getSize().toLowerCase().contains(lowerCase)){
                        return true;
                    }
                    return false;
                });
                productTable.setItems(filteredData);
            });
        });
        productListTask.setOnFailed(event -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Unable to grab data from database!\n" + event.getSource().exceptionProperty().getValue());
            alert.setTitle("Database Error");
            alert.showAndWait();
        });
        executor.execute(transactionListTask);
    }

    @Override
    public void setMainClass(SaleSystem saleSystem) {
        this.saleSystem = saleSystem;
        loadDataFromDB();
        if(this.saleSystem.getStaff().getPosition().equals(Staff.Position.MANAGER)){
            productTransactionSubtotalCol = new TableColumn<>("SubTotal");
            productTransactionUnitPriceCol = new TableColumn<>("UnitPrice");
            productTransactionUnitPriceCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, Number>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<Transaction, Number> param) {
                    return param.getValue().getProductTransactionList().get(0).unitPriceProperty();
                }
            });
            productTransactionSubtotalCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, Number>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<Transaction, Number> param) {
                    return param.getValue().getProductTransactionList().get(0).subTotalProperty();
                }
            });
            productTransactionTableView.getColumns().add(productTransactionUnitPriceCol);
            productTransactionTableView.getColumns().add(productTransactionSubtotalCol);
        }
    }

    @Override
    public void setDialogStage(Stage stage){
        this.dialogStage = stage;
    }

    private void showProductDetail(Product product){
        if(product != null){
            productIdLabel.setText(String.valueOf(product.getProductId()));
            textualLabel.setText(product.getTexture());
            sizeLabel.setText(product.getSize());
            totalNumLabel.setText(String.valueOf(product.getTotalNum()));
            unitPriceLabel.setText(String.valueOf(product.getUnitPrice()));
            piecesPerBoxLabel.setText(String.valueOf(product.getPiecesPerBox()));
            showProductTransactionDetail(product);
        }
        else{
            productIdLabel.setText("");
            textualLabel.setText("");
            sizeLabel.setText("");
            totalNumLabel.setText("");
            unitPriceLabel.setText("");
            piecesPerBoxLabel.setText("");
        }
    }
    private void showProductTransactionDetail(Product product){
        List<Transaction> tmpTransactionList = transactionList.stream()
                .filter(t->t.getProductTransactionList().stream()
                        .anyMatch(p -> p.getProductId().equals(product.getProductId())))
                .filter(t -> t.getType().equals(Transaction.TransactionType.IN))
                .collect(Collectors.toList());

        List<Transaction> realTransactionList = new ArrayList<>();
        tmpTransactionList.forEach(transaction -> {
            Transaction tmpTransaction = new Transaction.TransactionBuilder()
                    .date(transaction.getDate().toString())
                    .staffId(transaction.getStaffId())
                    .info(transaction.getInfo())
                    .build();
            List<ProductTransaction> tmpProductTransactionList = transaction.getProductTransactionList().stream()
                    .filter(p->p.getProductId().equals(product.getProductId()))
                    .collect(Collectors.toList());
            tmpTransaction.setProductTransactionList(tmpProductTransactionList);
            realTransactionList.add(tmpTransaction);
        });
        productTransactionTableView.setItems(FXCollections.observableArrayList(realTransactionList));
    }

}
