package leonardo.savona.sudoku.solver;

import leonardo.savona.sudoku.integration.ExternalSudokuConverter;
import leonardo.savona.sudoku.solver.model.Cell;
import leonardo.savona.sudoku.solver.model.Sudoku;

import java.util.ArrayList;
import java.util.List;

public class Chronology {

    private final List<int[][]> STEPS = new ArrayList<>();

    public void addSudoku(Sudoku sudoku) {
        if (STEPS.isEmpty() || sudokuChanged(sudoku)) {
            int size = sudoku.getSize();
            int[][] m = new int[size][size];
            for (Cell cell : sudoku.getSudoku()) {
                int r = cell.getCoordinate().getRow();
                int c = cell.getCoordinate().getColumn();
                m[r][c] = cell.getValue();
            }
            STEPS.add(m);
        }
    }

    private boolean sudokuChanged(Sudoku sudoku) {
        Sudoku lastIteration = ExternalSudokuConverter.toExternalSudoku(
                ExternalSudokuConverter.fromMatrix(STEPS.get(STEPS.size()-1))
        );
        return !sudoku.equals(lastIteration);
    }

    public List<int[][]> getSteps() {
        return new ArrayList<>(STEPS);
    }
}
