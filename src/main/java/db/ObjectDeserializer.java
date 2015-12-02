package db;

import model.Customer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public interface ObjectDeserializer<E> {

    public abstract E deserialize(ResultSet rs) throws SQLException;

    public static final ObjectDeserializer<Customer> CUSTOMER_OBJECT_DESERIALIZER =  new ObjectDeserializer<Customer>() {
        @Override
        public Customer deserialize(ResultSet rs) throws SQLException {
            Customer customer = new Customer(rs.getString("FirstName"), rs.getString("LastName"), rs.getString("Street"), rs.getString("PostalCode"), rs.getString("City"), rs.getString("Phone"));
            return customer;
        }
    };
    public static final ObjectDeserializer<Map<String, Object>> DEFAULT_DESERIALIZER = new ObjectDeserializer<Map<String, Object>>() {
        @Override
        public Map<String, Object> deserialize(ResultSet rs) throws SQLException {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                map.put(rs.getMetaData().getColumnLabel(i + 1), rs.getObject(i + 1));
            }
            return map;
        }
    };

}
