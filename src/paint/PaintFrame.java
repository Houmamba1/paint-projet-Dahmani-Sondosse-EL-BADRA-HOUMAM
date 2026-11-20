package paint;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Coordination de l'interface ; les pixels et les fichiers restent dans leurs classes dédiées. */
public final class PaintFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final LanguageManager language = new LanguageManager();
    private final DrawingPanel drawing = new DrawingPanel();
    private final Map<String, Action> actions = new LinkedHashMap<>();
    private final Map<DrawingTool, JRadioButton> tools = new EnumMap<>(DrawingTool.class);
    private final JPanel root = new JPanel(new BorderLayout(0, 0));
    private final JLabel status = new JLabel();
    private File currentFile;
    private Point position;
    private JButton colorButton;
    private JComboBox<Integer> widths;
    private boolean busy;

    public PaintFrame() {
        setName("PaintInternational");
        createActions();
        setContentPane(root);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { closeDocument(); }
        });
        drawing.setChangeListener(this::refreshState);
        drawing.setPositionListener(p -> { position = p; refreshState(); });
        rebuildInterface();
        setMinimumSize(new Dimension(820, 580));
        setSize(1200, 820);
        setLocationRelativeTo(null);
    }
    private String t(String key) { return language.text(key); }
    private void action(String key, int keyCode, int modifiers, Runnable command) {
        AbstractAction a = new AbstractAction(t(key)) {
            @Override public void actionPerformed(ActionEvent e) {
                if (!busy) { drawing.cancelGesture(); command.run(); }
            }
        };
        if (keyCode != 0) a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyCode, modifiers));
        actions.put(key, a);
    }
    private void createActions() {
        int ctrl = InputEvent.CTRL_DOWN_MASK;
        action("new", KeyEvent.VK_N, ctrl, this::newDocument);
        action("open", KeyEvent.VK_O, ctrl, this::openDocument);
        action("save", KeyEvent.VK_S, ctrl, () -> save(false, false));
        action("saveAs", KeyEvent.VK_S, ctrl | InputEvent.SHIFT_DOWN_MASK, () -> save(true, false));
        action("export", KeyEvent.VK_E, ctrl, () -> save(true, true));
        action("quit", KeyEvent.VK_Q, ctrl, this::closeDocument);
        action("undo", KeyEvent.VK_Z, ctrl, drawing::undo);
        action("redo", KeyEvent.VK_Y, ctrl, drawing::redo);
        action("clear", KeyEvent.VK_DELETE, ctrl, () -> {
            if (confirm("clearQuestion")) drawing.clearDrawing();
        });
        action("color", 0, 0, this::chooseColor);
        action("about", KeyEvent.VK_F1, 0, () -> JOptionPane.showMessageDialog(this, t("aboutText"), t("about"), JOptionPane.INFORMATION_MESSAGE));
    }
    private JMenu menu(String key, String... items) {
        JMenu menu = new JMenu(t(key));
        for (String item : items) {
            if (item.equals("-")) menu.addSeparator();
            else menu.add(new JMenuItem(actions.get(item)));
        }
        return menu;
    }
    private void rebuildInterface() {
        root.removeAll(); tools.clear();
        for (Map.Entry<String, Action> entry : actions.entrySet()) entry.getValue().putValue(Action.NAME, t(entry.getKey()));
        JMenuBar bar = new JMenuBar();
        bar.add(menu("file", "new", "open", "-", "save", "saveAs", "export", "-", "quit"));
        bar.add(menu("edit", "undo", "redo", "-", "clear"));
        JMenu languages = new JMenu(t("language"));
        ButtonGroup languageGroup = new ButtonGroup();
        String[] codes = {"fr", "en", "ar"};
        String[] names = {"Français", "English", "العربية"};
        for (int i = 0; i < codes.length; i++) {
            final String code = codes[i];
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(names[i], language.locale().getLanguage().equals(code));
            item.addActionListener(e -> { drawing.cancelGesture(); language.setLanguage(code); rebuildInterface(); });
            languages.add(item); languageGroup.add(item);
        }
        bar.add(languages); bar.add(menu("help", "about")); setJMenuBar(bar);
        JToolBar toolbar = new JToolBar(); toolbar.setFloatable(false);
        for (String key : new String[]{"new", "open", "save", "undo", "redo"}) {
            JButton button = toolbar.add(actions.get(key)); button.setFocusable(false); button.setMargin(new Insets(7, 10, 7, 10));
        }
        toolbar.addSeparator(); colorButton = toolbar.add(actions.get("color"));
        toolbar.addSeparator(); JLabel widthLabel = new JLabel(t("width")); toolbar.add(widthLabel);
        widths = new JComboBox<>(new Integer[]{1, 2, 4, 8, 12, 20, 32, 48, 64});
        widths.setSelectedItem(drawing.getStrokeWidth()); widths.setMaximumSize(new Dimension(75, 32));
        widths.setToolTipText(t("width")); widthLabel.setLabelFor(widths);
        widths.addActionListener(e -> drawing.setStrokeWidth((Integer) widths.getSelectedItem())); toolbar.add(widths);
        root.add(toolbar, BorderLayout.NORTH);
        JPanel toolGrid = new JPanel(new GridLayout(0, 1, 3, 3));
        toolGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        ButtonGroup group = new ButtonGroup();
        for (DrawingTool tool : DrawingTool.values()) {
            JRadioButton button = new JRadioButton(t(tool.key()), drawing.getTool() == tool);
            button.setName(tool.name()); button.addActionListener(e -> drawing.setTool(tool));
            group.add(button); toolGrid.add(button); tools.put(tool, button);
        }
        JPanel sidebar = new JPanel(new BorderLayout()); sidebar.add(toolGrid, BorderLayout.NORTH);
        JLabel hint = new JLabel(t("hint")); hint.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        sidebar.add(hint, BorderLayout.SOUTH); root.add(sidebar, BorderLayout.LINE_START);
        // Un document conserve sa taille en pixels ; le viewport ne l'étire pas.
        JPanel workspace = new JPanel(new FlowLayout(FlowLayout.LEADING, 18, 18));
        workspace.setBackground(new Color(218, 224, 232)); workspace.add(drawing);
        JScrollPane scroll = new JScrollPane(workspace); scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getHorizontalScrollBar().setUnitIncrement(24); root.add(scroll, BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); root.add(status, BorderLayout.SOUTH);
        root.applyComponentOrientation(language.orientation()); bar.applyComponentOrientation(language.orientation());
        // L'origine des coordonnées du dessin reste en haut à gauche dans toutes les langues.
        drawing.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        refreshState(); root.revalidate(); root.repaint();
    }
    private void refreshState() {
        if (actions.isEmpty()) return;
        actions.get("undo").setEnabled(drawing.canUndo()); actions.get("redo").setEnabled(drawing.canRedo());
        for (Map.Entry<DrawingTool, JRadioButton> entry : tools.entrySet()) entry.getValue().setSelected(entry.getKey() == drawing.getTool());
        if (widths != null && !Integer.valueOf(drawing.getStrokeWidth()).equals(widths.getSelectedItem())) widths.setSelectedItem(drawing.getStrokeWidth());
        String filename = currentFile == null ? t("untitled") : currentFile.getName();
        setTitle((drawing.isModified() ? "* " : "") + filename + " — " + t("app"));
        status.setText(t(drawing.getTool().key()) + "  |  " + drawing.getStrokeWidth() + " px  |  "
                + drawing.documentWidth() + " × " + drawing.documentHeight() + " px  |  "
                + (position == null ? t("ready") : "x=" + position.x + "  y=" + position.y)
                + "  |  " + t(drawing.isModified() ? "modified" : "saved"));
        if (colorButton != null) {
            Color color = drawing.getDrawingColor();
            colorButton.setBackground(color);
            colorButton.setForeground(color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114 < 145 ? Color.WHITE : Color.BLACK);
        }
    }
    private boolean confirm(String key) {
        Object[] choices = {t("yes"), t("no")};
        return JOptionPane.showOptionDialog(this, t(key), t("app"), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, choices, choices[1]) == 0;
    }
    private boolean canDiscard() {
        if (!drawing.isModified()) return true;
        Object[] choices = {t("save"), t("discard"), t("cancel")};
        int choice = JOptionPane.showOptionDialog(this, t("saveQuestion"), t("app"), JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE, null, choices, choices[0]);
        return choice == 1 || (choice == 0 && save(false, false));
    }
    private void newDocument() {
        JTextField w = new JTextField("1000", 7), h = new JTextField("650", 7);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel(t("imageWidth"))); form.add(w); form.add(new JLabel(t("imageHeight"))); form.add(h);
        form.applyComponentOrientation(language.orientation());
        while (true) {
            int choice = JOptionPane.showOptionDialog(this, form, t("new"), JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, new Object[]{t("ok"), t("cancel")}, t("ok"));
            if (choice != 0) return;
            try {
                int width = Integer.parseInt(w.getText().trim()), height = Integer.parseInt(h.getText().trim());
                DrawingPanel.validateSize(width, height);
                if (canDiscard()) { drawing.newDocument(width, height); currentFile = null; refreshState(); }
                return;
            } catch (IllegalArgumentException ex) { error("sizeError"); }
        }
    }
    private JFileChooser chooser(boolean saving) {
        JFileChooser chooser = new JFileChooser();
        chooser.setLocale(language.locale()); chooser.setDialogTitle(t(saving ? "saveAs" : "open"));
        chooser.setFileFilter(saving ? new FileNameExtensionFilter(t("pngFilter"), "png")
                : new FileNameExtensionFilter(t("imageFilter"), "png", "jpg", "jpeg", "gif", "bmp"));
        chooser.setAcceptAllFileFilterUsed(!saving);
        if (currentFile != null) chooser.setCurrentDirectory(currentFile.getAbsoluteFile().getParentFile());
        if (saving) chooser.setSelectedFile(new File(currentFile == null ? "drawing.png" : currentFile.getName()));
        chooser.applyComponentOrientation(language.orientation()); return chooser;
    }
    private void openDocument() {
        JFileChooser chooser = chooser(false);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!canDiscard()) return;
        BufferedImage image = performIo(() -> FileManager.read(file));
        if (image != null) {
            drawing.loadDocument(image);
            currentFile = file.getName().toLowerCase(Locale.ROOT).endsWith(".png") ? file : null;
            refreshState();
        }
    }
    private boolean save(boolean forceChooser, boolean export) {
        File target = currentFile;
        if (forceChooser || target == null) {
            JFileChooser chooser = chooser(true);
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return false;
            target = FileManager.pngFile(chooser.getSelectedFile());
            if (target.exists() && !confirm("overwriteQuestion")) return false;
        }
        final File output = target;
        final BufferedImage image = drawing.snapshot();
        Boolean success = performIo(() -> { FileManager.writePng(image, output); return Boolean.TRUE; });
        if (!Boolean.TRUE.equals(success)) return false;
        if (!export) { currentFile = target; drawing.markSaved(); }
        refreshState(); return true;
    }
    /** Un dialogue modal empêche de modifier le document pendant le travail d'E/S hors EDT. */
    private <T> T performIo(Callable<T> operation) {
        JDialog progress = new JDialog(this, t("app"), true);
        progress.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        JLabel label = new JLabel(t("working")); label.setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));
        progress.add(label); progress.applyComponentOrientation(language.orientation());
        progress.pack(); progress.setLocationRelativeTo(this);
        SwingWorker<T, Void> worker = new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return operation.call(); }
            @Override protected void done() { progress.dispose(); }
        };
        busy = true; worker.execute(); progress.setVisible(true); busy = false;
        try { return worker.get(); }
        catch (InterruptedException ex) { Thread.currentThread().interrupt(); error("ioError"); }
        catch (java.util.concurrent.ExecutionException ex) {
            Throwable cause = ex.getCause();
            error(cause != null && "size".equals(cause.getMessage()) ? "sizeError" : "ioError");
        }
        return null;
    }
    private void chooseColor() {
        // Trois sliders assurent des libellés traduits aussi en arabe, sans dépendre du thème système.
        Color original = drawing.getDrawingColor();
        JSlider r = new JSlider(0, 255, original.getRed());
        JSlider g = new JSlider(0, 255, original.getGreen());
        JSlider b = new JSlider(0, 255, original.getBlue());
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        JLabel preview = new JLabel(t("preview"), SwingConstants.CENTER); preview.setOpaque(true);
        JLabel values = new JLabel();
        Runnable update = () -> {
            Color chosen = new Color(r.getValue(), g.getValue(), b.getValue());
            preview.setBackground(chosen); preview.setForeground(chosen.getRed() + chosen.getGreen() + chosen.getBlue() < 380 ? Color.WHITE : Color.BLACK);
            values.setText(r.getValue() + " / " + g.getValue() + " / " + b.getValue());
        };
        JSlider[] sliders = {r, g, b}; String[] keys = {"red", "green", "blue"};
        for (int i = 0; i < 3; i++) {
            JLabel name = new JLabel(t(keys[i])); name.setLabelFor(sliders[i]);
            form.add(name); form.add(sliders[i]); sliders[i].addChangeListener(e -> update.run());
        }
        form.add(preview); form.add(values); update.run(); form.applyComponentOrientation(language.orientation());
        int result = JOptionPane.showOptionDialog(this, form, t("color"), JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, new Object[]{t("ok"), t("cancel")}, t("ok"));
        if (result == 0) drawing.setDrawingColor(new Color(r.getValue(), g.getValue(), b.getValue()));
    }
    private void error(String key) { JOptionPane.showMessageDialog(this, t(key), t("error"), JOptionPane.ERROR_MESSAGE); }
    private void closeDocument() { if (!busy && canDiscard()) dispose(); }
}
