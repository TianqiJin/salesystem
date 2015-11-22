package db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public interface ObjectDeserializer<E> {

    public abstract E deserialize(ResultSet rs) throws SQLException;


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
