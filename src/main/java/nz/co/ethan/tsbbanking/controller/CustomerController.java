package nz.co.ethan.tsbbanking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nz.co.ethan.tsbbanking.controller.dto.common.APIResponse;
import nz.co.ethan.tsbbanking.controller.dto.customer.request.BindCustomerUserRequest;
import nz.co.ethan.tsbbanking.controller.dto.customer.request.CreateCustomerRequest;
import nz.co.ethan.tsbbanking.controller.dto.customer.response.CreateCustomerResponse;
import nz.co.ethan.tsbbanking.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// --- Swagger / OpenAPI ---
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
@Tag(name = "Customers (Staff)", description = "Staff-only customer onboarding & binding APIs")
public class CustomerController {

    private final CustomerService customerService;

    // ------------------------ Staff: Create Customer ------------------------
    @PostMapping("/register")
    @Operation(
            summary = "Create a customer (staff)",
            description = "Create a customer profile with NZ-specific fields, then return the created entity summary.",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "refreshAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateCustomerRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customer created",
                            content = @Content(schema = @Schema(implementation = CreateCustomerResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<CreateCustomerResponse> register(
            @org.springframework.web.bind.annotation.RequestBody @Valid CreateCustomerRequest req) {
        var resp = customerService.createCustomer(req);
        return ResponseEntity.ok().body(resp);
    }

    // ------------------------ Staff: Bind Customer ↔ User ------------------------
    @PostMapping("/users/bind")
    @Operation(
            summary = "Bind a user to a customer (staff)",
            description = "Bind an existing user account to a customer with a specific access role (VIEW_ONLY/TRANSACT/ADMIN).",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "refreshAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = BindCustomerUserRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Binding created",
                            content = @Content(schema = @Schema(implementation = APIResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or duplicate binding"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<APIResponse<Void>> bind(
            @org.springframework.web.bind.annotation.RequestBody @Valid BindCustomerUserRequest req) {
        customerService.bindCustomerWithUser(req);
        return ResponseEntity.ok(APIResponse.success("Account bind successfully"));
    }
}
