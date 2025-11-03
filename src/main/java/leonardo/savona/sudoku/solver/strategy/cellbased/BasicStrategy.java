package leonardo.savona.sudoku.solver.strategy.cellbased;

import leonardo.savona.sudoku.solver.model.Cell;
import leonardo.savona.sudoku.solver.model.Coordinate;
import leonardo.savona.sudoku.solver.model.Sudoku;
import leonardo.savona.sudoku.solver.model.SudokuModelUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BasicStrategy extends CellBasedStrategy {

    private final int[] sudokuNumbersToFind;

    public BasicStrategy(Sudoku sudoku) {
        super(sudoku);
        this.sudokuNumbersToFind = new int[sudoku.getSize()];
        for (int i = 0; i < sudoku.getSize(); i++) {
            sudokuNumbersToFind[i] = i+1;
        }
    }

    @Override
    public void apply() {
        if (cell.isNumberFound()) {
            SudokuModelUtils.clearOtherCellsPossibleValues(cell, sudoku);
            return;
        }

        Set<Integer> rawMissingNumbers = getMissingNumbersFromRow(cell.getCoordinate().getRow());
        if (cell.getPossibleValues().isEmpty())
            cell.setPossibleValues(rawMissingNumbers);

        cell.getPossibleValues().removeIf(n -> !rawMissingNumbers.contains(n));
        if (cell.isNumberFound()) {
            SudokuModelUtils.clearOtherCellsPossibleValues(cell, sudoku);
            return;
        }

        Set<Integer> colMissingNumbers = getMissingNumbersFromColumn(cell.getCoordinate().getColumn());
        cell.getPossibleValues().removeIf(n -> !colMissingNumbers.contains(n));
        if (cell.isNumberFound()) {
            SudokuModelUtils.clearOtherCellsPossibleValues(cell, sudoku);
            return;
        }

        Set<Integer> squareMissingNumbers = getMissingNumbersFromSquare(cell);
        cell.getPossibleValues().removeIf(n -> !squareMissingNumbers.contains(n));
        if (cell.isNumberFound()) {
            SudokuModelUtils.clearOtherCellsPossibleValues(cell, sudoku);
        }
    }

    private Set<Integer> getMissingNumbersFromSquare(Cell cell) {
        Set<Integer> squareNumbers = new HashSet<>();
        Set<Coordinate> coordinates = sudoku.getCoordinatesSquares().get(cell.getCoordinate());
        for (Coordinate coordinate : coordinates) {
            Cell cell1 = sudoku.getCellByCoordinate(coordinate);
            if (cell1.getValue() != 0) squareNumbers.add(cell1.getValue());
        }
        return getMissingNumbersFromList(squareNumbers);
    }

    private Set<Integer> getMissingNumbersFromColumn(int column) {
        Set<Integer> columnNumbers = sudoku.getSudoku().stream()
                .filter(c -> c.getCoordinate().getColumn() == column && c.getValue() != 0)
                .map(Cell::getValue)
                .collect(Collectors.toSet());
        return getMissingNumbersFromList(columnNumbers);
    }

    private Set<Integer> getMissingNumbersFromRow(int row) {
        Set<Integer> rowNumbers = sudoku.getSudoku().stream()
                .filter(c -> c.getCoordinate().getRow() == row && c.getValue() != 0)
                .map(Cell::getValue)
                .collect(Collectors.toSet());
        return getMissingNumbersFromList(rowNumbers);
    }

    private Set<Integer> getMissingNumbersFromList(Set<Integer> numbers) {
        Set<Integer> result = new HashSet<>();
        for (int i : sudokuNumbersToFind) {
            if (!numbers.contains(i)) result.add(i);
        }
        return result;
    }
}
