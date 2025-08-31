package nz.co.ethan.tsbbanking.controller.dto.userAuth.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateAdminUserResponse {
    private String username;
    private String employeeNo;
}
