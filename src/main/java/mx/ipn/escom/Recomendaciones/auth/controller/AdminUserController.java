package mx.ipn.escom.Recomendaciones.auth.controller;

import mx.ipn.escom.Recomendaciones.auth.entity.Rol;
import mx.ipn.escom.Recomendaciones.auth.entity.Usuario;
import mx.ipn.escom.Recomendaciones.auth.repository.RolRepository;
import mx.ipn.escom.Recomendaciones.auth.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminUserController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        model.addAttribute("usuarios", usuarios);
        return "administrador";
    }


    @GetMapping("/usuarios/{id}")
    public String verUsuario(@PathVariable Long id, Model model) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            model.addAttribute("usuario", usuarioOpt.get());
            model.addAttribute("todosLosRoles", rolRepository.findAll());
            model.addAttribute("tieneImagen", usuarioOpt.get().getImagen() != null);
            return "editarUsuario";
        } else {
            return "redirect:/admin/usuarios";
        }
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, 
                                 @RequestParam(value = "roles", required = false) List<Long> roleIds,
                                 @RequestParam(value = "password", required = false) String password,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Verificar si es un usuario nuevo o existente
            boolean esNuevo = (usuario.getId() == null);
            
            // Si es un usuario existente, obtenerlo para no perder la imagen y verificar cambios en el email
            if (!esNuevo) {
                Optional<Usuario> usuarioExistente = usuarioRepository.findById(usuario.getId());
                if (usuarioExistente.isPresent()) {
                    Usuario uExistente = usuarioExistente.get();
                    
                    // Verificar si se está intentando cambiar el email a uno ya existente
                    if (!uExistente.getEmail().equals(usuario.getEmail())) {
                        Usuario emailExistente = usuarioRepository.findByEmail(usuario.getEmail());
                        if (emailExistente != null && !emailExistente.getId().equals(usuario.getId())) {
                            redirectAttributes.addFlashAttribute("mensaje", "El email ya está en uso por otro usuario");
                            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                            return "redirect:/admin/usuarios/" + usuario.getId();
                        }
                    }
                    
                    // Mantener la imagen existente
                    usuario.setImagen(uExistente.getImagen());
                    
                    // Mantener la contraseña si no se proporciona una nueva
                    if (password == null || password.trim().isEmpty()) {
                        usuario.setPassword(uExistente.getPassword());
                    } else {
                        usuario.setPassword(passwordEncoder.encode(password));
                    }
                }
            } else {
                // Para usuario nuevo, la contraseña es obligatoria
                if (password == null || password.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("mensaje", "La contraseña es obligatoria para un nuevo usuario");
                    redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                    return "redirect:/admin/usuarios/nuevo";
                }
                usuario.setPassword(passwordEncoder.encode(password));
            }
            
            // Asignar roles
            Set<Rol> roles = new HashSet<>();
            if (roleIds != null && !roleIds.isEmpty()) {
                for (Long roleId : roleIds) {
                    rolRepository.findById(roleId).ifPresent(roles::add);
                }
            } else {
                // Si no se seleccionó ningún rol, asignar ROLE_USER por defecto
                rolRepository.findByNombre("ROLE_USER").ifPresent(roles::add);
            }
            usuario.setRoles(roles);
            
            // Guardar usuario
            usuarioRepository.save(usuario);
            
            redirectAttributes.addFlashAttribute("mensaje", 
                esNuevo ? "Usuario creado correctamente" : "Usuario actualizado correctamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "exito");
            
            return "redirect:/admin/usuarios";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al guardar el usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/usuarios";
        }
    }

    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("todosLosRoles", rolRepository.findAll());
        return "editarUsuario";
    }

    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado correctamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "exito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar el usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }
        return "redirect:/admin/usuarios";
    }
}