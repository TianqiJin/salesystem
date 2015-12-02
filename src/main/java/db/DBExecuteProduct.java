package db;

import model.Product;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static db.DBQueries.*;
/**
 * Created by tjin on 2015-11-22.
 */
public class DBExecuteProduct extends DBExecute{
    public static Logger logger= Logger.getLogger(DBExecuteProduct.class);

    public List<Product> selectFromDatabase(String query, Object... params){
//        try {
//            selectResult = DBConnect.executeQuery(query, ObjectDeserializer.DEFAULT_DESERIALIZER, params);
//            if (selectResult != null) {
//                return selectResult;
//            }
//        } catch (SQLException e) {
//            logger.error(e.getMessage());
//        }
        return null;
    }

    public String insertIntoDatabase(){
        return "Nothing";
    }

    public String updateDatabase(){
        return "Nothing";
    }
}
