-- Eliminar la base de datos "practica2" si ya existe
DROP DATABASE IF EXISTS practica2;

-- Crear la base de datos "practica2" con codificación UTF-8
CREATE DATABASE practica2 CHARACTER SET utf8 COLLATE utf8_general_ci;

-- Usar la base de datos "practica2"
USE practica2;

-- Crear la tabla de usuarios
CREATE TABLE usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(64) NOT NULL,
    email VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL
);

-- Crear la tabla de roles
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(64) NOT NULL UNIQUE
);

-- Crear la tabla intermedia para la relación muchos a muchos entre usuarios y roles
CREATE TABLE usuario_roles (
    usuario_id BIGINT,
    rol_id BIGINT,
    PRIMARY KEY (usuario_id, rol_id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (rol_id) REFERENCES roles(id)
);

-- Crear la tabla de libros favoritos
CREATE TABLE libros_favoritos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    libro_id VARCHAR(255) NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    autor VARCHAR(255),
    imagen_url VARCHAR(1024),
    fecha_agregado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    UNIQUE KEY uk_usuario_libro (usuario_id, libro_id)
);

-- Insertar roles en la tabla roles
INSERT INTO roles (nombre) VALUES ('ROLE_ADMIN'), ('ROLE_USER');

-- Eliminar el usuario 'admin' si ya existe
DROP USER IF EXISTS 'admin'@'localhost';
FLUSH PRIVILEGES;

-- Crear el usuario 'admin' con la contraseña 'admin'
CREATE USER 'admin'@'localhost' IDENTIFIED BY 'admin';

-- Otorgar todos los permisos sobre la base de datos "practica2" al usuario 'admin'
GRANT ALL PRIVILEGES ON practica2.* TO 'admin'@'localhost';

-- Aplicar los cambios
FLUSH PRIVILEGES;

-- Para el BLOB:
ALTER TABLE usuarios ADD COLUMN imagen LONGBLOB;

-- Crear usuario administrador (la contraseña 'secreto' está encriptada con BCrypt)
INSERT INTO usuarios (nombre, email, password) 
VALUES ('administrador', 'admin@sistema.com', '$2a$10$TRQFRRFbVMQGUvEZ.gE07OXnCHcr0nFbWO6CXk4QTg7A8QyGb3RMO');

-- Obtener el ID del usuario recién creado
SET @admin_id = LAST_INSERT_ID();

-- Asignar el rol de administrador al usuario
INSERT INTO usuario_roles (usuario_id, rol_id) 
SELECT @admin_id, id FROM roles WHERE nombre = 'ROLE_ADMIN';

-- Agregar un libro favorito para el administrador
INSERT INTO libros_favoritos (usuario_id, libro_id, titulo, autor, imagen_url, fecha_agregado) 
VALUES (@admin_id, '/works/OL26365569W', 'Yzklerin Efendisi 1', 'J.R.R. Tolkien', 
        'https://covers.openlibrary.org/b/id/12308941-M.jpg', NOW());