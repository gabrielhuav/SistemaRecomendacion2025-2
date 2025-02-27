package mx.ipn.escom.Recomendaciones.auth.controller;

import mx.ipn.escom.Recomendaciones.auth.entity.Usuario;
import mx.ipn.escom.Recomendaciones.auth.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Blob;
import java.util.Base64;

@Controller
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/perfil")
    public String perfil(Model model) {
        // Obtener el usuario actual autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // Buscar el usuario en la base de datos
        Usuario usuario = usuarioRepository.findByNombre(username);
        
        if (usuario != null) {
            // Añadir los datos del usuario al modelo (excepto la contraseña)
            model.addAttribute("nombre", usuario.getNombre());
            model.addAttribute("email", usuario.getEmail());
            model.addAttribute("id", usuario.getId());
            model.addAttribute("tieneImagen", usuario.getImagen() != null);
        }
        
        return "perfil";
    }

    @GetMapping("/usuario/imagen/{id}")
    @ResponseBody
    public String obtenerImagen(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        
        if (usuario != null && usuario.getImagen() != null) {
            try {
                // Convertir la imagen BLOB a Base64 para mostrarla en HTML
                byte[] imagenBytes = usuario.getImagen();
                return Base64.getEncoder().encodeToString(imagenBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return "";
    }

    @PostMapping("/perfil/actualizar-password")
    public String actualizarPassword(
            @RequestParam("password") String nuevaPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("currentPassword") String currentPassword,
            Model model) {
        
        // Obtener el usuario autenticado actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByNombre(username);
        
        if (usuario == null) {
            model.addAttribute("mensaje", "Error: No se pudo encontrar el usuario.");
            model.addAttribute("tipoMensaje", "error");
            return "perfil";
        }
        
        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(currentPassword, usuario.getPassword())) {
            model.addAttribute("mensaje", "Error: La contraseña actual es incorrecta.");
            model.addAttribute("tipoMensaje", "error");
            return "perfil";
        }
        
        // Verificar que las contraseñas nuevas coincidan
        if (!nuevaPassword.equals(confirmPassword)) {
            model.addAttribute("mensaje", "Error: Las contraseñas nuevas no coinciden.");
            model.addAttribute("tipoMensaje", "error");
            return "perfil";
        }
        
        // Actualizar la contraseña
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        
        // Añadir mensaje de éxito y datos del usuario al modelo
        model.addAttribute("mensaje", "¡Contraseña actualizada con éxito!");
        model.addAttribute("tipoMensaje", "exito");
        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("email", usuario.getEmail());
        model.addAttribute("id", usuario.getId());
        model.addAttribute("tieneImagen", usuario.getImagen() != null);
        
        return "perfil";
    }
}