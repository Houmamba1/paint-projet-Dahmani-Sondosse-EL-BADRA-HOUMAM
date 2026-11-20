package paint;

import javax.swing.*;

/** Point d'entrée : les composants Swing sont créés sur le thread EDT. */
public final class Main {
    private Main() { }
    static void configureUi() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ex) { System.err.println(ex.getMessage()); }
        javax.swing.plaf.FontUIResource font = new javax.swing.plaf.FontUIResource("SansSerif", java.awt.Font.PLAIN, 14);
        for (Object key : java.util.Collections.list(UIManager.getDefaults().keys())) {
            if (UIManager.get(key) instanceof javax.swing.plaf.FontUIResource) UIManager.put(key, font);
        }
        UIManager.put("swing.boldMetal", Boolean.FALSE);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configureUi();
            new PaintFrame().setVisible(true);
        });
    }
}
