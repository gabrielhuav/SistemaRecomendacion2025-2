package mx.ipn.escom.Recomendaciones.auth.controller;

import mx.ipn.escom.Recomendaciones.auth.entity.Usuario;
import mx.ipn.escom.Recomendaciones.auth.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;  // Inyecta el servicio de usuario para guardar en la BD

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("usuario") Usuario usuario, Model model) {
        // Lógica para registrar al usuario en la base de datos
        usuarioService.registrarUsuario(usuario);

        // Añadir el usuario al modelo para mostrar en la vista de registro exitoso
        model.addAttribute("usuario", usuario);

        return "registroExitoso";  // Redirige a la página de registro exitoso
    }
}
