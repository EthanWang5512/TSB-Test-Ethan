package nz.co.ethan.tsbbanking.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AccountAuthMapper {

    @Select("""
        SELECT EXISTS(
            SELECT 1
            FROM account_owners ao
            JOIN customer_users cu ON cu.customer_id = ao.customer_id
            WHERE ao.account_id = #{accountId}
              AND cu.user_id    = #{userId}
              AND cu.access_role IN ('OWNER','ADMIN','DELEGATE')
        )
        """)
    boolean hasDebitPermission(@Param("userId") Long userId,
                               @Param("accountId") Long accountId);
}