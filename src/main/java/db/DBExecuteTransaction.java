package db;

import model.Transaction;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by jiawei.liu on 12/2/15.
 */
public class DBExecuteTransaction extends DBExecute {
    public static Logger logger= Logger.getLogger(DBExecuteTransaction.class);
    private static List<Transaction> selectResult;


    @Override
    public List selectFromDatabase(String query, Object... params) {
        return null;
    }

    @Override
    public String insertIntoDatabase() {
        return null;
    }

    @Override
    public String updateDatabase() {
        return null;
    }
}
