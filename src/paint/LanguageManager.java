package paint;

import java.awt.ComponentOrientation;
import java.util.*;
import javax.swing.*;

/** Les fichiers properties sont compatibles Java 8 et 17 grâce aux échappements Unicode. */
public final class LanguageManager {
    private Locale locale;
    private ResourceBundle messages;
    public LanguageManager() { setLanguage("fr"); }
    public void setLanguage(String code) {
        locale = new Locale(code);
        messages = ResourceBundle.getBundle("messages", locale);
        Locale.setDefault(locale);
        JComponent.setDefaultLocale(locale);
        UIManager.getDefaults().setDefaultLocale(locale);
        // Traduire aussi les composants standards du JFileChooser.
        for (String key : messages.keySet()) {
            if (key.startsWith("ui.")) UIManager.put(key.substring(3), messages.getString(key));
        }
    }
    public String text(String key) { return messages.getString(key); }
    public Locale locale() { return locale; }
    public ComponentOrientation orientation() { return ComponentOrientation.getOrientation(locale); }
}
