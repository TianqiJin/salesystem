package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tjin on 11/29/2015.
 */
public class Customer {
    private final StringProperty userName;
    private final StringProperty firstName;    //lol....interesting.....
    private final StringProperty lastName;
    private final StringProperty street;
    private final StringProperty postalCode;
    private final StringProperty city;
    private final StringProperty phone;
    private final StringProperty userClass;
    private final StringProperty email;
    private final IntegerProperty storeCredit;

    public Customer(Object... params){
        this.userName = new SimpleStringProperty((String)params[0]);
        this.firstName = new SimpleStringProperty((String)params[1]);
        this.lastName = new SimpleStringProperty((String)params[2]);
        this.street = new SimpleStringProperty((String)params[3]);
        this.postalCode = new SimpleStringProperty((String)params[4]);
        this.city = new SimpleStringProperty((String)params[5]);
        this.phone = new SimpleStringProperty((String)params[6]);
        this.userClass = new SimpleStringProperty((String)params[7]);
        this.email = new SimpleStringProperty((String)params[8]);
        this.storeCredit = new SimpleIntegerProperty((Integer)params[9]);
    }

    public String getFirstName() {
        return firstName.get();
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public String getLastName() {
        return lastName.get();
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public String getStreet() {
        return street.get();
    }

    public StringProperty streetProperty() {
        return street;
    }

    public void setStreet(String street) {
        this.street.set(street);
    }

    public String getPostalCode() {
        return postalCode.get();
    }

    public StringProperty postalCodeProperty() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode.set(postalCode);
    }

    public String getCity() {
        return city.get();
    }

    public StringProperty cityProperty() {
        return city;
    }

    public void setCity(String city) {
        this.city.set(city);
    }
    public String getPhone() {
        return phone.get();
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    public String getUserName() {
        return userName.get();
    }


    public String getUserClass() {
        return userClass.get();
    }

    public StringProperty userClassProperty() {
        return userClass;
    }

    public void setUserClass(String userClass) {
        this.userClass.set(userClass);
    }

    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public int getStoreCredit() {
        return storeCredit.get();
    }

    public IntegerProperty storeCreditProperty() {
        return storeCredit;
    }

    public void setStoreCredit(int storeCredit) {
        this.storeCredit.set(storeCredit);
    }

}
