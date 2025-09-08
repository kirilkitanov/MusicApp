package app.web;

import app.exception.EmailPreferenceDisabledException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailPreferenceDisabledException.class)
    public String handlerEmailPreferenceDisabledException() {
//        return "Email preference for user [%s] are disabled.".formatted(userId);
        return "Email preference are disabled.";
    }
}
