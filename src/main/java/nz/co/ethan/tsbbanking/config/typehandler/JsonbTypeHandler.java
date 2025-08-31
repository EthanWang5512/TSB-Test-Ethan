package nz.co.ethan.tsbbanking.config.typehandler;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.*;
import org.postgresql.util.PGobject;
import java.sql.*;

@MappedJdbcTypes(JdbcType.OTHER)
@MappedTypes({java.util.Map.class, Object.class, String.class})
public class JsonbTypeHandler extends BaseTypeHandler<Object> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        String json;
        try {
            if (parameter instanceof String s) {
                json = s;
            } else {
                json = MAPPER.writeValueAsString(parameter); // Map/POJO/List → JSON
            }
        } catch (Exception e) {
            throw new SQLException("Failed to serialize parameter to json", e);
        }

        PGobject pg = new PGobject();
        pg.setType("jsonb");
        pg.setValue(json);
        ps.setObject(i, pg);
    }

    @Override public Object getNullableResult(ResultSet rs, String columnName) throws SQLException { return rs.getString(columnName); }
    @Override public Object getNullableResult(ResultSet rs, int columnIndex)   throws SQLException { return rs.getString(columnIndex); }
    @Override public Object getNullableResult(CallableStatement cs, int i)     throws SQLException { return cs.getString(i); }
}
