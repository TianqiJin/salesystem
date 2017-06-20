package db;

/**
 * Created by tjin on 12/23/15.
 */

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ObjectSerializer<E> {
    static Logger logger= Logger.getLogger(ObjectSerializer.class);
    public abstract Object[] serialize(E input) throws SQLException, IOException;

    public static final ObjectSerializer<Transaction> TRANSACTION_OBJECT_SERIALIZER = new ObjectSerializer<Transaction>() {
        @Override
        public Object[] serialize(Transaction transaction) throws SQLException, IOException {
            StringWriter productInfoWriter =new StringWriter();
            StringWriter payInfoWriter = new StringWriter();
            StringWriter typeWriter = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(productInfoWriter, transaction.getProductTransactionList());
            mapper.writeValue(payInfoWriter, transaction.getPayinfo());
            JsonFactory jsonfactory = new JsonFactory();
            String typeJson = null;
            try {
                JsonGenerator jsonGenerator = jsonfactory.createJsonGenerator(typeWriter);
                jsonGenerator.writeStartObject();
                jsonGenerator.writeFieldName("type");
                jsonGenerator.writeString(transaction.getType().toString());
                jsonGenerator.writeFieldName("info");
                jsonGenerator.writeString(transaction.getInfo());
                jsonGenerator.writeEndObject();
                jsonGenerator.close();
                typeJson = typeWriter.toString();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
            return new Object[]{
                    productInfoWriter.toString(),
                    transaction.getDate(),
                    transaction.getPayment(),
                    transaction.getPaymentType(),
                    transaction.getStoreCredit(),
                    transaction.getStaffId(),
                    typeJson,
                    transaction.getTotal(),
                    payInfoWriter.toString(),
                    transaction.getGstTax(),
                    transaction.getPstTax(),
                    transaction.getNote()
            };
        }
    };

    public static final ObjectSerializer<Transaction> TRANSACTION_OBJECT_SERIALIZER_UPDATE = new ObjectSerializer<Transaction>() {
        @Override
        public Object[] serialize(Transaction transaction) throws SQLException, IOException {
            StringWriter payInfoWriter = new StringWriter();
            StringWriter typeWriter = new StringWriter();
            StringWriter productInfoWriter = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(payInfoWriter, transaction.getPayinfo());
            mapper.writeValue(productInfoWriter, transaction.getProductTransactionList());
            JsonFactory jsonfactory = new JsonFactory();
            String typeJson = null;
            try {
                JsonGenerator jsonGenerator = jsonfactory.createJsonGenerator(typeWriter);
                jsonGenerator.writeStartObject();
                jsonGenerator.writeFieldName("type");
                jsonGenerator.writeString(transaction.getType().toString());
                jsonGenerator.writeFieldName("info");
                jsonGenerator.writeString(transaction.getInfo());
                jsonGenerator.writeEndObject();
                jsonGenerator.close();
                typeJson = typeWriter.toString();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            return new Object[]{
                    productInfoWriter.toString(),
                    typeJson,
                    transaction.getDate(),
                    transaction.getPayment(),
                    transaction.getPaymentType(),
                    transaction.getStoreCredit(),
                    payInfoWriter.toString(),
                    transaction.getNote(),
                    transaction.getTransactionId(),
            };
        }
    };

    public static final ObjectSerializer<Transaction> TRANSACTION_QUOTATION_SERIALIZER_UPDATE = new ObjectSerializer<Transaction>() {
        @Override
        public Object[] serialize(Transaction transaction) throws SQLException, IOException {
            StringWriter payInfoWriter = new StringWriter();
            StringWriter productInfoWriter = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(payInfoWriter, transaction.getPayinfo());
            mapper.writeValue(productInfoWriter, transaction.getProductTransactionList());
            StringWriter typeWriter = new StringWriter();
            JsonFactory jsonfactory = new JsonFactory();
            String typeJson = null;
            try {
                JsonGenerator jsonGenerator = jsonfactory.createJsonGenerator(typeWriter);
                jsonGenerator.writeStartObject();
                jsonGenerator.writeFieldName("type");
                jsonGenerator.writeString(transaction.getType().toString());
                jsonGenerator.writeFieldName("info");
                jsonGenerator.writeString(transaction.getInfo());
                jsonGenerator.writeEndObject();
                jsonGenerator.close();
                typeJson = typeWriter.toString();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            return new Object[]{
                    productInfoWriter.toString(),
                    typeJson,
                    transaction.getDate(),
                    transaction.getPayment(),
                    transaction.getPaymentType(),
                    transaction.getStoreCredit(),
                    payInfoWriter.toString(),
                    transaction.getTotal(),
                    transaction.getGstTax(),
                    transaction.getPstTax(),
                    transaction.getNote(),
                    transaction.getTransactionId(),
            };
        }
    };

    public static final ObjectSerializer<Transaction> TRANSACTION_NOTE_SERIALIZER_UPDATE = new ObjectSerializer<Transaction>() {
        @Override
        public Object[] serialize(Transaction transaction) throws SQLException, IOException {
            return new Object[]{
                    transaction.getNote(),
                    transaction.getTransactionId()
            };
        }
    };

    public static final ObjectSerializer<Property> PROPERTY_OBJECT_SERIALIZER = new ObjectSerializer<Property>() {
        @Override
        public Object[] serialize(Property property) throws SQLException, IOException {
            StringWriter userClassWriter = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(userClassWriter, property.getUserClass());
            return new Object[]{
                    property.getProductWarnLimit(),
                    property.getGstRate(),
                    property.getPstRate(),
                    property.getGstNumber(),
                    userClassWriter.toString()
            };
        }
    };
}

