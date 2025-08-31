package nz.co.ethan.tsbbanking.util;

public interface SmsSender {
     void SendOtp(String phone, String message);
}
