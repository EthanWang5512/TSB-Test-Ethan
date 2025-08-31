package nz.co.ethan.tsbbanking.controller.dto.userAuth.request;

import lombok.Data;

@Data
public class StaffLoginRequest {
    private String username;
    private String password;
}
