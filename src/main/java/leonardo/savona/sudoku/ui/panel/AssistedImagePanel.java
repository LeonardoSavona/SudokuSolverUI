package leonardo.savona.sudoku.ui.panel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Pannello che mostra un'immagine e sopra una griglia 9x9.
 * - al primo paint adatta l'immagine al pannello
 * - mouse drag: sposta
 * - rotellina: zoom in/out
 * - tasto destro: reset
 * - exportAlignedImage(...) esporta l'area dell'overlay, non più fissa a 900x900
 */
public class AssistedImagePanel extends JPanel {

    private BufferedImage image;
    private double scale = 1.0;
    private int offsetX = 0;
    private int offsetY = 0;

    private Point lastDrag;
    private boolean fitInitialized = false;   // per fare il fit solo la prima volta

    public AssistedImagePanel(BufferedImage image) {
        this.image = image;
        setBackground(Color.DARK_GRAY);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastDrag = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastDrag != null) {
                    int dx = e.getX() - lastDrag.x;
                    int dy = e.getY() - lastDrag.y;
                    offsetX += dx;
                    offsetY += dy;
                    lastDrag = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastDrag = null;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    resetView();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int rot = e.getWheelRotation();
                double factor = (rot < 0) ? 1.1 : 0.9;
                double oldScale = scale;
                scale *= factor;
                if (scale < 0.1) scale = 0.1;
                if (scale > 8) scale = 8;

                // zoom attorno al mouse
                double relX = (e.getX() - offsetX) / oldScale;
                double relY = (e.getY() - offsetY) / oldScale;
                offsetX = (int) (e.getX() - relX * scale);
                offsetY = (int) (e.getY() - relY * scale);

                repaint();
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    public void setImage(BufferedImage img) {
        this.image = img;
        this.fitInitialized = false; // rifare il fit
        repaint();
    }

    public void resetView() {
        fitInitialized = false;
        repaint();
    }

    /**
     * Ritorna la dimensione (in px) del quadrato overlay che disegniamo
     * = lato minimo del pannello.
     */
    public int getOverlaySize() {
        return Math.min(getWidth(), getHeight());
    }

    /**
     * Esporta l'area dell'overlay (quella verde) alle dimensioni dell'overlay.
     * Se l'immagine è più piccola non la forziamo a 900, usiamo l'overlay.
     */
    public BufferedImage exportAlignedImage() {
        int size = getOverlaySize();
        if (size <= 0) size = 600; // fallback

        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2 = out.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, size, size);

        if (image != null) {
            // disegniamo l'immagine con lo stesso offset/scale che vediamo nel pannello
            int drawW = (int) (image.getWidth() * scale);
            int drawH = (int) (image.getHeight() * scale);

            // qui però il pannello era più grande: dobbiamo spostare il sistema di riferimento
            // nel pannello l'overlay è centrato → prendiamo metà differenza
            int panelOverlay = getOverlaySize();
            int panelOx = (getWidth() - panelOverlay) / 2;
            int panelOy = (getHeight() - panelOverlay) / 2;

            // quello che l'utente vede nell'overlay è:
            // - immagine a (offsetX, offsetY)
            // - overlay a (panelOx, panelOy)
            // quindi per esportare dobbiamo "traslare indietro" di panelOx/panelOy
            int drawX = offsetX - panelOx;
            int drawY = offsetY - panelOy;

            g2.drawImage(image, drawX, drawY, drawW, drawH, null);
        }

        g2.dispose();
        return out;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        // se è la prima volta e abbiamo un'immagine, facciamo il fit al pannello
        if (!fitInitialized && image != null && getWidth() > 0 && getHeight() > 0) {
            double sx = getWidth() / (double) image.getWidth();
            double sy = getHeight() / (double) image.getHeight();
            // vogliamo che si veda tutta → prendiamo il min
            this.scale = Math.min(sx, sy);
            // centriamo
            int imgW = (int) (image.getWidth() * scale);
            int imgH = (int) (image.getHeight() * scale);
            this.offsetX = (getWidth() - imgW) / 2;
            this.offsetY = (getHeight() - imgH) / 2;
            fitInitialized = true;
        }

        // disegna immagine
        if (image != null) {
            int w = (int) (image.getWidth() * scale);
            int h = (int) (image.getHeight() * scale);
            g2.drawImage(image, offsetX, offsetY, w, h, null);
        }

        // overlay 9x9 centrato
        int size = getOverlaySize();
        int ox = (getWidth() - size) / 2;
        int oy = (getHeight() - size) / 2;

        // bordo
        g2.setColor(new Color(0, 255, 0, 160));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(ox, oy, size, size);

        // linee
        for (int i = 1; i < 9; i++) {
            int p = oy + i * size / 9;
            int q = ox + i * size / 9;
            g2.setColor((i % 3 == 0) ? new Color(0, 200, 0, 180) : new Color(0, 255, 0, 120));
            g2.drawLine(ox, p, ox + size, p);
            g2.drawLine(q, oy, q, oy + size);
        }

        g2.dispose();
    }
}
