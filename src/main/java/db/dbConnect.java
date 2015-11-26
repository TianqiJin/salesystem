package db;

import org.apache.log4j.Logger;
import util.PropertiesSys;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tjin on 2015-11-22.
 */
public class DBConnect {
    private static Logger logger= Logger.getLogger(DBConnect.class);
    private static final String DB_URL = PropertiesSys.properties.getProperty("db_url");
    private static final String DB_USER = PropertiesSys.properties.getProperty("db_user");
    private static final String DB_PASSWORD = PropertiesSys.properties.getProperty("db_password");

    private static Connection connection;
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("Created connection to database: "+ DB_URL);
    }

    public static void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }


    public static ResultSet excuteAndReturn(String query, Object...params) throws SQLException{
        PreparedStatement preparedStatement = createStatement(query, params);
        return preparedStatement.executeQuery();
    }
    public static boolean execute(String query, Object... params) throws SQLException {
        PreparedStatement preparedStatement = createStatement(query, params);
        return preparedStatement.execute();
    }


    public static <E> List<E> executeQuery(String query, ObjectDeserializer<E> loader, Object... params) throws SQLException {
        PreparedStatement preparedStatement = createStatement(query, params);
        ResultSet rs = preparedStatement.executeQuery();
        return resultSetToList(rs, loader);
    }

    private static  <E> List<E> resultSetToList(ResultSet rs, ObjectDeserializer<E> loader) throws SQLException {
        List<E> list = new LinkedList<>();
        while (rs.next()) {
            list.add(loader.deserialize(rs));
        }
        return list;
    }


    public static int executeUpdate(String query, Object... params) throws SQLException {
        PreparedStatement preparedStatement = createStatement(query, params);
        return preparedStatement.executeUpdate();
    }


    private static PreparedStatement createStatement(String query, Object... params) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
        return preparedStatement;
    }


}
