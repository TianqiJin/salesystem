package db;

import model.Customer;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tjin on 11/29/2015.
 */
public class DBExecuteCustomer extends DBExecute<Customer>{
    public static Logger logger= Logger.getLogger(DBExecuteCustomer.class);
    private static List<Customer> selectResult;

    //TODO: Create Thread For Each DB Execute
    public List<Customer> selectFromDatabase(String query, Object... params) throws SQLException {
        DBConnect.getConnection();
        logger.info(query + Arrays.toString(params));
        selectResult = DBConnect.executeQuery(query, ObjectDeserializer.CUSTOMER_OBJECT_DESERIALIZER, params);
        return selectResult;
    }
}
