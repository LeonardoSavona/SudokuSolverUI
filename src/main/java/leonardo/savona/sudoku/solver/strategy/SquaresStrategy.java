package leonardo.savona.sudoku.solver.strategy;

import leonardo.savona.sudoku.solver.Helper;
import leonardo.savona.sudoku.solver.model.Cell;
import leonardo.savona.sudoku.solver.model.Sudoku;
import leonardo.savona.sudoku.solver.model.square.Square;
import leonardo.savona.sudoku.solver.model.square.SquareColumn;
import leonardo.savona.sudoku.solver.model.square.SquareRow;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class SquaresStrategy extends Strategy {

    public SquaresStrategy(Sudoku sudoku, BiConsumer<Cell, String> onValuePlaced) {
        super(sudoku, onValuePlaced);
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

                                c.removePossibleValue(n);
                                if (c.isNumberFound()) {
                                    Helper.clearOtherCellsPossibleValues(c, sudoku);
                                    notifyValuePlacement(c, "Riduzione per quadranti");
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

                                c.removePossibleValue(n);
                                if (c.isNumberFound()) {
                                    Helper.clearOtherCellsPossibleValues(c, sudoku);
                                    notifyValuePlacement(c, "Riduzione per quadranti");
                                }
                            }
                        });
                    }
                }
            }

        }
    }
}
