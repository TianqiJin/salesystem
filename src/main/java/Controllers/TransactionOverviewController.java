package Controllers;


import MainClass.SaleSystem;
import db.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.converter.BigDecimalStringConverter;
import model.Customer;
import model.ProductTransaction;
import model.Transaction;
import util.DateUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

public class TransactionOverviewController implements OverviewController{

    private SaleSystem saleSystem;
    private ObservableList<Transaction> transactionList;
    private DBExecuteTransaction dbExecuteTransaction;
    private DBExecuteCustomer dbExecuteCustomer;
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
    private void initialize(){
        transactionIdCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("Date"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("Type"));
        infoCol.setCellValueFactory(new PropertyValueFactory<>("Info"));
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        subTotalCol.setCellValueFactory(new PropertyValueFactory<>("subTotal"));

        loadDataFromDB();
        showTransactionDetail(null);
        FilteredList<Transaction> filteredData = new FilteredList<Transaction>(transactionList,p->true);
        filterField.textProperty().addListener((observable,oldVal,newVal)->{
            filteredData.setPredicate(transaction -> {
                if (newVal == null || newVal.isEmpty()){
                    return true;
                }
                String lowerCase = newVal.toLowerCase();
                if (String.valueOf(transaction.getTransactionId()).equals(lowerCase)){
                    return true;
                }else if (transaction.getType().name().toLowerCase().contains(lowerCase)){
                    return true;
                }else if (transaction.getDate().toString().toLowerCase().contains(lowerCase)){
                    return true;
                }else if (transaction.getInfo().toLowerCase().contains(lowerCase)){
                    return true;
                }
                return false;
            });
            transactionTable.setItems(filteredData);
        });
        transactionTable.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Transaction>() {
                    @Override
                    public void changed(ObservableValue<? extends Transaction> observable, Transaction oldValue, Transaction newValue) {
                        showTransactionDetail(newValue);
                    }
                }
        );
    }

    @FXML
    private void handleDeleteTransaction(){
        int selectedIndex = transactionTable.getSelectionModel().getSelectedIndex();
        //transactionTable.getItems().remove(selectedIndex);
        //TODO: delete the corresponding info in the database
        //TODO: add if-else block for selectedIndex = -1 situation
    }

    @FXML
    private void handleAddTransaction(){
        Transaction newTransaction = saleSystem.showGenerateCustomerTransactionDialog();
        if(newTransaction != null){
            transactionList.add(newTransaction);
            loadDataFromDB();
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
        Transaction newTransaction = saleSystem.showGenerateReturnTransactionDialog();
        if(newTransaction != null){
            transactionList.add(newTransaction);
            loadDataFromDB();
        }
    }

    @FXML
    private void handleEditTransaction(){
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        if(selectedTransaction != null){
            if(!selectedTransaction.getType().equals(Transaction.TransactionType.OUT)){
                Alert alert = new Alert(Alert.AlertType.ERROR, "You can only edit OUT transaction!\n");
                alert.showAndWait();
            }else{
                boolean okClicked = saleSystem.showTransactionEditDialog(selectedTransaction);
                if(okClicked){
                    showTransactionDetail(selectedTransaction);
                }
            }

        }
    }

    public TransactionOverviewController(){
        dbExecuteTransaction = new DBExecuteTransaction();
        dbExecuteCustomer = new DBExecuteCustomer();
    }

    @Override
    public void loadDataFromDB() {
        try{
            transactionList = FXCollections.observableArrayList(
                    dbExecuteTransaction.selectFromDatabase(DBQueries.SelectQueries.Transaction.SELECT_ALL_TRANSACTION)
            );
        }catch(SQLException e){
            Alert alert = new Alert(Alert.AlertType.WARNING, "Unable to grab data from database!\n" + e.getMessage());
            alert.setTitle("Database Error");
            alert.showAndWait();
        }
        transactionTable.setItems(transactionList);
        transactionTable.getSelectionModel().selectFirst();
    }

    @Override
    public void setMainClass(SaleSystem saleSystem) {
        this.saleSystem = saleSystem;
    }


    public void showTransactionDetail(Transaction transaction){
        if(transaction != null){
            transactionIdLabel.setText(String.valueOf(transaction.getTransactionId()));
            dateLabel.setText(DateUtil.format(transaction.getDate()));
            totalLabel.setText(String.valueOf(transaction.getTotal()));
            paymentLabel.setText(String.valueOf(transaction.getPayment()));
            paymentTypeLabel.setText(transaction.getPaymentType());
            storeCreditLabel.setText(String.valueOf(transaction.getStoreCredit()));
            staffLabel.setText(String.valueOf(transaction.getStaffId()));
            typeLabel.setText(transaction.getType().name());
            infoLabel.setText(transaction.getInfo());
            transactionDetaiTableView.setItems(
                    FXCollections.observableArrayList(transaction.getProductTransactionList()));
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
        }
    }


}
