package Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;


/**
 * Created by Tjin on 5/20/2017.
 */
public class TransactionConfirmationController {
    public static Logger logger= Logger.getLogger(TransactionConfirmationController.class);
    private Stage dialogStage;
    private Customer customer;
    private ObservableList<ProductTransaction> productTransactionObservableList;
    private Transaction transaction;
    private boolean confirmedClicked;

    //TableView GUI Items
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
    //Payment Details GUI Items
    @FXML
    private Label paymentDateLabel;
    @FXML
    private Label paymentTypeLabel;
    @FXML
    private Label paymentAmountLabel;
    @FXML
    private Label isDepositLabel;
    //Customer GUI Items
    @FXML
    private Label firstNameLabel;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Button confirmButton;

    @FXML
    private void initialize(){
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        displayNameCol.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discount"));
        subTotalCol.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        showCustomerDetails();
        showPaymentDetails(null);
    }

    @FXML
    public void handleCancelButton(){
        this.dialogStage.close();
    }

    @FXML
    public void handleConfirmButton() throws IOException {
        this.confirmedClicked = true;
        confirmButton.setDisable(true);
        dialogStage.close();
    }
    /*
    * Constructor
    * */
    public TransactionConfirmationController(){}

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

    public void setSelectedTransaction(Transaction transaction) {
        this.transaction = transaction;
        this.productTransactionObservableList = FXCollections.observableArrayList(transaction.getProductTransactionList());
        this.transactionTableView.setItems(this.productTransactionObservableList);
        if(transaction.getPayinfo().size() != 0){
            showPaymentDetails(transaction.getPayinfo().get(transaction.getPayinfo().size() - 1));
        }else{
            showPaymentDetails(null);
        }
    }

    public void setProductTransactionObservableList(ObservableList<ProductTransaction> productTransactionObservableList){
        this.productTransactionObservableList = productTransactionObservableList;
        this.transactionTableView.setItems(this.productTransactionObservableList);
    }

    public void setCustomer(Customer customer){
        this.customer = customer;
        showCustomerDetails();
    }

    public boolean isConfirmedClicked(){
        return this.confirmedClicked;
    }

    /*
    * Show customer details grid pane
    * */
    private void showCustomerDetails(){
        if(this.customer != null){
            firstNameLabel.setText(this.customer.getFirstName());
            lastNameLabel.setText(this.customer.getLastName());
            phoneLabel.setText(this.customer.getPhone());
            emailLabel.setText(this.customer.getEmail());
        }
        else{
            firstNameLabel.setText("");
            lastNameLabel.setText("");
            phoneLabel.setText("");
            emailLabel.setText("");
        }
    }

    private void showPaymentDetails(PaymentRecord paymentRecord){
        if(paymentRecord != null){
            paymentDateLabel.setText(paymentRecord.getDate());
            paymentTypeLabel.setText(paymentRecord.getPaymentType());
            paymentAmountLabel.setText(String.valueOf(paymentRecord.getPaid()));
            isDepositLabel.setText(paymentRecord.isDeposit()? "Yes" : "No");
        }else{
            paymentDateLabel.setText("");
            paymentTypeLabel.setText("");
            paymentAmountLabel.setText("");
            isDepositLabel.setText("");
        }
    }

}
