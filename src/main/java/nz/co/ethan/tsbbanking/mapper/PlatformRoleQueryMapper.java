package nz.co.ethan.tsbbanking.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface PlatformRoleQueryMapper {
    @Select("""
    SELECT r.code
    FROM user_roles ur
    JOIN roles r ON r.id = ur.role_id
    WHERE ur.user_id = #{userId} AND r.scope = 'PLATFORM'
  """)
    List<String> selectPlatformRoleCodes(@Param("userId") Long userId);

    @Select("""
    SELECT DISTINCT r.code
    FROM user_roles ur
    JOIN roles r ON r.id = ur.role_id
    JOIN role_permissions rp ON rp.role_id = r.id
    WHERE ur.user_id = #{userId} AND r.scope = 'PLATFORM'
  """)
    Set<String> selectPlatformPermissions(@Param("userId") Long userId);
}

