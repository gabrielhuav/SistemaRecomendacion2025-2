package mx.ipn.escom.Recomendaciones.auth.repository;

import mx.ipn.escom.Recomendaciones.auth.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByNombre(String nombre);
    Usuario findByEmail(String email);
}