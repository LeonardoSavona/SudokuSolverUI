package leonardo.savona.sudoku.model;

public class SudokuBoard {

    public static final int SIZE = 9;

    private final int[][] values = new int[SIZE][SIZE];     // 0 = vuoto
    private final boolean[][] fixed = new boolean[SIZE][SIZE];
    private final NoteSet[][] notes = new NoteSet[SIZE][SIZE];

    public SudokuBoard() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                notes[r][c] = new NoteSet();
            }
        }
    }

    public int getValue(int row, int col) {
        checkBounds(row, col);
        return values[row][col];
    }

    public boolean isFixed(int row, int col) {
        checkBounds(row, col);
        return fixed[row][col];
    }

    public NoteSet getNotes(int row, int col) {
        checkBounds(row, col);
        return notes[row][col];
    }

    /** Imposta un valore: se value==0, la cella NON è più fixed. */
    public void setValue(int row, int col, int value, boolean fixedValue) {
        checkBounds(row, col);
        if (value < 0 || value > 9) throw new IllegalArgumentException("Valore non valido: " + value);
        values[row][col] = value;
        if (value == 0) {
            fixed[row][col] = false;
        } else {
            fixed[row][col] = fixedValue;
            notes[row][col].clear();
        }
    }

    /** Inserimento “utente” in risoluzione (non fixed). */
    public void setValue(int row, int col, int value) {
        setValue(row, col, value, false);
    }

    public void clearValue(int row, int col) {
        checkBounds(row, col);
        if (fixed[row][col]) return;
        values[row][col] = 0;
    }

    public boolean isCellEmpty(int row, int col) {
        return getValue(row, col) == 0;
    }

    public boolean isValueAllowed(int row, int col, int value) {
        if (value == 0) return true;
        for (int c = 0; c < SIZE; c++) if (c != col && values[row][c] == value) return false;
        for (int r = 0; r < SIZE; r++) if (r != row && values[r][col] == value) return false;
        int br = (row / 3) * 3, bc = (col / 3) * 3;
        for (int r = br; r < br + 3; r++)
            for (int c = bc; c < bc + 3; c++)
                if ((r != row || c != col) && values[r][c] == value) return false;
        return true;
    }

    public boolean isCellInConflict(int row, int col) {
        int v = getValue(row, col);
        if (v == 0) return false;
        return !isValueAllowed(row, col, v);
    }

    public void toggleNote(int row, int col, int number) {
        checkBounds(row, col);
        if (fixed[row][col]) return;
        notes[row][col].toggleNote(number);
    }

    public void clearNotes(int row, int col) {
        checkBounds(row, col);
        notes[row][col].clear();
    }

    private void checkBounds(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE)
            throw new IndexOutOfBoundsException("Cell out of bounds: " + row + "," + col);
    }
}
