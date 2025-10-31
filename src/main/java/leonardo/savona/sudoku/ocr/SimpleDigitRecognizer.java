package leonardo.savona.sudoku.ocr;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class SimpleDigitRecognizer {

    private static final int SIZE = 56;
    private final Map<Integer, BufferedImage> templates = new HashMap<>();

    public SimpleDigitRecognizer() {
        // generiamo i template 1..9
        for (int d = 1; d <= 9; d++) {
            templates.put(d, generateTemplate(d));
        }
    }

    private BufferedImage generateTemplate(int digit) {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, SIZE, SIZE);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, 44));
        String s = String.valueOf(digit);
        FontMetrics fm = g2.getFontMetrics();
        int x = (SIZE - fm.stringWidth(s)) / 2;
        int y = (SIZE - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(s, x, y);
        g2.dispose();
        return img;
    }

    /**
     * @param cell immagine binaria (nero/bianco) della cella
     * @return numero 0..9 (0 = vuota / non riconosciuta)
     */
    public int recognize(BufferedImage cell) {
        // portiamo a 28x28
        BufferedImage bin = BinarizationUtils.toBinaryOtsu(BinarizationUtils.resize(cell, SIZE, SIZE));
        // se la cella ha troppo poco nero → la consideriamo vuota
        double blackRatio = computeBlackRatio(bin);
        if (blackRatio < 0.02) { // 2% di nero
            return 0;
        }

        double bestScore = -1;
        int bestDigit = 0;
        for (int d = 1; d <= 9; d++) {
            BufferedImage tmpl = templates.get(d);
            double score = compare(bin, tmpl);
            if (score > bestScore) {
                bestScore = score;
                bestDigit = d;
            }
        }

        // se il punteggio è basso, meglio dire 0
        if (bestScore < 0.4) {
            return 0;
        }
        return bestDigit;
    }

    private double computeBlackRatio(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int black = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y) & 0xFFFFFF;
                if (rgb == 0x000000) black++;
            }
        }
        return black / (double) (w * h);
    }

    /**
     * Confronto semplicissimo: quante volte ho neronero o biancobianco
     */
    private double compare(BufferedImage a, BufferedImage b) {
        int w = a.getWidth();
        int h = a.getHeight();
        int same = 0;
        int total = w * h;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int pa = a.getRGB(x, y) & 0xFFFFFF;
                int pb = b.getRGB(x, y) & 0xFFFFFF;
                if (pa == pb) same++;
            }
        }
        return same / (double) total;
    }
}
