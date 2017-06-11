package Controllers;


import Constants.Constant;
import MainClass.SaleSystem;
import com.sun.org.apache.xpath.internal.operations.Bool;
import db.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.*;
import org.apache.log4j.Logger;
import util.AlertBuilder;
import util.DateUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class TransactionOverviewController implements OverviewController{

    public static Logger logger= Logger.getLogger(TransactionOverviewController.class);
    private SaleSystem saleSystem;
    private ObservableList<Transaction> transactionList;
    private DBExecuteTransaction dbExecuteTransaction;
    private DBExecuteCustomer dbExecuteCustomer;
    private DBExecuteProduct dbExecuteProduct;
    private Executor executor;
    private List<Customer> customerList;
    private List<Product> productList;
    private Stage dialogStage;

    @FXML
    private TableView<Transaction> transactionTable;
    @FXML
    private TextField filterField;
    @FXML
    private TableColumn<Transaction, Integer> transactionIdCol;
    @FXML
    private TableColumn<Transaction, String> dateCol;
    @FXML
    private TableColumn<Transaction, String> typeCol;
    @FXML
    private TableColumn<Transaction, String> infoCol;
    @FXML
    private TableColumn<Transaction, String> phoneCol;
    @FXML
    private Label transactionIdLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label paymentLabel;
    @FXML
    private Label paymentTypeLabel;
    @FXML
    private Label storeCreditLabel;
    @FXML
    private Label staffLabel;
    @FXML
    private Label typeLabel;
    @FXML
    private Label infoLabel;
    @FXML
    private Label noteLabel;
    @FXML
    private TableView<ProductTransaction> transactionDetaiTableView;
    @FXML
    private TableColumn<ProductTransaction, String> productIdCol;
    @FXML
    private TableColumn<ProductTransaction, Integer> qtyCol;
    @FXML
    private TableColumn<ProductTransaction, Float> subTotalCol;
    @FXML
    private TableColumn<ProductTransaction, Float> unitPriceCol;
    @FXML
    private TableColumn<ProductTransaction, String> remarkCol;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Button deleteButton;
    @FXML
    private Button stockButton;
    @FXML
    private HBox buttonHbox;
    @FXML
    private ComboBox<Transaction.TransactionType> transactionTypeComboBox;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private TableView<PaymentRecord> paymentRecordTableView;
    @FXML
    private TableColumn<PaymentRecord, String> paymentDateCol;
    @FXML
    private TableColumn<PaymentRecord, String> paymentTypeCol;
    @FXML
    private TableColumn<PaymentRecord, Double> paymentAmountCol;
    @FXML
    private TableColumn<PaymentRecord, String> paymentIsDepositCol;


    @FXML
    private void initialize(){
        transactionIdCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("Date"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("Type"));
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        subTotalCol.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        remarkCol.setCellValueFactory(new PropertyValueFactory<>("remark"));
        remarkCol.setCellFactory(new Callback<TableColumn<ProductTransaction, String>, TableCell<ProductTransaction, String>>() {
            @Override
            public TableCell<ProductTransaction, String> call(TableColumn<ProductTransaction, String> param) {
                return new TableCell<ProductTransaction, String>(){
                    @Override
                    public void updateItem(String item, boolean empty){
                        super.updateItem(item, empty);
                        if (!isEmpty()) {
                            Text text = new Text(item.toString());
                            text.setWrappingWidth(remarkCol.getWidth());
                            setGraphic(text);
                        }else{
                            setText(null);
                            setGraphic(null);
                        }
                    }
                };
            }
        });

        phoneCol.setCellValueFactory(param -> {
            if(param.getValue().getType().equals(Transaction.TransactionType.IN)){
                return null;
            }else{

                Customer customer = customerList.stream().filter(c -> c.getUserName().equals(param.getValue().getInfo())).findFirst().orElse(null);
                if(customer != null && customer.getPhone() != null){
                    return new SimpleStringProperty(customer.getPhone());
                }else{
                    return null;
                }
            }
        });
        showTransactionDetail(null);
        transactionTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showTransactionDetail(newValue)
        );

        transactionTypeComboBox.setItems(FXCollections.observableArrayList(Transaction.TransactionType.values()));
        transactionTypeComboBox.setConverter(new StringConverter<Transaction.TransactionType>() {
            @Override
            public String toString(Transaction.TransactionType object) {
                if(object == null){
                    return null;
                }
                return object.toString();
            }

            @Override
            public Transaction.TransactionType fromString(String string) {
                if(string == null){
                    return null;
                }
                return Transaction.TransactionType.valueOf(string);
            }
        });

        paymentAmountCol.setCellValueFactory(new PropertyValueFactory<>("paid"));
        paymentDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        paymentIsDepositCol.setCellValueFactory(param -> {
            if(param.getValue().isDeposit()){
                return new SimpleStringProperty("Yes");
            }else{
                return new SimpleStringProperty("No");
            }
        });
        paymentTypeCol.setCellValueFactory(new PropertyValueFactory<>("paymentType"));

        executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    @FXML
    private void handleFilterTransaction(){
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        Transaction.TransactionType type = transactionTypeComboBox.getValue();

        ObservableList<Transaction> subList = FXCollections.observableArrayList(transactionList);
        if(type != null){
            subList = subList.stream().filter(transaction -> transaction.getType().equals(type)).collect(Collectors.toCollection(FXCollections::observableArrayList));
        }
        if(fromDate != null){
            subList = subList.stream().filter(transaction -> transaction.getDate().isAfter(fromDate)).collect(Collectors.toCollection(FXCollections::observableArrayList));
        }
        if(toDate != null){
            subList = subList.stream().filter(transaction -> transaction.getDate().isBefore(toDate)).collect(Collectors.toCollection(FXCollections::observableArrayList));
        }

        transactionTable.setItems(subList);
    }

    @FXML
    private void handleClearFilter(){
        transactionTypeComboBox.getEditor().clear();
        fromDatePicker.getEditor().clear();
        toDatePicker.getEditor().clear();
        transactionTable.setItems(transactionList);
    }

    @FXML
    private void handleAddTransaction(){
        ContainerClass containerClass = saleSystem.showGenerateCustomerTransactionDialog();
        if(containerClass != null){
            Transaction newTransaction = containerClass.getTransaction();
            if(newTransaction != null){
                transactionList.add(newTransaction);
                customerList=containerClass.getCustomers();
                loadDataFromDB();
            }
        }
    }


    @FXML
    private void handleStockTransaction(){
        Transaction newTransaction = saleSystem.showGenerateProductTransactionDialog();
        if(newTransaction != null){
            transactionList.add(newTransaction);
            loadDataFromDB();
        }
    }

    @FXML
    private void handleReturnTransaction(){
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        if(selectedTransaction != null){
            if(!selectedTransaction.getType().equals(Transaction.TransactionType.OUT)){
                new AlertBuilder()
                        .alertType(Alert.AlertType.ERROR)
                        .alertContentText("You can only create RETURN transaction from OUT transaction!\n")
                        .build()
                        .showAndWait();
            }else{
                Transaction newTransaction = saleSystem.showGenerateReturnTransactionDialog(selectedTransaction);
                if(newTransaction != null){
                    transactionList.add(newTransaction);
                }
                loadDataFromDB();
            }

        }
    }

    @FXML
    private void handleDeleteTransaction() throws SQLException {
        Connection connection = DBConnect.getConnection();
        int selectIndex = transactionTable.getSelectionModel().getFocusedIndex();
        if(selectIndex >= 0){
            Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this transaction?");
            Optional<ButtonType> result =  alertConfirm.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.OK){
                try{
                    connection.setAutoCommit(false);
                    productList = dbExecuteProduct.selectFromDatabase(DBQueries.SelectQueries.Product.SELECT_ALL_PRODUCT);
                    customerList = dbExecuteCustomer.selectFromDatabase(DBQueries.SelectQueries.Customer.SELECT_ALL_CUSTOMER);
                    Transaction deleteTransaction = transactionTable.getItems().get(selectIndex);
                    List<String> deletedProductList = new ArrayList<>();
                    for(ProductTransaction tmp : deleteTransaction.getProductTransactionList()){
                        Product tmpProduct = productList
                                .stream()
                                .filter(product -> product.getProductId().equals(tmp.getProductId()))
                                .findFirst()
                                .orElse(null);
                        if(tmpProduct != null){
                            float quantity;
                            float currentQuality = tmpProduct.getTotalNum();
                            if(deleteTransaction.getType().equals(Transaction.TransactionType.OUT) ||
                                    deleteTransaction.getType().equals(Transaction.TransactionType.QUOTATION)){
                                quantity = currentQuality + tmp.getQuantity();
                                dbExecuteProduct.updateDatabase(DBQueries.UpdateQueries.Product.UPDATE_PRODUCT_QUANTITY,
                                        quantity, tmp.getProductId());
                            }else{
                                quantity = currentQuality - tmp.getQuantity();
                                dbExecuteProduct.updateDatabase(DBQueries.UpdateQueries.Product.UPDATE_PRODUCT_QUANTITY,
                                        quantity, tmp.getProductId());
                            }
                        }else{
                            deletedProductList.add(tmp.getProductId());
                        }
                    }
                    if(!deletedProductList.isEmpty()){
                        new AlertBuilder()
                                .alertTitle("Missing Products")
                                .alertHeaderText("The following products are no longer in database anymore")
                                .alertContentText(Arrays.toString(deletedProductList.toArray()))
                                .build()
                                .showAndWait();
                    }
                    if(!deleteTransaction.getType().equals(Transaction.TransactionType.IN)){
                        Customer customer = customerList
                                .stream()
                                .filter(c -> c.getUserName().equals(deleteTransaction.getInfo()))
                                .findFirst()
                                .orElse(null);
                        if(customer != null){
                            double storeCredit = 0;
                            if(deleteTransaction.getType().equals(Transaction.TransactionType.OUT)){
                                storeCredit = customer.getStoreCredit() + deleteTransaction.getStoreCredit();
                            }else if(deleteTransaction.getType().equals(Transaction.TransactionType.RETURN)){
                                storeCredit = customer.getStoreCredit() - deleteTransaction.getStoreCredit();
                            }
                            if(storeCredit > 0){
                                dbExecuteCustomer.updateDatabase(DBQueries.UpdateQueries.Customer.UPDATE_CUSTOMER_STORE_CREDIT,
                                        storeCredit, customer.getUserName());
                            }
                        }
                    }
                    deleteTransaction.setDeleted(true);
                    dbExecuteTransaction.updateDatabase(DBQueries.UpdateQueries.Transaction.UPDATE_TRANSACTION_DELETE, deleteTransaction.getPropertiesForDeleteUpdate());
                    connection.commit();
                }catch (SQLException e){
                    logger.error(e.getMessage(), e);
                    connection.rollback();
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to store transaction to database!\n" + e.getMessage());
                    alert.showAndWait();
                }
                finally {
                    connection.setAutoCommit(true);
                    loadDataFromDB();
                }
            }
        }
        else{
            new AlertBuilder()
                    .alertTitle("No Transaction Selected!")
                    .alertContentText("Please select a transaction in the table")
                    .build()
                    .showAndWait();
        }
    }

    @FXML
    private void handleEditTransaction(){
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        boolean okClicked = false;
        if(selectedTransaction != null){
            if(!selectedTransaction.getType().equals(Transaction.TransactionType.OUT) &&
                    !selectedTransaction.getType().equals(Transaction.TransactionType.QUOTATION)){
                new AlertBuilder()
                        .alertTitle("Edit Transaction Error")
                        .alertType(Alert.AlertType.ERROR)
                        .alertContentText("You can only edit OUT/QUOTATION transaction!\n")
                        .build()
                        .showAndWait();
            }else{
                if(selectedTransaction.getType().equals(Transaction.TransactionType.QUOTATION)){

                    ButtonType quotationType = new ButtonType("Quotation");
                    ButtonType outType = new ButtonType("OUT");
                    ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                    Alert alert = new AlertBuilder()
                            .alertType(Alert.AlertType.CONFIRMATION)
                            .alertTitle("Edit Transaction")
                            .alertHeaderText("Edit Transaction Confirmation Type")
                            .alertContentText("Choose what type of transaction you want to create")
                            .alertButton(quotationType, outType, cancelType)
                            .build();

                    Optional<ButtonType> result = alert.showAndWait();
                    if(result.isPresent() && result.get() == quotationType){
                        okClicked = saleSystem.showTransactionEditDialog(selectedTransaction, Transaction.TransactionType.QUOTATION);
                    }else if(result.isPresent() && result.get() == outType){
                        okClicked = saleSystem.showTransactionEditDialog(selectedTransaction, Transaction.TransactionType.OUT);
                    }else{
                        alert.close();
                    }
                }else{
                    okClicked = saleSystem.showTransactionEditDialog(selectedTransaction, Transaction.TransactionType.OUT);
                }
                if(okClicked){
                    loadDataFromDB();
                }
            }

        }
    }
    @FXML
    private void handleInvoice(){
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        if(selectedTransaction != null){
            if(!selectedTransaction.getType().equals(Transaction.TransactionType.OUT) && !selectedTransaction.getType().equals(Transaction.TransactionType.RETURN)
                    && !selectedTransaction.getType().equals(Transaction.TransactionType.QUOTATION)){
                new AlertBuilder()
                        .alertType(Alert.AlertType.ERROR)
                        .alertContentText("Please select OUT/QUOTATION/RETURN transaction to generate Invoice!\n")
                        .alertTitle("Invoice Generation Error")
                        .build()
                        .showAndWait();
            }else{
                Customer customer = customerList
                        .stream()
                        .filter(c -> c.getUserName().equals(selectedTransaction.getInfo()))
                        .findFirst()
                        .get();
                saleSystem.showInvoiceDirectoryEditDialog(customer, selectedTransaction, this.saleSystem.getStaff());
            }
        }

    }

    public TransactionOverviewController(){
        dbExecuteTransaction = new DBExecuteTransaction();
        dbExecuteCustomer = new DBExecuteCustomer();
        dbExecuteProduct = new DBExecuteProduct();
    }

    @Override
    public void loadDataFromDB() {
        Task<List<Transaction>> transactionListTask = new Task<List<Transaction>>() {
            @Override
            protected List<Transaction> call() throws Exception {
                progressBar.setVisible(true);
                List<Transaction> tmpTransactionList = new ArrayList<>();
                for(int i = 0; i < 1; i++){
                    tmpTransactionList = dbExecuteTransaction.selectFromDatabase(DBQueries.SelectQueries.Transaction.SELECT_ALL_TRANSACTION);
                    updateProgress(i+1, 1);
                }
                return  tmpTransactionList;
            }
        };
        Task<List<Customer>> customerListTask = new Task<List<Customer>>() {
            @Override
            protected List<Customer> call() throws Exception {
                List<Customer> tmpCustomerList = new ArrayList<>();
                for(int i = 0; i < 1; i++){
                    tmpCustomerList = dbExecuteCustomer.selectFromDatabase(DBQueries.SelectQueries.Customer.SELECT_ALL_CUSTOMER);
                    updateProgress(i+1, 1);
                }
                return tmpCustomerList;
            }
        };
        Task<List<Product>> productListTask = new Task<List<Product>>() {
            @Override
            protected List<Product> call() throws Exception {
                List<Product> tmpProductList = new ArrayList<>();
                for(int i = 0; i < 1; i++){
                    tmpProductList = dbExecuteProduct.selectFromDatabase(DBQueries.SelectQueries.Product.SELECT_ALL_PRODUCT);
                    updateProgress(i+1, 1);
                }
                return tmpProductList;
            }
        };
        transactionListTask.exceptionProperty().addListener((observable, oldValue, newValue) ->  {
            if(newValue != null) {
                Exception ex = (Exception) newValue;
                logger.error(ex.getMessage(), ex);
            }
        });
        productListTask.exceptionProperty().addListener((observable, oldValue, newValue) ->  {
            if(newValue != null) {
                Exception ex = (Exception) newValue;
                logger.error(ex.getMessage(), ex);
            }
        });
        customerListTask.exceptionProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                Exception ex = (Exception) newValue;
                logger.error(ex.getMessage(), ex);
            }
        });
        final int numTasks = 3;
        DoubleBinding totalProgress = Bindings.createDoubleBinding(() -> ( Math.max(0, transactionListTask.getProgress())
                + Math.max(0, customerListTask.getProgress())
                + Math.max(0, productListTask.getProgress())) / numTasks, transactionListTask.progressProperty(), customerListTask.progressProperty(), productListTask.progressProperty());

        progressBar.progressProperty().bind(totalProgress);
        transactionListTask.setOnSucceeded(event -> {
            transactionList = FXCollections.observableArrayList(transactionListTask.getValue());
            transactionTable.setItems(transactionList);
            transactionTable.getSelectionModel().selectFirst();
            FilteredList<Transaction> filteredData = new FilteredList<Transaction>(transactionList,p->true);
            filterField.textProperty().addListener((observable,oldVal,newVal)->{
                filteredData.setPredicate(transaction -> {
                    if (newVal == null || newVal.isEmpty()){
                        return true;
                    }
                    String lowerCase = newVal.toLowerCase();
                    if (String.valueOf(transaction.getTransactionId()).toLowerCase().equals(lowerCase)){
                        return true;
                    }else if (transaction.getType().name().toLowerCase().toLowerCase().contains(lowerCase)){
                        return true;
                    }else if (transaction.getDate().toString().toLowerCase().toLowerCase().contains(lowerCase)){
                        return true;
                    }else if (transaction.getInfo().toLowerCase().toLowerCase().contains(lowerCase)){
                        return true;
                    }else if (!transaction.getType().equals(Transaction.TransactionType.IN) &&
                            customerList.stream().filter(c -> c.getUserName().equals(transaction.getInfo())).findFirst().get().getPhone() != null &&
                            customerList.stream().filter(c -> c.getUserName().equals(transaction.getInfo())).findFirst().get().getPhone().toLowerCase().contains(lowerCase)){
                        return true;
                    }
                    return false;
                });
                transactionTable.setItems(filteredData);
            });
        });
        transactionListTask.setOnFailed(event -> {
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertContentText(Constant.DatabaseError.databaseReturnError + event.getSource().exceptionProperty().getValue())
                    .alertHeaderText(Constant.DatabaseError.databaseErrorAlertTitle)
                    .build()
                    .showAndWait();
        });
        customerListTask.setOnSucceeded(event -> {
            customerList = customerListTask.getValue();
            infoCol.setCellValueFactory(param -> {
               if(param.getValue().getType().equals(Transaction.TransactionType.IN)){
                   return new SimpleStringProperty(param.getValue().getInfo());
               }else{
                   Optional<Customer> tmpCustomer = customerList.stream().filter(customer -> customer.getUserName().equals(param.getValue().getInfo()))
                           .findFirst();
                   if(tmpCustomer.isPresent()){
                       return new SimpleStringProperty(tmpCustomer.get().getFirstName() + " " + tmpCustomer.get().getLastName());
                   }else{
                       return new SimpleStringProperty("");
                   }
               }
            });
        });
        customerListTask.setOnFailed(event -> {
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertContentText(Constant.DatabaseError.databaseReturnError + event.getSource().exceptionProperty().getValue())
                    .alertHeaderText(Constant.DatabaseError.databaseErrorAlertTitle)
                    .build()
                    .showAndWait();
        });
        productListTask.setOnSucceeded(event -> {
            productList = productListTask.getValue();
        });
        productListTask.setOnFailed(event -> {
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertContentText(Constant.DatabaseError.databaseReturnError + event.getSource().exceptionProperty().getValue())
                    .alertHeaderText(Constant.DatabaseError.databaseErrorAlertTitle)
                    .build()
                    .showAndWait();
        });
        executor.execute(transactionListTask);
        executor.execute(customerListTask);
        executor.execute(productListTask);
    }

    @Override
    public void setMainClass(SaleSystem saleSystem) {
        this.saleSystem = saleSystem;
        if(!this.saleSystem.getStaff().getPosition().equals(Staff.Position.MANAGER)){
            buttonHbox.getChildren().remove(deleteButton);
            buttonHbox.getChildren().remove(stockButton);
        }
        loadDataFromDB();
    }

    @Override
    public void setDialogStage(Stage stage){
        this.dialogStage = stage;
    }

    public void showTransactionDetail(Transaction transaction){
        if(transaction != null){
            transactionIdLabel.setText(String.valueOf(transaction.getTransactionId()));
            dateLabel.setText(DateUtil.format(transaction.getDate()));
            paymentTypeLabel.setText(transaction.getPaymentType());
            storeCreditLabel.setText(String.valueOf(transaction.getStoreCredit()));
            staffLabel.setText(String.valueOf(transaction.getStaffId()));
            typeLabel.setText(transaction.getType().name());
            infoLabel.setText(transaction.getInfo());
            noteLabel.setText(transaction.getNote());
            if(!saleSystem.getStaff().getPosition().equals(Staff.Position.MANAGER)
                    && transaction.getType().equals(Transaction.TransactionType.IN)){
                unitPriceCol.setVisible(false);
                subTotalCol.setVisible(false);
                totalLabel.setText("Only Manager Can View This Info");
                paymentLabel.setText("Only Manager Can View This Info");
            }else{
                unitPriceCol.setVisible(true);
                subTotalCol.setVisible(true);
                totalLabel.setText(String.valueOf(transaction.getTotal()));
                paymentLabel.setText(String.valueOf(transaction.getPayment()));
            }
            transactionDetaiTableView.setItems(
                    FXCollections.observableArrayList(transaction.getProductTransactionList()));
            paymentRecordTableView.setItems(FXCollections.observableArrayList(transaction.getPayinfo()));
        }
        else{
            transactionIdLabel.setText("");
            dateLabel.setText("");
            totalLabel.setText("");
            paymentLabel.setText("");
            paymentTypeLabel.setText("");
            storeCreditLabel.setText("");
            staffLabel.setText("");
            typeLabel.setText("");
            infoLabel.setText("");
            noteLabel.setText("");
        }
    }
}
