package Controllers;

import MainClass.SaleSystem;
import db.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.*;
import util.AutoCompleteComboBoxListener;
import util.ButtonCell;
import util.DateUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by tjin on 1/25/2016.
 */
public class TransactionEditDialogController {
    private final static String INIT_TRANSACTION_PAYMENT_TYPE = "Cash";
    private Stage dialogStage;
    private Customer customer;
    private ObservableList<ProductTransaction> productTransactionObservableList;
    private DBExecuteProduct dbExecuteProduct;
    private DBExecuteCustomer dbExecuteCustomer;
    private DBExecuteTransaction dbExecuteTransaction;
    private SaleSystem saleSystem;
    private Transaction transaction;
    private StringBuffer errorMsgBuilder;
    private boolean confirmedClicked;
    private BooleanBinding confimButtonBinding;
    private int discount;

    @FXML
    private TableView<ProductTransaction> transactionTableView;
    @FXML
    private TableColumn<ProductTransaction, Integer> productIdCol;
    @FXML
    private TableColumn<ProductTransaction, BigDecimal> unitPriceCol;
    @FXML
    private TableColumn<ProductTransaction, Integer> qtyCol;
    @FXML
    private TableColumn<ProductTransaction, BigDecimal> subTotalCol;

    @FXML
    private Label firstNameLabel;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Label storeCreditLabel;
    @FXML
    private Label discountLabel;

    @FXML
    private Label itemsCountLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label residualLabel;

    @FXML
    private TextField paymentField;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label balanceLabel;
    @FXML
    private ChoiceBox<String> paymentTypeChoiceBox;
    @FXML
    private ChoiceBox<Integer> discountChoiceBox;
    @FXML
    private TextField storeCreditField;
    @FXML
    private CheckBox storeCreditCheckBox;

    @FXML
    private void initialize(){
        confimButtonBinding = paymentField.textProperty().isEmpty().or(Bindings.size(transactionTableView.getItems()).greaterThan(1));
        confirmButton.disableProperty().bind(confimButtonBinding);
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        subTotalCol.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        paymentField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                showBalanceDetails();
            }
        });
        storeCreditField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                showBalanceDetails();
            }
        });
        showCustomerDetails(null);
        showPaymentDetails(null);

        paymentTypeChoiceBox.getSelectionModel().selectFirst();
        paymentTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                transaction.setPaymentType(newValue);
            }
        });
//        discountChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
//            @Override
//            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
//                discount = newValue;
//                showPaymentDetails(productTransactionObservableList, customer);
//            }
//        });
        storeCreditCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                storeCreditField.setDisable(newValue ? false : true);
                if(!newValue){
                    storeCreditField.clear();
                }
            }
        });
    }


    @FXML
    public void handleCancelButton(){
        this.dialogStage.close();
    }

    @FXML
    public void handleConfirmButton() throws IOException, SQLException {
        if(!isTransactionValid()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Transaction Is Invalid");
            alert.setHeaderText("Please fix the following errors before proceed");
            alert.setContentText(errorMsgBuilder.toString());
            alert.showAndWait();
            transaction.getProductTransactionList().clear();
        }
        else{
            String originalPaymentType = transaction.getPaymentType();
            double originalPayment = transaction.getPayment();
            double originalStoreCredit = transaction.getStoreCredit();
            LocalDate originalDate = transaction.getDate();
            List<PaymentRecord> originalPayInfo = transaction.getPayinfo();

            double currentPayment = 0;
            double currentStoreCredit = 0;
            if(!paymentField.getText().trim().isEmpty()){
                currentPayment = Double.valueOf(paymentField.getText());
            }
            if(!storeCreditField.getText().trim().isEmpty()){
                currentStoreCredit = Double.valueOf(storeCreditField.getText());
            }
            transaction.setDate(DateUtil.parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
            transaction.setPayment(originalPayment + currentPayment);
            if(storeCreditCheckBox.isSelected()){
                transaction.setStoreCredit(originalStoreCredit + currentStoreCredit);
            }
            transaction.getPayinfo().add(new PaymentRecord(
                    transaction.getDate().toString(),
                    currentPayment + currentStoreCredit,
                    transaction.getPaymentType()));

            StringBuffer overviewTransactionString = new StringBuffer();
            StringBuffer overviewProductTransactionString = new StringBuffer();
            for(ProductTransaction tmp: transaction.getProductTransactionList()){
                overviewProductTransactionString
                        .append("Product ID: " + tmp.getProductId() + " ")
                        .append("Total Num: " + tmp.getTotalNum() + " ")
                        .append("Quantity: " + tmp.getQuantity() + " ")
                        .append("Unit Price: " + tmp.getUnitPrice() + " ")
                        .append("Sub Total: " + tmp.getSubTotal() + " ")
                        .append("\n");
            }
            overviewTransactionString
                    .append("Customer Name: " + customer.getFirstName() + " " + customer.getLastName() + "\n\n")
                    .append(overviewProductTransactionString)
                    .append("\n" + "Total: " + totalLabel.getText() + "\n")
                    .append("Payment: " + currentPayment + "\n")
                    .append("Store Credit: " + currentStoreCredit + "\n")
                    .append("Payment Type: " + transaction.getPaymentType() + "\n")
                    .append("Date: " + transaction.getDate() + "\n");

            Alert alert = new Alert(Alert.AlertType.INFORMATION, overviewTransactionString.toString(), ButtonType.OK, ButtonType.CANCEL);
            alert.setTitle("Transaction Overview");
            alert.setHeaderText("Please confirm the following transaction");
            alert.setResizable(true);
            alert.getDialogPane().setPrefWidth(500);
            Optional<ButtonType> result = alert.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.OK){
                commitTransactionToDatabase();
                confirmedClicked = true;
                dialogStage.close();
            }else{
                transaction.getProductTransactionList().clear();
                transaction.getPayinfo().clear();
                transaction.getPayinfo().addAll(originalPayInfo);
                transaction.setDate(originalDate);
                transaction.setPayment(originalPayment);
                transaction.setStoreCredit(originalStoreCredit);
                transaction.setPaymentType(originalPaymentType);
            }
        }
    }
    /*
    * Constructor
    * */
    public TransactionEditDialogController(){
        dbExecuteProduct = new DBExecuteProduct();
        dbExecuteCustomer = new DBExecuteCustomer();
        dbExecuteTransaction = new DBExecuteTransaction();
        confirmedClicked = false;
        discount = 100;
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
//            discountChoiceBox.setDisable(false);
//            discountChoiceBox.getItems().setAll(Customer.getDiscountMap().get(customer.getUserClass()));
            storeCreditLabel.setText(String.valueOf(customer.getStoreCredit()));
            discountLabel.setText(customer.getUserClass());
        }
        else{
            firstNameLabel.setText("");
            lastNameLabel.setText("");
//            discountChoiceBox.setDisable(true);
            storeCreditLabel.setText("");
            discountLabel.setText("");
        }
    }
    /**
     * Show payment details grid pane
     */
    private void showPaymentDetails(Transaction transaction){
        if(transaction != null ){
            BigDecimal residual = new BigDecimal(0);
            for(PaymentRecord paymentRecord: transaction.getPayinfo()){
                residual = residual.add(new BigDecimal(paymentRecord.getPaid()));
            }
            residual = new BigDecimal(transaction.getTotal()).subtract(residual).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            itemsCountLabel.setText(String.valueOf(transaction.getProductTransactionList().size()));
            totalLabel.setText(String.valueOf(transaction.getTotal()));
            residualLabel.setText(residual.toString());
            showBalanceDetails();
        }
        else{
            itemsCountLabel.setText("");
            residualLabel.setText("");
            totalLabel.setText("");
            balanceLabel.setText("");
        }
    }

    private void showBalanceDetails(){
        BigDecimal balance;
        if((storeCreditCheckBox.isSelected() && !storeCreditField.getText().trim().isEmpty() && isStoreCreditValidNoCustomer())
                &&(!paymentField.getText().trim().isEmpty() && isPaymentValid())){
            balance = new BigDecimal(residualLabel.getText()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            balance = balance.subtract(new BigDecimal(paymentField.getText())).subtract(new BigDecimal(storeCreditField.getText()));
            balanceLabel.setText(balance.toString());
        }
        else if(storeCreditCheckBox.isSelected() && !storeCreditField.getText().trim().isEmpty() && isStoreCreditValidNoCustomer()){
            balance = new BigDecimal(residualLabel.getText()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            balance = balance.subtract(new BigDecimal(storeCreditField.getText()));
            balanceLabel.setText(balance.toString());
        }
        else if(!paymentField.getText().trim().isEmpty() && isPaymentValid()){
            balance = new BigDecimal(residualLabel.getText()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            balance = balance.subtract(new BigDecimal(paymentField.getText()));
            balanceLabel.setText(balance.toString());
        }
        else{
            balanceLabel.setText("");
        }
    }

    public void setSelectedTransaction(Transaction transaction){
        this.transaction = transaction;
        this.productTransactionObservableList = FXCollections.observableArrayList(transaction.getProductTransactionList());
        transactionTableView.setItems(this.productTransactionObservableList);
        try{
            this.customer = dbExecuteCustomer.selectFromDatabase(DBQueries.SelectQueries.Customer.SELECT_SINGLE_CUSTOMER, this.transaction.getInfo()).get(0);
        }catch(SQLException e){
            Alert alert = new Alert(Alert.AlertType.WARNING, "Unable to grab data from database!\n" + e.getMessage());
            alert.setTitle("Database Error");
            alert.showAndWait();
        }
        showCustomerDetails(customer);
        showPaymentDetails(this.transaction);

    }

    private boolean isTransactionValid(){
        errorMsgBuilder = new StringBuffer();
        if(!isPaymentValid()){
            errorMsgBuilder.append("Payment must be numbers!\n");
        }
        if(customer == null){
            errorMsgBuilder.append("Customer is neither selected nor created!\n");
        }
        if(storeCreditCheckBox.isSelected() && !isStoreCreditValid()){
            errorMsgBuilder.append("Either Store Credit exceeds customer's limit or Store Credit must be numbers!\n");
        }
        if(!isProductQuantityValid()){
            errorMsgBuilder.append("Some product's quantity exceeds the stock quota!\n");
        }
        if(errorMsgBuilder.length() != 0){
            return false;
        }
        return true;
    }

    private boolean isPaymentValid(){
        try{
            Double.parseDouble(paymentField.getText());
        }catch(NumberFormatException e){
            return false;
        }
        return true;
    }

    private boolean isStoreCreditValid(){
        isStoreCreditValidNoCustomer();
        if(customer != null &&
                (customer.getStoreCredit() < Double.valueOf(storeCreditField.getText()))){
            return false;
        }
        return true;
    }

    private boolean isStoreCreditValidNoCustomer(){
        try{
            Double.parseDouble(storeCreditField.getText());
        }catch(NumberFormatException e){
            return false;
        }
        return true;
    }

    private boolean isProductQuantityValid(){
        for(ProductTransaction tmp : transaction.getProductTransactionList()){
            if(tmp.getTotalNum() - tmp.getQuantity() < 0){
                return false;
            }
        }
        return true;
    }

    private ArrayList<Integer> returnDiscount(){
        if(this.customer != null){
            return Customer.getDiscountMap().get(customer.getUserClass());
        }
        return null;
    }
    private void commitTransactionToDatabase() throws SQLException, IOException {
        Connection connection = DBConnect.getConnection();
        try{
            connection.setAutoCommit(false);
            Object[] objects = ObjectSerializer.TRANSACTION_OBJECT_SERIALIZER_UPDATE.serialize(transaction);
            dbExecuteTransaction.updateDatabase(DBQueries.UpdateQueries.Transaction.UPDATE_TRANSACTION_OUT, objects);
            if(storeCreditCheckBox.isSelected()){
                double remainStoreCredit = customer.getStoreCredit() - Double.valueOf(storeCreditField.getText());
                dbExecuteCustomer.updateDatabase(DBQueries.UpdateQueries.Customer.UPDATE_CUSTOMER_STORE_CREDIT,
                        remainStoreCredit, customer.getUserName());
            }
            connection.commit();
        }catch(SQLException e){
            connection.rollback(); //TODO: CRITICAL BUG!!!
            Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to update transaction to database!\n" + e.getMessage());
            alert.showAndWait();
        }
        connection.setAutoCommit(true);
    }
    public Transaction returnNewTrasaction(){
        return this.transaction;
    }

    public boolean isConfirmedClicked(){
        return this.confirmedClicked;
    }
}
