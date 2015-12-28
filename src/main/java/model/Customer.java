package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by tjin on 11/29/2015.
 */
public class Customer {

    private final StringProperty userName;
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty street;
    private final StringProperty postalCode;
    private final StringProperty city;
    private final StringProperty phone;
    private final StringProperty userClass;
    private final StringProperty email;
    private final IntegerProperty storeCredit;
    private String customerInfo;

    public Customer(Object... params){
        if(params.length == 10){
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
        else{
            this.userName = new SimpleStringProperty(null);
            this.firstName = new SimpleStringProperty(null);
            this.lastName = new SimpleStringProperty(null);
            this.street = new SimpleStringProperty(null);
            this.postalCode = new SimpleStringProperty(null);
            this.city = new SimpleStringProperty(null);
            this.phone = new SimpleStringProperty(null);
            this.userClass = new SimpleStringProperty("C");
            this.email = new SimpleStringProperty(null);
            this.storeCredit = new SimpleIntegerProperty(0) {
            };
        }
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

    public void setUserName() {
        this.userName.set(generateUserName());
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

    private String generateUserName(){
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        String tmpUserName = getFirstName().substring(0,1) + getLastName() +
                sdf.format(Calendar.getInstance().getTime());
        return tmpUserName;
    }

    public Object[] getAllProperties(){
        return (new Object[]{getUserName(), getFirstName(), getLastName(), getStreet(), getPostalCode(), getCity(),
            getPhone(), getUserClass(), getEmail(), getStoreCredit()});
    }

    public Object[] getAllPropertiesForUpdate(){
        return (new Object[]{getFirstName(), getLastName(), getStreet(), getPostalCode(), getCity(),
                getPhone(), getUserClass(), getEmail(), getStoreCredit(), getUserName()});
    }

    public void constructCustomerInfo(){
        this.customerInfo = new StringBuffer()
                .append(getFirstName())
                .append(" ")
                .append(getLastName())
                .append(" ")
                .append("(")
                .append(getUserName())
                .append(")")
                .toString();
    }

    public String getCustomerInfo(){
        return this.customerInfo;
    }
}
