package app.exception;

public class EmailPreferenceDisabledException extends RuntimeException {
    public EmailPreferenceDisabledException(String message) {
        super(message);
    }
}
