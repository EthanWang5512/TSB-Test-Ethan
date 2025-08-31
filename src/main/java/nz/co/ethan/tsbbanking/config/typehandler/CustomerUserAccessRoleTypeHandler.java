package nz.co.ethan.tsbbanking.config.typehandler;

import nz.co.ethan.tsbbanking.domain.enums.CustomerUserAccessRole;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;

@MappedTypes(CustomerUserAccessRole.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class CustomerUserAccessRoleTypeHandler extends BaseTypeHandler<CustomerUserAccessRole> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, CustomerUserAccessRole parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter.name(), Types.OTHER);
    }

    @Override
    public CustomerUserAccessRole getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String s = rs.getString(columnName);
        return s == null ? null : CustomerUserAccessRole.valueOf(s);
    }

    @Override
    public CustomerUserAccessRole getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String s = rs.getString(columnIndex);
        return s == null ? null : CustomerUserAccessRole.valueOf(s);
    }

    @Override
    public CustomerUserAccessRole getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String s = cs.getString(columnIndex);
        return s == null ? null : CustomerUserAccessRole.valueOf(s);
    }
}
