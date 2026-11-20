package paint;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

/** Surface persistante : paintComponent affiche les pixels, les listeners modifient le modèle. */
public final class DrawingPanel extends JPanel implements MouseListener, MouseMotionListener {
    private static final long serialVersionUID = 1L;
    public static final long MAX_PIXELS = 4_000_000;
    private static final long HISTORY_BYTES = 64L * 1024 * 1024;
    private static final int HISTORY_LIMIT = 20;
    private static final class State {
        final BufferedImage image;
        final long id;
        State(BufferedImage image, long id) { this.image = image; this.id = id; }
    }
    private final List<State> history = new ArrayList<>();
    private int index;
    private long nextId, savedId;
    private DrawingTool tool = DrawingTool.PENCIL;
    private Color color = new Color(35, 49, 68);
    private int strokeWidth = 4;
    private Point start, end, previous;
    private BufferedImage working;
    private Runnable changeListener = () -> { };
    private Consumer<Point> positionListener = p -> { };

    public DrawingPanel() {
        setBackground(Color.WHITE);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        addMouseListener(this);
        addMouseMotionListener(this);
        newDocument(1000, 650);
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        getActionMap().put("cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { cancelGesture(); }
        });
    }
    public void setChangeListener(Runnable listener) { changeListener = listener; }
    public void setPositionListener(Consumer<Point> listener) { positionListener = listener; }
    public DrawingTool getTool() { return tool; }
    public void setTool(DrawingTool value) { cancelGesture(); tool = value; changeListener.run(); }
    public Color getDrawingColor() { return color; }
    public void setDrawingColor(Color value) { if (value != null) color = value; changeListener.run(); }
    public int getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(int value) { strokeWidth = Math.max(1, Math.min(64, value)); changeListener.run(); }
    private BufferedImage image() { return history.get(index).image; }
    public int documentWidth() { return image().getWidth(); }
    public int documentHeight() { return image().getHeight(); }
    public boolean isModified() { return history.get(index).id != savedId; }
    public void markSaved() { savedId = history.get(index).id; changeListener.run(); }
    public boolean canUndo() { return index > 0; }
    public boolean canRedo() { return index + 1 < history.size(); }
    public BufferedImage snapshot() { return copy(image()); }
    public static BufferedImage copy(BufferedImage source) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.setColor(Color.WHITE); g.fillRect(0, 0, result.getWidth(), result.getHeight());
        g.drawImage(source, 0, 0, null); g.dispose();
        return result;
    }
    public static void validateSize(int w, int h) {
        if (w < 1 || h < 1 || (long) w * h > MAX_PIXELS) throw new IllegalArgumentException("size");
    }
    public void newDocument(int w, int h) {
        validateSize(w, h);
        BufferedImage blank = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = blank.createGraphics();
        g.setColor(Color.WHITE); g.fillRect(0, 0, w, h); g.dispose();
        loadDocument(blank);
    }
    public void loadDocument(BufferedImage source) {
        validateSize(source.getWidth(), source.getHeight());
        cancelGesture(); history.clear(); index = 0;
        State state = new State(copy(source), ++nextId);
        history.add(state); savedId = state.id;
        setPreferredSize(new Dimension(documentWidth(), documentHeight()));
        revalidate(); repaint(); changeListener.run();
    }
    private void commit(BufferedImage result) {
        while (history.size() > index + 1) history.remove(history.size() - 1);
        history.add(new State(result, ++nextId)); index++;
        long bytes = (long) documentWidth() * documentHeight() * 4;
        while (history.size() > 1 && (history.size() > HISTORY_LIMIT || history.size() * bytes > HISTORY_BYTES)) {
            history.remove(0); index--;
        }
        repaint(); changeListener.run();
    }
    public void undo() { cancelGesture(); if (canUndo()) { index--; repaint(); changeListener.run(); } }
    public void redo() { cancelGesture(); if (canRedo()) { index++; repaint(); changeListener.run(); } }
    public void clearDrawing() {
        cancelGesture(); BufferedImage blank = new BufferedImage(documentWidth(), documentHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = blank.createGraphics(); g.setColor(Color.WHITE);
        g.fillRect(0, 0, blank.getWidth(), blank.getHeight()); g.dispose(); commit(blank);
    }
    public void cancelGesture() { start = null; end = null; previous = null; working = null; repaint(); }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.drawImage(working == null ? image() : working, 0, 0, null);
        // La zone d'aperçu ne dépasse jamais les dimensions du document.
        g.clipRect(0, 0, documentWidth(), documentHeight());
        if (start != null && working == null) renderShape(g);
        g.dispose();
    }
    private void configure(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(tool == DrawingTool.ERASER ? Color.WHITE : color);
        g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }
    private void renderShape(Graphics2D g) {
        configure(g);
        int x = Math.min(start.x, end.x), y = Math.min(start.y, end.y);
        int w = Math.abs(end.x - start.x), h = Math.abs(end.y - start.y);
        Shape shape;
        switch (tool) {
            case LINE: shape = new Line2D.Double(start, end); break;
            case RECTANGLE: case FILLED_RECTANGLE: shape = new Rectangle2D.Double(x, y, w, h); break;
            case CIRCLE:
                int size = Math.max(w, h);
                x = end.x < start.x ? start.x - size : start.x;
                y = end.y < start.y ? start.y - size : start.y;
                shape = new Ellipse2D.Double(x, y, size, size); break;
            default: shape = new Ellipse2D.Double(x, y, w, h);
        }
        if (tool == DrawingTool.FILLED_RECTANGLE || tool == DrawingTool.FILLED_ELLIPSE) g.fill(shape);
        else g.draw(shape);
    }
    private void drawSegment(Point a, Point b) {
        Graphics2D g = working.createGraphics(); configure(g);
        if (a.equals(b)) g.fill(new Ellipse2D.Double(a.x - strokeWidth / 2.0, a.y - strokeWidth / 2.0, strokeWidth, strokeWidth));
        else g.draw(new Line2D.Double(a, b));
        g.dispose();
    }
    private Point bounded(MouseEvent e) {
        return new Point(Math.max(0, Math.min(documentWidth() - 1, e.getX())), Math.max(0, Math.min(documentHeight() - 1, e.getY())));
    }
    @Override public void mousePressed(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e) || e.getX() < 0 || e.getY() < 0 || e.getX() >= documentWidth() || e.getY() >= documentHeight()) return;
        start = bounded(e); end = start; previous = start;
        if (tool == DrawingTool.PENCIL || tool == DrawingTool.ERASER) { working = copy(image()); drawSegment(start, start); }
        repaint();
    }
    @Override public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
        if (start == null) return;
        end = bounded(e);
        if (working != null) { drawSegment(previous, end); previous = end; }
        repaint();
    }
    @Override public void mouseReleased(MouseEvent e) {
        if (start == null || !SwingUtilities.isLeftMouseButton(e)) return;
        end = bounded(e);
        BufferedImage result;
        if (working != null) { drawSegment(previous, end); result = working; }
        else { result = copy(image()); Graphics2D g = result.createGraphics(); renderShape(g); g.dispose(); }
        cancelGesture(); commit(result);
    }
    @Override public void mouseMoved(MouseEvent e) { positionListener.accept(bounded(e)); }
    @Override public void mouseExited(MouseEvent e) { positionListener.accept(null); }
    @Override public void mouseEntered(MouseEvent e) { mouseMoved(e); }
    @Override public void mouseClicked(MouseEvent e) { }
}
