package app.exception;

public class EmailPreferenceNotFoundException extends RuntimeException {
    public EmailPreferenceNotFoundException(String message) {
        super(message);
    }
}