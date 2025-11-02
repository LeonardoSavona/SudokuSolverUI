package leonardo.savona.sudoku.solver;

import leonardo.savona.sudoku.solver.model.Cell;
import leonardo.savona.sudoku.solver.model.Sudoku;
import leonardo.savona.sudoku.solver.strategy.SquaresStrategy;
import leonardo.savona.sudoku.solver.strategy.advanced.XWingStrategy;
import leonardo.savona.sudoku.solver.strategy.candidates.CoupleOfCandidatesStrategy;
import leonardo.savona.sudoku.solver.strategy.candidates.HiddenCoupleOfCandidatesStrategy;
import leonardo.savona.sudoku.solver.strategy.candidates.TrioOfCandidatesStrategy;
import leonardo.savona.sudoku.solver.strategy.cellbased.BasicStrategy;
import leonardo.savona.sudoku.solver.strategy.cellbased.PossibleValuesStrategy;

import java.util.List;

public class SudokuSolver {

    private static final int MAX_ITERATIONS = 10;
    private final Sudoku sudoku;
    private final BasicStrategy basicStrategy;
    private final PossibleValuesStrategy possibleValuesStrategy;
    private final CoupleOfCandidatesStrategy coupleOfCandidatesStrategy;
    private final SquaresStrategy squaresStrategy;
    private final TrioOfCandidatesStrategy trioOfCandidatesStrategy;
    private final HiddenCoupleOfCandidatesStrategy hiddenCoupleOfCandidatesStrategy;
    private final XWingStrategy xWingStrategy;

    private Chronology chronology;
    
    public SudokuSolver(Sudoku sudoku){
        // resettiamo gli step precedenti del solver esterno
        chronology = new Chronology();
        
        this.sudoku = sudoku;
        this.basicStrategy = new BasicStrategy(sudoku);
        this.possibleValuesStrategy = new PossibleValuesStrategy(sudoku);
        this.squaresStrategy = new SquaresStrategy(sudoku);
        this.coupleOfCandidatesStrategy = new CoupleOfCandidatesStrategy(sudoku);
        this.trioOfCandidatesStrategy = new TrioOfCandidatesStrategy(sudoku);
        this.hiddenCoupleOfCandidatesStrategy = new HiddenCoupleOfCandidatesStrategy(sudoku);
        this.xWingStrategy = new XWingStrategy(sudoku);
    }

    public void solve() {
        int iterations = 0;
        boolean solved = false;

        chronology.addSudoku(sudoku);
        while (!solved && iterations < MAX_ITERATIONS) {
            for (Cell cell : sudoku.getSudoku()) {
                if (cell.getValue() == 0) {
                    basicStrategy.apply(cell);
                    chronology.addSudoku(sudoku);
                }
            }

            for (Cell cell : sudoku.getSudoku()) {
                if (cell.getValue() == 0) {
                    possibleValuesStrategy.apply(cell);
                    chronology.addSudoku(sudoku);
                }
            }

            squaresStrategy.apply();
            chronology.addSudoku(sudoku);

            trioOfCandidatesStrategy.apply();
            chronology.addSudoku(sudoku);

            coupleOfCandidatesStrategy.apply();
            chronology.addSudoku(sudoku);

            hiddenCoupleOfCandidatesStrategy.apply();
            chronology.addSudoku(sudoku);

            solved = isCompleted(sudoku.getSudoku());
            iterations++;
        }

    }

    private boolean isCompleted(List<Cell> sudoku) {
        return sudoku.stream().noneMatch(c -> c.getValue() == 0);
    }

    public List<int[][]> getSteps() {
        return chronology.getSteps();
    }
}
