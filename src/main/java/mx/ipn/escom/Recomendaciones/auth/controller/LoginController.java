package mx.ipn.escom.Recomendaciones.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login"; 
    }

    @GetMapping("/home")
    public String home() {
        return "home";  
    }

    @GetMapping("/accessDenied")
    public String accessDenied() {
        return "accessDenied"; 
    }
}
