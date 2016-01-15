package db;

import model.Property;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by tjin on 1/14/2016.
 */
public class DBExecuteProperty extends DBExecute<Property>{
    public static Logger logger= Logger.getLogger(DBExecuteProduct.class);
    private static List<Property> selectResult;

    public List<Property> selectFromDatabase(String query, Object... params){
        try {
            selectResult = DBConnect.executeQuery(query, ObjectDeserializer.PROPERTY_OBJECT_DESERIALIZER, params);
            if (selectResult != null) {
                return selectResult;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public Property selectFirstFromDatabase(String query, Object... params){
        List<Property> result = selectFromDatabase(query, params);
        if(result != null){
            return result.get(0);
        }
        return null;
    }
}
