package mx.ipn.escom.Recomendaciones.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void checkConnection() {
        try {
            // Realizamos una consulta simple para verificar la conexión
            String sql = "SELECT 1";
            Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
            if (result != null && result == 1) {
                System.out.println("Conexión a la base de datos exitosa");
            } else {
                System.out.println("Conexión fallida o resultado inesperado");
            }
        } catch (Exception e) {
            System.out.println("Error al conectar con la base de datos: " + e.getMessage());
        }
    }
}
