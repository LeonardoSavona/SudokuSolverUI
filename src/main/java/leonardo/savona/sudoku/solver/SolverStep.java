package leonardo.savona.sudoku.solver;

import java.util.Arrays;

public class SolverStep {

    private final int[][] matrix;
    private final Integer highlightRow;
    private final Integer highlightCol;
    private final Integer value;
    private final String description;

    public SolverStep(int[][] matrix, Integer highlightRow, Integer highlightCol, Integer value, String description) {
        this.matrix = copyMatrix(matrix);
        this.highlightRow = highlightRow;
        this.highlightCol = highlightCol;
        this.value = value;
        this.description = description;
    }

    public int[][] getMatrix() {
        return copyMatrix(matrix);
    }

    public boolean hasHighlight() {
        return highlightRow != null && highlightCol != null;
    }

    public Integer getHighlightRow() {
        return highlightRow;
    }

    public Integer getHighlightCol() {
        return highlightCol;
    }

    public Integer getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    private static int[][] copyMatrix(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = Arrays.copyOf(source[i], source[i].length);
        }
        return copy;
    }
}
