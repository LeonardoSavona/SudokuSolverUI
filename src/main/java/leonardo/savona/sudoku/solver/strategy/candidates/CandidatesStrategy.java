package leonardo.savona.sudoku.solver.strategy.candidates;

import leonardo.savona.sudoku.solver.Helper;
import leonardo.savona.sudoku.solver.model.Cell;
import leonardo.savona.sudoku.solver.model.Sudoku;
import leonardo.savona.sudoku.solver.model.square.Square;
import leonardo.savona.sudoku.solver.strategy.Strategy;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public abstract class CandidatesStrategy extends Strategy {

    public CandidatesStrategy(Sudoku sudoku, BiConsumer<Cell, String> onValuePlaced) {
        super(sudoku, onValuePlaced);
    }

    @Override
    public void apply() {
        for (int r = 0; r < sudoku.getSize(); r++) {
            applyCandidatesStrategy(getNotEmptyRowCells(r));
            applyCandidatesStrategy(getNotEmptyColumnCells(r));
        }

        int sq = (int) Math.sqrt(sudoku.getSize());
        for (Cell cell : sudoku.getSudoku()) {
            if (cell.getCoordinate().getRow() % sq == 0 && cell.getCoordinate().getColumn() % sq ==0)
                applyCandidatesStrategy(getNotEmptySquareCells(cell));
        }
    }

    protected abstract void applyCandidatesStrategy(Set<Cell> notEmptySquareCells);

    protected Set<Cell> getNotEmptyRowCells(int x) {
        Set<Cell> rowCells = sudoku.getSudoku().stream()
                .filter(c -> c.getCoordinate().getRow() == x)
                .collect(Collectors.toSet());
        return getNotEmptyCells(rowCells);
    }

    protected Set<Cell> getNotEmptyColumnCells(int x) {
        Set<Cell> colCells = sudoku.getSudoku().stream()
                .filter(cell -> cell.getCoordinate().getColumn() == x)
                .collect(Collectors.toSet());
        return getNotEmptyCells(colCells);
    }

    protected Set<Cell> getNotEmptySquareCells(Cell cell) {
        Square square = Helper.getSquareFromCell(sudoku, cell);
        return getNotEmptyCells(square.getCells());
    }

    private Set<Cell> getNotEmptyCells(Set<Cell> cells) {
        if (cells.stream().noneMatch(Cell::isEmpty)) {
            return cells;
        }
        return new HashSet<>();
    }
}
