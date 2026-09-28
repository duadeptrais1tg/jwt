package vn.iotstar.services;

import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import vn.iotstar.exceptions.ExpiredJwtTokenException;
import vn.iotstar.exceptions.InvalidJwtException;
import vn.iotstar.exceptions.InvalidJwtSignatureException;

/**
 * Tao va kiem tra JWT bang thu vien Nimbus JOSE + JWT (thay cho JJWT).
 * <ul>
 * <li>Header: {"alg":"HS256","typ":"JWT"} - JWSHeader</li>
 * <li>Payload: cac claims (sub, iat, exp, ...) - JWTClaimsSet</li>
 * <li>Signature: HMAC-SHA256(header.payload, secret) - MACSigner / MACVerifier</li>
 * </ul>
 */
@Service
public class JwtService {
	@Value("${security.jwt.secret-key}")
	private String secretKey;

	@Value("${security.jwt.expiration-time}")
	private long jwtExpiration;

	public String extractUsername(String token) {
		return extractClaim(token, JWTClaimsSet::getSubject);
	}

	public <T> T extractClaim(String token, Function<JWTClaimsSet, T> claimsResolver) {
		final JWTClaimsSet claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	public String generateToken(UserDetails userDetails) {
		return generateToken(new HashMap<>(), userDetails);
	}

	public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
		return buildToken(extraClaims, userDetails, jwtExpiration);
	}

	public long getExpirationTime() {
		return jwtExpiration;
	}

	private String buildToken(
		Map<String, Object> extraClaims,
		UserDetails userDetails,
		long expiration
	) {
		long now = System.currentTimeMillis();

		// Payload
		JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
				.subject(userDetails.getUsername())
				.issueTime(new Date(now))
				.expirationTime(new Date(now + expiration));
		extraClaims.forEach(claimsBuilder::claim);

		// Header
		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
				.type(JOSEObjectType.JWT)
				.build();

		// Signature
		SignedJWT signedJWT = new SignedJWT(header, claimsBuilder.build());
		try {
			signedJWT.sign(new MACSigner(getSignInKey()));
		} catch (JOSEException e) {
			throw new IllegalStateException("Cannot sign JWT", e);
		}

		// header.payload.signature
		return signedJWT.serialize();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, JWTClaimsSet::getExpirationTime);
	}

	/**
	 * Parse token, kiem tra thuat toan, chu ky va thoi han roi tra ve payload.
	 */
	private JWTClaimsSet extractAllClaims(String token) {
		SignedJWT signedJWT;
		JWTClaimsSet claims;
		try {
			signedJWT = SignedJWT.parse(token);
			claims = signedJWT.getJWTClaimsSet();
		} catch (ParseException e) {
			throw new InvalidJwtException("Invalid compact JWT string: " + e.getMessage(), e);
		}

		// Chi chap nhan HS256 de tranh tan cong doi thuat toan (vd: alg=none)
		if (!JWSAlgorithm.HS256.equals(signedJWT.getHeader().getAlgorithm())) {
			throw new InvalidJwtException("Unsupported JWT algorithm: " + signedJWT.getHeader().getAlgorithm());
		}

		try {
			if (!signedJWT.verify(new MACVerifier(getSignInKey()))) {
				throw new InvalidJwtSignatureException(
						"JWT signature does not match locally computed signature.");
			}
		} catch (JOSEException e) {
			throw new InvalidJwtSignatureException("Cannot verify JWT signature: " + e.getMessage(), e);
		}

		Date expiration = claims.getExpirationTime();
		if (expiration == null || expiration.before(new Date())) {
			throw new ExpiredJwtTokenException("JWT expired at " + expiration);
		}

		return claims;
	}

	private byte[] getSignInKey() {
		return Base64.getDecoder().decode(secretKey);
	}
}
