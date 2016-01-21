package Controllers;

import MainClass.SaleSystem;
import com.sun.prism.impl.Disposer;
import db.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
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
import model.Customer;
import model.Product;
import model.ProductTransaction;
import model.Transaction;
import util.AutoCompleteComboBoxListener;
import util.ButtonCell;

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

    private final static String INIT_TRANSACTION_PAYMENT_TYPE = "Cash";
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
    private BooleanBinding confimButtonBinding;

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
    private Label paymentDiscountLabel;
    @FXML
    private Label taxLabel;
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
        confimButtonBinding = paymentField.textProperty().isEmpty().or(Bindings.size(transactionTableView.getItems()).greaterThan(1));
        confirmButton.disableProperty().bind(confimButtonBinding);
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
                showPaymentDetails(productTransactionObservableList, customer);
            }
        });
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
        showPaymentDetails(null, null);

        paymentTypeChoiceBox.getSelectionModel().selectFirst();
        paymentTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                transaction.setPaymentType(newValue);
            }
        });

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
            productTransactionObservableList.add(newTransaction);
        }

    }

    @FXML
    public void handleAddCustomer(){
        Customer newCustomer = new Customer();
        boolean okClicked = saleSystem.showCustomerEditDialog(newCustomer);
        if(okClicked){
            newCustomer.setUserName();
            boolean flag = true;
            try{
                dbExecuteProduct.insertIntoDatabase(DBQueries.InsertQueries.Customer.INSERT_INTO_CUSTOMER,
                        newCustomer.getAllProperties());
            }catch(SQLException e){
                flag = false;
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Unable To Add New Customer");
                alert.setHeaderText(null);
                alert.setContentText("Unable To Add New Customer" + newCustomer.getFirstName() + " " + newCustomer.getLastName());
                alert.showAndWait();
            }finally{
                if(flag){
                    customer = newCustomer;
                    showCustomerDetails(customer);
                }
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
            transaction.getProductTransactionList().clear();
        }
        else{
            transaction.getProductTransactionList().addAll(productTransactionObservableList);
            transaction.setPayment(Double.valueOf(paymentField.getText()));
            if(storeCreditCheckBox.isSelected()){
                transaction.setStoreCredit(Double.valueOf(storeCreditField.getText()));
            }
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
                    .append("Payment: " + transaction.getPayment() + "\n")
                    .append("Store Credit: " + transaction.getStoreCredit() + "\n")
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
    private void showPaymentDetails(ObservableList<ProductTransaction> transactions, Customer customer){
        if(transaction != null ){
            Iterator<ProductTransaction> iterator = transactions.iterator();
            BigDecimal subTotalAll = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            while(iterator.hasNext()){
                subTotalAll = subTotalAll.add(
                        new BigDecimal(iterator.next().getSubTotal()).setScale(2, BigDecimal.ROUND_HALF_EVEN)
                );
            }
            BigDecimal discount = new BigDecimal(returnDiscount()).multiply(subTotalAll).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal tax = new BigDecimal(saleSystem.getTaxRate()).multiply(subTotalAll).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal total = subTotalAll.add(tax).subtract(discount).setScale(2, BigDecimal.ROUND_HALF_EVEN);

            itemsCountLabel.setText(String.valueOf(transactions.size()));
            subTotalLabel.setText(subTotalAll.toString());
            paymentDiscountLabel.setText(discount.toString());
            taxLabel.setText(tax.toString());
            totalLabel.setText(total.toString());
            showBalanceDetails();
        }
        else{
            itemsCountLabel.setText("");
            subTotalLabel.setText("");
            paymentDiscountLabel.setText("");
            taxLabel.setText("");
            totalLabel.setText("");
            balanceLabel.setText("");
        }
    }

    private void showBalanceDetails(){
        BigDecimal balance;
        if((storeCreditCheckBox.isSelected() && !storeCreditField.getText().trim().isEmpty() && isStoreCreditValid())
                &&(!paymentField.getText().trim().isEmpty() && isPaymentValid())){
            balance = new BigDecimal(totalLabel.getText()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            balance = balance.subtract(new BigDecimal(paymentField.getText())).subtract(new BigDecimal(storeCreditField.getText()));
            balanceLabel.setText(balance.toString());
        }
        else if(storeCreditCheckBox.isSelected() && !storeCreditField.getText().trim().isEmpty() && isStoreCreditValid()){
            balance = new BigDecimal(totalLabel.getText()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            balance = balance.subtract(new BigDecimal(storeCreditField.getText()));
            balanceLabel.setText(balance.toString());
        }
        else if(!paymentField.getText().trim().isEmpty() && isPaymentValid()){
            balance = new BigDecimal(totalLabel.getText()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            balance = balance.subtract(new BigDecimal(paymentField.getText()));
            balanceLabel.setText(balance.toString());
        }
        else{
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
                .storeCredit(0)
                .paymentType(INIT_TRANSACTION_PAYMENT_TYPE)
                .build();
        productTransactionObservableList = FXCollections.observableArrayList(transaction.getProductTransactionList());
        transactionTableView.setItems(productTransactionObservableList);
        productTransactionObservableList.addListener(new ListChangeListener<ProductTransaction>() {
            @Override
            public void onChanged(Change<? extends ProductTransaction> c) {
                while(c.next()){
                    if( c.wasAdded() || c.wasRemoved()){
                        showPaymentDetails(productTransactionObservableList, customer);
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
            Double.parseDouble(storeCreditField.getText());
        }catch(NumberFormatException e){
            return false;
        }
        if(customer != null &&
                (customer.getStoreCredit() < Double.valueOf(storeCreditField.getText()))){
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

    private int returnDiscount(){
        if(this.customer != null){
            return Customer.getDiscountMap().get(customer.getUserClass());
        }
        return 0;
    }
    private void commitTransactionToDatabase() throws SQLException, IOException {
        Connection connection = DBConnect.getConnection();
        try{
            connection.setAutoCommit(false);
            System.out.println(connection.getAutoCommit());
            Object[] objects = ObjectSerializer.TRANSACTION_OBJECT_SERIALIZER.serialize(transaction);
            dbExecuteTransaction.insertIntoDatabase(DBQueries.InsertQueries.Transaction.INSERT_INTO_TRANSACTION,
                    objects);
            for(ProductTransaction tmp : transaction.getProductTransactionList()){
                int remain = tmp.getTotalNum() - tmp.getQuantity();
                dbExecuteProduct.updateDatabase(DBQueries.UpdateQueries.Product.UPDATE_PRODUCT_QUANTITY,
                    remain, tmp.getProductId());
            }
            if(storeCreditCheckBox.isSelected()){
                double remainStoreCredit = customer.getStoreCredit() - Double.valueOf(storeCreditField.getText());
                dbExecuteCustomer.updateDatabase(DBQueries.UpdateQueries.Customer.UPDATE_CUSTOMER_STORE_CREDIT,
                    remainStoreCredit, customer.getUserName());
            }
            connection.commit();
        }catch(SQLException e){
            connection.rollback(); //TODO: CRITICAL BUG!!!
            Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to store transaction to database!");
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
