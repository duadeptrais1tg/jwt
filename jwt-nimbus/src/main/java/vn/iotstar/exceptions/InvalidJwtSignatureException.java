package vn.iotstar.exceptions;

/**
 * Chu ky JWT khong khop voi secret key cua server.
 */
public class InvalidJwtSignatureException extends InvalidJwtException {

	private static final long serialVersionUID = 1L;

	public InvalidJwtSignatureException(String message) {
		super(message);
	}

	public InvalidJwtSignatureException(String message, Throwable cause) {
		super(message, cause);
	}
}
