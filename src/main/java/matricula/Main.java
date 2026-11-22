package matricula;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.util.Collections;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#2563EB"));
        FlatLightLaf.setup();
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("TableHeader.background", Estilo.AZUL);
        UIManager.put("TableHeader.foreground", Color.WHITE);
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
