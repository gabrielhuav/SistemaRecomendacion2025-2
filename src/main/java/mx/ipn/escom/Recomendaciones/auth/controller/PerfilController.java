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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Controller
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Constantes para el redimensionamiento de imágenes
    private static final int MAX_WIDTH = 500;
    private static final int MAX_HEIGHT = 500;
    private static final long MAX_FILE_SIZE = 1024 * 1024; // 1MB

    @GetMapping("/perfil")
    public String perfil(Model model) {
        // Obtener el usuario actual autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // Buscar el usuario en la base de datos
        Usuario usuario = usuarioRepository.findByNombre(username);
        
        if (usuario != null) {
            // Añadir los datos del usuario al modelo
            model.addAttribute("usuario", usuario);
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

    @PostMapping("/perfil/actualizar-info")
    public String actualizarInfo(
            @RequestParam("nombre") String nuevoNombre,
            @RequestParam("email") String nuevoEmail,
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
        
        // Verificar que el email no esté en uso por otro usuario
        Usuario usuarioExistente = usuarioRepository.findByEmail(nuevoEmail);
        if (usuarioExistente != null && !usuarioExistente.getId().equals(usuario.getId())) {
            model.addAttribute("mensaje", "Error: El email ya está en uso por otro usuario.");
            model.addAttribute("tipoMensaje", "error");
            model.addAttribute("usuario", usuario);
            model.addAttribute("tieneImagen", usuario.getImagen() != null);
            return "perfil";
        }
        
        // Actualizar la información del usuario
        usuario.setNombre(nuevoNombre);
        usuario.setEmail(nuevoEmail);
        usuarioRepository.save(usuario);
        
        // Añadir mensaje de éxito y datos del usuario al modelo
        model.addAttribute("mensaje", "¡Información actualizada con éxito!");
        model.addAttribute("tipoMensaje", "exito");
        model.addAttribute("usuario", usuario);
        model.addAttribute("tieneImagen", usuario.getImagen() != null);
        
        return "perfil";
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
            model.addAttribute("usuario", usuario);
            model.addAttribute("tieneImagen", usuario.getImagen() != null);
            return "perfil";
        }
        
        // Verificar que las contraseñas nuevas coincidan
        if (!nuevaPassword.equals(confirmPassword)) {
            model.addAttribute("mensaje", "Error: Las contraseñas nuevas no coinciden.");
            model.addAttribute("tipoMensaje", "error");
            model.addAttribute("usuario", usuario);
            model.addAttribute("tieneImagen", usuario.getImagen() != null);
            return "perfil";
        }
        
        // Actualizar la contraseña
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        
        // Añadir mensaje de éxito y datos del usuario al modelo
        model.addAttribute("mensaje", "¡Contraseña actualizada con éxito!");
        model.addAttribute("tipoMensaje", "exito");
        model.addAttribute("usuario", usuario);
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
                model.addAttribute("usuario", usuario);
                model.addAttribute("tieneImagen", usuario.getImagen() != null);
                return "perfil";
            }
            
            // Verificar el tipo de archivo
            String contentType = imagen.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                model.addAttribute("mensaje", "Error: Por favor selecciona un archivo de imagen válido.");
                model.addAttribute("tipoMensaje", "error");
                model.addAttribute("usuario", usuario);
                model.addAttribute("tieneImagen", usuario.getImagen() != null);
                return "perfil";
            }
            
            // Obtener los bytes de la imagen
            byte[] imagenBytes = imagen.getBytes();
            
            // Redimensionar la imagen si es demasiado grande
            if (imagen.getSize() > MAX_FILE_SIZE) {
                imagenBytes = redimensionarImagen(imagenBytes);
            }
            
            // Guardar la imagen en el usuario
            usuario.setImagen(imagenBytes);
            usuarioRepository.save(usuario);
            
            // Añadir mensaje de éxito y datos del usuario al modelo
            model.addAttribute("mensaje", "¡Imagen de perfil actualizada con éxito!");
            model.addAttribute("tipoMensaje", "exito");
            model.addAttribute("usuario", usuario);
            model.addAttribute("tieneImagen", true);
            
        } catch (IOException e) {
            model.addAttribute("mensaje", "Error al procesar la imagen: " + e.getMessage());
            model.addAttribute("tipoMensaje", "error");
            model.addAttribute("usuario", usuario);
            model.addAttribute("tieneImagen", usuario.getImagen() != null);
        }
        
        return "perfil";
    }
    
    /**
     * Redimensiona la imagen para reducir su tamaño
     */
    private byte[] redimensionarImagen(byte[] imagenOriginal) throws IOException {
        // Leer la imagen original
        ByteArrayInputStream bis = new ByteArrayInputStream(imagenOriginal);
        BufferedImage imagen = ImageIO.read(bis);
        
        if (imagen == null) {
            throw new IOException("No se pudo leer la imagen");
        }
        
        // Calcular nuevas dimensiones manteniendo la relación de aspecto
        int originalWidth = imagen.getWidth();
        int originalHeight = imagen.getHeight();
        
        // Si la imagen ya es pequeña, devolverla sin cambios
        if (originalWidth <= MAX_WIDTH && originalHeight <= MAX_HEIGHT) {
            return imagenOriginal;
        }
        
        int newWidth, newHeight;
        
        if (originalWidth > originalHeight) {
            newWidth = MAX_WIDTH;
            newHeight = (int) (originalHeight * ((double) MAX_WIDTH / originalWidth));
        } else {
            newHeight = MAX_HEIGHT;
            newWidth = (int) (originalWidth * ((double) MAX_HEIGHT / originalHeight));
        }
        
        // Crear la nueva imagen redimensionada
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(imagen, 0, 0, newWidth, newHeight, null);
        g.dispose();
        
        // Convertir la imagen redimensionada a bytes
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        // Determinar el formato de la imagen
        String format = "jpeg"; // Formato por defecto
        
        // Guardar la imagen en el formato determinado
        ImageIO.write(resizedImage, format, bos);
        
        return bos.toByteArray();
    }
}