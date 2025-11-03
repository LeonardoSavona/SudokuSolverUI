package leonardo.savona.sudoku.solver;

import leonardo.savona.sudoku.solver.model.Cell;
import leonardo.savona.sudoku.solver.model.Sudoku;

import java.util.ArrayList;
import java.util.List;

public class Chronology {

    private final List<SolverStep> steps = new ArrayList<>();
    private int[][] lastMatrix = null;

    public void addSudoku(Sudoku sudoku) {
        int size = sudoku.getSize();
        int[][] matrix = new int[size][size];
        for (Cell cell : sudoku.getSudoku()) {
            int r = cell.getCoordinate().getRow();
            int c = cell.getCoordinate().getColumn();
            matrix[r][c] = cell.getValue();
        }

        if (lastMatrix == null || !matricesEqual(lastMatrix, matrix)) {
            SolverStep step = buildStep(matrix);
            steps.add(step);
            lastMatrix = copyMatrix(matrix);
        }
    }

    private SolverStep buildStep(int[][] matrix) {
        if (lastMatrix == null) {
            return new SolverStep(matrix, null, null, null, "Stato iniziale");
        }

        Integer row = null;
        Integer col = null;
        Integer value = null;
        outer:
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[r].length; c++) {
                if (matrix[r][c] != lastMatrix[r][c]) {
                    row = r;
                    col = c;
                    value = matrix[r][c];
                    break outer;
                }
            }
        }

        String description;
        if (row != null && col != null && value != null && value > 0) {
            description = String.format("Impostato %d in r%d c%d", value, row + 1, col + 1);
        } else if (row != null && col != null && (value == null || value == 0)) {
            description = String.format("Svuotata r%d c%d", row + 1, col + 1);
        } else {
            description = "Aggiornamento";
        }

        return new SolverStep(matrix, row, col, value, description);
    }

    private boolean matricesEqual(int[][] a, int[][] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i].length != b[i].length) return false;
            for (int j = 0; j < a[i].length; j++) {
                if (a[i][j] != b[i][j]) return false;
            }
        }
        return true;
    }

    private int[][] copyMatrix(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i].clone();
        }
        return copy;
    }

    public List<SolverStep> getSteps() {
        return new ArrayList<>(steps);
    }
}
