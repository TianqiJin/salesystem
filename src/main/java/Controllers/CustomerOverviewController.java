package Controllers;

import com.sun.xml.internal.bind.v2.TODO;
import db.DBExecuteCustomer;
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
import model.Customer;


import java.util.List;

/**
 * Created by tjin on 11/29/2015.
 */
public class CustomerOverviewController implements OverviewController{
    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<Customer, String> firstNameCol;
    @FXML
    private TableColumn<Customer, String> lastNameCol;
    @FXML
    private Label firstNameLabel;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Label streetLabel;
    @FXML
    private Label postalCodeLabel;
    @FXML
    private Label cityLabel;

    @FXML
    private void initialize(){
        firstNameCol.setCellValueFactory(new PropertyValueFactory<Customer, String>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<Customer, String>("lastName"));
        showCustomerDetail(null);
        customerTable.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Customer>() {
                    @Override
                    public void changed(ObservableValue<? extends Customer> observable, Customer oldValue, Customer newValue) {
                        showCustomerDetail(newValue);
                    }
                }
        );
    }

    @FXML
    private void handleDeleteCustomer(){
        int selectedIndex = customerTable.getSelectionModel().getSelectedIndex();
        customerTable.getItems().remove(selectedIndex);
        //TODO: delete the corresponding info in the database
        //TODO: add if-else block for selectedIndex = -1 situation
    }

    private DBExecuteCustomer dbExecute;
    public CustomerOverviewController(){
        dbExecute = new DBExecuteCustomer();
    }
    public void loadDataFromDB(){
        ObservableList<Customer> customerList = FXCollections.observableArrayList(
                dbExecute.selectFromDatabase(DBQueries.SelectQueries.Customer.SELECT_ALL_CUSTOMER)
        );
        customerTable.setItems(customerList);
    }
    public void showCustomerDetail(Customer customer){
        if(customer != null){
            firstNameLabel.setText(customer.getFirstName());
            lastNameLabel.setText(customer.getLastName());
            streetLabel.setText(customer.getStreet());
            postalCodeLabel.setText(customer.getPostalCode());
            cityLabel.setText(customer.getCity());
        }
        else{
            firstNameLabel.setText("");
            lastNameLabel.setText("");
            streetLabel.setText("");
            postalCodeLabel.setText("");
            cityLabel.setText("");
        }
    }

}
