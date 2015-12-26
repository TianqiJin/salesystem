package Controllers;

import MainClass.SaleSystem;
import db.*;
import javafx.beans.property.IntegerProperty;
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
import javafx.util.StringConverter;
import model.Customer;
import model.Product;
import model.ProductTransaction;
import model.Transaction;
import util.AutoCompleteComboBoxListener;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by tjin on 12/7/2015.
 */
public class GenerateCustomerTransactController {

    private Stage dialogStage;
    private Customer customer;
    private List<Customer> customerList;
    private ObservableList<ProductTransaction> productTransactionObservableList;
    private DBExecuteProduct dbExecuteProduct;
    private DBExecuteCustomer dbExecuteCustomer;
    private DBExecuteTransaction dbExecuteTransaction;
    private SaleSystem saleSystem;
    private Transaction transaction;
    private StringBuffer errorMsgBuilder;
    private boolean confirmedClicked;

    @FXML
    private TableView<ProductTransaction> transactionTableView;
    @FXML
    private TableColumn<ProductTransaction, Integer> productIdCol;
    @FXML
    private TableColumn<ProductTransaction, Integer> stockCol;
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
    private Label discountLabel;
    @FXML
    private Label storeCreditLabel;

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
    private Button addNewCustomer;
    @FXML
    private TextField paymentField;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;
    @FXML
    private ComboBox customerComboBox;
    @FXML
    private Label balanceLabel;
    @FXML
    private ChoiceBox<String> paymentTypeChoiceBox;
    @FXML
    private TextField storeCreditField;
    @FXML
    private CheckBox storeCreditCheckBox;

    @FXML
    private void initialize(){
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("totalNum"));
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Integer>(){
            @Override
            public Integer fromString(String string) {
                return Integer.valueOf(string);
            }
            public String toString(Integer integer){
                return String.valueOf(integer);
            }
        }));
        qtyCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ProductTransaction, Integer>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<ProductTransaction, Integer> event) {
                (event.getTableView().getItems().get(event.getTablePosition().getRow()))
                        .setQuantity(event.getNewValue());
                showPaymentDetails(transaction, customer);
            }
        });
        subTotalCol.setCellValueFactory(new PropertyValueFactory<>("subTotal"));

        productIdField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(productIdField.getText().trim().isEmpty()){
                    addItemButton.setDisable(true);
                }
                else{
                    addItemButton.setDisable(false);
                }
            }
        });

        paymentField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if((paymentField.getText().trim().isEmpty() || transactionTableView.getItems().isEmpty())){
                    confirmButton.setDisable(true);
                    if(storeCreditCheckBox.isSelected() && !storeCreditField.getText().trim().isEmpty()
                            && isStoreCreditValid()){
                        BigDecimal balance = new BigDecimal(totalLabel.getText()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
                        balance = balance.subtract(new BigDecimal(storeCreditField.getText()));
                        balanceLabel.setText(balance.toString());
                    }
                    else{
                        balanceLabel.setText("");
                    }
                }
                else{
                    confirmButton.setDisable(false);
                    if(isPaymentValid()){
                        BigDecimal balance = new BigDecimal(totalLabel.getText()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
                        balance = balance.subtract(new BigDecimal(paymentField.getText()));
                        if(storeCreditCheckBox.isSelected() && !storeCreditField.getText().trim().isEmpty()
                                && isStoreCreditValid()){
                            balance = balance.subtract(new BigDecimal(storeCreditField.getText()));
                        }
                        balanceLabel.setText(balance.toString());
                    }
                }
            }
        });
        storeCreditField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!storeCreditCheckBox.isSelected() || storeCreditField.getText().trim().isEmpty()){
                    if(!paymentField.getText().trim().isEmpty() && isPaymentValid()){
                        BigDecimal balance = new BigDecimal(totalLabel.getText()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
                        balance = balance.subtract(new BigDecimal(paymentField.getText()));
                        balanceLabel.setText(balance.toString());
                    }
                    else{
                        balanceLabel.setText("");
                    }

                }
                else{
                    if(isStoreCreditValid()){
                        BigDecimal balance;
                        balance = new BigDecimal(totalLabel.getText()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
                        balance = balance.subtract(new BigDecimal(storeCreditField.getText()));
                        if(!paymentField.getText().trim().isEmpty()
                                && isPaymentValid()){
                            balance = balance.subtract(new BigDecimal(paymentField.getText()));
                        }
                        balanceLabel.setText(balance.toString());
                    }
                }
            }
        });
        showCustomerDetails(null);
        showPaymentDetails(null, null);

        paymentTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                transaction.setPaymentType(newValue);
            }
        });
        //TODO: Add default item

        customerList = dbExecuteCustomer.selectFromDatabase(DBQueries.SelectQueries.Customer.SELECT_ALL_CUSTOMER);
        List<String> tmpCustomerList = new ArrayList<>();
        for(Customer customer: customerList){
            customer.constructCustomerInfo();
            tmpCustomerList.add(customer.getCustomerInfo());
        }

        customerComboBox.setItems(FXCollections.observableArrayList(tmpCustomerList));
        customerComboBox.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                for(Customer tmpCustomer: customerList){
                    if(tmpCustomer.getCustomerInfo().equals(newValue)){
                        customer = tmpCustomer;
                        showCustomerDetails(customer);
                        break;
                    }
                }

            }
        });
        new AutoCompleteComboBoxListener<>(customerComboBox);
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
    public void handleAddItem(){
        List<Product> productResult = dbExecuteProduct.selectFromDatabase(DBQueries.SelectQueries.Product.SELECT_PRODUCTID_PROJECT,
                productIdField.getText().trim());
        if(productResult.isEmpty()){
             Alert alert = new Alert(Alert.AlertType.WARNING);
             alert.initOwner(dialogStage);
             alert.setTitle("Invalid Product ID");
             alert.setHeaderText(null);
             alert.setContentText("Please input valid product ID");
             alert.showAndWait();
         }
        else{
            ProductTransaction newTransaction = new ProductTransaction.ProductTransactionBuilder()
                    .productId(productResult.get(0).getProductId())
                    .totalNum(productResult.get(0).getTotalNum())
                    .unitPrice(productResult.get(0).getUnitPrice())
                    .build();
            transaction.getProductTransactionList().add(newTransaction);
            productTransactionObservableList.add(newTransaction);
        }

    }

    @FXML
    public void handleAddCustomer(){
        Customer newCustomer = new Customer();
        boolean okClicked = saleSystem.showCustomerEditDialog(newCustomer);
        if(okClicked){
            newCustomer.setUserName();
            if(dbExecuteProduct.insertIntoDatabase(DBQueries.InsertQueries.Customer.INSERT_INTO_CUSTOMER,
                    newCustomer.getAllProperties()) == 0){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Unable To Add New Customer");
                alert.setHeaderText(null);
                alert.setContentText("Unable To Add New Customer" + newCustomer.getFirstName() + " " + newCustomer.getLastName());
                alert.showAndWait();
            }
            else{
                customer = newCustomer;
                showCustomerDetails(customer);
            }
        }
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
        }
        else{
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
                    .append("Customer Name:" + customer.getFirstName() + " " + customer.getLastName() + "\n\n")
                    .append(overviewProductTransactionString)
                    .append("\n" + "Payment: " + transaction.getPayment() + "\n")
                    .append("Payment Type: " + transaction.getPaymentType() + "\n")
                    .append("Date: " + transaction.getDate() + "\n");

            transaction.setPayment(Double.valueOf(paymentField.getText()));
            Alert alert = new Alert(Alert.AlertType.INFORMATION, overviewTransactionString.toString(), ButtonType.OK, ButtonType.CANCEL);
            alert.setTitle("Transaction Overview");
            alert.setHeaderText("Please confirm the following transaction");
            Optional<ButtonType> result = alert.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.OK){
                commitTransactionToDatabase();
                confirmedClicked = true;
                dialogStage.close();
            }
        }
        return transaction;
    }
    /*
    * Constructor
    * */
    public GenerateCustomerTransactController(){
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
    private void showPaymentDetails(Transaction transaction, Customer customer){
        if(transaction != null ){
            Iterator<ProductTransaction> iterator = transaction.getProductTransactionList().iterator();
            BigDecimal subTotalAll = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            while(iterator.hasNext()){
                subTotalAll = subTotalAll.add(
                        new BigDecimal(iterator.next().getSubTotal()).setScale(2, BigDecimal.ROUND_HALF_EVEN)
                );
            }
            BigDecimal total = subTotalAll.multiply(new BigDecimal(1.05)).setScale(2,BigDecimal.ROUND_HALF_EVEN);
            itemsCountLabel.setText(String.valueOf(transaction.getProductTransactionList().size()));
            subTotalLabel.setText(subTotalAll.toString());
            totalLabel.setText(total.toString());
        }
        else{
            itemsCountLabel.setText("");
            subTotalLabel.setText("");
            totalLabel.setText("");
            balanceLabel.setText("");
        }
    }

    /*
    * Initilize the main class for this class
    * */
    public void setMainClass(SaleSystem saleSystem){
        this.saleSystem = saleSystem;
        transaction = new Transaction.TransactionBuilder()
                .productInfoList(new ArrayList<ProductTransaction>())
                .staffId(saleSystem.getStaffId())
                .date(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                .type(Transaction.TransactionType.OUT)
                .build();
        productTransactionObservableList = FXCollections.observableArrayList(transaction.getProductTransactionList());
        transactionTableView.setItems(productTransactionObservableList);
        productTransactionObservableList.addListener(new ListChangeListener<ProductTransaction>() {
            @Override
            public void onChanged(Change<? extends ProductTransaction> c) {
                while(c.next()){
                    if( c.wasAdded() || c.wasRemoved()){
                        showPaymentDetails(transaction, customer);
                    }
                }
            }
        });
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
        try{
            Integer.parseInt(storeCreditField.getText());
        }catch(NumberFormatException e){
            return false;
        }
        if(customer != null &&
                (customer.getStoreCredit() < Integer.valueOf(storeCreditField.getText()))){
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

    private void commitTransactionToDatabase() throws SQLException, IOException {
        Connection connection = DBConnect.getConnection();
        try{
            connection.setAutoCommit(false);
            Object[] objects = ObjectSerializer.TRANSACTION_OBJECT_SERIALIZER.serialize(transaction);
            System.out.println(Arrays.toString(objects));
            dbExecuteTransaction.insertIntoDatabase(DBQueries.InsertQueries.Transaction.INSERT_INTO_TRANSACTION,
                    objects);
            for(ProductTransaction tmp : transaction.getProductTransactionList()){
                int remain = tmp.getTotalNum() - tmp.getQuantity();
                dbExecuteProduct.updateDatabase(DBQueries.UpdateQueries.Product.UPDATE_PRODUCT_QUANTITY,
                        remain, tmp.getProductId());
            }
            if(storeCreditCheckBox.isSelected()){
                int remainStoreCredit = customer.getStoreCredit() - Integer.valueOf(storeCreditField.getText());
                dbExecuteCustomer.updateDatabase(DBQueries.UpdateQueries.Customer.UPDATE_CUSTOMER_STORE_CREDIT,
                        remainStoreCredit, customer.getUserName());
            }
            connection.commit();
        }catch(SQLException e){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to store transaction to database!");
            alert.showAndWait();
            connection.rollback();
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
