package app.web;


import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import app.web.dto.RegisterRequest;
import app.exception.UsernameAlreadyExistException;
import app.exception.EmailAlreadyExistException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;


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

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AccessDeniedException.class,
            NoResourceFoundException.class,
            MethodArgumentTypeMismatchException.class,
            HttpRequestMethodNotSupportedException.class
    })
    public ModelAndView handleNotFound() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("not-found");

        return modelAndView;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public ModelAndView handleAnyException() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("internal-server-error");

        return modelAndView;
    }
}




