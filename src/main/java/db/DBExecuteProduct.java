package db;

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

    public String selectFromDatabase(int id){
        try {
            selectResult = dbConnect.executeQuery(SelectQueries.SELECT_ALL_PRODUCT, ObjectDeserializer.DEFAULT_DESERIALIZER,id);
            if (selectResult != null) {
                return selectResult.get(0).get("Texture").toString();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return "Nothing";
    }

    public String insertIntoDatabase(){
        return "Nothing";
    }

    public String updateDatabase(){
        return "Nothing";
    }
}
