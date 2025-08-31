package nz.co.ethan.tsbbanking.util;

public interface MailSender {
    void send(String to, String subject, String body);
}
