package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Customer;

/**
 * Created by tjin on 12/2/2015.
 */
public class CustomerEditDialogController {
    @FXML
    private TextField firstNameLabel;
    @FXML
    private TextField lastNameLabel;
    @FXML
    private TextField phoneLabel;
    @FXML
    private TextField streetLabel;
    @FXML
    private TextField postalCodeLabel;
    @FXML
    private TextField cityLabel;

    private Stage dialogStage;
    private Customer customer;
    private boolean onClicked = false;

    @FXML
    private void inittialize(){}

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }
    public void setCustomer(Customer customer){
        this.customer = customer;

        firstNameLabel.setText(customer.getFirstName());
        lastNameLabel.setText(customer.getLastName());
        streetLabel.setText(customer.getStreet());
        postalCodeLabel.setText(customer.getPostalCode());
        cityLabel.setText(customer.getCity());
        phoneLabel.setText(customer.getPhone());
    }
}
