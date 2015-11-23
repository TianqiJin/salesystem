package db;

import java.util.List;
import java.util.Map;

/**
 * Created by jiawei.liu on 11/22/15.
 */
public abstract class DBExecute {

    protected static List<Map<String, Object>> selectResult;

    public abstract String selectFromDatabase(int id);
    public abstract String insertIntoDatabase();
    public abstract String updateDatabase();
}
