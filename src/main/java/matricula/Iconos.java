package matricula;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.Icon;

public class Iconos {
    public static Icon get(String name, Color color, int size) {
        return new Icon() {
            public int getIconWidth() { return size; }
            public int getIconHeight() { return size; }

            public void paintIcon(Component c, Graphics g0, int x, int y) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g.translate(x, y);
                g.scale(size / 16.0, size / 16.0);
                g.setColor(color);
                g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                draw(g, name);
                g.dispose();
            }
        };
    }

    private static void draw(Graphics2D g, String n) {
        switch (n) {
            case "add":
                g.drawLine(8, 3, 8, 13); g.drawLine(3, 8, 13, 8); break;
            case "check":
                g.drawPolyline(new int[]{3, 7, 13}, new int[]{8, 12, 4}, 3); break;
            case "delete":
                g.drawLine(2, 4, 14, 4); g.drawLine(6, 4, 6, 2); g.drawLine(6, 2, 10, 2); g.drawLine(10, 2, 10, 4);
                g.drawPolyline(new int[]{4, 5, 11, 12}, new int[]{4, 14, 14, 4}, 4); g.drawLine(8, 7, 8, 11); break;
            case "edit":
                g.drawLine(3, 13, 4, 10); g.drawLine(4, 10, 11, 3); g.drawLine(11, 3, 13, 5);
                g.drawLine(13, 5, 6, 12); g.drawLine(6, 12, 3, 13); break;
            case "save":
                g.draw(new RoundRectangle2D.Float(2, 2, 12, 12, 2, 2)); g.drawRect(5, 2, 6, 4); g.drawRect(5, 9, 6, 5); break;
            case "clear":
                g.draw(new Arc2D.Float(3, 3, 10, 10, 40, 280, Arc2D.OPEN)); g.drawLine(12, 5, 12, 2); g.drawLine(12, 5, 9, 5); break;
            case "search":
                g.draw(new Ellipse2D.Float(2, 2, 8, 8)); g.drawLine(9, 9, 14, 14); break;
            case "user":
                g.draw(new Ellipse2D.Float(5, 2, 6, 6)); g.draw(new Arc2D.Float(2, 9, 12, 10, 0, 180, Arc2D.OPEN)); break;
            case "book":
                g.draw(new RoundRectangle2D.Float(3, 2, 10, 12, 2, 2)); g.drawLine(6, 2, 6, 14); g.drawLine(8, 6, 11, 6); break;
            case "list":
                for (int i = 0; i < 3; i++) { g.drawLine(6, 4 + i * 4, 14, 4 + i * 4); g.fillOval(2, 3 + i * 4, 2, 2); } break;
            case "money":
                g.draw(new Ellipse2D.Float(2, 2, 12, 12)); g.setFont(new Font("SansSerif", Font.BOLD, 10)); g.drawString("$", 5.5f, 11.5f); break;
            case "wallet":
                g.draw(new RoundRectangle2D.Float(2, 4, 12, 9, 2, 2)); g.drawLine(2, 7, 14, 7); g.fillOval(10, 9, 2, 2); break;
            case "chart":
                g.drawLine(4, 13, 4, 8); g.drawLine(8, 13, 8, 3); g.drawLine(12, 13, 12, 6); break;
            case "print":
                g.drawRect(4, 2, 8, 4); g.draw(new RoundRectangle2D.Float(2, 6, 12, 6, 2, 2)); g.drawRect(5, 10, 6, 4); break;
            case "export":
                g.drawLine(8, 2, 8, 10); g.drawPolyline(new int[]{5, 8, 11}, new int[]{7, 10, 7}, 3); g.drawLine(3, 14, 13, 14); break;
            case "logout":
                g.drawPolyline(new int[]{7, 3, 3, 7}, new int[]{3, 3, 13, 13}, 4); g.drawLine(7, 8, 14, 8);
                g.drawPolyline(new int[]{11, 14, 11}, new int[]{5, 8, 11}, 3); break;
            default:
                break;
        }
    }
}
