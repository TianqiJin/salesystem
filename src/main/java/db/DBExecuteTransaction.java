package db;

import model.Transaction;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by jiawei.liu on 12/2/15.
 */
public class DBExecuteTransaction extends DBExecute<Transaction> {
    public static Logger logger= Logger.getLogger(DBExecuteTransaction.class);
    private static List<Transaction> selectResult;

    @Override
    public List selectFromDatabase(String query, Object... params) throws SQLException{
        selectResult = DBConnect.executeQuery(query, ObjectDeserializer.TRANSACTION_OBJECT_DESERIALIZER, params);
        return  selectResult;
    }


}
