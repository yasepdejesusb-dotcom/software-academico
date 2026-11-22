package matricula;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Principal extends JFrame {
    private JLabel lblResumen = new JLabel();

    public Principal() {
        super("Control de Matrícula");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Estilo.AZUL);
        encabezado.setBorder(new EmptyBorder(12, 22, 12, 22));
        JLabel titulo = new JLabel("Control de Matrícula", Iconos.get("book", Color.WHITE, 26), SwingConstants.LEFT);
        titulo.setIconTextGap(12);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        titulo.setForeground(Color.WHITE);
        lblResumen.setForeground(Color.WHITE);
        lblResumen.setFont(lblResumen.getFont().deriveFont(Font.BOLD, 13f));
        JButton btnSalir = Estilo.boton("Salir", new Color(0x1E40AF), "logout");
        btnSalir.setPreferredSize(new Dimension(110, 36));
        btnSalir.addActionListener(e -> {
            dispose();
            new Login().setVisible(true);
        });
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        derecha.setOpaque(false);
        derecha.add(lblResumen);
        derecha.add(btnSalir);
        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);

        PanelEstudiantes estudiantes = new PanelEstudiantes();
        PanelCursos cursos = new PanelCursos();
        PanelMatriculas matriculas = new PanelMatriculas();
        PanelPagos pagos = new PanelPagos();
        PanelReportes reportes = new PanelReportes();

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(pestanas.getFont().deriveFont(Font.BOLD, 14f));
        pestanas.addTab(" Estudiantes ", Iconos.get("user", Estilo.AZUL, 18), estudiantes);
        pestanas.addTab(" Cursos ", Iconos.get("book", Estilo.AZUL, 18), cursos);
        pestanas.addTab(" Matrículas ", Iconos.get("list", Estilo.AZUL, 18), matriculas);
        pestanas.addTab(" Pagos ", Iconos.get("wallet", Estilo.AZUL, 18), pagos);
        pestanas.addTab(" Reportes ", Iconos.get("chart", Estilo.AZUL, 18), reportes);
        pestanas.addChangeListener(e -> {
            estudiantes.cargar();
            cursos.cargar();
            matriculas.cargar();
            pagos.cargar();
            reportes.cargar();
            actualizarResumen();
        });

        setLayout(new BorderLayout());
        add(encabezado, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
        actualizarResumen();
    }

    private void actualizarResumen() {
        String sql = "SELECT (SELECT COUNT(*) FROM estudiante), (SELECT COUNT(*) FROM curso), (SELECT COUNT(*) FROM matricula), "
                + "(SELECT IFNULL(SUM(valor), 0) FROM pago)";
        try (Connection c = Conexion.conectar(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            lblResumen.setText("Estudiantes: " + rs.getInt(1) + "    Cursos: " + rs.getInt(2) + "    Matrículas: " + rs.getInt(3)
                    + "    Recaudado: " + Estilo.moneda(rs.getBigDecimal(4)));
        } catch (SQLException ex) {
            lblResumen.setText("Sin conexión a la BD");
        }
    }
}
