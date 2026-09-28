package vn.iotstar.exceptions;

/**
 * JWT da het han (claim exp nho hon thoi diem hien tai).
 */
public class ExpiredJwtTokenException extends InvalidJwtException {

	private static final long serialVersionUID = 1L;

	public ExpiredJwtTokenException(String message) {
		super(message);
	}
}
