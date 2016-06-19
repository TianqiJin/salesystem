package Controllers;

import PDF.PDFGenerator;
import com.itextpdf.text.DocumentException;
import db.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Customer;
import model.Product;
import model.Staff;
import model.Transaction;
import org.apache.log4j.Logger;
import util.AutoCompleteComboBoxListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tjin on 12/28/15.
 */
public class GeneratePDFReportDialog {
    private static Logger logger= Logger.getLogger(GeneratePDFReportDialog.class);
    private Stage dialogStage;
    private List<Customer> customerList;
    private List<Product> productList;
    private List<Staff> staffList;
    private Customer customer;
    private Integer productId;
    private Integer staffId;
    private DBExecuteCustomer dbExecuteCustomer;
    private DBExecuteProduct dbExecuteProduct;
    private DBExecuteStaff dbExecuteStaff;
    private DBExecuteTransaction dbExecuteTransaction;
    private LocalDate startDate;
    private LocalDate endDate;
    private File selectedDirectory;

    @FXML
    private ComboBox<String> customerComboBox;
    @FXML
    private ComboBox<String> productComboBox;
    @FXML
    private ComboBox<String> staffComboBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button generateButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button destinationButton;
    @FXML
    private Label destinationLabel;

    @FXML
    private void initialize(){
        destinationLabel.setText("Please select a directory");

        try{
            customerList = dbExecuteCustomer.selectFromDatabase(DBQueries.SelectQueries.Customer.SELECT_ALL_CUSTOMER);
            productList = dbExecuteProduct.selectFromDatabase(DBQueries.SelectQueries.Product.SELECT_ALL_PRODUCT);
            staffList = dbExecuteStaff.selectFromDatabase(DBQueries.SelectQueries.Staff.SELECT_ALL_STAFF);
        }catch(SQLException e){
            logger.error(e.getMessage());
            Alert alert = new Alert(Alert.AlertType.WARNING, "Unable to grab data from database!\n" + e.getMessage());
            alert.setTitle("Database Error");
            alert.showAndWait();
        }
        List<String> tmpCustomerList = new ArrayList<>();
        List<String> tmpProductList = new ArrayList<>();
        List<String> tmpStaffList = new ArrayList<>();

        for(Customer customer: customerList){
            customer.constructCustomerInfo();
            tmpCustomerList.add(customer.getCustomerInfo());
        }
        for(Product product: productList){
            tmpProductList.add(product.getProductId());
        }
        for(Staff staff: staffList){
            staff.setInfo();
            tmpStaffList.add(staff.getInfo());
        }

        customerComboBox.setItems(FXCollections.observableArrayList(tmpCustomerList));
        productComboBox.setItems(FXCollections.observableArrayList(tmpProductList));
        staffComboBox.setItems(FXCollections.observableArrayList(tmpStaffList));
        new AutoCompleteComboBoxListener<>(customerComboBox);
        new AutoCompleteComboBoxListener<>(productComboBox);
        new AutoCompleteComboBoxListener<>(staffComboBox);

        customerComboBox.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                for(Customer tmpCustomer: customerList){
                    if(tmpCustomer.getCustomerInfo().equals(newValue)){
                        customer = tmpCustomer;
                        break;
                    }
                }
            }
        });

        staffComboBox.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                for(Staff tmpStaff: staffList){
                    if(tmpStaff.getInfo().equals(newValue)){
                        staffId = tmpStaff.getStaffId();
                        break;
                    }
                }
            }
        });

    }

    @FXML
    public void handleDestinationButton(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a directory");
        selectedDirectory = directoryChooser.showDialog(dialogStage);
        if(selectedDirectory == null){
            destinationLabel.setText("Please select a directory");
        }
        else{
            destinationLabel.setText(selectedDirectory.getPath());
        }
    }
    @FXML
    public void handleCancelButton(){
        dialogStage.close();
    }

    @FXML
    public void handleConfirmButton() throws FileNotFoundException, DocumentException {
        startDate = startDatePicker.getValue();
        endDate = endDatePicker.getValue();
        if(startDate == null || endDate == null){
            Alert alert = new Alert(Alert.AlertType.ERROR, "You should select both start date and end date!");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
        List<Transaction> returnedTransaction = null;
        try{
            returnedTransaction = dbExecuteTransaction.selectFromDatabase(
                            DBQueries.SelectQueries.Transaction.SELECT_ALL_TRANSACTION_FOR_REPORT, startDate, endDate);
        }catch(SQLException e){
            logger.error(e.getMessage());
            Alert alert = new Alert(Alert.AlertType.WARNING, "Unable to grab data from database!\n" + e.getMessage());
            alert.setTitle("Database Error");
            alert.showAndWait();
        }

        if(returnedTransaction.size() == 0){
            Alert alert = new Alert(Alert.AlertType.ERROR, "No transactions are returned in selected date range!");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
        else if(selectedDirectory == null){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a directory first!");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
        else{
            new PDFGenerator.PDFGeneratorBuilder()
                    .transactionList(returnedTransaction)
                    .customer(customer)
                    .productId(productId)
                    .staffId(staffId)
                    .destination(selectedDirectory.getPath())
                    .build()
                    .generate();
            dialogStage.close();
        }
    }

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

    public GeneratePDFReportDialog(){
        dbExecuteCustomer = new DBExecuteCustomer();
        dbExecuteProduct = new DBExecuteProduct();
        dbExecuteStaff = new DBExecuteStaff();
        dbExecuteTransaction = new DBExecuteTransaction();
    }
}
