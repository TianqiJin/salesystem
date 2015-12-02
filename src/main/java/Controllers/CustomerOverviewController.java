package Controllers;

import com.sun.xml.internal.bind.v2.TODO;
import db.DBExecuteCustomer;
import db.DBQueries;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Customer;

import java.util.List;

/**
 * Created by tjin on 11/29/2015.
 */
public class CustomerOverviewController {
    private DBExecuteCustomer dbExecute;

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
    private Label phoneLabel;

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
        if(selectedIndex >= 0){
            String tempFirstName = customerTable.getItems().get(selectedIndex).getFirstName();
            String tempLastName = customerTable.getItems().get(selectedIndex).getLastName();
            if(dbExecute.deleteDatabase(DBQueries.DeleteQueries.Customer.DELETE_FROM_CUSTOMER, tempFirstName, tempLastName) == 0){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Delete Customer Error");
                alert.setHeaderText(null);
                alert.setContentText("Error when deleting customer "+tempFirstName+" "+tempLastName);
                alert.showAndWait();
            }
            else{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Delete Customer Successfully");
                alert.setHeaderText(null);
                alert.setContentText("Successfully deteled customer "+tempFirstName+" "+tempLastName);
                alert.showAndWait();
            }
            customerTable.getItems().remove(selectedIndex);
        }
        else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Customer Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a person in the table.");
            alert.showAndWait();
        }

    }

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
            phoneLabel.setText(customer.getPhone());
        }
        else{
            firstNameLabel.setText("");
            lastNameLabel.setText("");
            streetLabel.setText("");
            postalCodeLabel.setText("");
            cityLabel.setText("");
            phoneLabel.setText("");
        }
    }
}
