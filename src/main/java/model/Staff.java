package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.log4j.Logger;

/**
 * Created by jiawei.liu on 12/8/15.
 */
public class Staff {
    private static Logger logger = Logger.getLogger(Staff.class);
    private IntegerProperty staffId;
    private StringProperty userName;
    private StringProperty password;
    private StringProperty fullName;
    private Position position;
    private String street;
    private String postalCode;
    private String city;
    private String info;

    public Staff (StaffBuilder builder){
        this.staffId = new SimpleIntegerProperty((Integer) builder.staffId);
        this.userName = new SimpleStringProperty((String)builder.userName);
        this.password = new SimpleStringProperty((String)builder.password);
        this.fullName = new SimpleStringProperty((String)builder.fullName);
        this.position = builder.position;
        this.street = builder.street;
        this.city = builder.city;
        this.postalCode = builder.postalCode;
    }


    public void setStaffId(int staffId) {
        this.staffId.set(staffId);
    }

    public void setUserName(String userName) {
        this.userName.set(userName);
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public void setFullName(String fullName) {
        this.fullName.set(fullName);
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public static class StaffBuilder{
        private int staffId;
        private String userName = null;
        private String password = null;
        private String fullName = null;
        private Position position = null;
        private String street = null;
        private String postalCode = null;
        private String city = null;

        public StaffBuilder staffId(int staffId){
            this.staffId = staffId;
            return this;
        }
        public StaffBuilder userName(String userName){
            this.userName = userName;
            return this;
        }
        public StaffBuilder password(String password){
            this.password = password;
            return this;
        }
        public StaffBuilder fullName(String fullName){
            this.fullName = fullName;
            return this;
        }
        public StaffBuilder position(Position position){
            this.position = position;
            return this;
        }
        public StaffBuilder street(String street){
            this.street = street;
            return this;
        }
        public StaffBuilder postalCode(String postalCode){
            this.postalCode = postalCode;
            return this;
        }
        public StaffBuilder city(String city){
            this.city = city;
            return this;
        }
        public Staff build(){
            return new Staff(this);
        }
    }

    public int getStaffId() {
        return staffId.get();
    }

    public String getUserName() {
        return userName.get();
    }

    public String getPassword() {
        return password.get();
    }

    public String getFullName() {
        return fullName.get();
    }

    public Position getPosition() {
        return position;
    }

    public Object[] getAllProperties(){
        return (new Object[]{getUserName(), getPassword(), getFullName(), getPosition().name(), getStreet(), getCity(), getPostalCode()});
    }

    public Object[] getAllPropertiesForUpdate(){
        return (new Object[]{getUserName(), getPassword(), getFullName(), getPosition().name(), getStreet(), getCity(), getPostalCode(), getStaffId()});
    }

    public enum Position{
        SALES("sales"),MANAGER("manager");
        private String pos;
        private Position(String pos){
            this.pos = pos;
        }
        public static Position getPosition(String pos){
            for (Position ppos : Position.values()){
                if (ppos.name().equalsIgnoreCase(pos)){
                    return ppos;
                }
            }
            return null;
        }
    }

    public String getInfo() {
        return info;
    }
    public void setInfo() {
        this.info = this.getFullName() + "(" + this.getStaffId() + ")";
    }
}
