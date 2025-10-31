package leonardo.savona.sudoku.ocr;

import leonardo.savona.sudoku.model.SudokuBoard;

import java.awt.image.BufferedImage;

public class AssistedSudokuImporter {

    private final SimpleDigitRecognizer recognizer = new SimpleDigitRecognizer();

    /**
     * @param alignedImage immagine QUADRATA già allineata (quella che ci dà il dialog)
     */
    public SudokuBoard importSudoku(BufferedImage alignedImage) {
        int size = alignedImage.getWidth(); // = height
        int cellSize = size / 9;

        SudokuBoard board = new SudokuBoard();

        // usiamo solo gray
        BufferedImage gray = BinarizationUtils.toGrayscale(alignedImage);

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int x = c * cellSize;
                int y = r * cellSize;

                int margin = (int) (cellSize * 0.15);
                int w = cellSize - margin * 2;
                int h = cellSize - margin * 2;

                if (x + margin + w > gray.getWidth()) continue;
                if (y + margin + h > gray.getHeight()) continue;

                BufferedImage cell = gray.getSubimage(x + margin, y + margin, w, h);

                int digit = recognizer.recognize(cell);
                if (digit != 0) {
                    board.setValue(r, c, digit, true);
                }
            }
        }
        return board;
    }
}
