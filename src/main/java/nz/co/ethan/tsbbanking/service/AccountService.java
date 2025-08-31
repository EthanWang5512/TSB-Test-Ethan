package nz.co.ethan.tsbbanking.service;

import nz.co.ethan.tsbbanking.common.PageResult;
import nz.co.ethan.tsbbanking.controller.dto.account.request.CreateAccountRequest;
import nz.co.ethan.tsbbanking.controller.dto.account.request.ListAccountLedgerRequest;
import nz.co.ethan.tsbbanking.controller.dto.account.request.ListCustomerAccountsQuery;
import nz.co.ethan.tsbbanking.controller.dto.account.request.TransferCreateRequest;
import nz.co.ethan.tsbbanking.controller.dto.account.response.AccountLedger;
import nz.co.ethan.tsbbanking.controller.dto.account.response.AccountSummary;
import nz.co.ethan.tsbbanking.controller.dto.account.response.CreateAccountResponse;
import nz.co.ethan.tsbbanking.controller.dto.account.response.TransferResponse;

public interface AccountService {

    CreateAccountResponse createAccount(CreateAccountRequest req, String ip, String ua);

    PageResult<AccountSummary> listCustomerAccounts(ListCustomerAccountsQuery q, String ip, String ua);

    TransferResponse createInternalTransfer(TransferCreateRequest req);

    PageResult<AccountLedger> listAccountLedger(ListAccountLedgerRequest req);
}
