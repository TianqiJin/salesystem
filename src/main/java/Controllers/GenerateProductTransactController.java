package Controllers;

import Constants.Constant;
import MainClass.SaleSystem;
import db.*;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.*;
import org.apache.log4j.Logger;
import util.AlertBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by tjin on 12/7/2015.
 */
public class GenerateProductTransactController {

    public static Logger logger= Logger.getLogger(GenerateProductTransactController.class);
    private Stage dialogStage;
    private ObservableList<ProductTransaction> productTransactionObservableList;
    private FilteredList<ProductTransaction> filteredData;
    private DBExecuteProduct dbExecuteProduct;
    private DBExecuteTransaction dbExecuteTransaction;
    private SaleSystem saleSystem;
    private Transaction transaction;
    private StringBuffer errorMsgBuilder;
    private boolean confirmedClicked;
    private BooleanBinding confimButtonBinding;
    private Executor executor;

    @FXML
    private TableView<ProductTransaction> transactionTableView;
    @FXML
    private TableColumn<ProductTransaction, Integer> productIdCol;
    @FXML
    private TableColumn<ProductTransaction, Integer> stockCol;
    @FXML
    private TableColumn<ProductTransaction, Float> unitPriceCol;
    @FXML
    private TableColumn<ProductTransaction, Number> boxCol;
    @FXML
    private TableColumn<ProductTransaction, Number> residualTileCol;
    @FXML
    private TableColumn<ProductTransaction, Float> qtyCol;
    @FXML
    private TableColumn<ProductTransaction, BigDecimal> subTotalCol;

    @FXML
    private Label itemsCountLabel;
    @FXML
    private Label subTotalLabel;
    @FXML
    private Label totalLabel;

    @FXML
    private TextField productIdField;
    @FXML
    private Button addItemButton;

    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;

    @FXML
    private TextField supplierNameField;

    @FXML
    private void initialize(){
        confimButtonBinding = supplierNameField.textProperty().isEmpty().or(transactionTableView.itemsProperty().isNull());
        confirmButton.disableProperty().bind(confimButtonBinding);
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("totalNum"));
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        boxCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductTransaction, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<ProductTransaction, Number> param) {
                return new SimpleIntegerProperty(param.getValue().getBoxNum().getBox());
            }
        });
        residualTileCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductTransaction, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<ProductTransaction, Number> param) {
                return new SimpleIntegerProperty(param.getValue().getBoxNum().getResidualTile());
            }
        });
        loadDataFromDB();

        productIdField.textProperty().addListener((observable,oldVal,newVal)->{
            filteredData.setPredicate(productTransaction -> {
                if (newVal == null || newVal.isEmpty()){
                    return true;
                }
                String lowerCase = newVal.toLowerCase();
                if (String.valueOf(productTransaction.getProductId()).toLowerCase().contains(lowerCase)) {
                    return true;
                }
                return false;
            });
            transactionTableView.setItems(filteredData);
        });

        unitPriceCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Float>(){
            @Override
            public Float fromString(String string) {return new Float(string);}
            public String toString(Float f){return String.valueOf(f);}
        }));

        unitPriceCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ProductTransaction, Float>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<ProductTransaction, Float> event) {
                (event.getTableView().getItems().get(event.getTablePosition().getRow()))
                        .setUnitPrice(event.getNewValue().floatValue());
                transactionTableView.setItems(filteredData);
            }
        });


        boxCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return String.valueOf(object.intValue());
            }

            @Override
            public Number fromString(String string) {
                return Integer.valueOf(string);
            }
        }));

        residualTileCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return String.valueOf(object.intValue());
            }

            @Override
            public Number fromString(String string) {
                return Integer.valueOf(string);
            }
        }));

        qtyCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Float>() {
            @Override
            public String toString(Float object) {
                return String.valueOf(object);
            }

            @Override
            public Float fromString(String string) {
                return Float.valueOf(string);
            }
        }));

        boxCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ProductTransaction, Number>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<ProductTransaction, Number> event) {
                ProductTransaction pt = (event.getTableView().getItems().get(event.getTablePosition().getRow()));
                pt.getBoxNum().setBox(event.getNewValue().intValue());
                transactionTableView.setItems(filteredData);
            }
        });

        residualTileCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ProductTransaction, Number>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<ProductTransaction, Number> event) {
                ProductTransaction pt = (event.getTableView().getItems().get(event.getTablePosition().getRow()));
                pt.getBoxNum().setResidualTile(event.getNewValue().intValue());
                transactionTableView.setItems(filteredData);
            }
        });

        qtyCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ProductTransaction, Float>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<ProductTransaction, Float> event) {
                ProductTransaction pt = (event.getTableView().getItems().get(event.getTablePosition().getRow()));
                pt.setQuantity(event.getNewValue());
                transactionTableView.setItems(filteredData);
            }
        });

        subTotalCol.setCellValueFactory(new PropertyValueFactory<>("subTotal"));

        executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    @FXML
    public void handleCancelButton(){
        this.dialogStage.close();
    }

    @FXML
    public Transaction handleConfirmButton() throws IOException, SQLException {
        List<ProductTransaction> effectiveList = productTransactionObservableList.stream().filter(p->p.getQuantity()!=0).collect(Collectors.toList());
        if(!isTransactionValid(effectiveList)){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Transaction Is Invalid");
            alert.setHeaderText("Please fix the following errors before proceed");
            alert.setContentText(errorMsgBuilder.toString());
            alert.showAndWait();
        }
        else{
            transaction.setInfo(supplierNameField.getText().trim());
            transaction.getProductTransactionList().addAll(effectiveList);

            StringBuffer overviewTransactionString = new StringBuffer();
            StringBuffer overviewProductTransactionString = new StringBuffer();
            BigDecimal total = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            for(ProductTransaction tmp: transaction.getProductTransactionList()){
                overviewProductTransactionString
                        .append("Product ID: " + tmp.getProductId() + "\n")
                        .append("Total Num: " + tmp.getTotalNum() + "\n")
                        .append("Purchased Feet: " + tmp.getQuantity()+ "\n")
                        .append("Unit Price: " + tmp.getUnitPrice() + "\n")
                        .append("Sub Total: " + tmp.getSubTotal() + "\n")
                        .append("\n");
                total = total.add(new BigDecimal(tmp.getSubTotal()).setScale(2, BigDecimal.ROUND_HALF_EVEN));
            }
            transaction.setPayment(Double.valueOf(total.toString()));
            transaction.setTotal(Double.valueOf(total.toString()));
            transaction.getPayinfo().add(new PaymentRecord(
                    transaction.getDate().toString(),
                    transaction.getPayment() + transaction.getStoreCredit(),
                    transaction.getPaymentType()));

            overviewTransactionString
                    .append("Customer Name: " + transaction.getInfo() + "\n\n")
                    .append(overviewProductTransactionString)
                    .append("\n" + "Total: " + transaction.getPayment() + "\n")
                    .append("Payment Type: " + transaction.getPaymentType() + "\n")
                    .append("Date: " + transaction.getDate() + "\n");

            Alert alert = new Alert(Alert.AlertType.INFORMATION, overviewTransactionString.toString(), ButtonType.OK, ButtonType.CANCEL);
            alert.setTitle("Transaction Overview");
            alert.setHeaderText("Please confirm the following transaction");
            alert.setResizable(true);
            alert.getDialogPane().setPrefWidth(500);
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image(this.getClass().getResourceAsStream(Constant.Image.appIconPath)));
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
            Optional<ButtonType> result = alert.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.OK){
                commitTransactionToDatabase();
                confirmedClicked = true;
            }else{
                transaction.getProductTransactionList().clear();
                transaction.getPayinfo().clear();
            }
        }
        return transaction;
    }
    private DBExecuteProductTransaction dbExecute;

    /*
    * Constructor
    * */
    public GenerateProductTransactController(){
        dbExecute = new DBExecuteProductTransaction();
        dbExecuteProduct = new DBExecuteProduct();
        dbExecuteTransaction = new DBExecuteTransaction();
        confirmedClicked = false;
    }

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

    /*
    * Initilize the main class for this class
    * */
    public void setMainClass(SaleSystem saleSystem){
        this.saleSystem = saleSystem;
        transaction = new Transaction.TransactionBuilder()
                .productInfoList(new ArrayList<ProductTransaction>())
                .staffId(saleSystem.getStaff().getStaffId())
                .date(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                .type(Transaction.TransactionType.IN)
                .storeCredit(0)
                .paymentType("IN")
                .payinfo(new ArrayList<>())
                .build();
    }

    private boolean isTransactionValid(List<ProductTransaction> list){

        errorMsgBuilder = new StringBuffer();
        if (list.size()==0){
            errorMsgBuilder.append("No product quantity added!!");
        }
        if (list.stream().anyMatch(p->p.getUnitPrice()==0)){
            errorMsgBuilder.append("Unit Price should not be zero!!");
        }
        if(supplierNameField.getText().trim().isEmpty()){
            errorMsgBuilder.append("Supplier Name Cannot Empty!\n");
        }
        if(errorMsgBuilder.length() != 0){
            return false;
        }
        return true;
    }

    private void commitTransactionToDatabase() throws SQLException, IOException {
        Task<List<Product>> productListTask = new Task<List<Product>>() {
            @Override
            protected List<Product> call() throws Exception {
               return dbExecuteProduct.selectFromDatabase(DBQueries.SelectQueries.Product.SELECT_ALL_PRODUCT);
            }
        };
        Task<Void> commitTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Connection connection = DBConnect.getConnection();
                connection.setAutoCommit(false);
                Object[] objects = new Object[0];
                try {
                    objects = ObjectSerializer.TRANSACTION_OBJECT_SERIALIZER.serialize(transaction);
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
                dbExecuteTransaction.insertIntoDatabase(DBQueries.InsertQueries.Transaction.INSERT_INTO_TRANSACTION,
                        objects);
                for(ProductTransaction tmp : transaction.getProductTransactionList()){
                    float remain = tmp.getTotalNum() + tmp.getQuantity();
                    dbExecuteProduct.updateDatabase(DBQueries.UpdateQueries.Product.UPDATE_PRODUCT_QUANTITY,
                            remain, tmp.getProductId());
                }
                connection.commit();
                return null;
            }
        };
        productListTask.setOnSucceeded(event -> {
            transaction.getProductTransactionList().forEach(productTransaction -> {
                Product tmp = productListTask.getValue().stream()
                        .filter(product -> product.getProductId().equals(productTransaction.getProductId()))
                        .findFirst()
                        .get();
                if (tmp == null) {
                    new AlertBuilder()
                            .alertTitle("Product Error!")
                            .alertType(Alert.AlertType.ERROR)
                            .alertContentText("Product - " + productTransaction.getProductId() + " does not exist!")
                            .build()
                            .showAndWait();
                    dialogStage.close();
                } else {
                    productTransaction.setTotalNum(tmp.getTotalNum());
                }
            });
            executor.execute(commitTask);
        });
        commitTask.setOnFailed(event ->{
            Connection connection = DBConnect.getConnection();
            try {
                connection.rollback();
                connection.setAutoCommit(true);
                new AlertBuilder()
                        .alertType(Alert.AlertType.ERROR)
                        .alertTitle("Database Insert Error")
                        .alertContentText("Unable to insert transaction into Database!\n" +
                                event.getSource().exceptionProperty().getValue())
                        .build()
                        .showAndWait();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
            dialogStage.close();
        });
        commitTask.setOnSucceeded(event ->{
            Connection connection = DBConnect.getConnection();
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
            dialogStage.close();
        });
        executor.execute(productListTask);
    }

    public Transaction returnNewTrasaction(){
        return this.transaction;
    }

    public boolean isConfirmedClicked(){
        return this.confirmedClicked;
    }

    public void loadDataFromDB(){
        try{
            productTransactionObservableList = FXCollections.observableArrayList(
                    dbExecute.selectFromDatabase(DBQueries.SelectQueries.Product.SELECT_ALL_PRODUCT)
            );
        }catch(SQLException e){
            logger.error(e.getMessage());
            Alert alert = new Alert(Alert.AlertType.WARNING, "Unable to grab data from database!\n" + e.getMessage());
            alert.setTitle("Database Error");
            alert.showAndWait();
        }
        filteredData = new FilteredList<ProductTransaction>(productTransactionObservableList,p->true);
        transactionTableView.setItems(productTransactionObservableList);
    }
}
