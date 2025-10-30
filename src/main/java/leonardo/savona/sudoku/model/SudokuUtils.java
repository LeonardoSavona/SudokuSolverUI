package leonardo.savona.sudoku.model;

public final class SudokuUtils {
    private SudokuUtils() {}

    public static boolean isComplete(SudokuBoard board) {
        for (int r = 0; r < SudokuBoard.SIZE; r++) {
            for (int c = 0; c < SudokuBoard.SIZE; c++) {
                if (board.getValue(r, c) == 0) return false;
                if (board.isCellInConflict(r, c)) return false;
            }
        }
        return true;
    }

    /** True se esiste almeno una cella in conflitto. */
    public static boolean hasConflicts(SudokuBoard board) {
        for (int r = 0; r < SudokuBoard.SIZE; r++) {
            for (int c = 0; c < SudokuBoard.SIZE; c++) {
                int v = board.getValue(r, c);
                if (v != 0 && !board.isValueAllowed(r, c, v)) return true;
            }
        }
        return false;
    }

    public static boolean hasAnyNumber(SudokuBoard board) {
        for (int r = 0; r < SudokuBoard.SIZE; r++)
            for (int c = 0; c < SudokuBoard.SIZE; c++)
                if (board.getValue(r, c) != 0) return true;
        return false;
    }
}
