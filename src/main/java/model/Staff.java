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
    private  IntegerProperty staffId;
    private  StringProperty userName;
    private  StringProperty password;
    private  StringProperty fullName;
    private Position position;
    private Location location;

    public Staff (Object ... params){
        if (params.length==6) {
            this.staffId = new SimpleIntegerProperty((Integer) params[0]);
            this.userName = new SimpleStringProperty((String) params[1]);
            this.password = new SimpleStringProperty((String) params[2]);
            this.fullName = new SimpleStringProperty((String) params[3]);
            this.position = (Position) params[4];
            this.location = (Location) params[5];
        }else{
            this.staffId = new SimpleIntegerProperty(0);
            this.userName = new SimpleStringProperty(null);
            this.password = new SimpleStringProperty(null);
            this.fullName = new SimpleStringProperty(null);
            this.position = null;
            this.location = null;
        }
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

    public void setLocation(Location location) {
        this.location = location;
    }

    public static class StaffBuilder{
        private int staffId;
        private String userName;
        private String password;
        private String fullName;
        private Position position;
        private Location location;

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
        public StaffBuilder location(Location location){
            this.location = location;
            return this;
        }
        public Staff build(){
            return new Staff(staffId,userName,password,fullName,position,location);
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

    public Location getLocation() {
        return location;
    }

    public Object[] getAllProperties(){
        return (new Object[]{getUserName(), getPassword(), getFullName(), getPosition().name(), getLocation().name()});
    }

    public Object[] getAllPropertiesForUpdate(){
        return (new Object[]{getUserName(), getPassword(), getFullName(), getPosition().name(), getLocation().name(),getStaffId()});
    }



    public enum Location{
        EVERYWHERE("everywhere"),NOWHERE("nowhere"),SOMEWHERE("somewhere");
        private String loc;
        private Location(String loc){this.loc = loc;}
        public static Location getLocation(String loc){
            for (Location llos : Location.values()){
                if (llos.name().equalsIgnoreCase(loc)){
                    return llos;
                }
            }
            return null;
        }
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

}
