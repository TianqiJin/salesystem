package db;

import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import static db.DBQueries.Queries.*;
/**
 * Created by jiawei.liu on 11/22/15.
 */
public class DBExecute {
    private static Logger logger= Logger.getLogger(DBExecute.class);

    public static String selectFromProduct(int id){
        try {
            List<Map<String, Object>> result = dbConnect.executeQuery(SELECT_TEST, ObjectDeserializer.DEFAULT_DESERIALIZER,id);
            if (result != null) {
                return result.get(0).get("Texture").toString();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return "Nothing";
    }

}
