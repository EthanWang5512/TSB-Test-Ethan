package nz.co.ethan.tsbbanking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import nz.co.ethan.tsbbanking.common.BizException;
import nz.co.ethan.tsbbanking.common.ErrorCodes;
import nz.co.ethan.tsbbanking.common.PageResult;
import nz.co.ethan.tsbbanking.controller.dto.account.request.CreateAccountRequest;
import nz.co.ethan.tsbbanking.controller.dto.account.request.ListAccountLedgerRequest;
import nz.co.ethan.tsbbanking.controller.dto.account.request.ListCustomerAccountsQuery;
import nz.co.ethan.tsbbanking.controller.dto.account.request.TransferCreateRequest;
import nz.co.ethan.tsbbanking.controller.dto.account.response.AccountLedger;
import nz.co.ethan.tsbbanking.controller.dto.account.response.AccountSummary;
import nz.co.ethan.tsbbanking.controller.dto.account.response.CreateAccountResponse;
import nz.co.ethan.tsbbanking.controller.dto.account.response.TransferResponse;
import nz.co.ethan.tsbbanking.domain.account.Account;
import nz.co.ethan.tsbbanking.domain.account.AccountOwner;
import nz.co.ethan.tsbbanking.domain.account.LedgerEntry;
import nz.co.ethan.tsbbanking.domain.account.Transfer;
import nz.co.ethan.tsbbanking.domain.customer.Customer;
import nz.co.ethan.tsbbanking.domain.enums.*;
import nz.co.ethan.tsbbanking.mapper.AccountAuthMapper;
import nz.co.ethan.tsbbanking.mapper.AccountQueryMapper;
import nz.co.ethan.tsbbanking.mapper.LedgerMapper;
import nz.co.ethan.tsbbanking.mapper.account.AccountMapper;
import nz.co.ethan.tsbbanking.mapper.account.AccountOwnerMapper;
import nz.co.ethan.tsbbanking.mapper.account.LedgerEntryMapper;
import nz.co.ethan.tsbbanking.mapper.account.TransferMapper;
import nz.co.ethan.tsbbanking.mapper.customer.CustomerMapper;
import nz.co.ethan.tsbbanking.service.AccountService;
import nz.co.ethan.tsbbanking.service.AuthAudit;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static nz.co.ethan.tsbbanking.security.SecurityUtil.currentUserId;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final int MAX_RETRIES = 5;


    private final AccountMapper accountMapper;
    private final CustomerMapper customerMapper;
    private final AuthAudit authAudit;
    private final AccountOwnerMapper accountOwnerMapper;
    private final AccountQueryMapper accountQueryMapper;
    private final TransferMapper transferMapper;
    private final LedgerEntryMapper ledgerEntryMapper;
    private final AccountAuthMapper accountAuthMapper;
    private final LedgerMapper ledgerListMapper;

    @Override
    public CreateAccountResponse createAccount(CreateAccountRequest req, String ip, String ua) {

        //  Check customer
        Customer c = customerMapper.selectById(req.getCustomerId());
        if (c == null) throw BizException.error(ErrorCodes.CUSTOMER_NOT_FOUND.code(), "Customer not found");


        //  Generate account number
        String accountNumber = generateNzStyleAccountNumber();

        boolean paper = req.getPaperDelivery() != null && req.getPaperDelivery();
        String ccy = Strings.isNullOrEmpty(req.getCurrency()) ? "NZD" : req.getCurrency().trim().toUpperCase(Locale.ROOT);

        AccountType accountType = AccountType.parseFlexible(req.getAccountType());

        // Insert account
        // Set 1000 as balance to test
        Account a = new Account();
        a.setAccountNumber(accountNumber);
        a.setNickname(req.getNickname().trim());
        a.setAccountType(accountType.toString());
        a.setCurrency(ccy);
        a.setStatus(AccountStatus.ACTIVE.toString());
        a.setBalance(BigDecimal.valueOf(1000));
        a.setOverdraftLimit(BigDecimal.ZERO);
        a.setResidentWithholdingTaxRate(BigDecimal.ZERO);
        a.setPaperDelivery(paper);

        try {
            accountMapper.insert(a);
        } catch (DuplicateKeyException e) {
            // In extreme concurrency scenarios, the UNIQUE constraint on account_number may be violated.
            throw BizException.error(ErrorCodes.ACCOUNT_NUMBER_CONFLICT.code(), "Account number already exists");
        }

        AccountOwner owner = new AccountOwner();
        owner.setAccountId(a.getId());
        owner.setCustomerId(req.getCustomerId());
        owner.setRole(req.getRole());
        owner.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        accountOwnerMapper.insert(owner);

        final Long uid = currentUserId();
        authAudit.record(uid, AuthEventType.CREATE_ACCOUNT, ip, ua, Map.of(
                "customerId", req.getCustomerId(),
                "accountId", a.getId(),
                "accountNumber", a.getAccountNumber(),
                "openingBalance", BigDecimal.ZERO
        ));

        CreateAccountResponse response = new CreateAccountResponse();
        response.setAccountNumber(accountNumber);
        return  response;
    }

    @Override
    public PageResult<AccountSummary> listCustomerAccounts(ListCustomerAccountsQuery q, String ip, String ua) {
        if (customerMapper.selectById(q.getCustomerId()) == null) {
            throw BizException.error(ErrorCodes.CUSTOMER_NOT_FOUND.code());
        }

        String type = isBlank(q.getType()) ? null : q.getType().trim().toUpperCase(Locale.ROOT);
        String status = isBlank(q.getStatus()) ? null : q.getStatus().trim().toUpperCase(Locale.ROOT);
        int page = q.getPage() == null || q.getPage() < 1 ? 1 : q.getPage();
        int size = q.getSize() == null || q.getSize() < 1 ? 20 : Math.min(q.getSize(), 200);

        long total = accountQueryMapper.countAccountsByCustomer(q.getCustomerId(), type, status);
        List<AccountSummary> rows = total == 0 ? List.of()
                : accountQueryMapper.listAccountsByCustomer(q.getCustomerId(), type, status,
                (page - 1) * size, size);
        final Long uid = currentUserId();
        authAudit.record(uid, AuthEventType.VIEW_ACCOUNTS, ip, ua, Map.of(
                "channel","Retail","customerId", q.getCustomerId(), "page", page, "size", size));

        return PageResult.of(page, size, total, rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransferResponse createInternalTransfer(TransferCreateRequest req) {
        final Long uid = currentUserId();
        Account from = accountMapper.selectOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getAccountNumber, req.getFromAccountNumber()).last("limit 1"));
        Account to = accountMapper.selectOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getAccountNumber, req.getToAccountNumber()).last("limit 1"));

        if (from == null || to == null) throw BizException.error(ErrorCodes.ACCOUNT_NOT_FOND.code(), "Account not found.");

        boolean fromAllowed = accountAuthMapper.hasDebitPermission(uid, from.getId());
        if (!fromAllowed) {
            throw BizException.error(ErrorCodes.NO_PERMISSION.code(), "You are not allowed to transfer from this account.");
        }
        boolean toAllowed = accountAuthMapper.hasDebitPermission(uid, to.getId());
        if (!toAllowed) {
            throw BizException.error(ErrorCodes.FORBIDDEN.code(), "You are not allowed to transfer to another account.");
        }


        // 0) Ensure idempotency by returning the same result for repeated requests
        if (req.getClientRequestId() != null && !req.getClientRequestId().isBlank()) {
            Transfer existed = transferMapper.selectOne(new LambdaQueryWrapper<Transfer>()
                    .eq(Transfer::getClientRequestId, req.getClientRequestId()));
            if (existed != null) {
                throw BizException.error("CLIENT_REQUEST_ID EXISTED", "You are not allowed to transfer with existed clientRequestId.");
            }
        }

        // 1) Same-account transfer quick interception (based on normalized phone/account number)
        String f = req.getFromAccountNumber().replaceAll("[\\s-]", "");
        String t = req.getToAccountNumber().replaceAll("[\\s-]", "");
        if (f.equalsIgnoreCase(t)) throw new IllegalArgumentException("Cannot transfer to the same account.");
        if (!"NZD".equalsIgnoreCase(req.getCurrency())) {
            throw new IllegalArgumentException("Only NZD internal transfer is supported currently.");
        }

        // 2) Check account
        if (from.getId().equals(to.getId())) throw new IllegalArgumentException("Cannot transfer to the same account.");
        if (!"ACTIVE".equals(from.getStatus()) || !"ACTIVE".equals(to.getStatus())) {
            throw new IllegalStateException("Account not active.");
        }
        if (!from.getCurrency().equalsIgnoreCase(to.getCurrency()) ||
                !from.getCurrency().equalsIgnoreCase(req.getCurrency())) {
            throw new IllegalArgumentException("Currency mismatch.");
        }

        BigDecimal amount = req.getAmount().setScale(2, RoundingMode.HALF_UP);
        if (from.getBalance().subtract(amount).compareTo(from.getOverdraftLimit().negate()) < 0) {
            throw new IllegalStateException("Insufficient funds (overdraft limit exceeded).");
        }

        // 3) Insert transfer = PENDING
        Transfer tx = new Transfer();
        tx.setFromAccountId(from.getId());
        tx.setToAccountId(to.getId());
        tx.setCurrency(from.getCurrency());
        tx.setAmount(amount);
        tx.setReference(req.getReference());
        tx.setClientRequestId(blankToNull(req.getClientRequestId()));
        tx.setStatus(TransferStatus.PENDING.toString());
        tx.setCreatedAt(OffsetDateTime.now());
        tx.setCreatedByUserId(from.getId());
        transferMapper.insert(tx);

        // 4)  Optimistic locking + retry (keep your implementation as is)
        boolean success = false;
        for (int i = 0; i < MAX_RETRIES && !success; i++) {
            // Use the current version as condition in each iteration
            int updatedFrom = accountMapper.updateBalanceWithVersion(
                    from.getId(), from.getVersion(), amount.negate()); // 扣钱
            if (updatedFrom == 0) {
                // Version conflict, reload the latest data and retry
                from = accountMapper.selectById(from.getId());
                continue;
            }
            int updatedTo = accountMapper.updateBalanceWithVersion(
                    to.getId(), to.getVersion(), amount); // 加钱
            if (updatedTo == 0) {
                // Roll back
                from = accountMapper.selectById(from.getId());
                int rollback = accountMapper.updateBalanceWithVersion(
                        from.getId(), from.getVersion(), amount); // 加回
                if (rollback == 0) throw new IllegalStateException("Rollback failed due to concurrent update.");
                // Refresh `to` and retry
                to = accountMapper.selectById(to.getId());
                continue;
            }
            success = true;
        }
        if (!success) throw new IllegalStateException("Concurrent modification, please retry.");

        // 5) Ledger entry
        LedgerEntry debit = new LedgerEntry();
        debit.setTransferId(tx.getId());
        debit.setAccountId(from.getId());
        debit.setType("DEBIT");
        debit.setAmount(amount);
        debit.setReference(req.getReference());
        debit.setCurrency(from.getCurrency());
        debit.setCreatedAt(OffsetDateTime.now());
        ledgerEntryMapper.insert(debit);

        LedgerEntry credit = new LedgerEntry();
        credit.setTransferId(tx.getId());
        credit.setAccountId(to.getId());
        credit.setType("CREDIT");
        credit.setAmount(amount);
        credit.setReference(req.getReference());
        credit.setCurrency(from.getCurrency());
        credit.setCreatedAt(OffsetDateTime.now());
        ledgerEntryMapper.insert(credit);

        // 6) Update status
        tx.setStatus(TransferStatus.POSTED.toString());
        transferMapper.updateById(tx);

        // 7) Return
        Account fromAfter = accountMapper.selectById(from.getId());
        Account toAfter   = accountMapper.selectById(to.getId());
        tx.setFromBalanceAfter(fromAfter.getBalance());
        tx.setToBalanceAfter(toAfter.getBalance());
        return toResp(tx);

    }


    @Override
    public PageResult<AccountLedger> listAccountLedger(ListAccountLedgerRequest req) {
        final Long uid = currentUserId();
        System.out.println(req.getAccountNumber());
        Account account = accountMapper.selectOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getAccountNumber, req.getAccountNumber()).last("limit 1"));

        if (account == null) throw BizException.error(ErrorCodes.ACCOUNT_NOT_FOND.code(), "Account not found.");

        boolean checkAllowed = accountAuthMapper.hasDebitPermission(uid, account.getId());
        if (!checkAllowed) {
            throw BizException.error(ErrorCodes.NO_PERMISSION.code(), "You are not allowed to transfer from this account.");
        }

        int page = req.getPage() == null || req.getPage() < 1 ? 1 : req.getPage();
        int size = req.getSize() == null || req.getSize() < 1 ? 20 : Math.min(req.getSize(), 200);

        long total = ledgerListMapper.countByAccountNumberAndUserId(req.getAccountNumber(), uid);
        List<AccountLedger> rows = total == 0 ? List.of()
                : ledgerListMapper.listAllLedgerByAccountNumberAndUserId(req.getAccountNumber(), uid, (page - 1) * size, size);


        return PageResult.of(page, size, total, rows);

    }

    private static TransferResponse toResp(Transfer t) {
        return TransferResponse.builder()
                .transferId(t.getId())
                .status(t.getStatus())
                .currency(t.getCurrency())
                .fromAccountId(t.getFromAccountId())
                .toAccountId(t.getToAccountId())
                .amount(t.getAmount())
                .fromBalanceAfter(t.getFromBalanceAfter())
                .toBalanceAfter(t.getToBalanceAfter())
                .reference(t.getReference())
                .clientRequestId(t.getClientRequestId())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private boolean isBlank(String s){ return s==null || s.trim().isEmpty(); }


    private String generateNzStyleAccountNumber() {
        //  BB-bbbb-AAAAAAA-SS
        String bank = "01";
        String branch = String.format("%04d", ThreadLocalRandom.current().nextInt(0, 9999));
        String body = String.format("%07d", ThreadLocalRandom.current().nextInt(0, 9_999_999));
        String suffix = String.format("%02d", ThreadLocalRandom.current().nextInt(0, 99));

        return bank + "-" + branch + "-" + body + "-" + suffix;
    }




}
