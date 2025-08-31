package nz.co.ethan.tsbbanking.config.typehandler;

import nz.co.ethan.tsbbanking.domain.enums.AuthEventType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;

public class AuthEventTypeHandler extends BaseTypeHandler<AuthEventType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, AuthEventType parameter, JdbcType jdbcType) throws SQLException {
        // PG 枚举需用 OTHER 写入
        ps.setObject(i, parameter.name(), Types.OTHER);
    }

    @Override public AuthEventType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String s = rs.getString(columnName);
        return s == null ? null : AuthEventType.valueOf(s);
    }
    @Override public AuthEventType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String s = rs.getString(columnIndex);
        return s == null ? null : AuthEventType.valueOf(s);
    }
    @Override public AuthEventType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String s = cs.getString(columnIndex);
        return s == null ? null : AuthEventType.valueOf(s);
    }
}
