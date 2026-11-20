package paint;

/** Une valeur par outil ; la clé sert à retrouver son libellé traduit. */
public enum DrawingTool {
    PENCIL("pencil"), ERASER("eraser"), LINE("line"), RECTANGLE("rectangle"),
    FILLED_RECTANGLE("filledRectangle"), ELLIPSE("ellipse"), FILLED_ELLIPSE("filledEllipse"), CIRCLE("circle");
    private final String key;
    DrawingTool(String key) { this.key = key; }
    public String key() { return "tool." + key; }
}
