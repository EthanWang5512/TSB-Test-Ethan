package nz.co.ethan.tsbbanking.mapper.account;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nz.co.ethan.tsbbanking.domain.account.Account;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

public interface AccountMapper extends BaseMapper<Account> {

    // Optimistic lock: update balance based on version; delta can be positive or negative
    @Update("""
      UPDATE accounts
      SET balance = balance + #{delta}, version = version + 1, updated_at = now()
      WHERE id = #{accountId} AND version = #{version}
      """)
    int updateBalanceWithVersion(@Param("accountId") Long accountId,
                                 @Param("version") Long version,
                                 @Param("delta") BigDecimal delta);
}
