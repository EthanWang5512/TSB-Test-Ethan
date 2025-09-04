package nz.co.ethan.tsbbanking.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nz.co.ethan.tsbbanking.common.PageResult;
import nz.co.ethan.tsbbanking.controller.dto.account.request.CreateAccountRequest;
import nz.co.ethan.tsbbanking.controller.dto.account.request.ListAccountLedgerRequest;
import nz.co.ethan.tsbbanking.controller.dto.account.request.ListCustomerAccountsQuery;
import nz.co.ethan.tsbbanking.controller.dto.account.request.TransferCreateRequest;
import nz.co.ethan.tsbbanking.controller.dto.account.response.AccountLedger;
import nz.co.ethan.tsbbanking.controller.dto.account.response.AccountSummary;
import nz.co.ethan.tsbbanking.controller.dto.account.response.CreateAccountResponse;
import nz.co.ethan.tsbbanking.controller.dto.account.response.TransferResponse;
import nz.co.ethan.tsbbanking.service.AccountService;
import nz.co.ethan.tsbbanking.util.IpUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// ---- Swagger / OpenAPI ----
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
@Tag(name = "Accounts", description = "Staff-only account operations: create, Retail-only account opereation: show account list, " +
        "make transfer, show ledgers list")
public class AccountController {

    private final AccountService accountService;

    // -------------------- Create account --------------------
    @PostMapping("/create")
    @Operation(
            summary = "Create an account (staff)",
            description = "Create a new bank account for a customer (NZ market demo).",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "refreshAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateAccountRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Account created",
                            content = @Content(schema = @Schema(implementation = CreateAccountResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<CreateAccountResponse> create(
            @org.springframework.web.bind.annotation.RequestBody @Valid CreateAccountRequest req,
            HttpServletRequest servletRequest) {

        String ip = servletRequest.getRemoteAddr();
        String ua = servletRequest.getHeader("User-Agent");
        CreateAccountResponse a = accountService.createAccount(req, ip, ua);
        return ResponseEntity.ok().body(a);
    }

    // -------------------- List customer accounts (Retail) --------------------
    @PostMapping("/customer/accounts")
    @Operation(
            summary = "List accounts of a customer (Retail)",
            description = "Paged list of a customer's accounts with optional filters (type/status).",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "refreshAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = ListCustomerAccountsQuery.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paged account list",
                            content = @Content(schema = @Schema(implementation = PageResult.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid query"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public PageResult<AccountSummary> listCustomerAccountsForStaff(
            @org.springframework.web.bind.annotation.RequestBody @Valid ListCustomerAccountsQuery q,
            HttpServletRequest req) {

        return accountService.listCustomerAccounts(q, IpUtil.getClientIp(req), req.getHeader("User-Agent"));
    }

    // -------------------- Create internal transfer --------------------
    @PostMapping("/transfer")
    @Operation(
            summary = "Create internal transfer (Retail)",
            description = "Create a transfer between two accounts. Supports idempotency via header or clientRequestId.",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "refreshAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = TransferCreateRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transfer accepted",
                            content = @Content(schema = @Schema(implementation = TransferResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid transfer request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "409", description = "Duplicate (idempotent) request")
            }
    )
    public ResponseEntity<TransferResponse> createTransfer(
            @Parameter(description = "Idempotency key (optional). If present and clientRequestId is blank, the server will copy this value into clientRequestId.")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @org.springframework.web.bind.annotation.RequestBody @Valid TransferCreateRequest req) {

        if (idempotencyKey != null && (req.getClientRequestId() == null || req.getClientRequestId().isBlank())) {
            req.setClientRequestId(idempotencyKey);
        }
        return ResponseEntity.ok(accountService.createInternalTransfer(req));
    }

    // -------------------- List account ledgers --------------------
    @PostMapping("/ledgers")
    @Operation(
            summary = "List account ledgers (staff)",
            description = "Paged account ledger entries (credits/debits) for a given account.",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "refreshAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = ListAccountLedgerRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paged ledger entries",
                            content = @Content(schema = @Schema(implementation = PageResult.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid query"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public PageResult<AccountLedger> listLedgers(
            @org.springframework.web.bind.annotation.RequestBody @Valid ListAccountLedgerRequest req) {
        return accountService.listAccountLedger(req);
    }    

}
