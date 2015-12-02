package db;

import model.Customer;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by tjin on 11/29/2015.
 */
public class DBExecuteCustomer extends DBExecute<Customer>{
    public static Logger logger= Logger.getLogger(DBExecuteProduct.class);
    private static List<Customer> selectResult;

    public List<Customer> selectFromDatabase(String query, Object... params){
        try {
            selectResult = DBConnect.executeQuery(query, ObjectDeserializer.CUSTOMER_OBJECT_DESERIALIZER, params);
            logger.info(selectResult);
            if (selectResult != null) {
                return selectResult;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public String insertIntoDatabase(){
        return "Nothing";
    }

    public String updateDatabase(){
        return "Nothing";
    }

}
