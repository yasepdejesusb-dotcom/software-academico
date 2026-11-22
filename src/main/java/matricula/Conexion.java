package matricula;

import java.sql.*;

public class Conexion {
    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/control_matricula?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                "root", "");
    }
}
