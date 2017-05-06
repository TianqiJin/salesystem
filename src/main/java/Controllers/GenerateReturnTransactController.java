package Controllers;

import Constants.Constant;
import MainClass.SaleSystem;
import db.*;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.*;
import org.apache.log4j.Logger;
import util.AlertBuilder;
import util.AutoCompleteComboBoxListener;
import util.ButtonCell;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by tjin on 1/12/2016.
 */

public class GenerateReturnTransactController {

    public static Logger logger= Logger.getLogger(GenerateReturnTransactController.class);
    private final static String INIT_TRANSACTION_PAYMENT_TYPE = "Cash";
    private Stage dialogStage;
    private Customer customer;
    private List<Product> productList;
    private ObservableList<ProductTransaction> productTransactionObservableList;
    private DBExecuteProduct dbExecuteProduct;
    private DBExecuteCustomer dbExecuteCustomer;
    private DBExecuteTransaction dbExecuteTransaction;
    private SaleSystem saleSystem;
    private Transaction transaction;
    private StringBuffer errorMsgBuilder;
    private boolean confirmedClicked;
    private BooleanBinding confirmButtonBinding;
    private Executor executor;

    @FXML
    private TableView<ProductTransaction> transactionTableView;
    @FXML
    private TableColumn<ProductTransaction, Integer> productIdCol;
    @FXML
    private TableColumn<ProductTransaction, Integer> stockCol;
    @FXML
    private TableColumn<ProductTransaction, BigDecimal> unitPriceCol;
    @FXML
    private TableColumn<ProductTransaction, Float> qtyCol;
    @FXML
    private TableColumn<ProductTransaction, Integer> sizeCol;
    @FXML
    private TableColumn<ProductTransaction, Integer> discountCol;
    @FXML
    private TableColumn<ProductTransaction, BigDecimal> subTotalCol;
    @FXML
    private TableColumn deleteCol;

    @FXML
    private Label firstNameLabel;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Label discountLabel;
    @FXML
    private Label storeCreditLabel;

    @FXML
    private Label itemsCountLabel;
    @FXML
    private Label subTotalLabel;
    @FXML
    private Label gstTaxLabel;
    @FXML
    private Label pstTaxLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label paymentDiscountLabel;

    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label balanceLabel;
    @FXML
    private TextField returnAmountField;
    @FXML
    private ChoiceBox paymentTypeChoiceBox;

    @FXML
    private void initialize(){
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("totalNum"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("sizeNumeric"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
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
        qtyCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ProductTransaction, Float>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<ProductTransaction, Float> event) {
                (event.getTableView().getItems().get(event.getTablePosition().getRow()))
                        .setQuantity(event.getNewValue());
                showPaymentDetails(productTransactionObservableList, customer);
            }
        });
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discount"));
        subTotalCol.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        deleteCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ProductTransaction, Boolean>,
                        ObservableValue<Boolean>>() {
                    @Override
                    public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<ProductTransaction, Boolean> p) {
                        return new SimpleBooleanProperty(p.getValue() != null);
                    }
                });

        deleteCol.setCellFactory(
                new Callback<TableColumn<ProductTransaction, Boolean>, TableCell<ProductTransaction, Boolean>>() {
                    @Override
                    public TableCell<ProductTransaction, Boolean> call(TableColumn<ProductTransaction, Boolean> p) {
                        return new ButtonCell(transactionTableView);
                    }

                });
        paymentTypeChoiceBox.getSelectionModel().selectFirst();
        paymentTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                transaction.setPaymentType(newValue);
            }
        });
        returnAmountField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                showBalanceDetails();
            }
        });

        showCustomerDetails(null);
        showPaymentDetails(null, null);

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
        if(!isTransactionValid()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Transaction Is Invalid");
            alert.setHeaderText("Please fix the following errors before proceed");
            alert.setContentText(errorMsgBuilder.toString());
            alert.showAndWait();
            transaction.getProductTransactionList().clear();
        }
        else{
            transaction.getProductTransactionList().addAll(productTransactionObservableList);
            if(transaction.getPaymentType().equals("Store Credit")){
                transaction.setStoreCredit(Double.valueOf(returnAmountField.getText().trim()));
            }else{
                transaction.setPayment(Double.valueOf(returnAmountField.getText().trim()));
            }
            transaction.setGstTax(Double.valueOf(gstTaxLabel.getText()));
            transaction.setPstTax(Double.valueOf(pstTaxLabel.getText()));
            transaction.setTotal(Double.valueOf(totalLabel.getText()));
            transaction.getPayinfo().add(new PaymentRecord(
                    transaction.getDate().toString(),
                    transaction.getPayment() + transaction.getStoreCredit(),
                    transaction.getPaymentType(),
                    false));
            StringBuffer overviewTransactionString = new StringBuffer();
            StringBuffer overviewProductTransactionString = new StringBuffer();

            for(ProductTransaction tmp: transaction.getProductTransactionList()){
                overviewProductTransactionString
                        .append("Product ID: " + tmp.getProductId() + "\n")
                        .append("Returned Quantity: " + tmp.getQuantity() + "\n")
                        .append("Unit Price: " + tmp.getUnitPrice() + "\n")
                        .append("Sub Total: " + tmp.getSubTotal() + "\n")
                        .append("\n");
            }
            overviewTransactionString
                    .append("Customer Name: " + customer.getFirstName() + " " + customer.getLastName() + "\n\n")
                    .append(overviewProductTransactionString)
                    .append("\n" + "Total: " + totalLabel.getText() + "\n")
                    .append("Returned Store Credit: " + transaction.getStoreCredit() + "\n")
                    .append("Returned Money: " + transaction.getPayment() + "\n")
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
                transaction.setPayment(0);
                transaction.setStoreCredit(0);
            }
        }
        return transaction;
    }
    /*
    * Constructor
    * */
    public GenerateReturnTransactController(){
        dbExecuteProduct = new DBExecuteProduct();
        dbExecuteCustomer = new DBExecuteCustomer();
        dbExecuteTransaction = new DBExecuteTransaction();
        confirmedClicked = false;
    }

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }
    /*
    * Show customer details grid pane
    * */
    private void showCustomerDetails(Customer customer){
        if(customer != null){
            transaction.setInfo(customer.getUserName());
            firstNameLabel.setText(customer.getFirstName());
            lastNameLabel.setText(customer.getLastName());
            discountLabel.setText(customer.getUserClass());
            storeCreditLabel.setText(String.valueOf(customer.getStoreCredit()));
        }
        else{
            firstNameLabel.setText("");
            lastNameLabel.setText("");
            discountLabel.setText("");
            storeCreditLabel.setText("");
        }
    }
    /**
     * Show payment details grid pane
     */
    private void showPaymentDetails(ObservableList<ProductTransaction> productTransactions, Customer customer){
        if(productTransactions != null ){
            Iterator<ProductTransaction> iterator = productTransactions.iterator();
            BigDecimal subTotalAfterDiscount = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal subTotalBeforediscount = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            while(iterator.hasNext()){
                ProductTransaction tmp = iterator.next();
                subTotalAfterDiscount = subTotalAfterDiscount.add(
                        new BigDecimal(tmp.getSubTotal()).setScale(2, BigDecimal.ROUND_HALF_EVEN)
                );
                subTotalBeforediscount = subTotalBeforediscount.add(new BigDecimal(tmp.getUnitPrice()*tmp.getQuantity()).setScale(2, BigDecimal.ROUND_HALF_EVEN));
            }
            BigDecimal paymentDiscount = subTotalBeforediscount.subtract(subTotalAfterDiscount).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal pstTax;
            if(customer != null && customer.getPstNumber() != null){
                pstTax = new BigDecimal("0.0");
            }else{
                pstTax = new BigDecimal(saleSystem.getProperty().getPstRate()).multiply(subTotalAfterDiscount).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            }
            BigDecimal gstTax = new BigDecimal(saleSystem.getProperty().getGstRate()).multiply(subTotalAfterDiscount).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal total = subTotalAfterDiscount.add(pstTax).add(gstTax).setScale(2, BigDecimal.ROUND_HALF_EVEN);

            itemsCountLabel.setText(String.valueOf(productTransactions.size()));
            subTotalLabel.setText(subTotalAfterDiscount.toString());
            paymentDiscountLabel.setText(paymentDiscount.toString());
            pstTaxLabel.setText(pstTax.toString());
            gstTaxLabel.setText(gstTax.toString());
            totalLabel.setText(total.toString());
            showBalanceDetails();
        }
        else{
            itemsCountLabel.setText("");
            subTotalLabel.setText("");
            pstTaxLabel.setText("");
            gstTaxLabel.setText("");
            totalLabel.setText("");
            balanceLabel.setText("");
            paymentDiscountLabel.setText("");
        }
    }

    private void showBalanceDetails(){
        BigDecimal balance;
        if(!returnAmountField.getText().trim().isEmpty() && isReturnAmountValid()){
            balance = new BigDecimal(returnAmountField.getText()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            balance = balance.subtract(new BigDecimal(totalLabel.getText()));
            balanceLabel.setText(balance.toString());
        }
        else{
            balanceLabel.setText("");
        }
    }
    /*
    * Initilize the main class for this class
    * */
    public void setMainClass(SaleSystem saleSystem) {
        this.saleSystem = saleSystem;
    }

    public void setSelectedTransaction(Transaction selectedTransaction){
        this.transaction = selectedTransaction;
        Task<Customer> customerTask = new Task<Customer>() {
            @Override
            protected Customer call() throws Exception {
                return dbExecuteCustomer.selectFromDatabase(DBQueries.SelectQueries.Customer.SELECT_SINGLE_CUSTOMER, selectedTransaction.getInfo()).get(0);
            }
        };
        Task<List<Product>> productListTask = new Task<List<Product>>() {
            @Override
            protected List<Product> call() throws Exception {
                return dbExecuteProduct.selectFromDatabase(DBQueries.SelectQueries.Product.SELECT_ALL_PRODUCT);
            }
        };

        productListTask.setOnSucceeded(event -> {
            this.productList = productListTask.getValue();
            transaction.getProductTransactionList().forEach(productTransaction -> {
                Product tmp = productList.stream()
                        .filter(product -> product.getProductId().equals(productTransaction.getProductId()))
                        .findFirst()
                        .orElse(null);

                if(tmp == null){
                    new AlertBuilder()
                            .alertTitle("Product Error!")
                            .alertType(Alert.AlertType.WARNING)
                            .alertContentText("Product - " + productTransaction.getProductId() + " does not exist!")
                            .build()
                            .showAndWait();
                    dialogStage.close();
                }else{
                    productTransaction.setTotalNum(tmp.getTotalNum());
                }
            });
            this.productTransactionObservableList = FXCollections.observableArrayList(this.transaction.getProductTransactionList());
            this.productTransactionObservableList.addListener(new ListChangeListener<ProductTransaction>() {
                @Override
                public void onChanged(Change<? extends ProductTransaction> c) {
                    while (c.next()) {
                        if (c.wasAdded() || c.wasRemoved()) {
                            showPaymentDetails(productTransactionObservableList, customer);
                        }
                    }
                }
            });
            transactionTableView.setItems(this.productTransactionObservableList);
            this.transaction.setType(Transaction.TransactionType.RETURN);
            this.transaction.setPayment(0.0);
            this.transaction.setStoreCredit(0.0);
            this.transaction.setDate(LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
            this.transaction.getPayinfo().clear();
            this.transaction.getProductTransactionList().clear();
            executor.execute(customerTask);
        });

        productListTask.setOnFailed(event -> {
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertContentText(Constant.DatabaseError.databaseReturnError + event.getSource().exceptionProperty().getValue())
                    .alertHeaderText(Constant.DatabaseError.databaseErrorAlertTitle)
                    .build()
                    .showAndWait();
        });

        customerTask.setOnSucceeded(event -> {
            this.customer = customerTask.getValue();
            showCustomerDetails(this.customer);
            showPaymentDetails(this.productTransactionObservableList, this.customer);
        });

        customerTask.setOnFailed(event -> {
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertContentText(Constant.DatabaseError.databaseReturnError + event.getSource().exceptionProperty().getValue())
                    .alertHeaderText(Constant.DatabaseError.databaseErrorAlertTitle)
                    .build()
                    .showAndWait();
        });
        executor.execute(productListTask);
    }


    private boolean isTransactionValid(){
        errorMsgBuilder = new StringBuffer();
        if(customer == null){
            errorMsgBuilder.append("Customer is neither selected nor created!\n");
        }
        if(returnAmountField.getText().trim().isEmpty()){
            errorMsgBuilder.append("The Returned Money/Store Credit can't be empty!\n");
        }else{
            if(!isReturnAmountValid()){
                errorMsgBuilder.append("The Returned Money/Store Credit must be numbers!\n");
            }
        }
        if(errorMsgBuilder.length() != 0){
            return false;
        }
        return true;
    }

    private boolean isReturnAmountValid(){
        try{
            Double.parseDouble(returnAmountField.getText());
        }catch(NumberFormatException e){
            return false;
        }
        return true;
    }

    private void commitTransactionToDatabase() throws SQLException, IOException {
        Task<Customer> customerTask = new Task<Customer>() {
            @Override
            protected Customer call() throws Exception {
              return dbExecuteCustomer.selectFromDatabase(DBQueries.SelectQueries.Customer.SELECT_SINGLE_CUSTOMER, customer.getUserName()).get(0);
            }
        };
        Task<List<Product>> productListTask = new Task<List<Product>>() {
            @Override
            protected List<Product> call() throws Exception {
                return dbExecuteProduct.selectFromDatabase(DBQueries.SelectQueries.Product.SELECT_ALL_PRODUCT);
            }
        };
        Task<Void> commitTask = new Task<Void>(){
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
                    dbExecuteProduct.updateDatabase(DBQueries.UpdateQueries.Product.UPDATE_PRODUCT_QUANTITY, remain, tmp.getProductId());
                }
                if(transaction.getPaymentType().equals("Store Credit")){
                    double remainStoreCredit = customer.getStoreCredit() + Double.valueOf(returnAmountField.getText());
                    dbExecuteCustomer.updateDatabase(DBQueries.UpdateQueries.Customer.UPDATE_CUSTOMER_STORE_CREDIT, remainStoreCredit, customer.getUserName());
                }
                connection.commit();
                return null;
            }
        };
        productListTask.setOnSucceeded(event -> {
            List<String> missingProducts = new ArrayList<String>();
            transaction.getProductTransactionList().forEach(productTransaction -> {
                 Optional<Product> tmp = productListTask.getValue().stream()
                        .filter(product -> product.getProductId().equals(productTransaction.getProductId()))
                        .findFirst();
                if (!tmp.isPresent()) {
                    missingProducts.add(productTransaction.getProductId());
                } else {
                    productTransaction.setTotalNum(tmp.get().getTotalNum());
                }
            });
            if(missingProducts.size() != 0){
                StringBuilder sb = new StringBuilder();
                missingProducts.forEach(p -> sb.append(p).append("\n"));
                new AlertBuilder()
                        .alertTitle("Product Error!")
                        .alertType(Alert.AlertType.ERROR)
                        .alertContentText("The following products do not exist!\n" + sb.toString())
                        .build()
                        .showAndWait();
                dialogStage.close();
            }else{
                executor.execute(customerTask);
            }
        });
        customerTask.setOnSucceeded(event->{
            customer.setStoreCredit(customerTask.getValue().getStoreCredit());
            executor.execute(commitTask);
        });
        commitTask.setOnSucceeded(event->{
            Connection connection = DBConnect.getConnection();
            dialogStage.close();
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
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
        executor.execute(productListTask);
    }

    public Transaction returnNewTrasaction(){
        return this.transaction;
    }

    public boolean isConfirmedClicked(){
        return this.confirmedClicked;
    }

    private void refreshTable(){
        transactionTableView.getColumns().get(0).setVisible(false);
        transactionTableView.getColumns().get(0).setVisible(true);
    }
}

