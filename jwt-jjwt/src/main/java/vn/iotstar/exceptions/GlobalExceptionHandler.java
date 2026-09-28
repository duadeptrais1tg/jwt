package vn.iotstar.exceptions;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleSecurityException(Exception exception) {
		ProblemDetail errorDetail;
		// TODO send this stack trace to an observability tool
		exception.printStackTrace();

		if (exception instanceof BadCredentialsException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
			errorDetail.setProperty("description", "The username or password is incorrect");
		} else if (exception instanceof AccountStatusException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
			errorDetail.setProperty("description", "The account is locked");
		} else if (exception instanceof AccessDeniedException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
			errorDetail.setProperty("description", "You are not authorized to access this resource");
		} else if (exception instanceof SignatureException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
			errorDetail.setProperty("description", "The JWT signature is invalid");
		} else if (exception instanceof ExpiredJwtException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
			errorDetail.setProperty("description", "The JWT token has expired");
		} else if (exception instanceof JwtException) {
			// Token sai dinh dang (vd: "dfdfdfdfd")
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
			errorDetail.setProperty("description", "The JWT token is invalid");
		} else if (exception instanceof UsernameNotFoundException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
			errorDetail.setProperty("description", "The user of this token no longer exists");
		} else {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), exception.getMessage());
			errorDetail.setProperty("description", "Unknown internal server error.");
		}

		return errorDetail;
	}
}
