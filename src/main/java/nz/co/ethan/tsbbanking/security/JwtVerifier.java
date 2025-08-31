package nz.co.ethan.tsbbanking.security;

import io.jsonwebtoken.Claims;

public interface JwtVerifier {
    Claims verify(String token);
}
