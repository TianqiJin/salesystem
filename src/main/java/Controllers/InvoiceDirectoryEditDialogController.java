/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package Controllers;

import Constants.Constant;
import MainClass.SaleSystem;
import PDF.InvoiceGenerator;
import db.DBExecuteStaff;
import db.DBQueries;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.*;
import org.apache.log4j.Logger;
import util.AlertBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by tjin on 3/7/2016.
 */
public class InvoiceDirectoryEditDialogController {
    private static Logger logger= Logger.getLogger(InvoiceDirectoryEditDialogController.class);
    private Customer customer;
    private Stage dialogStage;
    private File selectedDirectory;
    private StringBuilder errorMsgBuilder;
    private Transaction transaction;
    private Staff staff;
    private SaleSystem saleSystem;
    private Executor executor;
    private DBExecuteStaff dbExecute;
    private List<Staff> staffList;

    @FXML
    private TextField streetField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField postalCodeField;
    @FXML
    private CheckBox customerAddressCheckbox;
    @FXML
    private CheckBox deliveryInvoiceCheckbox;
    @FXML
    private CheckBox invoiceCheckBox;
    @FXML
    private CheckBox quotationInvoiceCheckBox;
    @FXML
    private CheckBox poCheckBox;
    @FXML
    private Label invoiceDirectoryLabel;
    @FXML
    private TitledPane deliveryTitledPane;

    @FXML
    private void initialize(){
        customerAddressCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                streetField.setText(customer.getStreet());
                cityField.setText(customer.getCity());
                postalCodeField.setText(customer.getPostalCode());
            }else{
                streetField.clear();
                cityField.clear();
                postalCodeField.clear();
            }
        });
        invoiceCheckBox.setSelected(true);
        deliveryInvoiceCheckbox.setSelected(true);
        invoiceDirectoryLabel.setText("");

        executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    @FXML
    public void handleInvoiceDirectory(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Invoice Directory");
        selectedDirectory = directoryChooser.showDialog(dialogStage);
        if(selectedDirectory != null){
            invoiceDirectoryLabel.setText(selectedDirectory.toString());
        }
    }

    @FXML
    public void handleConfirmButton(){
        errorMsgBuilder = new StringBuilder();
        if(isInvoicedirectoryValid()){
            try{
                InvoiceGenerator generator = new InvoiceGenerator(selectedDirectory.toString(), this.saleSystem);
                if(isFieldValid()){
                    Address address = new Address(streetField.getText().trim(), cityField.getText().trim(), postalCodeField.getText().trim());
                    if(invoiceCheckBox.isSelected()){
                        generator.buildInvoice(transaction,customer,staff, address);
                    }
                    if(quotationInvoiceCheckBox.isSelected()){
                        generator.buildQuotation(transaction, customer, staff, address);
                    }
                    if(deliveryInvoiceCheckbox.isSelected()){
                        generator.buildDelivery(transaction, customer, staff, address);
                    }
                    if(poCheckBox.isSelected()){
                        generator.buildPo(transaction, customer, staff, address);
                    }
                }else{
                    new AlertBuilder()
                            .alertType(Alert.AlertType.ERROR)
                            .alertHeaderText("Please fix the following error")
                            .alertContentText(errorMsgBuilder.toString())
                            .alertTitle("Invoice Error")
                            .build()
                            .showAndWait();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            if(errorMsgBuilder.length() == 0){
                dialogStage.close();
            }
        }else{
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertHeaderText("Please fix the following error")
                    .alertContentText(errorMsgBuilder.toString())
                    .alertTitle("Invoice Error")
                    .build()
                    .showAndWait();
        }
    }
    @FXML
    public void handleCancelButton(){
        dialogStage.close();
    }

    public InvoiceDirectoryEditDialogController(){
        this.dbExecute = new DBExecuteStaff();
    }
    public void setCustomer(Customer customer){
        this.customer = customer;
    }
    public void setDialogStage(Stage stage){
        this.dialogStage = stage;
    }
    public void setTransaction(Transaction transaction){
        this.transaction = transaction;
        if(this.transaction.getType().equals(Transaction.TransactionType.RETURN)){
            deliveryInvoiceCheckbox.setSelected(false);
            deliveryInvoiceCheckbox.setDisable(true);
            quotationInvoiceCheckBox.setDisable(true);
            poCheckBox.setDisable(true);
        }else if(this.transaction.getType().equals(Transaction.TransactionType.QUOTATION)){
            deliveryInvoiceCheckbox.setSelected(false);
            deliveryInvoiceCheckbox.setDisable(true);
            invoiceCheckBox.setSelected(false);
            invoiceCheckBox.setDisable(true);
            quotationInvoiceCheckBox.setSelected(true);
            poCheckBox.setDisable(true);
        }else{
            quotationInvoiceCheckBox.setDisable(true);
        }
    }

    public void setMainClass(SaleSystem saleSystem){
        this.saleSystem = saleSystem;
    }

    private boolean isFieldValid(){
        if(streetField.getText() == null || streetField.getText().trim().isEmpty()){
            errorMsgBuilder.append("Street Field is empty!\n");
        }
        if(cityField.getText() == null || cityField.getText().trim().isEmpty()){
            errorMsgBuilder.append("City Field is empty!\n");
        }
        if(postalCodeField.getText() == null || postalCodeField.getText().trim().isEmpty()){
            errorMsgBuilder.append("Postal Code Field is empty!\n");
        }
        if(errorMsgBuilder.length() == 0){
            return true;
        }
        return false;
    }
    private boolean isInvoicedirectoryValid(){
        if(selectedDirectory == null){
            errorMsgBuilder.append("You should select directory for your invoices!\n");
        }
        if(errorMsgBuilder.length() == 0){
            return true;
        }
        return false;
    }

    public void loadDataFromDB() {
        Task<List<Staff>> staffTask = new Task<List<Staff>>() {
            @Override
            protected List<Staff> call() throws Exception {
                List<Staff> tmpStaffList = new ArrayList<>();
                tmpStaffList = dbExecute.selectFromDatabase(DBQueries.SelectQueries.Staff.SELECT_ALL_STAFF);
                return  tmpStaffList;
            }
        };

        staffTask.setOnSucceeded(event -> {
            staffList = staffTask.getValue();
            Staff tmpStaff = staffList.stream().filter(staff -> staff.getStaffId() == transaction.getStaffId()).findFirst().orElse(null);
            if(tmpStaff != null){
                this.staff = tmpStaff;
            }else{
                new AlertBuilder()
                        .alertType(Alert.AlertType.ERROR)
                        .alertTitle("Staff Error")
                        .alertContentText("Unable to find staff id " + transaction.getStaffId())
                        .build()
                        .showAndWait();
            }
        });
        staffTask.setOnFailed(event -> {
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertContentText(Constant.DatabaseError.databaseReturnError + event.getSource().exceptionProperty().getValue())
                    .alertHeaderText(Constant.DatabaseError.databaseErrorAlertTitle)
                    .build()
                    .showAndWait();
        });

        executor.execute(staffTask);
    }
}
