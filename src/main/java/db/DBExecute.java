package db;

import java.util.List;
import java.util.Map;

/**
 * Created by jiawei.liu on 11/22/15.
 */
public abstract class DBExecute {

    protected static List<Map<String, Object>> selectResult;

    public abstract List<Map<String, Object>> selectFromDatabase(String query, Object... params);
    public abstract String insertIntoDatabase();
    public abstract String updateDatabase();
}
