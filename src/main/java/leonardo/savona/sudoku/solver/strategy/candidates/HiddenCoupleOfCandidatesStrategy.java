package leonardo.savona.sudoku.solver.strategy.candidates;

import leonardo.savona.sudoku.solver.model.Cell;
import leonardo.savona.sudoku.solver.model.Sudoku;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HiddenCoupleOfCandidatesStrategy extends CoupleOfCandidatesStrategy {

    public HiddenCoupleOfCandidatesStrategy(Sudoku sudoku) {
        super(sudoku);
    }

    @Override
    protected void applyCandidatesStrategy(Set<Cell> cells) {
        Map<Object, Long> occurrences = cells.stream()
                .map(Cell::getPossibleValues)
                .flatMap(Set::stream)
                .collect(Collectors.groupingBy(n -> n, Collectors.counting()));

        Set<Integer> candidates = occurrences.entrySet().stream()
                .filter(e -> e.getValue() == 2)
                .map(e -> (Integer) e.getKey())
                .collect(Collectors.toSet());

        Map<Integer, Set<Cell>> candidatesMap = new HashMap<>();
        for (int candidate : candidates) {
            candidatesMap.put(candidate, cells.stream().filter(c -> c.getPossibleValues().contains(candidate)).collect(Collectors.toSet()));
        }

        for (Map.Entry<Integer, Set<Cell>> entry : candidatesMap.entrySet()) {
            candidatesMap.entrySet().stream()
                    .filter(entry1 -> !entry1.getKey().equals(entry.getKey()))
                    .collect(Collectors.toSet())
                    .forEach(entry2 -> {
                        if (entry2.getValue().equals(entry.getValue())) {
                            entry2.getValue().forEach(c -> {
                                c.clearPossibleValues();
                                c.addPossibleValue(entry2.getKey());
                                c.addPossibleValue(entry.getKey());
                            });
                        }
                    });
        }
    }

}
