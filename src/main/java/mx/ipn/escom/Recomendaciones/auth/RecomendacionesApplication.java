package mx.ipn.escom.Recomendaciones.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "mx.ipn.escom.Recomendaciones.auth.config",
    "mx.ipn.escom.Recomendaciones.auth.controller",
    "mx.ipn.escom.Recomendaciones.auth.entity",
    "mx.ipn.escom.Recomendaciones.auth.repository",
    "mx.ipn.escom.Recomendaciones.auth.service",
    "mx.ipn.escom.Recomendaciones.auth.SistemaAutenticacion"
})
@EnableJpaRepositories(basePackages = "mx.ipn.escom.Recomendaciones.auth.repository")
@EntityScan(basePackages = "mx.ipn.escom.Recomendaciones.auth.entity")
public class RecomendacionesApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecomendacionesApplication.class, args);
    }
}
