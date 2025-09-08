package app.web;


import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import app.web.dto.RegisterRequest;
import app.exception.UsernameAlreadyExistException;
import app.exception.EmailAlreadyExistException;


@ControllerAdvice
    public class GlobalExceptionHandler {

        @ExceptionHandler(UsernameAlreadyExistException.class)
        public String handleUsernameAlreadyExist(Model model, UsernameAlreadyExistException exception) {
            model.addAttribute("usernameAlreadyExistMessage", exception.getMessage());
            model.addAttribute("registerRequest", new RegisterRequest());
            return "register";
        }

        @ExceptionHandler(EmailAlreadyExistException.class)
        public String handleEmailAlreadyExist(Model model, EmailAlreadyExistException exception) {
            model.addAttribute("emailAlreadyExistMessage", exception.getMessage());
            model.addAttribute("registerRequest", new RegisterRequest());
            return "register";
        }


    }




