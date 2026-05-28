package at.spengergasse.spring_thymeleaf.controllers;

import org.springframework.dao.DataAccessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataAccessException.class)
    public String handleDataAccess(DataAccessException ex, Model model) {
        model.addAttribute("title", "Datenbankfehler");
        model.addAttribute("message", "Der Datenbankzugriff ist fehlgeschlagen. Bitte prüfen Sie, ob der Datenbankdienst läuft (z.B. MySQL) und versuchen Sie es später erneut.");
        model.addAttribute("details", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        return "db_error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception ex, Model model) {
        model.addAttribute("title", "Unerwarteter Fehler");
        model.addAttribute("message", "Es ist ein unerwarteter Fehler aufgetreten. Bitte versuchen Sie es später erneut.");
        model.addAttribute("details", ex.getMessage());
        return "db_error";
    }
}
