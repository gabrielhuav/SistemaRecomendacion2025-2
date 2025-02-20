package mx.ipn.escom.Recomendaciones.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Controller
public class AdminController {

    @GetMapping("/admin")
    public String adminPage(Authentication authentication) {
        System.out.println("Usuario autenticado: " + authentication.getName());
        System.out.println("Roles del usuario: " + authentication.getAuthorities());

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            System.out.println("Acceso concedido: Usuario es admin");
            return "admin";  // Vista para administrador
        } else {
            System.out.println("Acceso denegado: Usuario no es admin");
            return "accessDenied";  // Vista si no es admin
        }
    }
}