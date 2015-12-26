package Controllers;


import MainClass.SaleSystem;
import db.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.converter.BigDecimalStringConverter;
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

    @FXML
    private TableView<Transaction> transactionTable;
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
    private Label paymentLabel;
    @FXML
    private Label paymentTypeLabel;
    @FXML
    private Label staffLabel;
    @FXML
    private Label typeLabel;
    @FXML
    private Label infoLabel;
    @FXML
    private TableView<ProductTransaction> transactionDetaiTableView;
    @FXML
    private TableColumn<ProductTransaction, Integer> productIdCol;
    @FXML
    private TableColumn<ProductTransaction, Integer> qtyCol;
    @FXML
    private TableColumn<ProductTransaction, BigDecimal> subTotalCol;

    @FXML
    private void initialize(){
        transactionIdCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("Date"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("Type"));
        infoCol.setCellValueFactory(new PropertyValueFactory<>("Info"));
        showTransactionDetail(null);
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
        transactionTable.getItems().remove(selectedIndex);
        //TODO: delete the corresponding info in the database
        //TODO: add if-else block for selectedIndex = -1 situation
    }

    @FXML
    private void handleAddTransaction(){
        Transaction newTransaction = saleSystem.showGenerateCustomerTransactionDialog();
        if(newTransaction != null){
            transactionList.add(newTransaction);
        }
    }

    public TransactionOverviewController(){
        dbExecuteTransaction = new DBExecuteTransaction();
    }

    @Override
    public void loadDataFromDB() {
        transactionList = FXCollections.observableArrayList(
                dbExecuteTransaction.selectFromDatabase(DBQueries.SelectQueries.Transaction.SELECT_ALL_TRANSACTION)
        );
        transactionTable.setItems(transactionList);
    }

    @Override
    public void setMainClass(SaleSystem saleSystem) {
        this.saleSystem = saleSystem;
    }


    public void showTransactionDetail(Transaction transaction){
        if(transaction != null){
            transactionIdLabel.setText(String.valueOf(transaction.getTransactionId()));
            dateLabel.setText(DateUtil.format(transaction.getDate()));
            paymentLabel.setText(String.valueOf(transaction.getPayment()));
            paymentTypeLabel.setText(transaction.getPaymentType());
            staffLabel.setText(String.valueOf(transaction.getStaffId()));
            typeLabel.setText(transaction.getType().name());
            infoLabel.setText(transaction.getInfo());
            transactionDetaiTableView.setItems(FXCollections.observableArrayList(transaction.getProductTransactionList()));
            productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
            qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            subTotalCol.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        }
        else{
            transactionIdLabel.setText("");
            dateLabel.setText("");
            paymentLabel.setText("");
            paymentTypeLabel.setText("");
            staffLabel.setText("");
            typeLabel.setText("");
            infoLabel.setText("");
        }
    }


}
