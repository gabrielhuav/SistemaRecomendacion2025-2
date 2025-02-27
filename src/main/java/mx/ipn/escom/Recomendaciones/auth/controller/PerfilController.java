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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    
    @PostMapping("/perfil/actualizar-imagen")
    public String actualizarImagen(@RequestParam("imagen") MultipartFile imagen, Model model) {
        // Obtener el usuario autenticado actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByNombre(username);
        
        if (usuario == null) {
            model.addAttribute("mensaje", "Error: No se pudo encontrar el usuario.");
            model.addAttribute("tipoMensaje", "error");
            return "perfil";
        }
        
        try {
            // Verificar que la imagen no esté vacía
            if (imagen.isEmpty()) {
                model.addAttribute("mensaje", "Error: Por favor selecciona una imagen.");
                model.addAttribute("tipoMensaje", "error");
                return "perfil";
            }
            
            // Verificar el tipo de archivo (opcional)
            String contentType = imagen.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                model.addAttribute("mensaje", "Error: Por favor selecciona un archivo de imagen válido.");
                model.addAttribute("tipoMensaje", "error");
                return "perfil";
            }
            
            // Obtener los bytes de la imagen
            byte[] imagenBytes = imagen.getBytes();
            
            // Guardar la imagen en el usuario
            usuario.setImagen(imagenBytes);
            usuarioRepository.save(usuario);
            
            // Añadir mensaje de éxito y datos del usuario al modelo
            model.addAttribute("mensaje", "¡Imagen de perfil actualizada con éxito!");
            model.addAttribute("tipoMensaje", "exito");
            model.addAttribute("nombre", usuario.getNombre());
            model.addAttribute("email", usuario.getEmail());
            model.addAttribute("id", usuario.getId());
            model.addAttribute("tieneImagen", true);
            
        } catch (IOException e) {
            model.addAttribute("mensaje", "Error al procesar la imagen: " + e.getMessage());
            model.addAttribute("tipoMensaje", "error");
        }
        
        return "perfil";
    }
}