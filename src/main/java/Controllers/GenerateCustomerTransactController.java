package Controllers;

import MainClass.SaleSystem;
import db.DBExecuteProduct;
import db.DBExecuteTransaction;
import db.DBQueries;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import model.Customer;
import model.Product;
import model.ProductTransaction;
import model.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tjin on 12/7/2015.
 */
public class GenerateCustomerTransactController {

    private Stage dialogStage;
    private Customer customer;
    private ObservableList<Transaction> customerList;
    private DBExecuteProduct dbExecute;
    private SaleSystem saleSystem;
    private Transaction transaction;

    @FXML
    private TableView<ProductTransaction> transactionTableView;
    @FXML
    private TableColumn<ProductTransaction, String> productIdCol;
    @FXML
    private TableColumn<ProductTransaction, String> stockCol;
    @FXML
    private TableColumn<ProductTransaction, String> unitPriceCol;
    @FXML
    private TableColumn<ProductTransaction, String> qtyCol;
    @FXML
    private TableColumn<ProductTransaction, String> subTotalCol;

    @FXML
    private Label firstNameLabel;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Label discountLabel;

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
    private void initialize(){
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("totalNum"));
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        qtyCol.setCellFactory(TextFieldTableCell.forTableColumn());
        qtyCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ProductTransaction, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<ProductTransaction, String> event) {
                (event.getTableView().getItems().get(event.getTablePosition().getRow()))
                        .setQuantity(Integer.valueOf(event.getNewValue()));

            }
        });
        subTotalCol.setCellValueFactory(new PropertyValueFactory<ProductTransaction, String>("subTotal"));

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
        showCustomerDetails(null);
    }
    @FXML
    public void handleAddItem(){
        List<Product> productResult = dbExecute.selectFromDatabase(DBQueries.SelectQueries.Product.SELECT_PRODUCTID_PROJECT,
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
            transaction.getProductTransactionList().add(new ProductTransaction.ProductTransactionBuilder()
                                                            .productId(productResult.get(0).getProductId())
                                                            .totalNum(productResult.get(0).getTotalNum())
                                                            .unitPrice(productResult.get(0).getUnitPrice())
                                                            .build());
            transactionTableView.setItems(FXCollections.observableArrayList(transaction.getProductTransactionList()));
        }

    }

    public GenerateCustomerTransactController(){
        dbExecute = new DBExecuteProduct();
    }

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

    public void showCustomerDetails(Customer customer){
        if(customer != null){
            firstNameLabel.setText(customer.getFirstName());
            lastNameLabel.setText(customer.getLastName());
            discountLabel.setText(customer.getUserClass());
        }
        else{
            firstNameLabel.setText("");
            lastNameLabel.setText("");
            discountLabel.setText("");
        }
    }

    public void setMainClass(SaleSystem saleSystem){
        this.saleSystem = saleSystem;
        transaction = new Transaction.TransactionBuilder()
                .productInfoList(new ArrayList<ProductTransaction>())
                .staffId(saleSystem.getStaffId())
                .date(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                .type(Transaction.TransactionType.OUT)
                .build();
    }
}
