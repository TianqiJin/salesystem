package model;

import org.apache.log4j.Logger;

/**
 * Created by jiawei.liu on 12/8/15.
 */
public class Staff {
    private static Logger logger = Logger.getLogger(Staff.class);
    private int staffId;
    private String userName;
    private String password;
    private String fullName;
    private Position position;
    private Location location;

    public Staff (Object ... params){
        this.staffId = (Integer)params[0];
        this.userName = (String)params[1];
        this.password = (String)params[2];
        this.fullName = (String)params[3];
        this.position = (Position)params[4];
        this.location = (Location)params[5];
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
        return staffId;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public Position getPosition() {
        return position;
    }

    public Location getLocation() {
        return location;
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
