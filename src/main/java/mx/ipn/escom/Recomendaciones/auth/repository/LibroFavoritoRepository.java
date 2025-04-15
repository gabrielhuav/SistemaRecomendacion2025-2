package mx.ipn.escom.Recomendaciones.auth.repository;

import mx.ipn.escom.Recomendaciones.auth.entity.LibroFavorito;
import mx.ipn.escom.Recomendaciones.auth.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibroFavoritoRepository extends JpaRepository<LibroFavorito, Long> {
    
    List<LibroFavorito> findByUsuarioOrderByFechaAgregadoDesc(Usuario usuario);
    
    Optional<LibroFavorito> findByUsuarioAndLibroId(Usuario usuario, String libroId);
    
    boolean existsByUsuarioAndLibroId(Usuario usuario, String libroId);
    
    void deleteByUsuarioAndLibroId(Usuario usuario, String libroId);
}