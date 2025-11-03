package leonardo.savona.sudoku.ocr;

import leonardo.savona.sudoku.solver.model.Sudoku;

import java.awt.image.BufferedImage;

/**
 * Importer da immagine allineata.
 * - 1° pass: riconosce tutte le celle e fa "learn" dai risultati molto sicuri
 * - 2° pass: INSERISCE i numeri provando i candidati in ordine di score
 *            e scegliendo il primo che NON viola le regole del sudoku.
 *            Se nessuno dei candidati va bene, mette comunque il migliore
 *            ma lo marca come "lowConfidence".
 */
public class AssistedSudokuImporter {

    // sotto questa soglia lo evidenziamo in giallo
    private static final double LOW_CONFIDENCE_SCORE = 0.55;

    private final SimpleDigitRecognizer recognizer = new SimpleDigitRecognizer();

    public RecognizedSudoku importSudoku(BufferedImage alignedImage) {
        int size = alignedImage.getWidth();
        int cellSize = size / 9;

        Sudoku board = new Sudoku();
        SimpleDigitRecognizer.Result[][] results = new SimpleDigitRecognizer.Result[9][9];
        boolean[][] lowConfidence = new boolean[9][9];

        // 1) primo pass: riconoscimento + learning
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int x = c * cellSize;
                int y = r * cellSize;
                int margin = (int) (cellSize * 0.15);
                int w = cellSize - margin * 2;
                int h = cellSize - margin * 2;
                if (x + margin + w > alignedImage.getWidth()) continue;
                if (y + margin + h > alignedImage.getHeight()) continue;

                BufferedImage cell = alignedImage.getSubimage(x + margin, y + margin, w, h);
                SimpleDigitRecognizer.Result res = recognizer.recognize(cell);
                results[r][c] = res;

                // se molto sicuro → impariamo
                if (res.digit != 0 && res.score >= 0.80 && res.normalizedImage != null) {
                    recognizer.learn(res.digit, res.normalizedImage);
                }
            }
        }

        // 2) secondo pass sudoku-aware
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                SimpleDigitRecognizer.Result res = results[r][c];
                if (res == null) continue;

                // proprio bianco → skip
                if (res.digit == 0 && res.blackRatio < 0.015) {
                    continue;
                }

                int chosenDigit = 0;
                boolean chosenLow = false;

                if (res.candidates != null && !res.candidates.isEmpty()) {
                    boolean placed = false;
                    for (SimpleDigitRecognizer.Candidate cand : res.candidates) {
                        int d = cand.digit;
                        if (d == 0) continue;
                        if (!violatesSudoku(board, r, c, d)) {
                            chosenDigit = d;
                            chosenLow = (cand.score < LOW_CONFIDENCE_SCORE);
                            placed = true;
                            break;
                        }
                    }
                    if (!placed) {
                        // nessuna candidata compatibile → metti la migliore ma segna low
                        SimpleDigitRecognizer.Candidate best = res.candidates.get(0);
                        if (best.digit != 0) {
                            chosenDigit = best.digit;
                            chosenLow = true;
                        }
                    }
                } else {
                    // non aveva list di candidati ma aveva digit
                    if (res.digit != 0) {
                        if (!violatesSudoku(board, r, c, res.digit)) {
                            chosenDigit = res.digit;
                            chosenLow = (res.score < LOW_CONFIDENCE_SCORE);
                        } else {
                            chosenDigit = res.digit;
                            chosenLow = true;
                        }
                    }
                }

                if (chosenDigit != 0) {
                    board.setValue(r, c, chosenDigit, true);
                    if (chosenLow) {
                        lowConfidence[r][c] = true;
                    }
                }
            }
        }

        return new RecognizedSudoku(board, results, lowConfidence);
    }

    private boolean violatesSudoku(Sudoku board, int row, int col, int value) {
        if (value == 0) return false;
        return !board.isValueAllowed(row, col, value);
    }

    public static class RecognizedSudoku {
        public final Sudoku board;
        public final SimpleDigitRecognizer.Result[][] results;
        public final boolean[][] lowConfidence;

        public RecognizedSudoku(Sudoku board,
                                SimpleDigitRecognizer.Result[][] results,
                                boolean[][] lowConfidence) {
            this.board = board;
            this.results = results;
            this.lowConfidence = lowConfidence;
        }
    }
}
