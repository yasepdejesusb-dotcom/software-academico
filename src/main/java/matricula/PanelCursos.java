package matricula;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class PanelCursos extends JPanel {
    private JTextField txtCodigo = new JTextField();
    private JTextField txtNombre = new JTextField();
    private JTextField txtCreditos = new JTextField();
    private JTextField txtCupo = new JTextField();
    private JTextField txtValor = new JTextField();
    private JTextField txtBuscar = new JTextField();
    private DefaultTableModel modelo;
    private JTable tabla;
    private int idSeleccionado = -1;

    public PanelCursos() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Estilo.FONDO);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(Color.WHITE);
        formulario.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.gridx = 0;
        JTextField[] campos = {txtCodigo, txtNombre, txtCreditos, txtCupo, txtValor};
        String[] etiquetas = {"Código *", "Nombre *", "Créditos *", "Cupo *", "Valor matrícula *"};
        for (int i = 0; i < campos.length; i++) {
            g.gridy = i * 2;
            formulario.add(new JLabel(etiquetas[i]), g);
            g.gridy = i * 2 + 1;
            campos[i].setPreferredSize(new Dimension(230, 34));
            formulario.add(campos[i], g);
        }

        JButton btnGuardar = Estilo.boton("Guardar", Estilo.VERDE, "save");
        JButton btnActualizar = Estilo.boton("Actualizar", Estilo.AZUL, "edit");
        JButton btnEliminar = Estilo.boton("Eliminar", Estilo.ROJO, "delete");
        JButton btnLimpiar = Estilo.boton("Limpiar", Estilo.GRIS, "clear");
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        JPanel botones = new JPanel(new GridLayout(2, 2, 8, 8));
        botones.setOpaque(false);
        botones.add(btnGuardar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);
        g.gridy = 10;
        g.insets = new Insets(18, 4, 4, 4);
        formulario.add(botones, g);

        JPanel izquierda = new JPanel(new BorderLayout());
        izquierda.setOpaque(false);
        izquierda.add(formulario, BorderLayout.NORTH);
        add(izquierda, BorderLayout.WEST);

        modelo = new DefaultTableModel(new String[]{"ID", "Código", "Nombre", "Créditos", "Cupo", "Valor matrícula"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        Estilo.estiloTabla(tabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        tabla.getColumnModel().getColumn(5).setCellRenderer(Estilo.rendererMoneda());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionar();
        });

        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar...");
        txtBuscar.setPreferredSize(new Dimension(100, 36));
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { cargar(); }
            public void removeUpdate(DocumentEvent e) { cargar(); }
            public void changedUpdate(DocumentEvent e) { cargar(); }
        });
        JPanel derecha = new JPanel(new BorderLayout(0, 10));
        derecha.setBackground(Color.WHITE);
        derecha.setBorder(new EmptyBorder(15, 15, 15, 15));
        derecha.add(txtBuscar, BorderLayout.NORTH);
        derecha.add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(derecha, BorderLayout.CENTER);
        cargar();
    }

    private String valor(int fila, int columna) {
        Object v = modelo.getValueAt(fila, columna);
        return v == null ? "" : v.toString();
    }

    private void seleccionar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        idSeleccionado = ((Number) modelo.getValueAt(fila, 0)).intValue();
        txtCodigo.setText(valor(fila, 1));
        txtNombre.setText(valor(fila, 2));
        txtCreditos.setText(valor(fila, 3));
        txtCupo.setText(valor(fila, 4));
        txtValor.setText(new BigDecimal(valor(fila, 5)).toBigInteger().toString());
    }

    private boolean validar() {
        if (txtCodigo.getText().trim().isEmpty() || txtNombre.getText().trim().isEmpty() || txtCreditos.getText().trim().isEmpty()
                || txtCupo.getText().trim().isEmpty() || txtValor.getText().trim().isEmpty()) {
            Estilo.advertencia("Completa los campos obligatorios (*)");
            return false;
        }
        if (!txtCreditos.getText().trim().matches("\\d+") || !txtCupo.getText().trim().matches("\\d+") || !txtValor.getText().trim().matches("\\d+")) {
            Estilo.advertencia("Créditos, cupo y valor deben ser números enteros");
            return false;
        }
        return true;
    }

    private void guardar() {
        if (!validar()) return;
        String sql = "INSERT INTO curso(codigo, nombre, creditos, cupo, valor) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = Conexion.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, txtCodigo.getText().trim());
            ps.setString(2, txtNombre.getText().trim());
            ps.setInt(3, Integer.parseInt(txtCreditos.getText().trim()));
            ps.setInt(4, Integer.parseInt(txtCupo.getText().trim()));
            ps.setBigDecimal(5, new BigDecimal(txtValor.getText().trim()));
            ps.executeUpdate();
            limpiar();
            cargar();
            Estilo.mensaje("Curso guardado correctamente");
        } catch (SQLIntegrityConstraintViolationException ex) {
            Estilo.advertencia("Ya existe un curso con ese código");
        } catch (SQLException | NumberFormatException ex) {
            Estilo.advertencia("Error: " + ex.getMessage());
        }
    }

    private void actualizar() {
        if (idSeleccionado < 0) {
            Estilo.advertencia("Selecciona un curso de la tabla");
            return;
        }
        if (!validar()) return;
        String sql = "UPDATE curso SET codigo = ?, nombre = ?, creditos = ?, cupo = ?, valor = ? WHERE id = ?";
        try (Connection c = Conexion.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, txtCodigo.getText().trim());
            ps.setString(2, txtNombre.getText().trim());
            ps.setInt(3, Integer.parseInt(txtCreditos.getText().trim()));
            ps.setInt(4, Integer.parseInt(txtCupo.getText().trim()));
            ps.setBigDecimal(5, new BigDecimal(txtValor.getText().trim()));
            ps.setInt(6, idSeleccionado);
            ps.executeUpdate();
            limpiar();
            cargar();
            Estilo.mensaje("Curso actualizado correctamente");
        } catch (SQLIntegrityConstraintViolationException ex) {
            Estilo.advertencia("Ya existe un curso con ese código");
        } catch (SQLException | NumberFormatException ex) {
            Estilo.advertencia("Error: " + ex.getMessage());
        }
    }

    private void eliminar() {
        if (idSeleccionado < 0) {
            Estilo.advertencia("Selecciona un curso de la tabla");
            return;
        }
        if (!Estilo.confirmar("¿Eliminar el curso? También se borrarán sus matrículas y pagos.")) return;
        try (Connection c = Conexion.conectar(); PreparedStatement ps = c.prepareStatement("DELETE FROM curso WHERE id = ?")) {
            ps.setInt(1, idSeleccionado);
            ps.executeUpdate();
            limpiar();
            cargar();
        } catch (SQLException ex) {
            Estilo.advertencia("Error: " + ex.getMessage());
        }
    }

    private void limpiar() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtCreditos.setText("");
        txtCupo.setText("");
        txtValor.setText("");
        idSeleccionado = -1;
        tabla.clearSelection();
    }

    public void cargar() {
        modelo.setRowCount(0);
        String sql = "SELECT id, codigo, nombre, creditos, cupo, valor FROM curso "
                + "WHERE CONCAT_WS(' ', codigo, nombre, creditos, cupo, valor) LIKE ? ORDER BY id DESC";
        try (Connection c = Conexion.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + txtBuscar.getText().trim() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modelo.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5), rs.getBigDecimal(6)});
            }
        } catch (SQLException ex) {
            Estilo.advertencia("Error al cargar los cursos: " + ex.getMessage());
        }
    }
}
