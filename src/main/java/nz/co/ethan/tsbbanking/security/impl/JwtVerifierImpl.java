package nz.co.ethan.tsbbanking.security.impl;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import nz.co.ethan.tsbbanking.security.JwtSigner;
import nz.co.ethan.tsbbanking.security.JwtVerifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtVerifierImpl implements JwtVerifier {
    private final JwtSigner jwtSigner;
    @Override public Claims verify(String token) {
        return jwtSigner.verify(token); }
}