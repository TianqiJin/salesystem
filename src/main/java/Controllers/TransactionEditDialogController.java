package Controllers;

import Constants.Constant;
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
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.*;
import org.apache.log4j.Logger;
import org.omg.SendingContext.RunTime;
import util.AlertBuilder;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by tjin on 1/25/2016.
 */
public class TransactionEditDialogController {
    public static Logger logger= Logger.getLogger(TransactionEditDialogController.class);
    private final static String INIT_TRANSACTION_PAYMENT_TYPE = "Cash";
    private Stage dialogStage;
    private Customer customer;
    private List<Product> productList;
    private ObservableList<ProductTransaction> productTransactionObservableList;
    private HashMap<String, Float> originalProductQuantity;
    private Transaction transaction;
    private Transaction.TransactionType generateTransactionType;

    private DBExecuteProduct dbExecuteProduct;
    private DBExecuteCustomer dbExecuteCustomer;
    private DBExecuteTransaction dbExecuteTransaction;
    private DBExecuteStaff dbExecuteStaff;

    private SaleSystem saleSystem;
    private StringBuffer errorMsgBuilder;
    private boolean confirmedClicked;
    private Executor executor;
    private Staff staff;
    private boolean isEditable;

    @FXML
    private AnchorPane splitLeftAnchorPane;
    @FXML
    private TableView<ProductTransaction> transactionTableView;
    @FXML
    private TableColumn<ProductTransaction, Integer> productIdCol;
    @FXML
    private TableColumn<ProductTransaction, String> displayNameCol;
    @FXML
    private TableColumn<ProductTransaction, BigDecimal> unitPriceCol;
    @FXML
    private TableColumn<ProductTransaction, Float> qtyCol;
    @FXML
    private TableColumn<ProductTransaction, Integer> discountCol;
    @FXML
    private TableColumn<ProductTransaction, BigDecimal> subTotalCol;
    @FXML
    private TableColumn<ProductTransaction, Number> boxCol;
    @FXML
    private TableColumn<ProductTransaction, Number> residualTileCol;
    @FXML
    private TableColumn<ProductTransaction, String> remarkCol;
    @FXML
    private TableColumn deleteCol;

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
    private Label pstLabel;
    @FXML
    private Label gstLabel;
    @FXML
    private Label subtotalLabel;
    @FXML
    private Label transactionDiscountLabel;


    @FXML
    private TextField paymentField;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button addButton;
    @FXML
    private Button addDisplayNameButton;
    @FXML
    private Button quotationButton;
    @FXML
    private ComboBox productComboBox;
    @FXML
    private ComboBox displayNameComboBox;
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
    private Label typeLabel;
    @FXML
    private Label dateLabel;
    //Staff Information Labels
    @FXML
    private Label staffFullNameLabel;
    @FXML
    private Label staffPhoneLabel;
    @FXML
    private Label staffPositionLabel;
    //Transaction Additional Note
    @FXML
    private TextArea noteArea;

    @FXML
    private void initialize(){
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        displayNameCol.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<ProductTransaction, Float>("quantity"));
        discountCol.setCellValueFactory(new PropertyValueFactory<ProductTransaction, Integer>("discount"));
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
        showCustomerDetails();
        showPaymentDetails();

        paymentTypeChoiceBox.getSelectionModel().selectFirst();
        paymentTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                transaction.setPaymentType(newValue);
            }
        });
        paymentTypeChoiceBox.showingProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue){
                transaction.setPaymentType(paymentTypeChoiceBox.getValue());
            }
        });


        storeCreditCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                storeCreditField.setDisable(newValue ? false : true);
                if(!newValue){
                    storeCreditField.clear();
                }
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
        remarkCol.setCellValueFactory(new PropertyValueFactory<>("remark"));
        remarkCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ProductTransaction, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<ProductTransaction, String> event) {
                (event.getTableView().getItems().get(event.getTablePosition().getRow())).setRemark(event.getNewValue().toString());
            }
        });

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

        showTransactionBasicDetails(null, null);
        new AutoCompleteComboBoxListener<>(productComboBox);
        new AutoCompleteComboBoxListener<>(displayNameComboBox);
        executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    @FXML
    public void handleAddItem(){
        Product selectedProduct = this.productList
                .stream()
                .filter(product -> product.getProductId().equals(productComboBox.getSelectionModel().getSelectedItem()))
                .findFirst()
                .get();
        List<String> productIdList = this.productTransactionObservableList.stream()
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
                    .displayName(selectedProduct.getDisplayName())
                    .build();
            this.productTransactionObservableList.add(newProductTransaction);
        }
    }

    @FXML
    public void handleAddItemDisplayName(){
        Product selectedProduct = this.productList
                .stream()
                .filter(product -> product.getDisplayName().equals(displayNameComboBox.getSelectionModel().getSelectedItem()))
                .findFirst()
                .get();
        List<String> displayNameList = this.productTransactionObservableList.stream()
                .map(ProductTransaction::getDisplayName)
                .collect(Collectors.toList());
        if(displayNameList.contains(selectedProduct.getDisplayName())){
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertContentText("Product Add Error")
                    .alertContentText(selectedProduct.getDisplayName() + " has already been added!")
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
                    .displayName(selectedProduct.getDisplayName())
                    .build();
            this.productTransactionObservableList.add(newProductTransaction);
        }
    }

    @FXML
    public void handleCancelButton(){
        this.dialogStage.close();
    }

    @FXML
    public void handleQuotationButton() throws IOException, SQLException {
        generateTransaction(Transaction.TransactionType.QUOTATION);
    }

    @FXML
    public void handleAddNoteButton(){
        Task<Integer> updateNoteTask = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                transaction.setNote(noteArea.getText());
                return dbExecuteTransaction.updateDatabase(DBQueries.UpdateQueries.Transaction.UPDATE_TRANSACTION_NOTE,
                        ObjectSerializer.TRANSACTION_NOTE_SERIALIZER_UPDATE.serialize(transaction));
            }
        };

        updateNoteTask.exceptionProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                Exception ex = (Exception) newValue;
                logger.error(ex.getMessage(), ex);
            }
        });

        updateNoteTask.setOnSucceeded(event -> {
            int returnValue = updateNoteTask.getValue();
            if(returnValue == 1){
                new AlertBuilder()
                        .alertType(Alert.AlertType.INFORMATION)
                        .alertTitle("Transaction Note")
                        .alertContentText("Transaction Note is updated successfully")
                        .build()
                        .showAndWait();
            }else{
                new AlertBuilder()
                        .alertType(Alert.AlertType.ERROR)
                        .alertTitle("Transaction Note")
                        .alertContentText("Transaction Note is failed to update. No Transaction is affected.")
                        .build()
                        .showAndWait();
            }
        });

        updateNoteTask.setOnFailed(event ->
                new AlertBuilder()
                        .alertType(Alert.AlertType.ERROR)
                        .alertTitle("Transaction Note")
                        .alertContentText("Transaction Note is failed to update. No Transaction is affected.")
                        .build()
                        .showAndWait());

        executor.execute(updateNoteTask);
    }

    @FXML
    public void handleConfirmButton() throws IOException, SQLException {
        if(!isTransactionValid()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Transaction Is Invalid");
            alert.setHeaderText("Please fix the following errors before proceed");
            alert.setContentText(errorMsgBuilder.toString());
            alert.showAndWait();
        }
        else{
            generateTransaction(Transaction.TransactionType.OUT);
        }
    }
    /*
    * Constructor
    * */
    public TransactionEditDialogController(){
        dbExecuteProduct = new DBExecuteProduct();
        dbExecuteCustomer = new DBExecuteCustomer();
        dbExecuteTransaction = new DBExecuteTransaction();
        dbExecuteStaff = new DBExecuteStaff();
        confirmedClicked = false;
        this.originalProductQuantity = new HashMap<>();
    }

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }
    /*
    * Show customer details grid pane
    * */
    private void showCustomerDetails(){
        if(this.customer != null){
            transaction.setInfo(this.customer.getUserName());
            firstNameLabel.setText(this.customer.getFirstName());
            lastNameLabel.setText(this.customer.getLastName());
            storeCreditLabel.setText(String.valueOf(this.customer.getStoreCredit()));
            discountLabel.setText(this.customer.getUserClass());
        }
        else{
            firstNameLabel.setText("");
            lastNameLabel.setText("");
            storeCreditLabel.setText("");
            discountLabel.setText("");
        }
    }

    private void showPaymentDetails(){
        if(this.productTransactionObservableList != null ){
            Iterator<ProductTransaction> iterator = this.productTransactionObservableList.iterator();
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
            if(this.customer != null && this.customer.getPstNumber() != null){
                pstTax = new BigDecimal("0.0");
            }else{
                pstTax = new BigDecimal(saleSystem.getProperty().getPstRate()).multiply(subTotalAfterDiscount).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            }
            BigDecimal gstTax = new BigDecimal(saleSystem.getProperty().getGstRate()).multiply(subTotalAfterDiscount).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal total = subTotalAfterDiscount.add(pstTax).add(gstTax).setScale(2, BigDecimal.ROUND_HALF_EVEN);

            BigDecimal residual = new BigDecimal(0);
            for(PaymentRecord paymentRecord: transaction.getPayinfo()){
                residual = residual.add(new BigDecimal(paymentRecord.getPaid()));
            }
            residual = total.subtract(residual).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            itemsCountLabel.setText(String.valueOf(this.productTransactionObservableList.size()));
            subtotalLabel.setText(subTotalBeforediscount.toString());
            pstLabel.setText(pstTax.toString());
            gstLabel.setText(gstTax.toString());
            transactionDiscountLabel.setText(paymentDiscount.toString());
            totalLabel.setText(total.toString());
            residualLabel.setText(residual.toString());
            showBalanceDetails();
        }
        else{
            itemsCountLabel.setText("");
            residualLabel.setText("");
            pstLabel.setText("");
            gstLabel.setText("");
            totalLabel.setText("");
            residualLabel.setText("");
            balanceLabel.setText("");
            transactionDiscountLabel.setText("");
            subtotalLabel.setText("");
        }
    }

    private void showTransactionBasicDetails(Staff staff, Transaction transaction){
        if(staff == null){
            staffFullNameLabel.setText("");
            staffPhoneLabel.setText("");
            staffPositionLabel.setText("");
        }else{
            staffFullNameLabel.setText(staff.getFullName());
            staffPhoneLabel.setText(staff.getPhone());
            staffPositionLabel.setText(staff.getPosition().name());
        }

        if(transaction == null){
            typeLabel.setText("");
            dateLabel.setText("");
        }else{
            typeLabel.setText(transaction.getType().name());
            dateLabel.setText(transaction.getDate().toString());
        }
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

    private void refreshTable(){
        transactionTableView.getColumns().get(0).setVisible(false);
        transactionTableView.getColumns().get(0).setVisible(true);
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

    public void setMainClass(SaleSystem saleSystem){
        this.saleSystem = saleSystem;
    }

    public void setSelectedTransaction(Transaction transaction, Transaction.TransactionType type){
        this.transaction = transaction;
        this.transaction.getProductTransactionList().forEach(p -> originalProductQuantity.put(p.getProductId(), p.getQuantity()));
        this.productTransactionObservableList = FXCollections.observableArrayList(transaction.getProductTransactionList());
        this.generateTransactionType = type;
        this.isEditable = isTransactionEditable(this.transaction, type);
        noteArea.setText(this.transaction.getNote());
        this.productTransactionObservableList.addListener(new ListChangeListener<ProductTransaction>() {
            @Override
            public void onChanged(Change<? extends ProductTransaction> c) {
                while(c.next()){
                    if( c.wasAdded() || c.wasRemoved()){
                        showPaymentDetails();
                    }
                }
            }
        });
    }

    private void setRelatedUI(Transaction.TransactionType type){
        if(!isEditable){
            productComboBox.setVisible(false);
            displayNameComboBox.setVisible(false);
            addButton.setVisible(false);
            addDisplayNameButton.setVisible(false);
            deleteCol.setVisible(false);
            splitLeftAnchorPane.setTopAnchor(transactionTableView, 10.0);
            qtyCol.setEditable(false);
            discountCol.setEditable(false);
            quotationButton.setDisable(true);
            BooleanBinding confirmButtonBinding = paymentField.textProperty().isEmpty().or(Bindings.size(transactionTableView.getItems()).lessThan(1));
            confirmButton.disableProperty().bind(confirmButtonBinding);
        }else{
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
                    showPaymentDetails();
                    refreshTable();
                }
            });
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
                            showPaymentDetails();
                        }
                    }else{
                        (event.getTableView().getItems().get(event.getTablePosition().getRow()))
                                .setDiscount(event.getNewValue());
                        showPaymentDetails();
                    }
                    refreshTable();
                }
            });
            if(type.equals(Transaction.TransactionType.OUT)){
                quotationButton.setDisable(true);
            }else{
                confirmButton.setDisable(true);
            }
        }
    }

    private boolean isTransactionValid(){
        errorMsgBuilder = new StringBuffer();
        if(!isPaymentValid()){
            errorMsgBuilder.append("Payment must be numbers!\n");
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

    private boolean isTransactionEditable(Transaction transaction, Transaction.TransactionType type){
        //Return true if the type is QUOTATION
        if(type.equals(Transaction.TransactionType.QUOTATION)){
            return true;
        }
        //Return false if the type is OUT and every thing is paid up
        if(type.equals(Transaction.TransactionType.OUT) && transaction.getTotal() == transaction.getPayment()){
            return false;
        }

        boolean hasDeposit = false;
        boolean hasNonDeposit = false;
        for(PaymentRecord paymentRecord: transaction.getPayinfo()){
            if(paymentRecord.isDeposit()){
                hasDeposit = true;
                break;
            }
        }
        for(PaymentRecord paymentRecord: transaction.getPayinfo()){
            if(!paymentRecord.isDeposit() && paymentRecord.getPaid() != 0){
                hasNonDeposit = true;
                break;
            }
        }
        //Return false if the type is OUT and the customer has made one deposit and one non-deposit payments
        if(type.equals(Transaction.TransactionType.OUT) && hasDeposit && hasNonDeposit){
            return false;
        }

        return true;
    }


    public void loadDataFromDB(){
        Task<List<Product>> productTask = new Task<List<Product>>() {
            @Override
            protected List<Product> call() throws Exception {
                return dbExecuteProduct.selectFromDatabase(DBQueries.SelectQueries.Product.SELECT_ALL_PRODUCT);
            }
        };
        Task<Customer> customerTask = new Task<Customer>() {
            @Override
            protected Customer call() throws Exception {
                return dbExecuteCustomer.selectFromDatabase(DBQueries.SelectQueries.Customer.SELECT_SINGLE_CUSTOMER, transaction.getInfo()).get(0);
            }
        };
        Task<Staff> staffTask = new Task<Staff>() {
            @Override
            protected Staff call() throws Exception {
                return dbExecuteStaff.selectFromDatabase(DBQueries.SelectQueries.Staff.SELECT_ID_STAFF, transaction.getStaffId()).get(0);
            }
        };
        productTask.exceptionProperty().addListener((observable, oldValue, newValue) ->  {
            if(newValue != null) {
                Exception ex = (Exception) newValue;
                logger.error(ex.getMessage(), ex);
            }
        });
        customerTask.exceptionProperty().addListener((observable, oldValue, newValue) ->  {
            if(newValue != null) {
                Exception ex = (Exception) newValue;
                logger.error(ex.getMessage(), ex);
            }
        });
        staffTask.exceptionProperty().addListener((observable, oldValue, newValue) ->  {
            if(newValue != null) {
                Exception ex = (Exception) newValue;
                logger.error(ex.getMessage(), ex);
            }
        });

        customerTask.setOnSucceeded(event -> {
            this.customer = customerTask.getValue();
            transactionTableView.setItems(this.productTransactionObservableList);
            showCustomerDetails();
            showPaymentDetails();
            setRelatedUI(this.generateTransactionType);
        });
        customerTask.setOnFailed(event -> {
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertContentText(Constant.DatabaseError.databaseUpdateError + event.getSource().exceptionProperty().getValue())
                    .alertTitle(Constant.DatabaseError.databaseErrorAlertTitle)
                    .build()
                    .showAndWait();
                }
            );

        productTask.setOnSucceeded(event -> {
            this.productList = productTask.getValue();
            List<String> tmpProductIdList = productList.stream().map(Product::getProductId).collect(Collectors.toList());
            List<String> tmpDisplayNameList = productList.stream().map(Product::getDisplayName).collect(Collectors.toList());
            productComboBox.setItems(FXCollections.observableArrayList(tmpProductIdList));
            displayNameComboBox.setItems(FXCollections.observableArrayList(tmpDisplayNameList));
        });
        productTask.setOnFailed(event -> {
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertContentText(Constant.DatabaseError.databaseUpdateError + event.getSource().exceptionProperty().getValue())
                    .alertTitle(Constant.DatabaseError.databaseErrorAlertTitle)
                    .build()
                    .showAndWait();
        });

        staffTask.setOnSucceeded(event -> {
            this.staff = staffTask.getValue();
            showTransactionBasicDetails(this.staff, this.transaction);
        });

        staffTask.setOnFailed(event -> {
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertContentText(Constant.DatabaseError.databaseUpdateError + event.getSource().exceptionProperty().getValue())
                    .alertTitle(Constant.DatabaseError.databaseErrorAlertTitle)
                    .build()
                    .showAndWait();
        });

        executor.execute(productTask);
        executor.execute(customerTask);
        executor.execute(staffTask);
    }

    private void commitUpdatedQuotationToDatabase(){
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
                //Add back products for previous quotations
                for(ProductTransaction tmp : transaction.getProductTransactionList()){
                    float remain = tmp.getTotalNum() + originalProductQuantity.get(tmp.getProductId());
                    Optional<ProductTransaction> tmpProductTransaction = productTransactionObservableList.stream()
                            .filter(p -> p.getProductId().equals(tmp.getProductId())).findAny();
                    tmpProductTransaction.ifPresent(p -> p.setTotalNum(remain));
                    dbExecuteProduct.updateDatabase(DBQueries.UpdateQueries.Product.UPDATE_PRODUCT_QUANTITY,
                            remain, tmp.getProductId());
                }
                //Decrease products for current quotations
                for(ProductTransaction tmp : productTransactionObservableList){
                    float remain = tmp.getTotalNum() - tmp.getQuantity();
                    dbExecuteProduct.updateDatabase(DBQueries.UpdateQueries.Product.UPDATE_PRODUCT_QUANTITY,
                            remain, tmp.getProductId());
                }
                //Update Transaction to have the latest quotation
                Object[] objects = new Object[0];
                try {
                    transaction.getProductTransactionList().clear();
                    transaction.getProductTransactionList().addAll(productTransactionObservableList);
                    objects = ObjectSerializer.TRANSACTION_QUOTATION_SERIALIZER_UPDATE.serialize(transaction);
                } catch (IOException e) {
                    logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
                }
                int row = dbExecuteTransaction.updateDatabase(DBQueries.UpdateQueries.Transaction.UPDATE_TRANSACTION, objects);
                if(row == 0){
                    connection.rollback();
                    throw new RuntimeException("Error occurred when updating database. This might be caused by conflict actions on the transaction");
                }
                connection.commit();
                return  null;

            }
        };
        commitTask.exceptionProperty().addListener((observable, oldValue, newValue) ->  {
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
        productListTask.setOnSucceeded(event -> {
            List<String> missingProducts = new ArrayList<>();
            this.productList = productListTask.getValue();
            this.transaction.getProductTransactionList().forEach(p -> {
                Optional<Product> tmpProduct = this.productList.stream().filter(p1 -> p1.getProductId().equals(p.getProductId())).findFirst();
                if(!tmpProduct.isPresent()){
                    missingProducts.add(p.getProductId());

                }else{
                    p.setTotalNum(tmpProduct.get().getTotalNum());
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
                executor.execute(commitTask);
            }
        });

        //What about two process wanna manipulate the same quotation

        commitTask.setOnFailed(event ->{
            Connection connection = DBConnect.getConnection();
            try {
                connection.rollback();
                connection.setAutoCommit(true);
                new AlertBuilder()
                        .alertType(Alert.AlertType.ERROR)
                        .alertContentText(Constant.DatabaseError.databaseUpdateError + event.getSource().exceptionProperty().getValue())
                        .alertTitle(Constant.DatabaseError.databaseErrorAlertTitle)
                        .build()
                        .showAndWait();
            } catch (SQLException e) {
                logger.error(e.getMessage() + "\nThe full stack trace is: ", e);

            }
            dialogStage.close();
        });
        commitTask.setOnSucceeded(event ->{
            Connection connection = DBConnect.getConnection();
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            }
            dialogStage.close();
        });
        executor.execute(productListTask);
    }

    private void commitTransactionToDatabase() throws SQLException, IOException {
        Task<Customer> customerTask = new Task<Customer>() {
            @Override
            protected Customer call() throws Exception {
                return dbExecuteCustomer.selectFromDatabase(DBQueries.SelectQueries.Customer.SELECT_SINGLE_CUSTOMER, customer.getUserName()).get(0);
            }
        };
        Task<Void> commitTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Connection connection = DBConnect.getConnection();

                connection.setAutoCommit(false);
                Object[] objects = ObjectSerializer.TRANSACTION_QUOTATION_SERIALIZER_UPDATE.serialize(transaction);
                int row = dbExecuteTransaction.updateDatabase(DBQueries.UpdateQueries.Transaction.UPDATE_TRANSACTION, objects);
                if(storeCreditCheckBox.isSelected()){
                    double remainStoreCredit = customer.getStoreCredit() - Double.valueOf(storeCreditField.getText());
                    dbExecuteCustomer.updateDatabase(DBQueries.UpdateQueries.Customer.UPDATE_CUSTOMER_STORE_CREDIT,
                            remainStoreCredit, customer.getUserName());
                }
                if(row == 0){
                    connection.rollback();
                    throw new RuntimeException("Error occurred when updating database. This might be caused by conflict actions on the transaction");
                }
                connection.commit();
                return null;
            }
        };
        customerTask.exceptionProperty().addListener((observable, oldValue, newValue) ->  {
            if(newValue != null) {
                Exception ex = (Exception) newValue;
                logger.error(ex.getMessage(), ex);
            }
        });
        commitTask.exceptionProperty().addListener((observable, oldValue, newValue) ->  {
            if(newValue != null) {
                Exception ex = (Exception) newValue;
                logger.error(ex.getMessage(), ex);
            }
        });
        customerTask.setOnSucceeded(event -> {
            this.customer.setStoreCredit(customerTask.getValue().getStoreCredit());
            executor.execute(commitTask);
        });
        commitTask.setOnFailed(event ->{
            Connection connection = DBConnect.getConnection();
            try {
                connection.rollback();
                connection.setAutoCommit(true);
                new AlertBuilder()
                        .alertType(Alert.AlertType.ERROR)
                        .alertContentText(Constant.DatabaseError.databaseUpdateError + event.getSource().exceptionProperty().getValue())
                        .alertTitle(Constant.DatabaseError.databaseErrorAlertTitle)
                        .build()
                        .showAndWait();
            } catch (SQLException e) {
                logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            }
            dialogStage.close();
        });
        commitTask.setOnSucceeded(event ->{
            Connection connection = DBConnect.getConnection();
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.error(e.getMessage() + "\nThe full stack trace is: ", e);
            }
            dialogStage.close();
        });
        executor.execute(customerTask);
    }

    private void generateTransaction(Transaction.TransactionType type) throws IOException, SQLException {
        LocalDate originalDate = transaction.getDate();
        String originalPaymentType = transaction.getPaymentType();
        double originalPayment = transaction.getPayment();
        double originalStoreCredit = transaction.getStoreCredit();
        double originalTotal = transaction.getTotal();
        double originalGst = transaction.getGstTax();
        double originalPst = transaction.getPstTax();
        Transaction.TransactionType originalTransactionType = transaction.getType();
        transaction.setDate(DateUtil.parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
        transaction.setGstTax(Double.valueOf(gstLabel.getText()));
        transaction.setPstTax(Double.valueOf(pstLabel.getText()));
        transaction.setTotal(Double.valueOf(totalLabel.getText()));
        transaction.setNote(noteArea.getText());

        if(type.equals(Transaction.TransactionType.OUT)){
            double currentPayment = 0;
            double currentStoreCredit = 0;
            if(!paymentField.getText().trim().isEmpty()){
                currentPayment = Double.valueOf(paymentField.getText());
            }
            if(!storeCreditField.getText().trim().isEmpty()){
                currentStoreCredit = Double.valueOf(storeCreditField.getText());
            }
            transaction.setPayment(originalPayment + currentPayment);
            if(storeCreditCheckBox.isSelected()){
                transaction.setStoreCredit(originalStoreCredit + currentStoreCredit);
            }
            transaction.getPayinfo().add(new PaymentRecord(
                    transaction.getDate().toString(),
                    currentPayment + currentStoreCredit,
                    transaction.getPaymentType(),
                    (isDepositCheckBox.isSelected())? true : false));
            transaction.setType(Transaction.TransactionType.OUT);
        }
        boolean confirmed = this.saleSystem.showTransactionConfirmationPanel(this.transaction, this.customer, this.productTransactionObservableList);
        if(confirmed){
            if(!isEditable){
            commitTransactionToDatabase();
            }else{
                commitUpdatedQuotationToDatabase();
            }
            confirmedClicked = true;
        }else{
            if(type.equals(Transaction.TransactionType.OUT)){
                if(transaction.getPayinfo().size() != 0){
                    transaction.getPayinfo().remove(transaction.getPayinfo().size() - 1);
                }
                transaction.setPayment(originalPayment);
                transaction.setStoreCredit(originalStoreCredit);
                transaction.setPaymentType(originalPaymentType);
                transaction.setType(originalTransactionType);
            }
            transaction.setDate(originalDate);
            transaction.setGstTax(originalGst);
            transaction.setTotal(originalTotal);
            transaction.setPstTax(originalPst);
        }
    }

    public boolean isConfirmedClicked(){
        return this.confirmedClicked;
    }
}
