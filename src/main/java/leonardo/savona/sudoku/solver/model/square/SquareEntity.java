package leonardo.savona.sudoku.solver.model.square;

import leonardo.savona.sudoku.solver.model.Cell;

import java.util.*;
import java.util.stream.Collectors;

public abstract class SquareEntity {

    protected final int index;
    protected Set<Cell> cells = new HashSet<>();

    public SquareEntity(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public Set<Cell> getCells() {
        return cells;
    }

    public void addCell(Cell cell) {
        this.cells.add(cell);
    }

    public List<Integer> getPossibleValues() {
        return cells.stream()
                .flatMap(cell -> cell.getPossibleValues().stream())
                .collect(Collectors.toList());
    }

    public Set<Integer> getPossibleValuesPresentInEveryCells() {
        Map<Integer, Long> frequencyMap = getPossibleValues().stream()
                .collect(Collectors.groupingBy(n -> n, Collectors.counting()));
        Set<Integer> result = new HashSet<>();
        for (Map.Entry<Integer, Long> entry : frequencyMap.entrySet()) {
            if (entry.getValue() == getCellsWithPossibleValues().size()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private Set<Cell> getCellsWithPossibleValues() {
        return cells.stream().filter(c -> c.getPossibleValues().size() > 1).collect(Collectors.toSet());
    }

    @Override
    public abstract String toString();

    @Override
    public abstract boolean equals(Object o);

    @Override
    public int hashCode() {
        return Objects.hash(index, cells);
    }
}
