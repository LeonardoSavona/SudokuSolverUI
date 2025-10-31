package leonardo.savona.sudoku.ocr;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BinarizationUtils {

    public static BufferedImage toGrayscale(BufferedImage src) {
        BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = gray.getGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return gray;
    }

    public static BufferedImage toBinaryOtsu(BufferedImage srcGray) {
        int w = srcGray.getWidth();
        int h = srcGray.getHeight();
        int[] hist = new int[256];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int v = srcGray.getRaster().getSample(x, y, 0);
                hist[v]++;
            }
        }

        int total = w * h;
        float sum = 0;
        for (int t = 0; t < 256; t++) sum += t * hist[t];

        float sumB = 0;
        int wB = 0;
        int wF;
        float varMax = 0;
        int threshold = 0;

        for (int t = 0; t < 256; t++) {
            wB += hist[t];
            if (wB == 0) continue;
            wF = total - wB;
            if (wF == 0) break;

            sumB += (float) (t * hist[t]);

            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }

        BufferedImage bin = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int v = srcGray.getRaster().getSample(x, y, 0);
                int val = (v > threshold) ? 0xFFFFFF : 0x000000;
                bin.setRGB(x, y, val);
            }
        }
        return bin;
    }

    public static BufferedImage resize(BufferedImage src, int newW, int newH) {
        BufferedImage out = new BufferedImage(newW, newH, src.getType());
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, newW, newH, null);
        g2.dispose();
        return out;
    }
}
