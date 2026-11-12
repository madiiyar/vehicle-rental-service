package com.vehiclerental.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/** Run this class for the desktop app. Main remains the original console demonstration. */
public final class RentalApplication {
    private RentalApplication() {}
    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("A graphical desktop is required. Run this app on your computer using JDK 21.");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                    if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; }
            } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ignored) {
                // Platform default remains usable.
            }
            Font font = new Font("SansSerif",Font.PLAIN,13);
            for (Object key : UIManager.getDefaults().keySet().toArray())
                if (UIManager.get(key) instanceof Font) UIManager.put(key,font);
            UIManager.put("nimbusBase",new Color(25,58,80));
            UIManager.put("nimbusSelectionBackground",new Color(12,119,113));
            UIManager.put("control",new Color(245,248,250));
            RentalDesktopModel model = new RentalDesktopModel();
            if (!Arrays.asList(args).contains("--empty")) model.seedDemo();
            JFrame window = new JFrame("Vehicle Rental Service | Desktop");
            window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            window.setContentPane(new MainWindow(model));
            window.setMinimumSize(new Dimension(1050,760));
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
