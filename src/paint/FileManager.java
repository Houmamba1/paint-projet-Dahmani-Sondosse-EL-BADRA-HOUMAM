package paint;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.Iterator;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;

/** Lecture contrôlée et écriture PNG avec remplacement seulement après succès. */
public final class FileManager {
    private FileManager() { }
    public static BufferedImage read(File file) throws IOException {
        try (ImageInputStream stream = ImageIO.createImageInputStream(file)) {
            if (stream == null) throw new IOException("format");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new IOException("format");
            ImageReader reader = readers.next();
            try {
                reader.setInput(stream, true, true);
                int w = reader.getWidth(0), h = reader.getHeight(0);
                try { DrawingPanel.validateSize(w, h); }
                catch (IllegalArgumentException ex) { throw new IOException("size", ex); }
                BufferedImage image = reader.read(0);
                if (image == null) throw new IOException("format");
                return image;
            } finally { reader.dispose(); }
        }
    }
    public static File pngFile(File file) {
        return file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".png") ? file : new File(file.getAbsolutePath() + ".png");
    }
    public static void writePng(BufferedImage image, File file) throws IOException {
        Path destination = file.toPath().toAbsolutePath();
        Path temporary = Files.createTempFile(destination.getParent(), ".paint-", ".png");
        try {
            if (!ImageIO.write(image, "png", temporary.toFile())) throw new IOException("format");
            try { Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
}
