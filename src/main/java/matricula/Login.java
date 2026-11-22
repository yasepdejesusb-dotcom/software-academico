package matricula;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Login extends JFrame {
    private JTextField txtUsuario = new JTextField();
    private JPasswordField txtClave = new JPasswordField();

    public Login() {
        super("Acceso - Control de Matrícula");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 460);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(Estilo.AZUL);
        JPanel tarjeta = new JPanel(new GridLayout(0, 1, 0, 8));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel titulo = new JLabel("Control de Matrícula", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        titulo.setForeground(Estilo.AZUL);
        JLabel subtitulo = new JLabel("Inicia sesión para continuar", SwingConstants.CENTER);
        subtitulo.setForeground(Estilo.GRIS);
        txtUsuario.putClientProperty("JTextField.placeholderText", "Usuario");
        txtClave.putClientProperty("JTextField.placeholderText", "Contraseña");
        txtUsuario.setPreferredSize(new Dimension(260, 38));
        txtClave.setPreferredSize(new Dimension(260, 38));
        JButton btnIngresar = Estilo.boton("Ingresar", Estilo.AZUL, null);
        btnIngresar.setPreferredSize(new Dimension(260, 40));
        btnIngresar.addActionListener(e -> ingresar());
        JLabel ayuda = new JLabel("Usuario: admin  |  Clave: admin123", SwingConstants.CENTER);
        ayuda.setForeground(Estilo.GRIS);
        ayuda.setFont(ayuda.getFont().deriveFont(11f));
        tarjeta.add(titulo);
        tarjeta.add(subtitulo);
        tarjeta.add(txtUsuario);
        tarjeta.add(txtClave);
        tarjeta.add(btnIngresar);
        tarjeta.add(ayuda);
        fondo.add(tarjeta);
        setContentPane(fondo);
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void ingresar() {
        String sql = "SELECT 1 FROM usuario WHERE usuario = ? AND clave = SHA2(?, 256)";
        try (Connection c = Conexion.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, txtUsuario.getText().trim());
            ps.setString(2, new String(txtClave.getPassword()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                dispose();
                new Principal().setVisible(true);
            } else {
                Estilo.advertencia("Usuario o contraseña incorrectos");
            }
        } catch (SQLException ex) {
            Estilo.advertencia("No se pudo conectar a la base de datos.\nVerifica que MySQL esté activo en XAMPP y que hayas importado el script.\n\n" + ex.getMessage());
        }
    }
}
