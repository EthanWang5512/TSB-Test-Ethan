package nz.co.ethan.tsbbanking.controller.dto.customer.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateCustomerRequest {
    @NotBlank
    @Schema(description = "Customer's legal first name", example = "Jack")
    private String firstName;
    @NotBlank
    @Schema(description = "Customer's legal last name", example = "Smith")
    private String lastName;

    @Schema(description = "Preferred name or nickname", example = "E")
    private String preferredName;

    @Email
    @Schema(description = "Customer email address", example = "jack@example.nz")
    private String email;

    @Schema(description = "Contact phone number in NZ format", example = "+64211234567")
    private String contactNumber;

    @NotBlank
    @Schema(description = "Address line 1 (street address)", example = "12 Queen Street")
    private String addressLine1;

    @Schema(description = "Address line 2 (apartment, suite, etc.)", example = "Apt 3B")
    private String addressLine2;

    @Schema(description = "Suburb or locality", example = "Auckland Central")
    private String suburb;

    @NotBlank
    @Schema(description = "City", example = "Auckland")
    private String city;

    @NotBlank
    @Schema(description = "Region (province/state)", example = "Auckland")
    private String region;

    @NotBlank
    @Schema(description = "Postcode", example = "1010")
    private String postcode;


    private String country = "New Zealand";


    private List<@NotBlank String> bindUsernames;


    private String primaryUsername;
}

