DROP DATABASE IF EXISTS control_matricula;
CREATE DATABASE control_matricula CHARACTER SET utf8mb4;
USE control_matricula;
CREATE TABLE usuario (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario VARCHAR(30) UNIQUE NOT NULL,
  clave CHAR(64) NOT NULL
);
CREATE TABLE estudiante (
  id INT AUTO_INCREMENT PRIMARY KEY,
  documento VARCHAR(20) UNIQUE NOT NULL,
  nombres VARCHAR(60) NOT NULL,
  apellidos VARCHAR(60) NOT NULL,
  correo VARCHAR(80),
  telefono VARCHAR(20)
);
CREATE TABLE curso (
  id INT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(10) UNIQUE NOT NULL,
  nombre VARCHAR(80) NOT NULL,
  creditos INT NOT NULL,
  cupo INT NOT NULL,
  valor DECIMAL(12,2) NOT NULL DEFAULT 0
);
CREATE TABLE matricula (
  id INT AUTO_INCREMENT PRIMARY KEY,
  id_estudiante INT NOT NULL,
  id_curso INT NOT NULL,
  periodo VARCHAR(10) NOT NULL,
  fecha DATE NOT NULL,
  valor_total DECIMAL(12,2) NOT NULL,
  cuotas INT NOT NULL DEFAULT 1,
  FOREIGN KEY (id_estudiante) REFERENCES estudiante(id) ON DELETE CASCADE,
  FOREIGN KEY (id_curso) REFERENCES curso(id) ON DELETE CASCADE,
  UNIQUE (id_estudiante, id_curso, periodo)
);
CREATE TABLE pago (
  id INT AUTO_INCREMENT PRIMARY KEY,
  id_matricula INT NOT NULL,
  valor DECIMAL(12,2) NOT NULL,
  concepto VARCHAR(40) NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_matricula) REFERENCES matricula(id) ON DELETE CASCADE
);
INSERT INTO usuario(usuario, clave) VALUES ('admin', SHA2('admin123', 256));
INSERT INTO estudiante(documento,nombres,apellidos,correo,telefono) VALUES
 ('1001','Ana','Pérez','ana@correo.com','3001112233'),
 ('1002','Luis','Gómez','luis@correo.com','3004445566'),
 ('1003','Marta','Díaz','marta@correo.com','3007778899');
INSERT INTO curso(codigo,nombre,creditos,cupo,valor) VALUES
 ('PRG3','Programación III',4,30,1200000),
 ('BD1','Bases de Datos',3,25,900000),
 ('RED1','Redes de Computadores',3,2,1000000);
INSERT INTO matricula(id_estudiante,id_curso,periodo,fecha,valor_total,cuotas) VALUES
 (1,1,'2026-2',CURDATE(),1200000,4),
 (2,2,'2026-2',CURDATE(),900000,3);
INSERT INTO pago(id_matricula,valor,concepto) VALUES
 (1,300000,'Abono inicial'),
 (2,900000,'Pago total');
