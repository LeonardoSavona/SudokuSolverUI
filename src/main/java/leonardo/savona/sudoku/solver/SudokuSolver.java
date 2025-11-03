package leonardo.savona.sudoku.solver;

import leonardo.savona.sudoku.model.SudokuBoard;
import leonardo.savona.sudoku.solver.model.Cell;
import leonardo.savona.sudoku.solver.model.Coordinate;
import leonardo.savona.sudoku.solver.model.Sudoku;
import leonardo.savona.sudoku.solver.strategy.SquaresStrategy;
import leonardo.savona.sudoku.solver.strategy.advanced.XWingStrategy;
import leonardo.savona.sudoku.solver.strategy.candidates.CoupleOfCandidatesStrategy;
import leonardo.savona.sudoku.solver.strategy.candidates.HiddenCoupleOfCandidatesStrategy;
import leonardo.savona.sudoku.solver.strategy.candidates.TrioOfCandidatesStrategy;
import leonardo.savona.sudoku.solver.strategy.cellbased.BasicStrategy;
import leonardo.savona.sudoku.solver.strategy.cellbased.PossibleValuesStrategy;
import leonardo.savona.sudoku.util.SudokuModelConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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
    private final BiConsumer<Cell, String> onValuePlaced;

    private SudokuSolver(Sudoku sudoku){
        this.chronology = new Chronology();
        this.sudoku = sudoku;
        this.onValuePlaced = (cell, strategyName) -> {
            if (cell == null) {
                return;
            }
            int[][] snapshot = snapshotSudoku();
            chronology.addSudoku(new SolverStep(snapshot, cell.getCoordinate(), cell.getValue(), strategyName));
        };

        this.basicStrategy = new BasicStrategy(sudoku, onValuePlaced);
        this.possibleValuesStrategy = new PossibleValuesStrategy(sudoku, onValuePlaced);
        this.squaresStrategy = new SquaresStrategy(sudoku, onValuePlaced);
        this.coupleOfCandidatesStrategy = new CoupleOfCandidatesStrategy(sudoku, onValuePlaced);
        this.trioOfCandidatesStrategy = new TrioOfCandidatesStrategy(sudoku, onValuePlaced);
        this.hiddenCoupleOfCandidatesStrategy = new HiddenCoupleOfCandidatesStrategy(sudoku, onValuePlaced);
        this.xWingStrategy = new XWingStrategy(sudoku, onValuePlaced);
    }

    public static List<SolverStep> solveAndGetSteps(SudokuBoard board) {
        try {
            Sudoku externalSudoku = SudokuModelConverter.toExternalSudoku(board);

            SudokuSolver solver = new SudokuSolver(externalSudoku);
            solver.recordInitialState();
            solver.solve();

            List<SolverStep> steps = solver.getSteps();
            if (steps.isEmpty()) {
                List<SolverStep> single = new ArrayList<>();
                single.add(new SolverStep(SudokuModelConverter.toMatrix(board), null, 0, "Stato iniziale"));
                return single;
            }
            return steps;
        } catch (Exception ex) {
            ex.printStackTrace();
            List<SolverStep> single = new ArrayList<>();
            single.add(new SolverStep(SudokuModelConverter.toMatrix(board), null, 0, "Stato iniziale"));
            return single;
        }
    }

    private void solve() {
        int iterations = 0;
        boolean solved = false;

        while (!solved && iterations < MAX_ITERATIONS) {
            for (Cell cell : sudoku.getSudoku()) {
                if (cell.getValue() == 0) {
                    basicStrategy.apply(cell);
                }
            }

            for (Cell cell : sudoku.getSudoku()) {
                if (cell.getValue() == 0) {
                    possibleValuesStrategy.apply(cell);
                }
            }

            squaresStrategy.apply();
            trioOfCandidatesStrategy.apply();
            coupleOfCandidatesStrategy.apply();
            hiddenCoupleOfCandidatesStrategy.apply();
            xWingStrategy.apply();

            solved = isCompleted(sudoku.getSudoku());
            iterations++;
        }

    }

    private void recordInitialState() {
        chronology.addSudoku(new SolverStep(snapshotSudoku(), null, 0, "Stato iniziale"));
    }

    private int[][] snapshotSudoku() {
        int size = sudoku.getSize();
        int[][] matrix = new int[size][size];
        for (Cell cell : sudoku.getSudoku()) {
            Coordinate coordinate = cell.getCoordinate();
            matrix[coordinate.getRow()][coordinate.getColumn()] = cell.getValue();
        }
        return matrix;
    }

    private boolean isCompleted(List<Cell> sudoku) {
        return sudoku.stream().noneMatch(c -> c.getValue() == 0);
    }

    private List<SolverStep> getSteps() {
        return chronology.getSteps();
    }
}
