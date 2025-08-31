package nz.co.ethan.tsbbanking.mapper;

import nz.co.ethan.tsbbanking.controller.dto.account.response.AccountLedger;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LedgerMapper {

    @Select("""
    WITH target AS (
      SELECT id
      FROM accounts
      WHERE account_number = CAST(#{accountNumber, jdbcType=VARCHAR} AS VARCHAR)
      LIMIT 1
    )
    SELECT
      le.transfer_id AS "transferId",
      le.direction   AS "direction",             -- DEBIT | CREDIT (ledger_entries)
      le.amount      AS "amount",
      le.currency    AS "currency",
      CASE WHEN le.direction = 'DEBIT' THEN -le.amount ELSE le.amount END AS "delta",
      t.status       AS "status",                -- transfers.status
      t.reference  AS "reference",             -- transfers.description
      t.posted_at    AS "postedAt",
      le.created_at  AS "createdAt",
      cp.account_number AS "counterpartyAccountNumber"
    FROM ledger_entries le
    JOIN transfers t ON t.id = le.transfer_id
    JOIN accounts  cp ON cp.id = CASE
        WHEN le.direction = 'DEBIT' THEN t.to_account_id
        ELSE t.from_account_id
      END
    WHERE le.account_id = (SELECT id FROM target)
      AND EXISTS (
        SELECT 1
        FROM account_owners ao
        JOIN customer_users cu ON cu.customer_id = ao.customer_id
        WHERE ao.account_id = (SELECT id FROM target)
          AND cu.user_id = CAST(#{userId, jdbcType=BIGINT} AS BIGINT)
      )
    ORDER BY le.created_at DESC, le.id DESC
    LIMIT #{size} OFFSET #{offset}
  """)
    List<AccountLedger> listAllLedgerByAccountNumberAndUserId(
            @Param("accountNumber") String accountNumber,
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("size") int size
    );

    @Select("""
    WITH target AS (
      SELECT id
      FROM accounts
      WHERE account_number = CAST(#{accountNumber, jdbcType=VARCHAR} AS VARCHAR)
      LIMIT 1
    )
    SELECT count(*)
    FROM ledger_entries le
    JOIN transfers t ON t.id = le.transfer_id
    WHERE le.account_id = (SELECT id FROM target)
      AND EXISTS (
        SELECT 1
        FROM account_owners ao
        JOIN customer_users cu ON cu.customer_id = ao.customer_id
        WHERE ao.account_id = (SELECT id FROM target)
          AND cu.user_id = CAST(#{userId, jdbcType=BIGINT} AS BIGINT)
      )
  """)
    long countByAccountNumberAndUserId(
            @Param("accountNumber") String accountNumber,
            @Param("userId") Long userId
    );
}
