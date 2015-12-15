package db;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


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
            Product product = new Product.ProductBuilder()
                    .productId(rs.getInt("ProductID"))
                    .totalNum(rs.getInt("TotalPiece"))
                    .unitPrice(rs.getBigDecimal("UnitPrice").setScale(2, BigDecimal.ROUND_HALF_EVEN))
                    .size(rs.getString("Size"))
                    .texture(rs.getString("Texture"))
                    .build();
            return product;
        }
    };

    public static final ObjectDeserializer<Transaction> TRANSACTION_OBJECT_DESERIALIZER =  new ObjectDeserializer<Transaction>() {
        @Override
        public Transaction deserialize(ResultSet rs) throws SQLException {
            ObjectMapper mapper = new ObjectMapper();
            String type = null, info = null;
            List<ProductTransaction> list = new ArrayList<>();
            try {
                JsonNode root = mapper.readValue(rs.getString("Type"),JsonNode.class);
                type = root.path("type").textValue();
                info = root.path("info").textValue();
                root = mapper.readValue(rs.getString("ProductInfo"), JsonNode.class);
                for(JsonNode tmpNode: root){
                    list.add(new ProductTransaction.ProductTransactionBuilder()
                            .productId(tmpNode.path("ProductID").asInt())
                            .totalNum(tmpNode.path("TotalPiece").asInt())
                            .unitPrice(new BigDecimal(String.valueOf(tmpNode.path("UnitPrice").asDouble())).setScale(2, BigDecimal.ROUND_HALF_EVEN))
                            .quantity(tmpNode.path("Quantity").asInt())
                            .subTotal(new BigDecimal(String.valueOf(tmpNode.path("SubTotal").asDouble())).setScale(2, BigDecimal.ROUND_HALF_EVEN))
                            .build()
                    );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Transaction transaction = new Transaction.TransactionBuilder()
                    .transactionId(rs.getInt("TransactionID"))
                    .productInfoList(list)
                    .date(rs.getDate("Date").toString())
                    .payment(rs.getDouble("Payment"))
                    .paymentType(rs.getString("PaymentType"))
                    .staffId(rs.getInt("StaffID"))
                    .type(Transaction.TransactionType.getType(type))
                    .info(info)
                    .build();
            return transaction;
        }
    };

    public static final ObjectDeserializer<Staff> STAFF_OBJECT_DESERIALIZER =  new ObjectDeserializer<Staff>() {
        @Override
        public Staff deserialize(ResultSet rs) throws SQLException {

            Staff staff = new Staff.StaffBuilder()
                    .staffId(rs.getInt("StaffID"))
                    .userName(rs.getString("UserName"))
                    .password(rs.getString("Password"))
                    .fullName(rs.getString("FullName"))
                    .position(Staff.Position.getPosition(rs.getString("Position")))
                    .location(Staff.Location.getLocation(rs.getString("Location")))
                    .build();
            return staff;
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
