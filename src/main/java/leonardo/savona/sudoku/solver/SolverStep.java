package leonardo.savona.sudoku.solver;

import leonardo.savona.sudoku.model.Cell;
import leonardo.savona.sudoku.model.Sudoku;

import java.util.Arrays;

/**
 * Rappresenta un singolo passo della risoluzione del Sudoku,
 * includendo lo stato della griglia e le informazioni sul numero inserito.
 */
public class SolverStep {

    private final int[][] matrix;
    private final Integer row;
    private final Integer column;
    private final Integer value;
    private final String strategy;

    private SolverStep(int[][] matrix, Integer row, Integer column, Integer value, String strategy) {
        this.matrix = deepCopy(matrix);
        this.row = row;
        this.column = column;
        this.value = value;
        this.strategy = strategy;
    }

    public static SolverStep capture(Sudoku sudoku, Integer row, Integer column, Integer value, String strategy) {
        int size = sudoku.getSize();
        int[][] snapshot = new int[size][size];
        for (Cell cell : sudoku.getSudoku()) {
            int r = cell.getCoordinate().getRow();
            int c = cell.getCoordinate().getColumn();
            snapshot[r][c] = cell.getValue();
        }
        return new SolverStep(snapshot, row, column, value, strategy);
    }

    public static SolverStep ofMatrix(int[][] matrix, Integer row, Integer column, Integer value, String strategy) {
        return new SolverStep(matrix, row, column, value, strategy);
    }

    public int[][] getMatrix() {
        return deepCopy(matrix);
    }

    public Integer getRow() {
        return row;
    }

    public Integer getColumn() {
        return column;
    }

    public Integer getValue() {
        return value;
    }

    public String getStrategy() {
        return strategy;
    }

    public boolean hasSameMatrix(SolverStep other) {
        if (other == null) {
            return false;
        }
        return matricesEqual(this.matrix, other.matrix);
    }

    private static int[][] deepCopy(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = Arrays.copyOf(source[i], source[i].length);
        }
        return copy;
    }

    private static boolean matricesEqual(int[][] a, int[][] b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (!Arrays.equals(a[i], b[i])) {
                return false;
            }
        }
        return true;
    }
}
