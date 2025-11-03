package leonardo.savona.sudoku.solver.strategy.cellbased;

import leonardo.savona.sudoku.model.Cell;
import leonardo.savona.sudoku.model.Sudoku;
import leonardo.savona.sudoku.model.square.Square;
import leonardo.savona.sudoku.util.SudokuUtils;

import java.util.Set;
import java.util.stream.Collectors;

public class PossibleValuesStrategy extends CellBasedStrategy {

    public PossibleValuesStrategy(Sudoku sudoku) {
        super(sudoku);
    }

    @Override
    public void apply() {
       for (int possibleValue : cell.getPossibleValues()) {
            if (!isPresentInOtherRowsPossibleValues(cell, possibleValue) ||
                    !isPresentInOtherColumnsPossibleValues(cell, possibleValue) ||
                    !isPresentInOtherSquaresPossibleValues(cell, possibleValue)) {

                cell.clearPossibleValues();
                cell.addPossibleValue(possibleValue);
                if (cell.isNumberFound()) {
                    SudokuUtils.clearOtherCellsPossibleValues(cell, sudoku);
                }
                break;
            }
        }
    }

    private boolean isPresentInOtherSquaresPossibleValues(Cell cell, int possibleValue) {
        Square square = SudokuUtils.getSquareFromCell(sudoku, cell);
        return isPresentInCellsPossibleValues(square.getCells().stream()
                        .filter(c -> !c.equals(cell)).collect(Collectors.toSet()),
                possibleValue);
    }

    private boolean isPresentInOtherRowsPossibleValues(Cell cell, int possibleValue) {
        Set<Cell> cells = sudoku.getSudoku().stream()
                .filter(c -> c.getCoordinate().getRow() == cell.getCoordinate().getRow() &&
                        c.getCoordinate().getColumn() != cell.getCoordinate().getColumn())
                .collect(Collectors.toSet());
        return isPresentInCellsPossibleValues(cells, possibleValue);
    }

    private boolean isPresentInOtherColumnsPossibleValues(Cell cell, int possibleValue) {
        Set<Cell> cells = sudoku.getSudoku().stream()
                .filter(c -> c.getCoordinate().getColumn() == cell.getCoordinate().getColumn() &&
                        c.getCoordinate().getRow() != cell.getCoordinate().getRow())
                .collect(Collectors.toSet());
        return isPresentInCellsPossibleValues(cells, possibleValue);
    }

    private boolean isPresentInCellsPossibleValues(Set<Cell> cells, int possibleValue) {
        return cells.stream()
                .anyMatch(c -> c.getValue() == 0 &&
                        (c.getPossibleValues().isEmpty() || c.getPossibleValues().contains(possibleValue)));
    }
}
