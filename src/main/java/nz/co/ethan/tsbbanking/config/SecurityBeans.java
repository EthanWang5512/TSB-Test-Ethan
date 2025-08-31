package nz.co.ethan.tsbbanking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class SecurityBeans {
    @Bean
    public PasswordEncoder passwordEncoder() {
        int saltLen = 16;      // 16 bytes
        int hashLen = 32;      // 32 bytes
        int parallel = 1;
        int memoryKb = 19456;  // ≈19MB
        int iterations = 3;

        PasswordEncoder argon2 = new Argon2PasswordEncoder(saltLen, hashLen, parallel, memoryKb, iterations);

        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("argon2id", argon2);
        encoders.put("bcrypt", new BCryptPasswordEncoder());

        return new DelegatingPasswordEncoder("argon2id", encoders);


    }
}