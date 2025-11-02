package leonardo.savona.sudoku.integration;

import leonardo.savona.sudoku.solver.SudokuSolver;
import leonardo.savona.sudoku.solver.model.Sudoku;
import leonardo.savona.sudoku.model.SudokuBoard;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade per lanciare il solver esterno e ottenere TUTTI i passaggi.
 */
public class ExternalSolverRunner {

    public static List<int[][]> solveAndGetSteps(SudokuBoard board) {
        try {
            // convertiamo al modello esterno
            Sudoku externalSudoku = ExternalSudokuConverter.toExternalSudoku(board);

            // lanciamo il solver esterno
            SudokuSolver solver = new SudokuSolver(externalSudoku);
            solver.solve();

            // recuperiamo tutti i passaggi
            List<int[][]> matrices = solver.getSteps();
            if (matrices.isEmpty()) {
                // in caso di nulla, almeno ritorniamo la board iniziale
                List<int[][]> single = new ArrayList<>();
                single.add(ExternalSudokuConverter.toMatrix(board));
                return single;
            }
            return matrices;
        } catch (Exception ex) {
            ex.printStackTrace();
            // in caso di errore, ritorniamo solo lo stato iniziale
            List<int[][]> single = new ArrayList<>();
            single.add(ExternalSudokuConverter.toMatrix(board));
            return single;
        }
    }
}
