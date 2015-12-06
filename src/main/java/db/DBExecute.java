package db;

import model.Customer;

import java.util.List;

/**
 * Created by jiawei.liu on 11/22/15.
 */
public abstract class DBExecute<T> {

    public abstract List<T> selectFromDatabase(String query, Object... params);
    public abstract int insertIntoDatabase(String query, Object... params);
    public abstract String updateDatabase();
    public abstract int deleteDatabase(String query, Object... params);

}
