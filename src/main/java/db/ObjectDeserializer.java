package db;

import model.Customer;
import model.Product;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public interface ObjectDeserializer<E> {

    public abstract E deserialize(ResultSet rs) throws SQLException;

    public static final ObjectDeserializer<Customer> CUSTOMER_OBJECT_DESERIALIZER =  new ObjectDeserializer<Customer>() {
        @Override
        public Customer deserialize(ResultSet rs) throws SQLException {
            Customer customer = new Customer(rs.getString("UserName"), rs.getString("FirstName"), rs.getString("LastName"), rs.getString("Street"),
                    rs.getString("PostalCode"), rs.getString("City"), rs.getString("Phone"), rs.getString("Class"), rs.getString("Email"),
                    rs.getInt("StoreCredit"));
            return customer;
        }
    };

    public static final ObjectDeserializer<Product> PRODUCT_OBJECT_DESERIALIZER =  new ObjectDeserializer<Product>() {
        @Override
        public Product deserialize(ResultSet rs) throws SQLException {
            Product product = new Product(rs.getInt("ProductID"), rs.getString("Texture"), rs.getString("Size"), rs.getInt("TotalPiece"));
            return product;
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
