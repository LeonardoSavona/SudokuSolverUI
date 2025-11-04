package leonardo.savona.sudoku.solver;

import leonardo.savona.sudoku.model.Coordinate;
import leonardo.savona.sudoku.model.Sudoku;
import leonardo.savona.sudoku.util.SudokuModelConverter;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Rappresenta un singolo passo della risoluzione del Sudoku,
 * includendo lo stato della griglia e le informazioni sul numero inserito.
 */
public class SolverStep {

    private final int[][] matrix;
    private final boolean[][][] notes;
    private final Integer row;
    private final Integer column;
    private final Integer value;
    private final String strategy;
    private final Set<Coordinate> highlightCells;

    private SolverStep(int[][] matrix,
                       boolean[][][] notes,
                       Integer row,
                       Integer column,
                       Integer value,
                       String strategy,
                       Set<Coordinate> highlightCells) {
        this.matrix = deepCopy(matrix);
        this.notes = deepCopy(notes);
        this.row = row;
        this.column = column;
        this.value = value;
        this.strategy = strategy;
        if (highlightCells == null) {
            this.highlightCells = Collections.emptySet();
        } else {
            Set<Coordinate> copy = new LinkedHashSet<>();
            for (Coordinate coordinate : highlightCells) {
                copy.add(new Coordinate(coordinate.getRow(), coordinate.getColumn()));
            }
            this.highlightCells = Collections.unmodifiableSet(copy);
        }
    }

    public static SolverStep capture(Sudoku sudoku, Integer row, Integer column, Integer value, String strategy) {
        return capture(sudoku, row, column, value, strategy, null);
    }

    public static SolverStep capture(Sudoku sudoku,
                                     Integer row,
                                     Integer column,
                                     Integer value,
                                     String strategy,
                                     Set<Coordinate> highlightCells) {
        int[][] snapshot = SudokuModelConverter.toMatrix(sudoku);
        boolean[][][] notes = SudokuModelConverter.toNotes(sudoku);
        return new SolverStep(snapshot, notes, row, column, value, strategy, highlightCells);
    }

    public static SolverStep ofState(int[][] matrix,
                                     boolean[][][] notes,
                                     Integer row,
                                     Integer column,
                                     Integer value,
                                     String strategy,
                                     Set<Coordinate> highlightCells) {
        return new SolverStep(matrix, notes, row, column, value, strategy, highlightCells);
    }

    public int[][] getMatrix() {
        return deepCopy(matrix);
    }

    public boolean[][][] getNotes() {
        return deepCopy(notes);
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

    public Set<Coordinate> getHighlightCells() {
        if (highlightCells.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Coordinate> copy = new LinkedHashSet<>();
        for (Coordinate coordinate : highlightCells) {
            copy.add(new Coordinate(coordinate.getRow(), coordinate.getColumn()));
        }
        return copy;
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

    private static boolean[][][] deepCopy(boolean[][][] source) {
        if (source == null) {
            return null;
        }
        boolean[][][] copy = new boolean[source.length][][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = new boolean[source[i].length][];
            for (int j = 0; j < source[i].length; j++) {
                copy[i][j] = source[i][j] == null ? null : Arrays.copyOf(source[i][j], source[i][j].length);
            }
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
