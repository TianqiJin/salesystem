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
            Customer customer = new Customer.CustomerBuilder()
                    .userName(rs.getString("UserName"))
                    .firstName(rs.getString("FirstName"))
                    .lastName(rs.getString("LastName"))
                    .street(rs.getString("Street"))
                    .postalCode(rs.getString("PostalCode"))
                    .city(rs.getString("City"))
                    .phone(rs.getString("Phone"))
                    .userClass(rs.getString("Class"))
                    .email(rs.getString("Email"))
                    .storeCredit(rs.getDouble("StoreCredit"))
                    .company(rs.getString("Company"))
                    .pstNumber(rs.getString("PstNumber"))
                    .build();

            return customer;
        }
    };

    public static final ObjectDeserializer<Product> PRODUCT_OBJECT_DESERIALIZER =  new ObjectDeserializer<Product>() {
        @Override
        public Product deserialize(ResultSet rs) throws SQLException {
            Product product = new Product.ProductBuilder()
                    .productId(rs.getString("ProductId"))
                    .totalNum(rs.getInt("TotalNum"))
                    .unitPrice(rs.getBigDecimal("UnitPrice").setScale(2, BigDecimal.ROUND_HALF_EVEN).floatValue())
                    .texture(rs.getString("Texture"))
                    .piecesPerBox(rs.getInt("PiecesPerBox"))
                    .size(rs.getString("Size"))
                    .sizeNumeric(rs.getInt("SizeNumeric"))
                    .build();
            return product;
        }
    };

    public static final ObjectDeserializer<ProductTransaction> PRODUCT_TRANSACTION_OBJECT_DESERIALIZER =  new ObjectDeserializer<ProductTransaction>() {
        @Override
        public ProductTransaction deserialize(ResultSet rs) throws SQLException {
            ProductTransaction productTransaction = new ProductTransaction.ProductTransactionBuilder()
                    .productId(rs.getString("ProductId"))
                    .totalNum(rs.getInt("TotalNum"))
                    .unitPrice(Float.valueOf(0))
                    //.unitPrice(rs.getBigDecimal("UnitPrice").setScale(2, BigDecimal.ROUND_HALF_EVEN).floatValue())
                    .piecesPerBox(rs.getInt("PiecesPerBox"))
                    .quantity(0)
                    .size(rs.getString("Size"))
                    .sizeNumeric(rs.getInt("SizeNumeric"))
                    .build();
            return productTransaction;
        }
    };

    public static final ObjectDeserializer<Transaction> TRANSACTION_OBJECT_DESERIALIZER =  new ObjectDeserializer<Transaction>() {
        @Override
        public Transaction deserialize(ResultSet rs) throws SQLException {
            ObjectMapper mapper = new ObjectMapper();
            String type = null, info = null;
            List<ProductTransaction> list = new ArrayList<>();
            List<PaymentRecord> listPaymentRecord = new ArrayList<>();
            try {
                JsonNode root = mapper.readValue(rs.getString("Type"),JsonNode.class);
                type = root.path("type").textValue();
                info = root.path("info").textValue();
                root = mapper.readValue(rs.getString("ProductInfo"), JsonNode.class);
                for(JsonNode tmpNode: root){
                    list.add(new ProductTransaction.ProductTransactionBuilder()
                            .productId(tmpNode.path("productId").asText())
                            .totalNum(tmpNode.path("totalNum").asInt())
                            .unitPrice(new BigDecimal(String.valueOf(tmpNode.path("unitPrice").asDouble())).setScale(2, BigDecimal.ROUND_HALF_EVEN).floatValue())
                            .piecesPerBox(tmpNode.path("piecesPerBox").asInt())
                            .size(tmpNode.path("size").asText())
                            .sizeNumeric(tmpNode.path("sizeNumeric").asInt())
                            .quantity(tmpNode.path("quantity").asInt())
                            .discount(tmpNode.path("discount").asInt())
                            .subTotal(new BigDecimal(String.valueOf(tmpNode.path("subTotal").asDouble())).setScale(2, BigDecimal.ROUND_HALF_EVEN).floatValue())
                            .build()
                    );
                }
                root = mapper.readValue(rs.getString("payinfo"), JsonNode.class);
                for(JsonNode tmpNode: root){
                    listPaymentRecord.add(new PaymentRecord(
                            tmpNode.path("date").asText(),
                            tmpNode.path("paid").asDouble(),
                            tmpNode.path("paymentType").asText()));
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
                    .storeCredit(rs.getDouble("storeCredit"))
                    .staffId(rs.getInt("StaffID"))
                    .type(Transaction.TransactionType.getType(type))
                    .info(info)
                    .total(rs.getDouble("Total"))
                    .payinfo(listPaymentRecord)
                    .gstTax(rs.getDouble("GstTax"))
                    .pstTax(rs.getDouble("PstTax"))
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
                    .street(rs.getString("Street"))
                    .city(rs.getString("City"))
                    .postalCode(rs.getString("PostalCode"))
                    .build();
            return staff;
        }
    };

    public static final ObjectDeserializer<Property> PROPERTY_OBJECT_DESERIALIZER =  new ObjectDeserializer<Property>() {
        @Override
        public Property deserialize(ResultSet rs) throws SQLException {
            Property property = new Property(rs.getInt("ProductWarnLimit"), rs.getInt("GstTax"), rs.getInt("PstTax"));
            return property;
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
