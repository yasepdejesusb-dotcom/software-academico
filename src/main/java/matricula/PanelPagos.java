package matricula;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class PanelPagos extends JPanel {
    private JComboBox<String> cmbMatricula = new JComboBox<>();
    private ArrayList<Integer> idsMatriculas = new ArrayList<>();
    private JLabel lblTotal = crearCaja();
    private JLabel lblPago = crearCaja();
    private JLabel lblDebe = crearCaja();
    private JLabel lblCuotas = crearCaja();
    private JLabel lblPendientes = crearCaja();
    private JLabel lblEstado = crearCaja();
    private JTextField txtValor = new JTextField();
    private DefaultTableModel modelo;
    private JTable tabla;
    private boolean cargando = false;
    private boolean hayMatricula = false;
    private int cuotas = 1;
    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal pagado = BigDecimal.ZERO;
    private BigDecimal saldo = BigDecimal.ZERO;

    public PanelPagos() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Estilo.FONDO);

        JPanel arriba = new JPanel();
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        arriba.setBackground(Color.WHITE);
        arriba.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel filaSeleccion = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filaSeleccion.setOpaque(false);
        filaSeleccion.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbMatricula.setPreferredSize(new Dimension(560, 36));
        cmbMatricula.addActionListener(e -> {
            if (!cargando) actualizar();
        });
        filaSeleccion.add(new JLabel("Matrícula:"));
        filaSeleccion.add(cmbMatricula);

        JPanel filaResumen = new JPanel(new GridLayout(1, 6, 12, 0));
        filaResumen.setOpaque(false);
        filaResumen.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaResumen.add(lblTotal);
        filaResumen.add(lblPago);
        filaResumen.add(lblDebe);
        filaResumen.add(lblCuotas);
        filaResumen.add(lblPendientes);
        filaResumen.add(lblEstado);

        JPanel filaPago = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filaPago.setOpaque(false);
        filaPago.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtValor.setPreferredSize(new Dimension(150, 36));
        JButton btnPagar = Estilo.boton("Registrar pago", Estilo.VERDE, "money");
        JButton btnEliminar = Estilo.boton("Eliminar pago", Estilo.ROJO, "delete");
        JButton btnImprimir = Estilo.boton("Imprimir", Estilo.AZUL, "print");
        btnPagar.setPreferredSize(new Dimension(160, 38));
        btnEliminar.setPreferredSize(new Dimension(160, 38));
        btnPagar.addActionListener(e -> registrar());
        btnEliminar.addActionListener(e -> eliminar());
        btnImprimir.addActionListener(e -> imprimir());
        filaPago.add(new JLabel("Valor a pagar:"));
        filaPago.add(txtValor);
        filaPago.add(btnPagar);
        filaPago.add(btnEliminar);
        filaPago.add(btnImprimir);

        arriba.add(filaSeleccion);
        arriba.add(Box.createVerticalStrut(12));
        arriba.add(filaResumen);
        arriba.add(Box.createVerticalStrut(12));
        arriba.add(filaPago);
        add(arriba, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"ID", "Fecha", "Concepto", "Valor"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        Estilo.estiloTabla(tabla);
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        tabla.getColumnModel().getColumn(3).setCellRenderer(Estilo.rendererMoneda());
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(Color.WHITE);
        centro.setBorder(new EmptyBorder(15, 15, 15, 15));
        centro.add(new JScrollPane(tabla));
        add(centro, BorderLayout.CENTER);
        cargar();
    }

    private static JLabel crearCaja() {
        JLabel l = new JLabel(" ", SwingConstants.CENTER);
        l.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0)), new EmptyBorder(10, 10, 10, 10)));
        return l;
    }

    private static String caja(String titulo, String valor, String color) {
        return "<html><div style='text-align:center'><font color='#64748B'>" + titulo + "</font><br><font size='5' color='" + color + "'><b>" + valor + "</b></font></div></html>";
    }

    private int idActual() {
        int i = cmbMatricula.getSelectedIndex();
        return i >= 0 && i < idsMatriculas.size() ? idsMatriculas.get(i) : -1;
    }

    public void cargar() {
        int idAnterior = idActual();
        cargando = true;
        cmbMatricula.removeAllItems();
        idsMatriculas.clear();
        String sql = "SELECT m.id, e.nombres, e.apellidos, c.codigo, m.periodo FROM matricula m "
                + "JOIN estudiante e ON e.id = m.id_estudiante JOIN curso c ON c.id = m.id_curso ORDER BY m.id DESC";
        try (Connection c = Conexion.conectar(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                idsMatriculas.add(rs.getInt(1));
                cmbMatricula.addItem("N° " + Estilo.numero(rs.getInt(1)) + " - " + rs.getString(2) + " " + rs.getString(3)
                        + " - " + rs.getString(4) + " (" + rs.getString(5) + ")");
            }
        } catch (SQLException ex) {
            Estilo.advertencia("Error al cargar: " + ex.getMessage());
        }
        int posicion = idsMatriculas.indexOf(idAnterior);
        if (posicion >= 0) cmbMatricula.setSelectedIndex(posicion);
        cargando = false;
        actualizar();
    }

    private void actualizar() {
        modelo.setRowCount(0);
        int id = idActual();
        hayMatricula = id >= 0;
        total = BigDecimal.ZERO;
        pagado = BigDecimal.ZERO;
        saldo = BigDecimal.ZERO;
        cuotas = 1;
        if (hayMatricula) {
            String sqlMatricula = "SELECT valor_total, cuotas, IFNULL((SELECT SUM(valor) FROM pago WHERE id_matricula = matricula.id), 0) FROM matricula WHERE id = ?";
            String sqlPagos = "SELECT id, fecha, concepto, valor FROM pago WHERE id_matricula = ? ORDER BY id";
            try (Connection c = Conexion.conectar();
                 PreparedStatement ps = c.prepareStatement(sqlMatricula);
                 PreparedStatement ps2 = c.prepareStatement(sqlPagos)) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    total = rs.getBigDecimal(1);
                    cuotas = rs.getInt(2);
                    pagado = rs.getBigDecimal(3);
                    saldo = total.subtract(pagado);
                }
                ps2.setInt(1, id);
                rs = ps2.executeQuery();
                while (rs.next()) {
                    modelo.addRow(new Object[]{rs.getInt(1), rs.getTimestamp(2), rs.getString(3), rs.getBigDecimal(4)});
                }
            } catch (SQLException ex) {
                Estilo.advertencia("Error: " + ex.getMessage());
            }
        }
        BigDecimal cuota = total.divide(BigDecimal.valueOf(cuotas), 0, RoundingMode.CEILING);
        boolean debe = saldo.signum() > 0;
        lblTotal.setText(caja("Total a pagar", Estilo.moneda(total), "#1E293B"));
        lblPago.setText(caja("Pagó", Estilo.moneda(pagado), "#16A34A"));
        lblDebe.setText(caja("Debe", Estilo.moneda(saldo), debe ? "#DC2626" : "#16A34A"));
        int pagadas = Estilo.cuotasPagadas(total, pagado, cuotas);
        int faltan = cuotas - pagadas;
        lblCuotas.setText(caja("Cuotas pagadas", pagadas + " de " + cuotas, "#16A34A"));
        lblPendientes.setText(caja("Cuotas que debe", faltan > 0 ? faltan + " x " + Estilo.moneda(cuota) : "Ninguna", faltan > 0 ? "#DC2626" : "#16A34A"));
        lblEstado.setText(caja("Estado", !hayMatricula ? "-" : debe ? "PENDIENTE" : "PAGADA", debe ? "#DC2626" : "#16A34A"));
        txtValor.setText(debe ? cuota.min(saldo).toBigInteger().toString() : "0");
    }

    private void registrar() {
        int id = idActual();
        if (id < 0) {
            Estilo.advertencia("No hay matrículas registradas");
            return;
        }
        BigDecimal valor;
        try {
            valor = new BigDecimal(txtValor.getText().trim());
        } catch (NumberFormatException ex) {
            Estilo.advertencia("Escribe un valor numérico");
            return;
        }
        if (valor.signum() <= 0) {
            Estilo.advertencia("El valor debe ser mayor a cero");
            return;
        }
        if (valor.compareTo(saldo) > 0) {
            Estilo.advertencia("El valor supera lo que debe (" + Estilo.moneda(saldo) + ")");
            return;
        }
        try (Connection c = Conexion.conectar(); PreparedStatement ps = c.prepareStatement("INSERT INTO pago(id_matricula, valor, concepto) VALUES (?, ?, 'Pago de cuota')")) {
            ps.setInt(1, id);
            ps.setBigDecimal(2, valor);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Estilo.advertencia("Error: " + ex.getMessage());
            return;
        }
        actualizar();
        Comprobante.mostrar(this, id, "Pago registrado");
    }

    private void imprimir() {
        int id = idActual();
        if (id < 0) {
            Estilo.advertencia("No hay matrículas registradas");
            return;
        }
        Comprobante.mostrar(this, id, "Comprobante de matrícula");
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.advertencia("Selecciona un pago de la tabla");
            return;
        }
        if (!Estilo.confirmar("¿Eliminar el pago seleccionado?")) return;
        try (Connection c = Conexion.conectar(); PreparedStatement ps = c.prepareStatement("DELETE FROM pago WHERE id = ?")) {
            ps.setInt(1, ((Number) modelo.getValueAt(fila, 0)).intValue());
            ps.executeUpdate();
            actualizar();
        } catch (SQLException ex) {
            Estilo.advertencia("Error: " + ex.getMessage());
        }
    }
}
