package mx.ipn.escom.Recomendaciones.auth.controller;

import mx.ipn.escom.Recomendaciones.auth.entity.Rol;
import mx.ipn.escom.Recomendaciones.auth.entity.Usuario;
import mx.ipn.escom.Recomendaciones.auth.repository.RolRepository;
import mx.ipn.escom.Recomendaciones.auth.repository.UsuarioRepository;
import mx.ipn.escom.Recomendaciones.auth.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class ApiRegisterController {

    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Usuario usuario) {
        try {
            // Verificar si el correo ya existe
            Usuario existingUser = usuarioRepository.findByEmail(usuario.getEmail());
            if (existingUser != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El correo electrónico ya está registrado");
            }
            
            // Verificar que la contraseña no sea nula o vacía
            if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("La contraseña no puede estar vacía");
            }

            // Asignar rol de ROLE_USER por defecto
            Set<Rol> roles = new HashSet<>();
            rolRepository.findByNombre("ROLE_USER").ifPresent(roles::add);
            usuario.setRoles(roles);
            
            // Registrar usuario
            usuarioService.registrarUsuario(usuario);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body("Usuario registrado exitosamente");
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al registrar el usuario: " + e.getMessage());
        }
    }
}