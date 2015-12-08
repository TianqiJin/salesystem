package Controllers;


import MainClass.SaleSystem;
import db.DBExecuteTransaction;
import db.DBQueries;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Transaction;

public class TransactionOverviewController implements OverviewController{

    private SaleSystem saleSystem;
    private ObservableList<Transaction> transactionList;


    @FXML
    private TableView<Transaction> transactionTable;
    //@FXML
    //private TableColumn<Transaction, Integer> transactionIdCol;
    @FXML
    private TableColumn<Transaction, Integer> productIdCol;
    @FXML
    private TableColumn<Transaction, String> dateCol;
    @FXML
    private TableColumn<Transaction, String> typeCol;
    @FXML
    private TableColumn<Transaction, String> infoCol;
    @FXML
    private Label transactionIdLabel;
    @FXML
    private Label productIdLabel;
    @FXML
    private Label numBoxesLabel;
    @FXML
    private Label numPiecesLabel;
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
    private void initialize(){
        //transactionIdCol.setCellValueFactory(new PropertyValueFactory<>("TransactionId"));
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("ProductId"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("Date"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("Type"));
        infoCol.setCellValueFactory(new PropertyValueFactory<>("Info"));
        showProductDetail(null);
        transactionTable.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Transaction>() {
                    @Override
                    public void changed(ObservableValue<? extends Transaction> observable, Transaction oldValue, Transaction newValue) {
                        showProductDetail(newValue);
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

    private DBExecuteTransaction dbExecute;
    public TransactionOverviewController(){
        dbExecute = new DBExecuteTransaction();
    }

    @Override
    public void loadDataFromDB() {
        transactionList = FXCollections.observableArrayList(
                dbExecute.selectFromDatabase(DBQueries.SelectQueries.Transaction.SELECT_ALL_TRANSACTION)
        );
        transactionTable.setItems(transactionList);
    }

    @Override
    public void setMainClass(SaleSystem saleSystem) {
        this.saleSystem = saleSystem;
    }


    public void showProductDetail(Transaction transaction){
        if(transaction != null){
            transactionIdLabel.setText(String.valueOf(transaction.getTransactionId()));
            productIdLabel.setText(String.valueOf(transaction.getProductId()));
            numBoxesLabel.setText(String.valueOf(transaction.getNumofBoxes()));
            numPiecesLabel.setText(String.valueOf(transaction.getNumofPieces()));
            dateLabel.setText(transaction.getDate());
            paymentLabel.setText(String.valueOf(transaction.getPayment()));
            paymentTypeLabel.setText(transaction.getPaymentType());
            staffLabel.setText(String.valueOf(transaction.getStaffId()));
            typeLabel.setText(transaction.getType().name());
            infoLabel.setText(transaction.getInfo());
        }
        else{
            transactionIdLabel.setText("");
            productIdLabel.setText("");
            numBoxesLabel.setText("");
            numPiecesLabel.setText("");
            dateLabel.setText("");
            paymentLabel.setText("");
            paymentTypeLabel.setText("");
            staffLabel.setText("");
            typeLabel.setText("");
            infoLabel.setText("");
        }
    }
}
