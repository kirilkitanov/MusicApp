package app.exception;

public class EmailPreferenceAlreadyExistsException extends RuntimeException {
    public EmailPreferenceAlreadyExistsException(String message) {
        super(message);
    }
}
