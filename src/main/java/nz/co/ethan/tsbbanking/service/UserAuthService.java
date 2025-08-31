package nz.co.ethan.tsbbanking.service;


import nz.co.ethan.tsbbanking.controller.dto.userAuth.request.LoginRequest;
import nz.co.ethan.tsbbanking.controller.dto.userAuth.response.LoginResponse;
import nz.co.ethan.tsbbanking.controller.dto.userAuth.response.StaffLoginResponse;
import nz.co.ethan.tsbbanking.domain.user.User;

public interface UserAuthService {
    
    User registerUser(String email, String phone);

    void sendVerifyEmailAndSetPasswordLink(String email, String username, String originForLink); // origin 例如 https://app.xxx.nz
    void setPasswordByToken(String token, String newPassword);

    LoginResponse login(LoginRequest req, String ip, String ua);

    StaffLoginResponse staffLogin(String username, String rawPwd, String ip, String userAgent);

    void sendOtpToPhone(String phone, String ip, String ua);

    void verifyAndResetPassword(String rawPhoneE164, String otp, String newPassword,
                                String ip, String userAgent);

    void logout(String refreshTokenPlain, String ip, String ua, Boolean fromStaffEndpoint);

}
