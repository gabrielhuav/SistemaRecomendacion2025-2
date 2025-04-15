package mx.ipn.escom.Recomendaciones.auth.service;

import mx.ipn.escom.Recomendaciones.auth.entity.LibroFavorito;
import mx.ipn.escom.Recomendaciones.auth.entity.Usuario;
import mx.ipn.escom.Recomendaciones.auth.repository.LibroFavoritoRepository;
import mx.ipn.escom.Recomendaciones.auth.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LibroFavoritoService {

    @Autowired
    private LibroFavoritoRepository libroFavoritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtiene el usuario actual autenticado
     */
    public Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return usuarioRepository.findByNombre(username);
    }

    /**
     * Obtiene todos los libros favoritos del usuario actual
     */
    public List<LibroFavorito> obtenerFavoritosUsuarioActual() {
        Usuario usuario = getUsuarioActual();
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }
        return libroFavoritoRepository.findByUsuarioOrderByFechaAgregadoDesc(usuario);
    }

    /**
     * Agrega un libro a favoritos
     */
    @Transactional
    public LibroFavorito agregarFavorito(String libroId, String titulo, String autor, String imagenUrl) {
        Usuario usuario = getUsuarioActual();
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // Verifica si ya existe el favorito
        if (libroFavoritoRepository.existsByUsuarioAndLibroId(usuario, libroId)) {
            throw new RuntimeException("El libro ya está en favoritos");
        }

        // Crea y guarda el nuevo favorito
        LibroFavorito favorito = new LibroFavorito(usuario, libroId, titulo, autor, imagenUrl);
        return libroFavoritoRepository.save(favorito);
    }

    /**
     * Elimina un libro de favoritos
     */
    @Transactional
    public void eliminarFavorito(String libroId) {
        Usuario usuario = getUsuarioActual();
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        libroFavoritoRepository.findByUsuarioAndLibroId(usuario, libroId)
                .ifPresent(favorito -> libroFavoritoRepository.delete(favorito));
    }

    /**
     * Verifica si un libro está en favoritos
     */
    public boolean esFavorito(String libroId) {
        Usuario usuario = getUsuarioActual();
        if (usuario == null) {
            return false;
        }
        return libroFavoritoRepository.existsByUsuarioAndLibroId(usuario, libroId);
    }
}