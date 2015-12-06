package db;

import model.Customer;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by jiawei.liu on 11/22/15.
 */
public abstract class DBExecute<T> {
    public static Logger logger= Logger.getLogger(DBExecute.class);
    public abstract List<T> selectFromDatabase(String query, Object... params);

    public int insertIntoDatabase(String query, Object... params){
        try{
            return DBConnect.executeUpdate(query, params);
        }catch(SQLException e){
            logger.error(e.getMessage());
        }
        return 0;
    };
    public int updateDatabase(String query, Object... params){
        try{
            return DBConnect.executeUpdate(query, params);
        }catch(SQLException e){
            logger.error(e.getMessage());
        }
        return 0;
    };
    public int deleteDatabase(String query, Object... params){
        try{
            return DBConnect.executeUpdate(query, params);
        }catch(SQLException e){
            logger.error(e.getMessage());
        }
        return 0;
    };

}
