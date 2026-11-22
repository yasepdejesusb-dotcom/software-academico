package matricula;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class PanelMatriculas extends JPanel {
    private JComboBox<String> cmbEstudiante = new JComboBox<>();
    private JComboBox<String> cmbCurso = new JComboBox<>();
    private ArrayList<Integer> idsEstudiantes = new ArrayList<>();
    private ArrayList<Integer> idsCursos = new ArrayList<>();
    private JTextField txtPeriodo = new JTextField("2026-2");
    private JTextField txtAbono = new JTextField("0");
    private JSpinner spnCuotas = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
    private JLabel lblInfo = new JLabel(" ");
    private DefaultTableModel modelo;
    private JTable tabla;
    private boolean cargando = false;

    public PanelMatriculas() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Estilo.FONDO);

        JPanel arriba = new JPanel(new GridLayout(2, 1));
        arriba.setBackground(Color.WHITE);
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        fila1.setOpaque(false);
        fila2.setOpaque(false);
        cmbEstudiante.setPreferredSize(new Dimension(300, 36));
        cmbCurso.setPreferredSize(new Dimension(300, 36));
        txtPeriodo.setPreferredSize(new Dimension(90, 36));
        txtAbono.setPreferredSize(new Dimension(120, 36));
        spnCuotas.setPreferredSize(new Dimension(70, 36));
        lblInfo.setFont(lblInfo.getFont().deriveFont(Font.BOLD));
        lblInfo.setForeground(Estilo.AZUL);

        JButton btnMatricular = Estilo.boton("Matricular", Estilo.VERDE, "check");
        JButton btnAnular = Estilo.boton("Anular", Estilo.ROJO, "delete");
        JButton btnImprimir = Estilo.boton("Imprimir", Estilo.AZUL, "print");
        btnMatricular.addActionListener(e -> matricular());
        btnAnular.addActionListener(e -> anular());
        btnImprimir.addActionListener(e -> imprimir());
        cmbCurso.addActionListener(e -> mostrarInfo());
        txtPeriodo.addActionListener(e -> mostrarInfo());
        spnCuotas.addChangeListener(e -> mostrarInfo());

        fila1.add(new JLabel("Estudiante:"));
        fila1.add(cmbEstudiante);
        fila1.add(new JLabel("Curso:"));
        fila1.add(cmbCurso);
        fila1.add(new JLabel("Periodo:"));
        fila1.add(txtPeriodo);
        fila2.add(new JLabel("Cuotas:"));
        fila2.add(spnCuotas);
        fila2.add(new JLabel("Abono inicial:"));
        fila2.add(txtAbono);
        fila2.add(btnMatricular);
        fila2.add(btnAnular);
        fila2.add(btnImprimir);
        fila2.add(lblInfo);
        arriba.add(fila1);
        arriba.add(fila2);
        add(arriba, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"N°", "Documento", "Estudiante", "Curso", "Periodo", "Fecha", "Total", "Cuotas pagadas", "Pagó", "Debe", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        Estilo.estiloTabla(tabla);
        tabla.getColumnModel().getColumn(0).setMaxWidth(70);
        tabla.getColumnModel().getColumn(0).setCellRenderer(Estilo.rendererNumero());
        tabla.getColumnModel().getColumn(7).setMaxWidth(120);
        tabla.getColumnModel().getColumn(6).setCellRenderer(Estilo.rendererMoneda());
        tabla.getColumnModel().getColumn(8).setCellRenderer(Estilo.rendererMoneda());
        tabla.getColumnModel().getColumn(9).setCellRenderer(Estilo.rendererMoneda());
        tabla.getColumnModel().getColumn(10).setCellRenderer(Estilo.rendererEstado());
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(Color.WHITE);
        centro.setBorder(new EmptyBorder(15, 15, 15, 15));
        centro.add(new JScrollPane(tabla));
        add(centro, BorderLayout.CENTER);
        cargar();
    }

    private int idDe(JComboBox<String> combo, ArrayList<Integer> ids) {
        int i = combo.getSelectedIndex();
        return i >= 0 && i < ids.size() ? ids.get(i) : -1;
    }

    private void elegir(JComboBox<String> combo, ArrayList<Integer> ids, int id) {
        int i = ids.indexOf(id);
        if (i >= 0) combo.setSelectedIndex(i);
    }

    public void cargar() {
        int idE = idDe(cmbEstudiante, idsEstudiantes);
        int idC = idDe(cmbCurso, idsCursos);
        cargando = true;
        cmbEstudiante.removeAllItems();
        cmbCurso.removeAllItems();
        idsEstudiantes.clear();
        idsCursos.clear();
        modelo.setRowCount(0);
        String sql = "SELECT m.id, e.documento, CONCAT(e.nombres, ' ', e.apellidos), c.codigo, c.nombre, m.periodo, m.fecha, m.valor_total, m.cuotas, "
                + "IFNULL((SELECT SUM(p.valor) FROM pago p WHERE p.id_matricula = m.id), 0) "
                + "FROM matricula m JOIN estudiante e ON e.id = m.id_estudiante JOIN curso c ON c.id = m.id_curso ORDER BY m.id DESC";
        try (Connection c = Conexion.conectar(); Statement st = c.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT id, documento, nombres, apellidos FROM estudiante ORDER BY nombres");
            while (rs.next()) {
                idsEstudiantes.add(rs.getInt(1));
                cmbEstudiante.addItem(rs.getString(2) + " - " + rs.getString(3) + " " + rs.getString(4));
            }
            rs = st.executeQuery("SELECT id, codigo, nombre FROM curso ORDER BY nombre");
            while (rs.next()) {
                idsCursos.add(rs.getInt(1));
                cmbCurso.addItem(rs.getString(2) + " - " + rs.getString(3));
            }
            rs = st.executeQuery(sql);
            while (rs.next()) {
                BigDecimal total = rs.getBigDecimal(8);
                BigDecimal pagado = rs.getBigDecimal(10);
                BigDecimal debe = total.subtract(pagado);
                modelo.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4) + " - " + rs.getString(5),
                    rs.getString(6), rs.getDate(7), total, Estilo.cuotasPagadas(total, pagado, rs.getInt(9)) + " de " + rs.getInt(9), pagado, debe, debe.signum() <= 0 ? "Pagada" : "Pendiente"});
            }
        } catch (SQLException ex) {
            Estilo.advertencia("Error al cargar: " + ex.getMessage());
        }
        elegir(cmbEstudiante, idsEstudiantes, idE);
        elegir(cmbCurso, idsCursos, idC);
        cargando = false;
        mostrarInfo();
    }

    private BigDecimal valorCurso(int idCurso) {
        try (Connection c = Conexion.conectar(); PreparedStatement ps = c.prepareStatement("SELECT valor FROM curso WHERE id = ?")) {
            ps.setInt(1, idCurso);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException ex) {
            Estilo.advertencia("Error: " + ex.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private int cuposDisponibles(int idCurso, String periodo) {
        String sql = "SELECT cupo - (SELECT COUNT(*) FROM matricula WHERE id_curso = ? AND periodo = ?) FROM curso WHERE id = ?";
        try (Connection c = Conexion.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCurso);
            ps.setString(2, periodo);
            ps.setInt(3, idCurso);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            Estilo.advertencia("Error: " + ex.getMessage());
        }
        return 0;
    }

    private void mostrarInfo() {
        if (cargando) return;
        int idC = idDe(cmbCurso, idsCursos);
        if (idC < 0) {
            lblInfo.setText(" ");
            return;
        }
        BigDecimal valor = valorCurso(idC);
        int cuotas = (Integer) spnCuotas.getValue();
        BigDecimal cuota = valor.divide(BigDecimal.valueOf(cuotas), 0, RoundingMode.CEILING);
        lblInfo.setText("Valor: " + Estilo.moneda(valor) + "   Cuota: " + Estilo.moneda(cuota)
                + "   Cupos: " + cuposDisponibles(idC, txtPeriodo.getText().trim()));
    }

    private void matricular() {
        int idE = idDe(cmbEstudiante, idsEstudiantes);
        int idC = idDe(cmbCurso, idsCursos);
        String periodo = txtPeriodo.getText().trim();
        if (idE < 0 || idC < 0) {
            Estilo.advertencia("Registra primero estudiantes y cursos");
            return;
        }
        if (periodo.isEmpty()) {
            Estilo.advertencia("Escribe el periodo (ej. 2026-2)");
            return;
        }
        BigDecimal abono;
        try {
            String texto = txtAbono.getText().trim();
            abono = new BigDecimal(texto.isEmpty() ? "0" : texto);
        } catch (NumberFormatException ex) {
            Estilo.advertencia("El abono debe ser un número");
            return;
        }
        if (abono.signum() < 0) {
            Estilo.advertencia("El abono no puede ser negativo");
            return;
        }
        BigDecimal total = valorCurso(idC);
        int cuotas = (Integer) spnCuotas.getValue();
        if (cuposDisponibles(idC, periodo) <= 0) {
            Estilo.advertencia("El curso no tiene cupos disponibles");
            return;
        }
        if (abono.compareTo(total) > 0) {
            Estilo.advertencia("El abono no puede ser mayor al valor de la matrícula (" + Estilo.moneda(total) + ")");
            return;
        }
        int idMatricula = 0;
        try (Connection c = Conexion.conectar()) {
            c.setAutoCommit(false);
            try {
                String sql = "INSERT INTO matricula(id_estudiante, id_curso, periodo, fecha, valor_total, cuotas) VALUES (?, ?, ?, CURDATE(), ?, ?)";
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idE);
                    ps.setInt(2, idC);
                    ps.setString(3, periodo);
                    ps.setBigDecimal(4, total);
                    ps.setInt(5, cuotas);
                    ps.executeUpdate();
                    ResultSet claves = ps.getGeneratedKeys();
                    claves.next();
                    idMatricula = claves.getInt(1);
                }
                if (abono.signum() > 0) {
                    try (PreparedStatement ps = c.prepareStatement("INSERT INTO pago(id_matricula, valor, concepto) VALUES (?, ?, 'Abono inicial')")) {
                        ps.setInt(1, idMatricula);
                        ps.setBigDecimal(2, abono);
                        ps.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLIntegrityConstraintViolationException ex) {
            Estilo.advertencia("El estudiante ya está matriculado en ese curso para el periodo " + periodo);
            return;
        } catch (SQLException ex) {
            Estilo.advertencia("Error: " + ex.getMessage());
            return;
        }
        txtAbono.setText("0");
        cargar();
        Comprobante.mostrar(this, idMatricula, "¡Matrícula exitosa!");
    }

    private void imprimir() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.advertencia("Selecciona una matrícula de la tabla");
            return;
        }
        Comprobante.mostrar(this, ((Number) modelo.getValueAt(fila, 0)).intValue(), "Comprobante de matrícula");
    }

    private void anular() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.advertencia("Selecciona una matrícula de la tabla");
            return;
        }
        if (!Estilo.confirmar("¿Anular la matrícula seleccionada? También se eliminarán sus pagos.")) return;
        try (Connection c = Conexion.conectar(); PreparedStatement ps = c.prepareStatement("DELETE FROM matricula WHERE id = ?")) {
            ps.setInt(1, ((Number) modelo.getValueAt(fila, 0)).intValue());
            ps.executeUpdate();
            cargar();
        } catch (SQLException ex) {
            Estilo.advertencia("Error: " + ex.getMessage());
        }
    }
}
