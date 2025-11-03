package leonardo.savona.sudoku.solver;

import leonardo.savona.sudoku.solver.model.Coordinate;

import java.util.Arrays;
import java.util.Objects;

public class SolverStep {

    private final int[][] matrix;
    private final Coordinate coordinate;
    private final int value;
    private final String strategyName;

    public SolverStep(int[][] matrix, Coordinate coordinate, int value, String strategyName) {
        this.matrix = copyMatrix(matrix);
        this.coordinate = coordinate;
        this.value = value;
        this.strategyName = strategyName;
    }

    public int[][] getMatrix() {
        return copyMatrix(matrix);
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public int getValue() {
        return value;
    }

    public String getStrategyName() {
        return strategyName;
    }

    private int[][] copyMatrix(int[][] source) {
        if (source == null) {
            return new int[9][9];
        }
        int rows = source.length;
        int[][] copy = new int[rows][];
        for (int r = 0; r < rows; r++) {
            copy[r] = Arrays.copyOf(source[r], source[r].length);
        }
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SolverStep)) return false;
        SolverStep that = (SolverStep) o;
        return value == that.value &&
                Objects.equals(coordinate, that.coordinate) &&
                Objects.equals(strategyName, that.strategyName) &&
                matricesEqual(matrix, that.matrix);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(coordinate, value, strategyName);
        result = 31 * result + Arrays.deepHashCode(matrix);
        return result;
    }

    private boolean matricesEqual(int[][] m1, int[][] m2) {
        if (m1 == m2) return true;
        if (m1 == null || m2 == null) return false;
        if (m1.length != m2.length) return false;
        for (int r = 0; r < m1.length; r++) {
            if (!Arrays.equals(m1[r], m2[r])) {
                return false;
            }
        }
        return true;
    }
}
