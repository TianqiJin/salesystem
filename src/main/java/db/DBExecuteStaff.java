package db;

import model.Staff;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jiawei.liu on 12/8/15.
 */
public class DBExecuteStaff extends DBExecute<Staff>{
    public static Logger logger= Logger.getLogger(DBExecuteStaff.class);
    private static List<Staff> selectResult;

    public static boolean[] verification (String user, String pswd){
        DBConnect.getConnection();
        boolean[] result = {false,false};
        try {
            ResultSet rs = DBConnect.excuteAndReturn(DBQueries.SearchQueries.Staff.AUTHENTICATION, user);
            if (rs.next() && rs.getString("Password").equals(pswd)){
                result[0] = true;
                if (rs.getString("Position").equalsIgnoreCase("Manager")){
                    result[1] = true;
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List selectFromDatabase(String query, Object... params) throws SQLException{
        DBConnect.getConnection();
        logger.info(query + Arrays.toString(params));
        selectResult = DBConnect.executeQuery(query, ObjectDeserializer.STAFF_OBJECT_DESERIALIZER, params);
        return selectResult;
    }

    public int getMaxNum(String query){
        DBConnect.getConnection();
        try {
            selectResult = DBConnect.executeQuery(query, ObjectDeserializer.STAFF_OBJECT_DESERIALIZER);
            if (selectResult != null) {
                return selectResult.get(0).getStaffId()+1;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return 0;
    }

}
