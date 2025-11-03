package leonardo.savona.sudoku.solver;

import leonardo.savona.sudoku.model.Cell;
import leonardo.savona.sudoku.model.Sudoku;
import leonardo.savona.sudoku.solver.strategy.SquaresStrategy;
import leonardo.savona.sudoku.solver.strategy.Strategy;
import leonardo.savona.sudoku.solver.strategy.advanced.XWingStrategy;
import leonardo.savona.sudoku.solver.strategy.candidates.CoupleOfCandidatesStrategy;
import leonardo.savona.sudoku.solver.strategy.candidates.HiddenCoupleOfCandidatesStrategy;
import leonardo.savona.sudoku.solver.strategy.candidates.TrioOfCandidatesStrategy;
import leonardo.savona.sudoku.solver.strategy.cellbased.BasicStrategy;
import leonardo.savona.sudoku.solver.strategy.cellbased.CellBasedStrategy;
import leonardo.savona.sudoku.solver.strategy.cellbased.PossibleValuesStrategy;
import leonardo.savona.sudoku.util.SudokuModelConverter;

import java.util.ArrayList;
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

    private final Chronology chronology;
    
    private SudokuSolver(Sudoku sudoku){
        this.chronology = new Chronology();
        this.sudoku = sudoku;

        // strategies
        this.basicStrategy = new BasicStrategy(sudoku);
        this.possibleValuesStrategy = new PossibleValuesStrategy(sudoku);
        this.squaresStrategy = new SquaresStrategy(sudoku);
        this.coupleOfCandidatesStrategy = new CoupleOfCandidatesStrategy(sudoku);
        this.trioOfCandidatesStrategy = new TrioOfCandidatesStrategy(sudoku);
        this.hiddenCoupleOfCandidatesStrategy = new HiddenCoupleOfCandidatesStrategy(sudoku);
        this.xWingStrategy = new XWingStrategy(sudoku);
    }

    public static List<SolverStep> solveAndGetSteps(Sudoku board) {
        try {
            Sudoku workingSudoku = board.copy();
            SudokuSolver solver = new SudokuSolver(workingSudoku);
            solver.solve();

            List<SolverStep> steps = solver.getSteps();
            if (steps.isEmpty()) {
                List<SolverStep> single = new ArrayList<>();
                single.add(SolverStep.ofMatrix(
                        SudokuModelConverter.toMatrix(board),
                        null,
                        null,
                        null,
                        null
                ));
                return single;
            }
            return steps;
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            List<SolverStep> single = new ArrayList<>();
            single.add(SolverStep.ofMatrix(
                    SudokuModelConverter.toMatrix(board),
                    null,
                    null,
                    null,
                    null
            ));
            return single;
        }
    }

    private void solve() {
        int iterations = 0;
        boolean solved = false;

        chronology.addStep(SolverStep.capture(sudoku, null, null, null, "Stato iniziale"));
        while (!solved && iterations < MAX_ITERATIONS) {
            for (Cell cell : sudoku.getSudoku()) {
                if (cell.getValue() == 0) {
                    applyCellStrategy(cell, basicStrategy, "Strategia di base");
                }
            }

            for (Cell cell : sudoku.getSudoku()) {
                if (cell.getValue() == 0) {
                    applyCellStrategy(cell, possibleValuesStrategy, "Valori possibili");
                }
            }

            applyBoardStrategy(squaresStrategy, "Interazione righe/colonne-quadrati");

            applyBoardStrategy(trioOfCandidatesStrategy, "Tris di candidati");

            applyBoardStrategy(coupleOfCandidatesStrategy, "Coppie di candidati");

            applyBoardStrategy(hiddenCoupleOfCandidatesStrategy, "Coppie nascoste");

            solved = isCompleted(sudoku.getSudoku());
            iterations++;
        }

    }

    private void applyCellStrategy(Cell cell, CellBasedStrategy strategy, String strategyName) {
        int[][] before = SudokuModelConverter.toMatrix(sudoku);
        strategy.apply(cell);
        captureNewValues(before, strategyName);
    }

    private void applyBoardStrategy(Strategy strategy, String strategyName) {
        int[][] before = SudokuModelConverter.toMatrix(sudoku);
        strategy.apply();
        captureNewValues(before, strategyName);
    }

    private boolean isCompleted(List<Cell> sudoku) {
        return sudoku.stream().noneMatch(c -> c.getValue() == 0);
    }

    private void captureNewValues(int[][] before, String strategyName) {
        int[][] after = SudokuModelConverter.toMatrix(sudoku);
        int size = after.length;
        int[][] incremental = copyMatrix(before);
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (before[r][c] == 0 && after[r][c] != 0) {
                    incremental[r][c] = after[r][c];
                    chronology.addStep(SolverStep.ofMatrix(
                            incremental,
                            r,
                            c,
                            after[r][c],
                            strategyName
                    ));
                    incremental = copyMatrix(incremental);
                }
            }
        }
    }

    private int[][] copyMatrix(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = java.util.Arrays.copyOf(source[i], source[i].length);
        }
        return copy;
    }

    private List<SolverStep> getSteps() {
        return chronology.getSteps();
    }
}
