package matricula;

import java.awt.*;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Comprobante {
    public static void mostrar(Component padre, int id, String titulo) {
        StringBuilder html = new StringBuilder();
        String sqlMatricula = "SELECT e.documento, e.nombres, e.apellidos, c.codigo, c.nombre, m.periodo, m.fecha, m.valor_total, m.cuotas, "
                + "IFNULL((SELECT SUM(p.valor) FROM pago p WHERE p.id_matricula = m.id), 0) "
                + "FROM matricula m JOIN estudiante e ON e.id = m.id_estudiante JOIN curso c ON c.id = m.id_curso WHERE m.id = ?";
        String sqlPagos = "SELECT fecha, concepto, valor FROM pago WHERE id_matricula = ? ORDER BY id";
        try (Connection c = Conexion.conectar();
             PreparedStatement ps = c.prepareStatement(sqlMatricula);
             PreparedStatement ps2 = c.prepareStatement(sqlPagos)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                Estilo.advertencia("No se encontró la matrícula");
                return;
            }
            BigDecimal total = rs.getBigDecimal(8);
            BigDecimal pagado = rs.getBigDecimal(10);
            BigDecimal debe = total.subtract(pagado);
            int cuotas = rs.getInt(9);
            BigDecimal cuota = Estilo.valorCuota(total, cuotas);
            int pagadas = Estilo.cuotasPagadas(total, pagado, cuotas);
            int faltan = cuotas - pagadas;
            String colorDebe = debe.signum() > 0 ? "#DC2626" : "#16A34A";
            html.append("<html><body style='width:440px'><p><b>Estudiante:</b> ").append(rs.getString(2)).append(" ").append(rs.getString(3))
                    .append(" (Doc. ").append(rs.getString(1)).append(")<br><b>Curso:</b> ")
                    .append(rs.getString(4)).append(" - ").append(rs.getString(5))
                    .append("<br><b>Periodo:</b> ").append(rs.getString(6))
                    .append("<br><b>Fecha de matrícula:</b> ").append(rs.getDate(7)).append("</p><hr>")
                    .append("<table width='100%' cellpadding='4'>")
                    .append("<tr><td>Total a pagar</td><td align='right'><b>").append(Estilo.moneda(total)).append("</b></td></tr>")
                    .append("<tr><td>Cuotas</td><td align='right'>").append(cuotas).append(" de ").append(Estilo.moneda(cuota)).append("</td></tr>")
                    .append("<tr><td>Cuotas pagadas</td><td align='right'>").append(pagadas).append(" de ").append(cuotas).append("</td></tr>")
                    .append("<tr><td>Cuotas que debe</td><td align='right'><font color='").append(faltan > 0 ? "#DC2626" : "#16A34A").append("'><b>")
                    .append(faltan > 0 ? faltan + " x " + Estilo.moneda(cuota) : "Ninguna").append("</b></font></td></tr>")
                    .append("<tr><td>Pagó</td><td align='right'><font color='#16A34A'><b>").append(Estilo.moneda(pagado)).append("</b></font></td></tr>")
                    .append("<tr><td>Debe</td><td align='right'><font color='").append(colorDebe).append("'><b>").append(Estilo.moneda(debe)).append("</b></font></td></tr>")
                    .append("</table>");
            if (debe.signum() <= 0) html.append("<p><font color='#16A34A'><b>MATRÍCULA PAGADA POR COMPLETO</b></font></p>");
            html.append("<hr><p><b>Detalle de pagos</b></p><table width='100%' cellpadding='3'>")
                    .append("<tr><td><b>Fecha</b></td><td><b>Concepto</b></td><td align='right'><b>Valor</b></td></tr>");
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            boolean hayPagos = false;
            ps2.setInt(1, id);
            rs = ps2.executeQuery();
            while (rs.next()) {
                hayPagos = true;
                html.append("<tr><td>").append(formato.format(rs.getTimestamp(1))).append("</td><td>").append(rs.getString(2))
                        .append("</td><td align='right'>").append(Estilo.moneda(rs.getBigDecimal(3))).append("</td></tr>");
            }
            if (!hayPagos) html.append("<tr><td colspan='3'>Sin pagos registrados</td></tr>");
            html.append("</table></body></html>");
        } catch (SQLException ex) {
            Estilo.advertencia("Error: " + ex.getMessage());
            return;
        }

        JPanel hoja = new JPanel();
        hoja.setLayout(new BoxLayout(hoja, BoxLayout.Y_AXIS));
        hoja.setBackground(Color.WHITE);
        hoja.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel lblTitulo = new JLabel(titulo, Iconos.get("check", Estilo.VERDE, 28), SwingConstants.CENTER);
        lblTitulo.setIconTextGap(10);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 20f));
        lblTitulo.setForeground(Estilo.VERDE);
        JLabel lblNumero = new JLabel("Comprobante N° " + Estilo.numero(id), SwingConstants.CENTER);
        lblNumero.setFont(lblNumero.getFont().deriveFont(Font.BOLD, 15f));
        lblNumero.setForeground(Estilo.GRIS);
        JLabel lblCuerpo = new JLabel(html.toString());

        JPanel firmas = new JPanel(new GridLayout(1, 2, 50, 0));
        firmas.setOpaque(false);
        firmas.setBorder(new EmptyBorder(60, 0, 0, 0));
        JLabel firma1 = new JLabel("Firma del estudiante", SwingConstants.CENTER);
        JLabel firma2 = new JLabel("Firma del responsable", SwingConstants.CENTER);
        firma1.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
        firma2.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
        firmas.add(firma1);
        firmas.add(firma2);

        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblNumero.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblCuerpo.setAlignmentX(Component.CENTER_ALIGNMENT);
        firmas.setAlignmentX(Component.CENTER_ALIGNMENT);
        hoja.add(lblTitulo);
        hoja.add(Box.createVerticalStrut(6));
        hoja.add(lblNumero);
        hoja.add(Box.createVerticalStrut(14));
        hoja.add(lblCuerpo);
        hoja.add(firmas);

        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(padre), "Comprobante", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        barra.setBackground(Estilo.FONDO);
        JButton btnImprimir = Estilo.boton("Imprimir", Estilo.AZUL, "print");
        JButton btnCerrar = Estilo.boton("Cerrar", Estilo.GRIS, "logout");
        btnImprimir.addActionListener(e -> imprimir(hoja));
        btnCerrar.addActionListener(e -> dialogo.dispose());
        barra.add(btnImprimir);
        barra.add(btnCerrar);
        dialogo.setLayout(new BorderLayout());
        dialogo.add(hoja, BorderLayout.CENTER);
        dialogo.add(barra, BorderLayout.SOUTH);
        dialogo.pack();
        dialogo.setLocationRelativeTo(padre);
        dialogo.setVisible(true);
    }

    private static void imprimir(JPanel hoja) {
        PrinterJob trabajo = PrinterJob.getPrinterJob();
        trabajo.setJobName("Comprobante de matrícula");
        trabajo.setPrintable((g, formato, pagina) -> {
            if (pagina > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(formato.getImageableX(), formato.getImageableY());
            double escala = Math.min(1.0, formato.getImageableWidth() / hoja.getWidth());
            g2.scale(escala, escala);
            hoja.printAll(g2);
            return Printable.PAGE_EXISTS;
        });
        if (trabajo.printDialog()) {
            try {
                trabajo.print();
            } catch (PrinterException ex) {
                Estilo.advertencia("No se pudo imprimir: " + ex.getMessage());
            }
        }
    }
}
