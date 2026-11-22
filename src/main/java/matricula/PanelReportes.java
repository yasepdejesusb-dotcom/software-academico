package matricula;

import java.awt.*;
import java.awt.print.PrinterException;
import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class PanelReportes extends JPanel {
    private JTextField txtBuscar = new JTextField();
    private JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"Todos", "Pagadas", "Pendientes"});
    private JLabel lblResumen = new JLabel(" ");
    private DefaultTableModel modelo;
    private JTable tabla;

    public PanelReportes() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Estilo.FONDO);

        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        arriba.setBackground(Color.WHITE);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por estudiante, curso o periodo...");
        txtBuscar.setPreferredSize(new Dimension(330, 36));
        cmbEstado.setPreferredSize(new Dimension(140, 36));
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { cargar(); }
            public void removeUpdate(DocumentEvent e) { cargar(); }
            public void changedUpdate(DocumentEvent e) { cargar(); }
        });
        cmbEstado.addActionListener(e -> cargar());
        JButton btnExportar = Estilo.boton("Exportar CSV", Estilo.VERDE, "export");
        JButton btnImprimir = Estilo.boton("Imprimir", Estilo.AZUL, "print");
        btnExportar.setPreferredSize(new Dimension(150, 38));
        btnExportar.addActionListener(e -> exportar());
        btnImprimir.addActionListener(e -> imprimir());
        arriba.add(new JLabel("Buscar:"));
        arriba.add(txtBuscar);
        arriba.add(new JLabel("Estado:"));
        arriba.add(cmbEstado);
        arriba.add(btnExportar);
        arriba.add(btnImprimir);
        add(arriba, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"N°", "Documento", "Estudiante", "Curso", "Periodo", "Cuotas", "Total", "Pagó", "Debe", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        Estilo.estiloTabla(tabla);
        tabla.getColumnModel().getColumn(0).setMaxWidth(70);
        tabla.getColumnModel().getColumn(0).setCellRenderer(Estilo.rendererNumero());
        tabla.getColumnModel().getColumn(6).setCellRenderer(Estilo.rendererMoneda());
        tabla.getColumnModel().getColumn(7).setCellRenderer(Estilo.rendererMoneda());
        tabla.getColumnModel().getColumn(8).setCellRenderer(Estilo.rendererMoneda());
        tabla.getColumnModel().getColumn(9).setCellRenderer(Estilo.rendererEstado());
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(Color.WHITE);
        centro.setBorder(new EmptyBorder(15, 15, 15, 15));
        centro.add(new JScrollPane(tabla));
        add(centro, BorderLayout.CENTER);

        lblResumen.setFont(lblResumen.getFont().deriveFont(Font.BOLD, 14f));
        lblResumen.setForeground(Estilo.AZUL);
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        pie.setBackground(Color.WHITE);
        pie.add(lblResumen);
        add(pie, BorderLayout.SOUTH);
        cargar();
    }

    public void cargar() {
        modelo.setRowCount(0);
        String estado = (String) cmbEstado.getSelectedItem();
        String sql = "SELECT m.id, e.documento, CONCAT(e.nombres, ' ', e.apellidos), c.codigo, c.nombre, m.periodo, m.valor_total, "
                + "IFNULL((SELECT SUM(p.valor) FROM pago p WHERE p.id_matricula = m.id), 0), m.cuotas "
                + "FROM matricula m JOIN estudiante e ON e.id = m.id_estudiante JOIN curso c ON c.id = m.id_curso "
                + "WHERE CONCAT_WS(' ', e.documento, e.nombres, e.apellidos, c.codigo, c.nombre, m.periodo) LIKE ? ORDER BY m.id DESC";
        BigDecimal sumaTotal = BigDecimal.ZERO;
        BigDecimal sumaPagado = BigDecimal.ZERO;
        BigDecimal sumaDebe = BigDecimal.ZERO;
        int cantidad = 0;
        try (Connection c = Conexion.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + txtBuscar.getText().trim() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BigDecimal total = rs.getBigDecimal(7);
                BigDecimal pagado = rs.getBigDecimal(8);
                BigDecimal debe = total.subtract(pagado);
                String textoCuotas = Estilo.cuotasPagadas(total, pagado, rs.getInt(9)) + " de " + rs.getInt(9);
                boolean pagada = debe.signum() <= 0;
                if ("Pagadas".equals(estado) && !pagada) continue;
                if ("Pendientes".equals(estado) && pagada) continue;
                modelo.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4) + " - " + rs.getString(5),
                    rs.getString(6), textoCuotas, total, pagado, debe, pagada ? "Pagada" : "Pendiente"});
                sumaTotal = sumaTotal.add(total);
                sumaPagado = sumaPagado.add(pagado);
                sumaDebe = sumaDebe.add(debe);
                cantidad++;
            }
        } catch (SQLException ex) {
            Estilo.advertencia("Error al cargar el reporte: " + ex.getMessage());
        }
        lblResumen.setText("Matrículas: " + cantidad + "     Total: " + Estilo.moneda(sumaTotal)
                + "     Recaudado: " + Estilo.moneda(sumaPagado) + "     Pendiente por cobrar: " + Estilo.moneda(sumaDebe));
    }

    private void exportar() {
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File("reporte_matriculas.csv"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter pw = new PrintWriter(selector.getSelectedFile(), "UTF-8")) {
            pw.print('\uFEFF');
            StringBuilder linea = new StringBuilder();
            for (int c = 0; c < modelo.getColumnCount(); c++) {
                linea.append(c > 0 ? ";" : "").append(modelo.getColumnName(c));
            }
            pw.println(linea);
            for (int f = 0; f < modelo.getRowCount(); f++) {
                linea = new StringBuilder();
                for (int c = 0; c < modelo.getColumnCount(); c++) {
                    Object v = modelo.getValueAt(f, c);
                    String texto;
                    if (c == 0) texto = Estilo.numero(((Number) v).intValue());
                    else if (v instanceof BigDecimal) texto = ((BigDecimal) v).toPlainString();
                    else texto = String.valueOf(v);
                    linea.append(c > 0 ? ";" : "").append(texto.replace(";", ","));
                }
                pw.println(linea);
            }
            Estilo.mensaje("Reporte exportado correctamente. Puedes abrirlo con Excel.");
        } catch (IOException ex) {
            Estilo.advertencia("No se pudo guardar el archivo: " + ex.getMessage());
        }
    }

    private void imprimir() {
        try {
            tabla.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat("Reporte de Matrículas"), new MessageFormat("Página {0}"));
        } catch (PrinterException ex) {
            Estilo.advertencia("No se pudo imprimir: " + ex.getMessage());
        }
    }
}
