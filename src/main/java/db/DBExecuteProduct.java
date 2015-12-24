package db;

import model.Product;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by tjin on 2015-11-22.
 */
public class DBExecuteProduct extends DBExecute<Product>{
    public static Logger logger= Logger.getLogger(DBExecuteProduct.class);
    private static List<Product> selectResult;

    public List<Product> selectFromDatabase(String query, Object... params){
        try {
            selectResult = DBConnect.executeQuery(query, ObjectDeserializer.PRODUCT_OBJECT_DESERIALIZER, params);
            if (selectResult != null) {
                return selectResult;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return null;
    }
}
