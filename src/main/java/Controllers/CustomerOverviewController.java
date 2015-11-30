package Controllers;

import db.DBExecuteCustomer;
import db.DBQueries;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.Customer;

import java.util.List;

/**
 * Created by tjin on 11/29/2015.
 */
public class CustomerOverviewController {
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
        firstNameCol.setCellValueFactory(cellData->cellData.getValue().firstNameProperty());
        lastNameCol.setCellValueFactory(cellData->cellData.getValue().lastNameProperty());
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

}
