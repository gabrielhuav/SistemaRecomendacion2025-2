package mx.ipn.escom.Recomendaciones.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()  // Desactiva CSRF para facilitar el acceso desde la aplicación móvil
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/api/auth/**").permitAll()  // Permitir acceso sin autenticación a la app móvil
                .requestMatchers("/admin").hasRole("ADMIN")
                .requestMatchers("/perfil").authenticated()
                .requestMatchers("/home").authenticated()
                .anyRequest().permitAll()
            )
            .formLogin((form) -> form
                .loginPage("/login")  // Mantiene el form login para el navegador
                .defaultSuccessUrl("/home", true)  // Redirige a /home tras login exitoso
                .permitAll()
            )
            .logout((logout) -> logout.permitAll());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
