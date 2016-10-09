package Controllers;

import Constants.Constant;
import MainClass.SaleSystem;
import PDF.InvoiceGenerator;
import com.sun.prism.impl.Disposer;
import db.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;
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
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.*;
import org.apache.log4j.Logger;
import util.AlertBuilder;
import util.AutoCompleteComboBoxListener;
import util.ButtonCell;
//import util.EditCellFactory;

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
public class GenerateCustomerTransactController {

    public static Logger logger= Logger.getLogger(GenerateCustomerTransactController.class);
    private final static String INIT_TRANSACTION_PAYMENT_TYPE = "Cash";
    private Stage dialogStage;
    private Customer customer;
    private List<Customer> customerList;
    private List<Product> productList;
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
    private Executor executor;

    @FXML
    private TableView<ProductTransaction> transactionTableView;
    @FXML
    private TableColumn<ProductTransaction, Integer> productIdCol;
    @FXML
    private TableColumn<ProductTransaction, Number> stockCol;
    @FXML
    private TableColumn<ProductTransaction, BigDecimal> unitPriceCol;
    @FXML
    private TableColumn<ProductTransaction, Float> qtyCol;
    @FXML
    private TableColumn<ProductTransaction, Integer> discountCol;
    @FXML
    private TableColumn<ProductTransaction, Number> subTotalCol;
    @FXML
    private TableColumn<ProductTransaction, Float> sizeCol;
    @FXML
    private TableColumn deleteCol;
    @FXML
    private TableColumn<ProductTransaction, Number> boxCol;
    @FXML
    private TableColumn<ProductTransaction, Number> residualTileCol;
    @FXML
    private TableColumn<ProductTransaction, String> remarkCol;

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
    private Label subTotalLabel;
    @FXML
    private Label paymentDiscountLabel;
    @FXML
    private Label pstTaxLabel;
    @FXML
    private Label gstTaxLabel;
    @FXML
    private Label totalLabel;

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
    private ComboBox customerPhoneComboBox;
    @FXML
    private ComboBox productComboBox;
    @FXML
    private Label balanceLabel;
    @FXML
    private ChoiceBox<String> paymentTypeChoiceBox;
    @FXML
    private TextField storeCreditField;
    @FXML
    private CheckBox storeCreditCheckBox;
    @FXML
    private CheckBox isDepositCheckBox;

    @FXML
    private void initialize(){
        confimButtonBinding = Bindings.size(transactionTableView.getItems()).greaterThan(0);
        confirmButton.disableProperty().bind(confimButtonBinding);
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("totalNum"));
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("sizeNumeric"));
        remarkCol.setCellValueFactory(new PropertyValueFactory<>("remark"));
        remarkCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ProductTransaction, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<ProductTransaction, String> event) {
                (event.getTableView().getItems().get(event.getTablePosition().getRow())).setRemark(event.getNewValue().toString());
            }
        });
//        remarkCol.setCellFactory(cellFactory);

        remarkCol.setCellFactory(TextFieldTableCell.forTableColumn());

        boxCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return String.valueOf(object);
            }

            @Override
            public Number fromString(String string) {
                return Integer.valueOf(string);
            }
        }));
        boxCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ProductTransaction, Number>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<ProductTransaction, Number> event) {
                (event.getTableView().getItems().get(event.getTablePosition().getRow())).getBoxNum().setBox(event.getNewValue().intValue());
            }
        });

        boxCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductTransaction, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<ProductTransaction, Number> param) {
                return new SimpleIntegerProperty(param.getValue().getBoxNum().getBox());
            }
        });
        residualTileCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return String.valueOf(object);
            }

            @Override
            public Number fromString(String string) {
                return Integer.valueOf(string);
            }
        }));
        residualTileCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ProductTransaction, Number>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<ProductTransaction, Number> event) {
                (event.getTableView().getItems().get(event.getTablePosition().getRow())).getBoxNum().setResidualTile(event.getNewValue().intValue());
            }
        });
        residualTileCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductTransaction, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<ProductTransaction, Number> param) {
                return new SimpleIntegerProperty(param.getValue().getBoxNum().getResidualTile());
            }
        });
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
                refreshTable();
            }
        });
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discount"));
        discountCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Integer>(){
            @Override
            public Integer fromString(String string) {
                return Integer.valueOf(string);
            }
            public String toString(Integer integer){
                return String.valueOf(integer);
            }
        }));
        discountCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ProductTransaction, Integer>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<ProductTransaction, Integer> event) {
                if(event.getNewValue() > returnDiscount()){
                    Optional<ButtonType> result = new AlertBuilder().alertTitle("Discount Error")
                            .alertType(Alert.AlertType.CONFIRMATION)
                            .alertContentText("User Class is " + customer.getUserClass() + ", but the given discount is " + event.getNewValue() + "\n"
                                    + "Press OK to proceed with this discount. Press Cancel to discard the change")
                            .build()
                            .showAndWait();
                    if(result.get() == ButtonType.OK){
                        (event.getTableView().getItems().get(event.getTablePosition().getRow()))
                                .setDiscount(event.getNewValue());
                        showPaymentDetails(productTransactionObservableList, customer);
                    }
                }else{
                    (event.getTableView().getItems().get(event.getTablePosition().getRow()))
                            .setDiscount(event.getNewValue());
                    showPaymentDetails(productTransactionObservableList, customer);
                }
                refreshTable();
            }
        });

        subTotalCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductTransaction, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<ProductTransaction, Number> param) {
                return new SimpleFloatProperty(new BigDecimal(param.getValue().getSubTotal()).setScale(2, BigDecimal.ROUND_HALF_EVEN).floatValue());
            }
        });
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

        try{
            customerList = dbExecuteCustomer.selectFromDatabase(DBQueries.SelectQueries.Customer.SELECT_ALL_CUSTOMER);
            productList = dbExecuteProduct.selectFromDatabase(DBQueries.SelectQueries.Product.SELECT_ALL_PRODUCT);
        }catch(SQLException e){
            logger.error(e.getMessage());
            Alert alert = new Alert(Alert.AlertType.WARNING, "Unable to grab data from database!\n" + e.getMessage());
            alert.setTitle("Database Error");
            alert.showAndWait();
        }
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
        List<String> tmpCustomerPhoneList = new ArrayList<>();
        for(Customer customer: customerList){
            customer.constructCustomerPhoneInfo();
            tmpCustomerPhoneList.add(customer.getCustomerPhoneInfo());
        }
        customerPhoneComboBox.setItems(FXCollections.observableArrayList(tmpCustomerPhoneList));
        customerPhoneComboBox.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                for(Customer tmpCustomer: customerList){
                    if(tmpCustomer.getCustomerPhoneInfo() != null && tmpCustomer.getCustomerPhoneInfo().equals(newValue)){
                        customer = tmpCustomer;
                        showCustomerDetails(customer);
                        break;
                    }
                }

            }
        });
        List<String> tmpProductList = productList
                .stream()
                .map(product -> product.getProductId())
                .collect(Collectors.toList());
        productComboBox.setItems(FXCollections.observableArrayList(tmpProductList));
        new AutoCompleteComboBoxListener<>(customerComboBox);
        new AutoCompleteComboBoxListener<>(customerPhoneComboBox);
        new AutoCompleteComboBoxListener<>(productComboBox);
        storeCreditCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                storeCreditField.setDisable(newValue ? false : true);
                if(!newValue){
                    storeCreditField.clear();
                }
            }
        });
        executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    @FXML
    public void handleAddItem(){
        Product selectedProduct = productList
                .stream()
                .filter(product -> product.getProductId().equals(productComboBox.getSelectionModel().getSelectedItem()))
                .findFirst()
                .get();
        List<String> productIdList = productTransactionObservableList.stream()
                .map(ProductTransaction::getProductId)
                .collect(Collectors.toList());
        if(productIdList.contains(selectedProduct.getProductId())){
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertContentText("Product Add Error")
                    .alertContentText(selectedProduct.getProductId() + " has already been added!")
                    .build()
                    .showAndWait();
        }else{
            ProductTransaction newProductTransaction = new ProductTransaction.ProductTransactionBuilder()
                    .productId(selectedProduct.getProductId())
                    .totalNum(selectedProduct.getTotalNum())
                    .unitPrice(selectedProduct.getUnitPrice())
                    .piecesPerBox(selectedProduct.getPiecesPerBox())
                    .size(selectedProduct.getSize())
                    .sizeNumeric(selectedProduct.getSizeNumeric())
                    .boxNum(new BoxNum.boxNumBuilder().build())
                    .build();
            productTransactionObservableList.add(newProductTransaction);
        }
    }

    @FXML
    public void handleAddCustomer(){
        Customer newCustomer = new Customer(new Customer.CustomerBuilder());
        boolean okClicked = saleSystem.showCustomerEditDialog(newCustomer);
        if(okClicked){
            newCustomer.setUserName();
            boolean flag = true;
            try{
                dbExecuteProduct.insertIntoDatabase(DBQueries.InsertQueries.Customer.INSERT_INTO_CUSTOMER,
                        newCustomer.getAllProperties());
            }catch(SQLException e){
                logger.error(e.getMessage());
                flag = false;
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Unable To Add New Customer");
                alert.setHeaderText(null);
                alert.setContentText("Unable To Add New Customer" + newCustomer.getFirstName() + " " + newCustomer.getLastName());
                alert.showAndWait();
            }finally{
                if(flag){
                    customer = newCustomer;
                    customerList.add(customer);
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
    public Transaction handleQuotationButton() throws IOException, SQLException {
        if(!isQuotationValid()){
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertTitle("Transaction is Invalid")
                    .alertHeaderText("Please fix the following errors before proceed")
                    .alertContentText(errorMsgBuilder.toString())
                    .build()
                    .showAndWait();
            transaction.getProductTransactionList().clear();
        }
        else{
            generateTransaction();
        }
        return transaction;
    }

    @FXML
    public Transaction handleConfirmButton() throws IOException, SQLException {
        if(!isTransactionValid()){
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertTitle("Transaction is Invalid")
                    .alertHeaderText("Please fix the following errors before proceed")
                    .alertContentText(errorMsgBuilder.toString())
                    .build()
                    .showAndWait();
            transaction.getProductTransactionList().clear();
        }
        else{
            generateTransaction();
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
        discount = 100;
    }

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }
    /*
    * Show customer details grid pane
    * */

    private void generateTransaction() throws IOException, SQLException{
        transaction.getProductTransactionList().addAll(productTransactionObservableList);
        if(!paymentField.getText().trim().isEmpty()){
            transaction.setPayment(Double.valueOf(paymentField.getText()));
        }
        if(storeCreditCheckBox.isSelected() && !storeCreditField.getText().trim().isEmpty()){
            transaction.setStoreCredit(Double.valueOf(storeCreditField.getText()));
        }
        transaction.setGstTax(Double.valueOf(gstTaxLabel.getText()));
        transaction.setPstTax(Double.valueOf(pstTaxLabel.getText()));
        transaction.setTotal(Double.valueOf(totalLabel.getText()));
        transaction.getPayinfo().add(new PaymentRecord(
                transaction.getDate().toString(),
                transaction.getPayment() + transaction.getStoreCredit(),
                transaction.getPaymentType(),
                (isDepositCheckBox.isSelected())? true : false));

        StringBuffer overviewTransactionString = new StringBuffer();
        StringBuffer overviewProductTransactionString = new StringBuffer();
        for(ProductTransaction tmp: transaction.getProductTransactionList()){
            overviewProductTransactionString
                    .append("Product ID: " + tmp.getProductId() + "\n")
                    .append("Total Num: " + tmp.getTotalNum() + "\n")
                    .append("Quantity: " + tmp.getQuantity() + "\n")
                    .append("Unit Price: " + tmp.getUnitPrice() + "\n")
                    .append("Discount (%): " + tmp.getDiscount() + "\n")
                    .append("Sub Total: " + tmp.getSubTotal() + "\n")
                    .append("Remark: " + tmp.getRemark() + "\n")
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

    private void showCustomerDetails(Customer customer){
        if(customer != null){
            addItemButton.setDisable(false);
            transaction.setInfo(customer.getUserName());
            firstNameLabel.setText(customer.getFirstName());
            lastNameLabel.setText(customer.getLastName());
            storeCreditLabel.setText(String.valueOf(customer.getStoreCredit()));
            discountLabel.setText(customer.getUserClass());
        }
        else{
            addItemButton.setDisable(true);
            firstNameLabel.setText("");
            lastNameLabel.setText("");
            storeCreditLabel.setText("");
            discountLabel.setText("");
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
            paymentDiscountLabel.setText("");
            pstTaxLabel.setText("");
            gstTaxLabel.setText("");
            totalLabel.setText("");
            balanceLabel.setText("");
        }
    }

    private void showBalanceDetails(){
        BigDecimal balance;
        if((storeCreditCheckBox.isSelected() && !storeCreditField.getText().trim().isEmpty() && isStoreCreditValidNoCustomer())
                &&(!paymentField.getText().trim().isEmpty() && isPaymentValid())){
            balance = new BigDecimal(totalLabel.getText()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            balance = balance.subtract(new BigDecimal(paymentField.getText())).subtract(new BigDecimal(storeCreditField.getText()));
            balanceLabel.setText(balance.toString());
        }
        else if(storeCreditCheckBox.isSelected() && !storeCreditField.getText().trim().isEmpty() && isStoreCreditValidNoCustomer()){
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
                .productInfoList(new ArrayList<>())
                .payinfo(new ArrayList<>())
                .staffId(saleSystem.getStaff().getStaffId())
                .date(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                .type(Transaction.TransactionType.OUT)
                .storeCredit(0)
                .payment(0)
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
        if(paymentField.getText().trim().isEmpty() && storeCreditField.getText().trim().isEmpty()){
            errorMsgBuilder.append("You need to specify either to pay or use store credit!\n");
        }
        if(!paymentField.getText().trim().isEmpty()){
            if(!isPaymentValid()) {
                errorMsgBuilder.append("Payment must be numbers!\n");
            }
        }
        if(customer == null){
            errorMsgBuilder.append("Customer is neither selected nor created!\n");
        }
        if(storeCreditCheckBox.isSelected()){
            if(storeCreditField.getText().trim().isEmpty()){
                errorMsgBuilder.append("Store Credit Field is empty, but it is selected!\n");
            }else{
                if(!isStoreCreditValid()){
                    errorMsgBuilder.append("Either Store Credit exceeds customer's limit or Store Credit must be numbers!\n");
                }
            }
        }
        if(errorMsgBuilder.length() != 0){
            return false;
        }
        return true;
    }

    private boolean isQuotationValid(){
        errorMsgBuilder = new StringBuffer();
        if(customer == null){
            errorMsgBuilder.append("Customer is neither selected nor created!\n");
        }
        if(storeCreditCheckBox.isSelected()){
            if(storeCreditField.getText().trim().isEmpty()){
                errorMsgBuilder.append("Store Credit Field is empty, but it is selected!\n");
            }else{
                if(!isStoreCreditValid()){
                    errorMsgBuilder.append("Either Store Credit exceeds customer's limit or Store Credit must be numbers!\n");
                }
            }
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

    private Integer returnDiscount(){
        if(this.customer != null){
            if(customer.getUserClass().toLowerCase().equals("a")){
                return this.saleSystem.getProperty().getUserClass().getClassA();
            }else if(customer.getUserClass().toLowerCase().equals("b")){
                return this.saleSystem.getProperty().getUserClass().getClassB();
            }else if(customer.getUserClass().toLowerCase().equals("c")){
                return this.saleSystem.getProperty().getUserClass().getClassC();
            }
        }
        return null;
    }

    public Transaction returnNewTrasaction(){
        return this.transaction;
    }

   public List<Customer> returnCustomerList(){
       return this.customerList;
   }

    public ContainerClass returnContainer(){
        return new ContainerClass(transaction, customerList);
    }

    public boolean isConfirmedClicked(){
        return this.confirmedClicked;
    }

    private void refreshTable(){
        transactionTableView.getColumns().get(0).setVisible(false);
        transactionTableView.getColumns().get(0).setVisible(true);
    }

    private void commitTransactionToDatabase()  throws SQLException, IOException{
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
                    float remain = tmp.getTotalNum() - tmp.getQuantity();
                    dbExecuteProduct.updateDatabase(DBQueries.UpdateQueries.Product.UPDATE_PRODUCT_QUANTITY,
                            remain, tmp.getProductId());
                }
                if(storeCreditCheckBox.isSelected()){
                    double remainStoreCredit = customer.getStoreCredit() - Double.valueOf(storeCreditField.getText());
                    dbExecuteCustomer.updateDatabase(DBQueries.UpdateQueries.Customer.UPDATE_CUSTOMER_STORE_CREDIT,
                            remainStoreCredit, customer.getUserName());
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
            executor.execute(customerTask);
        });
        customerTask.setOnSucceeded(event -> {
            customer.setStoreCredit(customerTask.getValue().getStoreCredit());
            executor.execute(commitTask);
        });
        commitTask.setOnFailed(event -> {
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
        commitTask.setOnSucceeded(event -> {
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

}
