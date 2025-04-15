package mx.ipn.escom.Recomendaciones.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "libros_favoritos")
public class LibroFavorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "libro_id", nullable = false)
    private String libroId;

    @Column(nullable = false)
    private String titulo;

    private String autor;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "fecha_agregado")
    private LocalDateTime fechaAgregado;

    // Constructores
    public LibroFavorito() {
        this.fechaAgregado = LocalDateTime.now();
    }

    public LibroFavorito(Usuario usuario, String libroId, String titulo, String autor, String imagenUrl) {
        this.usuario = usuario;
        this.libroId = libroId;
        this.titulo = titulo;
        this.autor = autor;
        this.imagenUrl = imagenUrl;
        this.fechaAgregado = LocalDateTime.now();
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getLibroId() {
        return libroId;
    }

    public void setLibroId(String libroId) {
        this.libroId = libroId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public LocalDateTime getFechaAgregado() {
        return fechaAgregado;
    }

    public void setFechaAgregado(LocalDateTime fechaAgregado) {
        this.fechaAgregado = fechaAgregado;
    }
}