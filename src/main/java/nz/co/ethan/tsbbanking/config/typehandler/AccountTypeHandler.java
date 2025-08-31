package nz.co.ethan.tsbbanking.config.typehandler;

import nz.co.ethan.tsbbanking.domain.enums.AccountType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;

public class AccountTypeHandler extends BaseTypeHandler<AccountType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, AccountType parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter.name(), Types.OTHER);
    }

    @Override
    public AccountType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String s = rs.getString(columnName);
        return s == null ? null : AccountType.valueOf(s);
    }

    @Override
    public AccountType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String s = rs.getString(columnIndex);
        return s == null ? null : AccountType.valueOf(s);
    }

    @Override
    public AccountType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String s = cs.getString(columnIndex);
        return s == null ? null : AccountType.valueOf(s);
    }
}
