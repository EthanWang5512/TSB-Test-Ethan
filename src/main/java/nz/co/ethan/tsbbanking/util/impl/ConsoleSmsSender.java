package nz.co.ethan.tsbbanking.util.impl;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import nz.co.ethan.tsbbanking.util.SmsSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConsoleSmsSender implements SmsSender {


//    @PostConstruct
//    public void init() { com.twilio.Twilio.init(accountSid, authToken); }
    @Override
    public void SendOtp(String phone, String body) {

//        var msg = com.twilio.rest.api.v2010.account.Message.creator(
//                new com.twilio.type.PhoneNumber(phone),
//                new com.twilio.type.PhoneNumber(from),
//                body
//        ).create();
//        msg.getSid();
        log.info("[MOCK SMS] {} -> {} \n", phone, body);
    }
}
