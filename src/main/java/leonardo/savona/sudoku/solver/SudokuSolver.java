package leonardo.savona.sudoku.solver;

import leonardo.savona.sudoku.model.Cell;
import leonardo.savona.sudoku.model.Coordinate;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    private final StrategyContext context;

    private SudokuSolver(Sudoku sudoku){
        this.chronology = new Chronology();
        this.sudoku = sudoku;
        this.context = new StrategyContext(sudoku);

        // strategies
        this.basicStrategy = new BasicStrategy(sudoku);
        this.possibleValuesStrategy = new PossibleValuesStrategy(sudoku);
        this.squaresStrategy = new SquaresStrategy(sudoku);
        this.coupleOfCandidatesStrategy = new CoupleOfCandidatesStrategy(sudoku);
        this.trioOfCandidatesStrategy = new TrioOfCandidatesStrategy(sudoku);
        this.hiddenCoupleOfCandidatesStrategy = new HiddenCoupleOfCandidatesStrategy(sudoku);
        this.xWingStrategy = new XWingStrategy(sudoku);

        this.basicStrategy.setContext(context);
        this.possibleValuesStrategy.setContext(context);
        this.squaresStrategy.setContext(context);
        this.coupleOfCandidatesStrategy.setContext(context);
        this.trioOfCandidatesStrategy.setContext(context);
        this.hiddenCoupleOfCandidatesStrategy.setContext(context);
        this.xWingStrategy.setContext(context);
    }

    public static List<SolverStep> solveAndGetSteps(Sudoku board) {
        try {
            Sudoku workingSudoku = board.copy();
            SudokuSolver solver = new SudokuSolver(workingSudoku);
            solver.solve();

            List<SolverStep> steps = solver.getSteps();
            if (steps.isEmpty()) {
                List<SolverStep> single = new ArrayList<>();
                single.add(SolverStep.ofState(
                        SudokuModelConverter.toMatrix(board),
                        SudokuModelConverter.toNotes(board),
                        null,
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
            single.add(SolverStep.ofState(
                    SudokuModelConverter.toMatrix(board),
                    SudokuModelConverter.toNotes(board),
                    null,
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

        chronology.addStep(SolverStep.capture(sudoku, null, null, null, "Stato iniziale", null));
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
        Sudoku beforeState = sudoku.copy();
        int[][] before = SudokuModelConverter.toMatrix(beforeState);
        context.clear();
        strategy.apply(cell);
        captureNewValues(beforeState, before, strategyName);
    }

    private void applyBoardStrategy(Strategy strategy, String strategyName) {
        Sudoku beforeState = sudoku.copy();
        int[][] before = SudokuModelConverter.toMatrix(beforeState);
        context.clear();
        strategy.apply();
        captureNewValues(beforeState, before, strategyName);
    }

    private boolean isCompleted(List<Cell> sudoku) {
        return sudoku.stream().noneMatch(c -> c.getValue() == 0);
    }

    private void captureNewValues(Sudoku beforeState, int[][] before, String strategyName) {
        int[][] after = SudokuModelConverter.toMatrix(sudoku);
        int size = after.length;
        Sudoku incrementalSudoku = beforeState;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (before[r][c] == 0 && after[r][c] != 0) {
                    incrementalSudoku.setValue(r, c, after[r][c]);
                    int[][] snapshot = SudokuModelConverter.toMatrix(incrementalSudoku);
                    boolean[][][] notes = SudokuModelConverter.toNotes(incrementalSudoku);
                    Set<Coordinate> highlight = context.consumeHighlight(r, c);
                    if (highlight == null || highlight.isEmpty()) {
                        highlight = new LinkedHashSet<>();
                        highlight.add(new Coordinate(r, c));
                    }
                    chronology.addStep(SolverStep.ofState(
                            snapshot,
                            notes,
                            r,
                            c,
                            after[r][c],
                            strategyName,
                            highlight
                    ));
                }
            }
        }
    }

    private List<SolverStep> getSteps() {
        return chronology.getSteps();
    }
}
