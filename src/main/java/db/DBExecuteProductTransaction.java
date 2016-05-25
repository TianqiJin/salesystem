package db;

import model.Product;
import model.ProductTransaction;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;


public class DBExecuteProductTransaction extends DBExecute<ProductTransaction>{
    public static Logger logger= Logger.getLogger(DBExecuteProductTransaction.class);
    private static List<ProductTransaction> selectResult;

    public List<ProductTransaction> selectFromDatabase(String query, Object... params) throws SQLException{
        DBConnect.getConnection();
        logger.info(query + Arrays.toString(params));
        selectResult = DBConnect.executeQuery(query, ObjectDeserializer.PRODUCT_TRANSACTION_OBJECT_DESERIALIZER, params);
        return selectResult;
    }
}
