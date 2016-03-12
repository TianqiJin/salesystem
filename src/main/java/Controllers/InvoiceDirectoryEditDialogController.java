/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package Controllers;

import MainClass.SaleSystem;
import PDF.InvoiceGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.Address;
import model.Customer;
import model.Staff;
import model.Transaction;
import util.AlertBuilder;

import java.io.File;

/**
 * Created by tjin on 3/7/2016.
 */
public class InvoiceDirectoryEditDialogController {
    private Customer customer;
    private Stage dialogStage;
    private File selectedDirectory;
    private StringBuilder errorMsgBuilder;
    private Transaction transaction;
    private Staff staff;

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
        deliveryInvoiceCheckbox.setSelected(true);
        deliveryInvoiceCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            deliveryTitledPane.setDisable(newValue? false : true);
        });
        invoiceDirectoryLabel.setText("");
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
        if(deliveryInvoiceCheckbox.isSelected()){
            if(isInvoicedirectoryValid() && isFieldValid()){
                Address address = new Address(streetField.getText().trim(), cityField.getText().trim(), postalCodeField.getText().trim());
                try {
                    InvoiceGenerator generator = new InvoiceGenerator(selectedDirectory.toString());
                    generator.buildInvoice(transaction, customer, staff);
                    if (transaction.getType()== Transaction.TransactionType.OUT){
                        generator.buildDelivery(transaction,customer,staff,address);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                dialogStage.close();
            }else{
                new AlertBuilder()
                        .alertType(Alert.AlertType.ERROR)
                        .alertHeaderText("Please fix the following error!")
                        .alertContentText(errorMsgBuilder.toString())
                        .alertTitle("Invoice Error")
                        .build()
                        .showAndWait();
            }

        }else{
            if(isInvoicedirectoryValid()){
                try {
                    InvoiceGenerator generator = new InvoiceGenerator(selectedDirectory.toString());
                    generator.buildInvoice(transaction, customer, staff);
                    if (transaction.getType()== Transaction.TransactionType.OUT){
                        generator.buildDelivery(transaction,customer,staff);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                dialogStage.close();
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

        //            try {
//                Customer customer= dbExecuteCustomer.selectFromDatabase(DBQueries.SelectQueries.Customer.SELECT_SINGLE_CUSTOMER,info).get(0);
//                InvoiceGenerator generator = new InvoiceGenerator(selectedDirectory.toString());
//                generator.buildInvoice(selectedTransaction,customer, this.saleSystem.getStaff());
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            new AlertBuilder()
//                    .alertType(Alert.AlertType.INFORMATION)
//                    .alertContentText("Report generated successfully!")
//                    .build()
//                    .showAndWait();

    }
    @FXML
    public void handleCancelButton(){
        dialogStage.close();
    }

    public void setCustomer(Customer customer){
        this.customer = customer;
    }
    public void setDialogStage(Stage stage){
        this.dialogStage = stage;
    }
    public void setTransaction(Transaction transaction){
        this.transaction = transaction;
    }
    public void setStaff(Staff staff){
        this.staff = staff;
    }

    private boolean isFieldValid(){
        if(streetField.getText().trim().isEmpty()){
            errorMsgBuilder.append("Street Field is empty!\n");
        }
        if(cityField.getText().trim().isEmpty()){
            errorMsgBuilder.append("City Field is empty!\n");
        }
        if(postalCodeField.getText().trim().isEmpty()){
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

}
