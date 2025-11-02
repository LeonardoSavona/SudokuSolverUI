package leonardo.savona.sudoku.ocr;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Riconoscitore di cifre "manuale" con:
 * - normalizzazione via bounding box
 * - più font
 * - dilation/erosion
 * - micro-shift nel confronto
 * - score combinato (IoU neri + pixel match + profilo)
 * - top 3 candidati
 * - learning runtime di template (dallo stesso sudoku)
 */
public class SimpleDigitRecognizer {

    private static final int CANVAS = 56;
    private static final int PREPROCESS_SIZE = 120;
    private static final int PADDING = 4;

    // soglie
    private static final double EMPTY_BLACK_RATIO = 0.05;      // sotto -> vuota
    private static final double FORCE_DIGIT_BLACK_RATIO = 0.015; // sopra -> metti qualcosa
    private static final double SCORE_THRESHOLD = 0.55;
    public static final double AREA_RATIO = 0.005;

    // template "fissi"
    private final Map<Integer, List<BufferedImage>> baseTemplates = new HashMap<>();
    // template imparati a runtime
    private final Map<Integer, List<BufferedImage>> learnedTemplates = new HashMap<>();

    public SimpleDigitRecognizer() {
        String[] fontNames = {"SansSerif", "Dialog", "Serif"};
        int[] fontSizes = {36, 40, 44, 48};

        for (int d = 1; d <= 9; d++) {
            List<BufferedImage> list = new ArrayList<>();
            for (String fn : fontNames) {
                for (int fs : fontSizes) {
                    list.add(generateTemplate(d, fn, fs));
                }
            }
            baseTemplates.put(d, list);
            learnedTemplates.put(d, new ArrayList<>());
        }
    }

    private BufferedImage generateTemplate(int digit, String fontName, int fontSize) {
        BufferedImage img = new BufferedImage(CANVAS, CANVAS, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, CANVAS, CANVAS);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font(fontName, Font.BOLD, fontSize));

        String s = String.valueOf(digit);
        FontMetrics fm = g2.getFontMetrics();
        int x = (CANVAS - fm.stringWidth(s)) / 2;
        int y = (CANVAS - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(s, x, y);
        g2.dispose();
        return img;
    }

    public Result recognize(BufferedImage cell) {
        // 1) upscala
        BufferedImage up = BinarizationUtils.resize(cell, PREPROCESS_SIZE, PREPROCESS_SIZE);
        // 2) binarizza
        BufferedImage bin = BinarizationUtils.toBinaryOtsu(up);
        // 3) bbox
        Rectangle box = findBlackBoundingBox(bin);
        if (box == null) {
            return Result.empty();
        }

        double rawBlackRatio = computeBlackRatio(bin);
        if (rawBlackRatio < EMPTY_BLACK_RATIO) {
            return Result.empty();
        }

        double areaRatio = (box.width * box.height) / (double) (PREPROCESS_SIZE * PREPROCESS_SIZE);
        if (areaRatio < AREA_RATIO) {
            return Result.empty();
        }

        // 4) normalizza su canvas
        BufferedImage normalized = normalizeToCanvas(bin, box, CANVAS, PADDING);

        // 5) varianti di spessore
        List<BufferedImage> variants = new ArrayList<>();
        variants.add(normalized);
        variants.add(dilate(normalized));
        variants.add(erode(normalized));

        // 6) confronta con tutti i template
        List<Candidate> allCandidates = new ArrayList<>();

        for (int d = 1; d <= 9; d++) {
            List<BufferedImage> tmplList = new ArrayList<>(baseTemplates.get(d));
            tmplList.addAll(learnedTemplates.get(d));

            double bestForDigit = -1.0;

            for (BufferedImage var : variants) {
                for (BufferedImage tmpl : tmplList) {
                    // micro-shift ±1
                    double bestShiftScore = -1.0;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            double score = compareWithShifts(var, tmpl, dx, dy);
                            if (score > bestShiftScore) {
                                bestShiftScore = score;
                            }
                        }
                    }
                    if (bestShiftScore > bestForDigit) {
                        bestForDigit = bestShiftScore;
                    }
                }
            }

            allCandidates.add(new Candidate(d, bestForDigit));
        }

        allCandidates.sort((a, b) -> Double.compare(b.score, a.score));

        Candidate best = allCandidates.get(0);
        Candidate second = allCandidates.size() > 1 ? allCandidates.get(1) : null;
        Candidate third = allCandidates.size() > 2 ? allCandidates.get(2) : null;

        int digit;
        boolean forced = false;
        if (rawBlackRatio >= FORCE_DIGIT_BLACK_RATIO) {
            digit = best.digit;
            forced = true;
        } else {
            digit = (best.score >= SCORE_THRESHOLD) ? best.digit : 0;
        }

        Result r = new Result();
        r.digit = digit;
        r.score = best.score;
        r.blackRatio = rawBlackRatio;
        r.normalizedImage = normalized;
        r.forced = forced;
        r.candidates = new ArrayList<>();
        r.candidates.add(best);
        if (second != null) r.candidates.add(second);
        if (third != null) r.candidates.add(third);
        return r;
    }

    public void learn(int digit, BufferedImage normalizedDigit) {
        if (digit < 1 || digit > 9) return;
        learnedTemplates.get(digit).add(normalizedDigit);
    }

    // ------------------------------------------------------------
    // utils
    // ------------------------------------------------------------

    private Rectangle findBlackBoundingBox(BufferedImage bin) {
        int w = bin.getWidth();
        int h = bin.getHeight();
        int minX = w, minY = h, maxX = -1, maxY = -1;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = bin.getRGB(x, y) & 0xFFFFFF;
                if (rgb == 0x000000) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        if (maxX == -1) return null;
        return new Rectangle(minX, minY, (maxX - minX + 1), (maxY - minY + 1));
    }

    private BufferedImage normalizeToCanvas(BufferedImage src, Rectangle box, int canvasSize, int padding) {
        BufferedImage out = new BufferedImage(canvasSize, canvasSize, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = out.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, canvasSize, canvasSize);

        int avail = canvasSize - padding * 2;
        int bw = box.width;
        int bh = box.height;
        double scale = avail / (double) Math.max(bw, bh);

        int drawW = (int) Math.round(bw * scale);
        int drawH = (int) Math.round(bh * scale);
        int dx = (canvasSize - drawW) / 2;
        int dy = (canvasSize - drawH) / 2;

        g2.drawImage(
                src,
                dx, dy, dx + drawW, dy + drawH,
                box.x, box.y, box.x + box.width, box.y + box.height,
                null
        );
        g2.dispose();
        return out;
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

    private double compareWithShifts(BufferedImage a, BufferedImage b, int shiftX, int shiftY) {
        int w = a.getWidth();
        int h = a.getHeight();
        int inter = 0;
        int union = 0;
        int eq = 0;
        int[] rowCountA = new int[h];
        int[] rowCountB = new int[h];
        int[] colCountA = new int[w];
        int[] colCountB = new int[w];

        for (int y = 0; y < h; y++) {
            int yy = y + shiftY;
            for (int x = 0; x < w; x++) {
                int xx = x + shiftX;

                boolean ba = false;
                boolean bb;

                if (yy >= 0 && yy < h && xx >= 0 && xx < w) {
                    ba = (a.getRGB(xx, yy) & 0xFFFFFF) == 0x000000;
                }
                bb = (b.getRGB(x, y) & 0xFFFFFF) == 0x000000;

                if (ba && bb) inter++;
                if (ba || bb) union++;
                if ((yy >= 0 && yy < h && xx >= 0 && xx < w)
                        && ((a.getRGB(xx, yy) & 0xFFFFFF) == (b.getRGB(x, y) & 0xFFFFFF))) {
                    eq++;
                }

                if (ba) {
                    rowCountA[y]++;
                    colCountA[x]++;
                }
                if (bb) {
                    rowCountB[y]++;
                    colCountB[x]++;
                }
            }
        }

        double iou = (union == 0) ? 0.0 : inter / (double) union;
        double pix = eq / (double) (w * h);
        double profile = compareProfiles(rowCountA, rowCountB, colCountA, colCountB, w, h);

        return 0.55 * iou + 0.25 * pix + 0.20 * profile;
    }

    private double compareProfiles(int[] rowA, int[] rowB, int[] colA, int[] colB, int w, int h) {
        double rowScore = 0.0;
        for (int i = 0; i < h; i++) {
            int a = rowA[i];
            int b = rowB[i];
            int max = Math.max(a, b);
            if (max == 0) {
                rowScore += 1.0;
            } else {
                rowScore += 1.0 - Math.abs(a - b) / (double) max;
            }
        }
        rowScore /= h;

        double colScore = 0.0;
        for (int i = 0; i < w; i++) {
            int a = colA[i];
            int b = colB[i];
            int max = Math.max(a, b);
            if (max == 0) {
                colScore += 1.0;
            } else {
                colScore += 1.0 - Math.abs(a - b) / (double) max;
            }
        }
        colScore /= w;

        return (rowScore + colScore) / 2.0;
    }

    private BufferedImage dilate(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = out.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);
        g2.dispose();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                boolean black = false;
                for (int yy = y - 1; yy <= y + 1 && !black; yy++) {
                    for (int xx = x - 1; xx <= x + 1 && !black; xx++) {
                        if (xx >= 0 && xx < w && yy >= 0 && yy < h) {
                            if ((src.getRGB(xx, yy) & 0xFFFFFF) == 0x000000) {
                                black = true;
                            }
                        }
                    }
                }
                out.setRGB(x, y, black ? 0x000000 : 0xFFFFFF);
            }
        }
        return out;
    }

    private BufferedImage erode(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = out.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);
        g2.dispose();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                boolean allBlack = true;
                for (int yy = y - 1; yy <= y + 1 && allBlack; yy++) {
                    for (int xx = x - 1; xx <= x + 1 && allBlack; xx++) {
                        if (xx < 0 || xx >= w || yy < 0 || yy >= h ||
                                (src.getRGB(xx, yy) & 0xFFFFFF) != 0x000000) {
                            allBlack = false;
                        }
                    }
                }
                out.setRGB(x, y, allBlack ? 0x000000 : 0xFFFFFF);
            }
        }
        return out;
    }

    // ------------------------------------------------------------
    // helper classes
    // ------------------------------------------------------------

    public static class Candidate {
        public final int digit;
        public final double score;

        public Candidate(int digit, double score) {
            this.digit = digit;
            this.score = score;
        }
    }

    public static class Result {
        public int digit;
        public double score;
        public double blackRatio;
        public boolean forced;
        public BufferedImage normalizedImage;
        public List<Candidate> candidates;

        public static Result empty() {
            Result r = new Result();
            r.digit = 0;
            r.score = 0;
            r.blackRatio = 0;
            r.forced = false;
            r.candidates = Collections.emptyList();
            return r;
        }
    }
}
