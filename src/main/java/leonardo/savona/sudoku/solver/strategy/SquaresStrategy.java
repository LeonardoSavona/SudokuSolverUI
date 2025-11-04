package leonardo.savona.sudoku.solver.strategy;

import leonardo.savona.sudoku.model.Sudoku;
import leonardo.savona.sudoku.model.square.Square;
import leonardo.savona.sudoku.model.square.SquareColumn;
import leonardo.savona.sudoku.model.square.SquareRow;
import leonardo.savona.sudoku.util.SudokuUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SquaresStrategy extends Strategy {

    public SquaresStrategy(Sudoku sudoku) {
        super(sudoku);
    }

    @Override
    public void apply() {
        for (Square square : sudoku.getSquares()) {

            Map<SquareRow, Set<Integer>> rowsMap = new HashMap<>();
            for (SquareRow squareRow : square.getRows()) {
                rowsMap.put(squareRow, squareRow.getPossibleValuesPresentInEveryCells());
            }

            for (Map.Entry<SquareRow, Set<Integer>> entry : rowsMap.entrySet()) {
                for (int n : entry.getValue()) {
                    if (!square.getRows().stream()
                            .filter(r -> !r.equals(entry.getKey()))
                            .flatMap(r -> r.getPossibleValues().stream())
                            .collect(Collectors.toSet())
                            .contains(n)) {

                        sudoku.getSudoku().forEach(c -> {
                            if (c.getCoordinate().getRow() == entry.getKey().getIndex() &&
                                    !entry.getKey().getCells().contains(c) &&
                                    !c.getPossibleValues().isEmpty()) {

                                boolean removed = c.getPossibleValues().contains(n);
                                c.removePossibleValue(n);
                                if (removed && c.getValue() == 0 && c.getPossibleValues().size() == 1) {
                                    context.highlightRow(c);
                                    context.highlightCells(c, entry.getKey().getCells());
                                }
                                if (c.isNumberFound()) {
                                    SudokuUtils.clearOtherCellsPossibleValues(c, sudoku);
                                }
                            }
                        });
                    }
                }
            }

            Map<SquareColumn, Set<Integer>> columnsMap = new HashMap<>();
            for (SquareColumn squareColumn : square.getColumns()) {
                columnsMap.put(squareColumn, squareColumn.getPossibleValuesPresentInEveryCells());
            }

            for (Map.Entry<SquareColumn, Set<Integer>> entry : columnsMap.entrySet()) {
                for (int n : entry.getValue()) {
                    if (!square.getColumns().stream()
                            .filter(c -> !c.equals(entry.getKey()))
                            .flatMap(c -> c.getPossibleValues().stream())
                            .collect(Collectors.toSet())
                            .contains(n)) {

                        sudoku.getSudoku().forEach(c -> {
                            if (c.getCoordinate().getColumn() == entry.getKey().getIndex() &&
                                    !entry.getKey().getCells().contains(c) &&
                                    !c.getPossibleValues().isEmpty()){

                                boolean removed = c.getPossibleValues().contains(n);
                                c.removePossibleValue(n);
                                if (removed && c.getValue() == 0 && c.getPossibleValues().size() == 1) {
                                    context.highlightColumn(c);
                                    context.highlightCells(c, entry.getKey().getCells());
                                }
                                if (c.isNumberFound()) {
                                    SudokuUtils.clearOtherCellsPossibleValues(c, sudoku);
                                }
                            }
                        });
                    }
                }
            }

        }
    }
}
