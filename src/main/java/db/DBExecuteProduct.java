package db;

import model.Product;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tjin on 2015-11-22.
 */
public class DBExecuteProduct extends DBExecute<Product>{
    public static Logger logger= Logger.getLogger(DBExecuteProduct.class);
    private static List<Product> selectResult;

    public List<Product> selectFromDatabase(String query, Object... params) throws SQLException{
        DBConnect.getConnection();
        logger.info(query + Arrays.toString(params));
        selectResult = DBConnect.executeQuery(query, ObjectDeserializer.PRODUCT_OBJECT_DESERIALIZER, params);
        return selectResult;
    }
}
