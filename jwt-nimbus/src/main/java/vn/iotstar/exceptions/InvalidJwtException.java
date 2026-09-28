package vn.iotstar.exceptions;

/**
 * JWT khong hop le (sai dinh dang, sai thuat toan, ...).
 */
public class InvalidJwtException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidJwtException(String message) {
		super(message);
	}

	public InvalidJwtException(String message, Throwable cause) {
		super(message, cause);
	}
}
