package nz.co.ethan.tsbbanking.util.impl;

import lombok.extern.slf4j.Slf4j;
import nz.co.ethan.tsbbanking.util.MailSender;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ConsoleMailSender implements MailSender {
    @Override
    public void send(String to, String subject, String body) {
        log.info("[MOCK EMAIL] SendMail to={} subject={} \n{}", to, subject, body);
    }
}
