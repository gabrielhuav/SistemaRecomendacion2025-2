package mx.ipn.escom.Recomendaciones.auth.service;

import mx.ipn.escom.Recomendaciones.auth.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CustomUserDetails implements UserDetails {

    private Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convertir los roles del usuario a GrantedAuthority
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        usuario.getRoles().forEach(rol -> {
            // Imprimir los roles para depuración
            System.out.println("Rol encontrado: " + rol.getNombre());
            authorities.add(new SimpleGrantedAuthority(rol.getNombre())); // Usar el nombre del rol directamente
        });
        
        // Imprimir todas las autoridades para depuración
        for (SimpleGrantedAuthority auth : authorities) {
            System.out.println("Autoridad asignada: " + auth.getAuthority());
        }
        
        return authorities;
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getNombre();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}