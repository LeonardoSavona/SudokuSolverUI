package leonardo.savona.sudoku.solver.strategy.advanced;

import leonardo.savona.sudoku.model.Cell;
import leonardo.savona.sudoku.model.Sudoku;
import leonardo.savona.sudoku.solver.strategy.Strategy;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class XWingStrategy extends Strategy {

    public XWingStrategy(Sudoku sudoku) {
        super(sudoku);
    }

    @Override
    public void apply() {
        for (Cell cell : sudoku.getSudoku().stream().filter(c -> c.getValue() == 0).collect(Collectors.toList())) {
            try {
                for (int candidate : cell.getPossibleValues()) {
                    Set<Cell> aboveRectangle = getRectangle(cell, candidate, true);
                    if (aboveRectangle.size() == 4) {
                        removeCandidatesBetweenVertices(aboveRectangle, candidate);
                    }

                    Set<Cell> belowRectangle = getRectangle(cell, candidate, false);
                    if (belowRectangle.size() == 4) {
                        removeCandidatesBetweenVertices(belowRectangle, candidate);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Set<Cell> getRectangle(Cell cell, int candidate, boolean above) {
        Set<Cell> vertices = new HashSet<>();
        vertices.add(cell);
        Set<Cell> possibleColumnVertices = getPossibleVerticesOnSameColumn(cell, candidate, above);
        possibleColumnVertices.removeIf(c -> !hasPossibleVerticesOnSameRow(c, candidate));

        for (Cell c : possibleColumnVertices.stream().sorted((c1,c2) -> Integer.compare(getDistanceByRow(c2, cell), getDistanceByRow(c1, cell))).collect(Collectors.toList())) {
            Set<Cell> possibleVerticesOnSameRow = getPossibleVerticesOnSameRow(c, candidate);
            possibleVerticesOnSameRow.removeIf(cell1 -> !hasPossibleVerticesOnSameColumn(c, candidate, !above));

            for (Cell cell1 : possibleVerticesOnSameRow.stream().sorted((c1,c2) -> Integer.compare(getDistanceByColumn(c2, cell), getDistanceByColumn(c1, cell))).collect(Collectors.toList())) {
                Set<Cell> possibleVerticesOnSamecolumn = getPossibleVerticesOnSameColumn(cell1, candidate, !above);
                possibleVerticesOnSamecolumn.removeIf(cell2 -> cell2.getCoordinate().getRow() != cell.getCoordinate().getRow());

                if (!possibleVerticesOnSamecolumn.isEmpty()) {
                    vertices.add(c);
                    vertices.add(cell1);
                    vertices.add(possibleVerticesOnSamecolumn.stream().findFirst().get());
                    return vertices;
                }
            }
        }
        return vertices;
    }

    private int getDistanceByRow(Cell c1, Cell cell) {
        return Math.abs(cell.getCoordinate().getRow() - c1.getCoordinate().getRow());
    }

    private int getDistanceByColumn(Cell cell, Cell cell1) {
        return Math.abs(cell.getCoordinate().getColumn() - cell1.getCoordinate().getColumn());
    }

    private boolean hasPossibleVerticesOnSameColumn(Cell c, int candidate, boolean above) {
        return !getPossibleVerticesOnSameColumn(c, candidate, above).isEmpty();
    }

    private Set<Cell> getPossibleVerticesOnSameColumn(Cell cell, int candidate, boolean above) {
        return sudoku.getSudoku().stream()
                .filter(c -> !c.equals(cell) && c.getValue() == 0 && c.getPossibleValues().contains(candidate) &&
                        c.getCoordinate().getColumn() == cell.getCoordinate().getColumn() &&
                        (c.getCoordinate().getRow() < cell.getCoordinate().getRow() == above))
                .collect(Collectors.toSet());
    }

    private boolean hasPossibleVerticesOnSameRow(Cell cell, int candidate) {
        return !getPossibleVerticesOnSameRow(cell, candidate).isEmpty();
    }

    private Set<Cell> getPossibleVerticesOnSameRow(Cell cell, int candidate) {
        return sudoku.getSudoku().stream()
                .filter(c -> !c.equals(cell) && c.getValue() == 0 && c.getPossibleValues().contains(candidate) &&
                        c.getCoordinate().getRow() == cell.getCoordinate().getRow() &&
                        c.getCoordinate().getColumn() > cell.getCoordinate().getColumn())
                .collect(Collectors.toSet());
    }

    private void removeCandidatesBetweenVertices(Set<Cell> rectangle, int candidate) {
        for (Cell vertex : rectangle) {
            Cell vertexWithSameRow = rectangle.stream()
                    .filter(c -> !c.equals(vertex) && c.getCoordinate().getRow() == vertex.getCoordinate().getRow())
                    .findFirst()
                    .get();

            sudoku.getSudoku().stream()
                    .filter(c -> !rectangle.contains(c) && c.getCoordinate().getRow() == vertex.getCoordinate().getRow() &&
                            (
                                    (vertexWithSameRow.getCoordinate().getColumn() > c.getCoordinate().getColumn() && c.getCoordinate().getColumn() > vertex.getCoordinate().getColumn()) ||
                                    (vertexWithSameRow.getCoordinate().getColumn() < c.getCoordinate().getColumn() && c.getCoordinate().getColumn() < vertex.getCoordinate().getColumn())
                            )
                    )
                    .forEach(c -> c.removePossibleValue(candidate));

            Cell vertexWithSameColumn = rectangle.stream()
                    .filter(c -> !c.equals(vertex) && c.getCoordinate().getColumn() == vertex.getCoordinate().getColumn())
                    .findFirst()
                    .get();

            sudoku.getSudoku().stream()
                    .filter(c -> !rectangle.contains(c) && c.getCoordinate().getColumn() == vertex.getCoordinate().getColumn() &&
                            (
                                    (vertexWithSameColumn.getCoordinate().getRow() > c.getCoordinate().getRow() && c.getCoordinate().getRow() > vertex.getCoordinate().getRow()) ||
                                    (vertexWithSameColumn.getCoordinate().getRow() < c.getCoordinate().getRow() && c.getCoordinate().getRow() < vertex.getCoordinate().getRow())
                            )
                    )
                    .forEach(c -> c.removePossibleValue(candidate));
        }
    }
}
