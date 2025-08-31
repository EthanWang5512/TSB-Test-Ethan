package nz.co.ethan.tsbbanking.mapper;

import nz.co.ethan.tsbbanking.controller.dto.account.response.AccountSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AccountQueryMapper {

    @Select("""
      SELECT COUNT(*) 
      FROM account_owners ao
      JOIN accounts a ON a.id = ao.account_id
      WHERE ao.customer_id = #{customerId}
        AND a.account_type = COALESCE(#{type,jdbcType=VARCHAR}, a.account_type)
        AND a.status       = COALESCE(#{status,jdbcType=VARCHAR}, a.status)
      """)
    long countAccountsByCustomer(@Param("customerId") Long customerId,
                                 @Param("type") String type,
                                 @Param("status") String status);

    @Select("""
      SELECT a.id            AS accountId,
             a.account_number AS accountNumber,
             a.nickname       AS nickname,
             a.account_type   AS accountType,
             a.currency       AS currency,
             a.status         AS status,
             a.balance        AS balance,
             a.overdraft_limit AS overdraftLimit,
             a.open_at      AS openedAt
      FROM account_owners ao
      JOIN accounts a ON a.id = ao.account_id
      WHERE ao.customer_id = #{customerId}
        AND a.account_type = COALESCE(#{type,jdbcType=VARCHAR}, a.account_type)
        AND a.status       = COALESCE(#{status,jdbcType=VARCHAR}, a.status)
      ORDER BY a.open_at DESC NULLS LAST, a.id DESC
      LIMIT #{size} OFFSET #{offset}
      """)
    List<AccountSummary> listAccountsByCustomer(@Param("customerId") Long customerId,
                                                @Param("type") String type,
                                                @Param("status") String status,
                                                @Param("offset") int offset,
                                                @Param("size") int size);



}

