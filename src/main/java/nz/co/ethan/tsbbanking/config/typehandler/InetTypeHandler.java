package nz.co.ethan.tsbbanking.config.typehandler;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.*;

@MappedJdbcTypes(JdbcType.OTHER)
@MappedTypes(String.class)
public class InetTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        PGobject inet = new PGobject();
        inet.setType("inet");
        inet.setValue(parameter);
        ps.setObject(i, inet);
    }

    @Override public String getNullableResult(ResultSet rs, String columnName) throws SQLException { return rs.getString(columnName); }
    @Override public String getNullableResult(ResultSet rs, int columnIndex)   throws SQLException { return rs.getString(columnIndex); }
    @Override public String getNullableResult(CallableStatement cs, int i)     throws SQLException { return cs.getString(i); }
}

