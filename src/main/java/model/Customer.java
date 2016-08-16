package model;

import javafx.beans.property.*;

import java.text.SimpleDateFormat;
import java.util.*;

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
    private final DoubleProperty storeCredit;
    private final StringProperty company;
    private final StringProperty pstNumber;
    private String customerInfo;
    private String customerPhoneInfo;

    public Customer(CustomerBuilder builder){
        this.userName = new SimpleStringProperty(builder.userName);
        this.firstName = new SimpleStringProperty(builder.firstName);
        this.lastName = new SimpleStringProperty(builder.lastName);
        this.street = new SimpleStringProperty(builder.street);
        this.postalCode = new SimpleStringProperty(builder.postalCode);
        this.city = new SimpleStringProperty(builder.city);
        this.phone = new SimpleStringProperty(builder.phone);
        this.userClass = new SimpleStringProperty(builder.userClass);
        this.email = new SimpleStringProperty(builder.email);
        this.storeCredit = new SimpleDoubleProperty(builder.storeCredit);
        this.company = new SimpleStringProperty(builder.company);
        this.pstNumber = new SimpleStringProperty(builder.pstNumber);
    }

    public static class CustomerBuilder{
        private String userName;
        private String firstName;
        private String lastName;
        private String street;
        private String postalCode;
        private String city;
        private String phone;
        private String userClass = "C";
        private String email;
        private double storeCredit = 0.0;
        private String company;
        private String pstNumber;

        public CustomerBuilder userName(String userName){
            this.userName = userName;
            return this;
        }
        public CustomerBuilder firstName(String firstName){
            this.firstName = firstName;
            return this;
        }
        public CustomerBuilder lastName(String lastName){
            this.lastName = lastName;
            return this;
        }
        public CustomerBuilder street(String street){
            this.street = street;
            return this;
        }
        public CustomerBuilder postalCode(String postalCode){
            this.postalCode = postalCode;
            return this;
        }
        public CustomerBuilder city(String city){
            this.city = city;
            return this;
        }
        public CustomerBuilder phone(String phone){
            this.phone = phone;
            return this;
        }
        public CustomerBuilder userClass(String userClass){
            this.userClass = userClass;
            return this;
        }
        public CustomerBuilder email(String email){
            this.email = email;
            return this;
        }
        public CustomerBuilder storeCredit(double storeCredit){
            this.storeCredit = storeCredit;
            return this;
        }
        public CustomerBuilder company(String company){
            this.company = company;
            return this;
        }
        public CustomerBuilder pstNumber(String pstNumber){
            this.pstNumber = pstNumber;
            return this;
        }
        public Customer build(){
            return new Customer(this);
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

    public double getStoreCredit() {
        return storeCredit.get();
    }

    public DoubleProperty storeCreditProperty() {
        return storeCredit;
    }

    public void setStoreCredit(double storeCredit) {
        this.storeCredit.set(storeCredit);
    }

    public String getCompany() {
        return company.get();
    }

    public StringProperty companyProperty() {
        return company;
    }

    public void setCompany(String company) {
        this.company.set(company);
    }

    public String getPstNumber() {
        return pstNumber.get();
    }

    public StringProperty pstNumberProperty() {
        return pstNumber;
    }

    public void setPstNumber(String pstNumber) {
        this.pstNumber.set(pstNumber);
    }

    private String generateUserName(){
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        String tmpUserName = getFirstName().substring(0,1) + getLastName() +
                sdf.format(Calendar.getInstance().getTime());
        return tmpUserName;
    }

    public Object[] getAllProperties(){
        return (new Object[]{getUserName(), getFirstName(), getLastName(), getStreet(), getPostalCode(), getCity(),
            getPhone(), getUserClass(), getEmail(), getStoreCredit(), getCompany(), getPstNumber()});
    }

    public Object[] getAllPropertiesForUpdate(){
        return (new Object[]{getFirstName(), getLastName(), getStreet(), getPostalCode(), getCity(),
                getPhone(), getUserClass(), getEmail(), getStoreCredit(), getCompany(), getPstNumber(), getUserName()});
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

    public void constructCustomerPhoneInfo(){
        if(this.getPhone() == null){
            this.customerPhoneInfo = new StringBuffer()
                    .append("N/A")
                    .append(" ")
                    .append("(")
                    .append(this.getFirstName())
                    .append(" ")
                    .append(this.getLastName())
                    .append(")")
                    .toString();
        }else{
            this.customerPhoneInfo = new StringBuffer()
                    .append(this.getPhone().trim())
                    .append(" ")
                    .append("(")
                    .append(this.getFirstName())
                    .append(" ")
                    .append(this.getLastName())
                    .append(")")
                    .toString();
        }
    }

    public String getCustomerPhoneInfo(){ return this.customerPhoneInfo; }

//    public static Map<String, Integer> getDiscountMap() {
//        return discountMap;
//    }
}
