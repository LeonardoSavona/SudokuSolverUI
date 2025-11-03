package leonardo.savona.sudoku.solver.strategy.candidates;

import leonardo.savona.sudoku.solver.Helper;
import leonardo.savona.sudoku.solver.model.Cell;
import leonardo.savona.sudoku.solver.model.Sudoku;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class TrioOfCandidatesStrategy extends CandidatesStrategy {

    public TrioOfCandidatesStrategy(Sudoku sudoku, BiConsumer<Cell, String> onValuePlaced) {
        super(sudoku, onValuePlaced);
    }

    @Override
    protected void applyCandidatesStrategy(Set<Cell> cells) {
        Map<Cell, Set<Cell>> map = new HashMap<>();
        for (Cell cell : cells) {
            if (cell.getValue() == 0) {
                map.put(cell, getCellsThatContainsCellPossibleValues(cell, cells.stream()
                        .filter(c -> c.getValue() == 0 && !c.equals(cell)).collect(Collectors.toSet())));
            }
        }

        for (Map.Entry<Cell, Set<Cell>> entry : map.entrySet()) {
            Cell cell = entry.getKey();
            Set<Cell> cellSet = entry.getValue();

            if (cellSet.size() == 2) {
                Set<Cell> cellsTrio = new HashSet<>(cellSet);
                cellsTrio.add(cell);

                Set<Integer> possibleCellsNumbers = cellsTrio.stream()
                        .map(Cell::getPossibleValues)
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet());

                if (cellsTrio.size() == possibleCellsNumbers.size()) {
                    for (int possibleCellNumber : possibleCellsNumbers) {
                        cells.stream()
                                .filter(c -> !cellsTrio.contains(c))
                                .forEach(c -> {
                                    c.removePossibleValue(possibleCellNumber);
                                    if (c.isNumberFound()) {
                                        Helper.clearOtherCellsPossibleValues(c, sudoku);
                                        notifyValuePlacement(c, "Tris di candidati");
                                    }
                                });
                    }
                }
            }
        }
    }

    private Set<Cell> getCellsThatContainsCellPossibleValues(Cell cell, Set<Cell> cells) {
        return cells.stream()
                .filter(c -> c.getPossibleValues().containsAll(cell.getPossibleValues()))
                .collect(Collectors.toSet());
    }

}
