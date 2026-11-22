package matricula;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class Estilo {
    public static final Color AZUL = new Color(0x2563EB);
    public static final Color VERDE = new Color(0x16A34A);
    public static final Color ROJO = new Color(0xDC2626);
    public static final Color GRIS = new Color(0x64748B);
    public static final Color FONDO = new Color(0xF1F5F9);

    private static final NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    static {
        formato.setMaximumFractionDigits(0);
        formato.setMinimumFractionDigits(0);
    }

    public static String moneda(Number n) {
        return formato.format(n == null ? 0 : n.doubleValue());
    }

    public static String numero(int n) {
        return String.format("%04d", n);
    }

    public static BigDecimal valorCuota(BigDecimal total, int cuotas) {
        return total.divide(BigDecimal.valueOf(Math.max(cuotas, 1)), 0, RoundingMode.CEILING);
    }

    public static int cuotasPagadas(BigDecimal total, BigDecimal pagado, int cuotas) {
        if (total.subtract(pagado).signum() <= 0) return cuotas;
        BigDecimal cuota = valorCuota(total, cuotas);
        if (cuota.signum() == 0) return 0;
        int pagadas = pagado.divide(cuota, 0, RoundingMode.FLOOR).intValue();
        return Math.min(pagadas, cuotas - 1);
    }

    public static JButton boton(String texto, Color color, String icono) {
        JButton b = new JButton(texto);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(130, 38));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (icono != null) {
            b.setIcon(Iconos.get(icono, Color.WHITE, 16));
            b.setIconTextGap(8);
        }
        return b;
    }

    public static void estiloTabla(JTable t) {
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(0xDBEAFE));
        t.setSelectionForeground(Color.BLACK);
        t.getTableHeader().setFont(t.getFont().deriveFont(Font.BOLD, 13f));
        t.getTableHeader().setPreferredSize(new Dimension(0, 34));
    }

    public static DefaultTableCellRenderer rendererMoneda() {
        return new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.RIGHT);
            }

            @Override
            protected void setValue(Object v) {
                setText(v instanceof Number ? moneda((Number) v) : String.valueOf(v));
            }
        };
    }

    public static DefaultTableCellRenderer rendererNumero() {
        return new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.CENTER);
            }

            @Override
            protected void setValue(Object v) {
                setText(v instanceof Number ? numero(((Number) v).intValue()) : String.valueOf(v));
            }
        };
    }

    public static DefaultTableCellRenderer rendererEstado() {
        return new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.CENTER);
            }

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component x = super.getTableCellRendererComponent(t, v, s, f, r, c);
                x.setFont(x.getFont().deriveFont(Font.BOLD));
                x.setForeground("Pagada".equals(v) ? VERDE : ROJO);
                return x;
            }
        };
    }

    public static void mensaje(String m) {
        JOptionPane.showMessageDialog(null, m, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void advertencia(String m) {
        JOptionPane.showMessageDialog(null, m, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirmar(String m) {
        return JOptionPane.showConfirmDialog(null, m, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
