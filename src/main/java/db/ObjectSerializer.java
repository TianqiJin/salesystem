package db;

/**
 * Created by tjin on 12/23/15.
 */

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.*;

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

    public abstract Object[] serialize(E input) throws SQLException, IOException;

    public static final ObjectSerializer<Transaction> TRANSACTION_OBJECT_SERIALIZER = new ObjectSerializer<Transaction>() {
        @Override
        public Object[] serialize(Transaction transaction) throws SQLException, IOException {
            StringWriter productInfoWriter =new StringWriter();
            StringWriter typeWriter = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
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
                e.printStackTrace();
            }
            return new Object[]{
                    productInfoWriter.toString(),
                    transaction.getDate(),
                    transaction.getPayment(),
                    transaction.getPaymentType(),
                    transaction.getStaffId(),
                    typeJson
            };
        }
    };
}
