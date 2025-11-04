package leonardo.savona.sudoku.solver;

import leonardo.savona.sudoku.model.Cell;
import leonardo.savona.sudoku.model.Coordinate;
import leonardo.savona.sudoku.model.Sudoku;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Collects highlighting information during the execution of a strategy so that
 * {@link SudokuSolver} can enrich {@link SolverStep} with the cells that led to
 * the discovered value.
 */
public class StrategyContext {

    public static final StrategyContext NO_OP = new StrategyContext(null) {
        @Override
        public void clear() { }

        @Override
        public void highlightCell(Cell cell) { }

        @Override
        public void highlightRow(Cell cell) { }

        @Override
        public void highlightColumn(Cell cell) { }

        @Override
        public void highlightSquare(Cell cell) { }

        @Override
        public void highlightCells(Cell cell, Set<Cell> cells) { }

        @Override
        public Set<Coordinate> consumeHighlight(int row, int column) {
            return null;
        }
    };

    private final Sudoku sudoku;
    private final Map<Coordinate, LinkedHashSet<Coordinate>> highlights = new LinkedHashMap<>();

    public StrategyContext(Sudoku sudoku) {
        this.sudoku = sudoku;
    }

    public void clear() {
        highlights.clear();
    }

    public void highlightCell(Cell cell) {
        if (cell == null) {
            return;
        }
        addHighlight(cell.getCoordinate(), single(cell.getCoordinate()));
    }

    public void highlightRow(Cell cell) {
        if (cell == null || sudoku == null) {
            return;
        }
        int row = cell.getCoordinate().getRow();
        LinkedHashSet<Coordinate> coords = new LinkedHashSet<>();
        for (int c = 0; c < sudoku.getSize(); c++) {
            coords.add(new Coordinate(row, c));
        }
        addHighlight(cell.getCoordinate(), coords);
    }

    public void highlightColumn(Cell cell) {
        if (cell == null || sudoku == null) {
            return;
        }
        int column = cell.getCoordinate().getColumn();
        LinkedHashSet<Coordinate> coords = new LinkedHashSet<>();
        for (int r = 0; r < sudoku.getSize(); r++) {
            coords.add(new Coordinate(r, column));
        }
        addHighlight(cell.getCoordinate(), coords);
    }

    public void highlightSquare(Cell cell) {
        if (cell == null || sudoku == null) {
            return;
        }
        LinkedHashSet<Coordinate> coords = new LinkedHashSet<>();
        Set<Coordinate> square = sudoku.getCoordinatesSquares().get(cell.getCoordinate());
        if (square != null) {
            for (Coordinate coordinate : square) {
                coords.add(new Coordinate(coordinate.getRow(), coordinate.getColumn()));
            }
        }
        coords.add(new Coordinate(cell.getCoordinate().getRow(), cell.getCoordinate().getColumn()));
        addHighlight(cell.getCoordinate(), coords);
    }

    public void highlightCells(Cell cell, Set<Cell> cells) {
        if (cell == null || cells == null || cells.isEmpty()) {
            return;
        }
        LinkedHashSet<Coordinate> coords = new LinkedHashSet<>();
        for (Cell c : cells) {
            coords.add(new Coordinate(c.getCoordinate().getRow(), c.getCoordinate().getColumn()));
        }
        addHighlight(cell.getCoordinate(), coords);
    }

    public Set<Coordinate> consumeHighlight(int row, int column) {
        if (highlights.isEmpty()) {
            return null;
        }
        Coordinate key = new Coordinate(row, column);
        LinkedHashSet<Coordinate> cells = highlights.remove(key);
        if (cells == null || cells.isEmpty()) {
            return null;
        }
        return new LinkedHashSet<>(cells);
    }

    private void addHighlight(Coordinate target, Set<Coordinate> coords) {
        if (target == null || coords == null || coords.isEmpty()) {
            return;
        }
        LinkedHashSet<Coordinate> list = highlights.computeIfAbsent(
                new Coordinate(target.getRow(), target.getColumn()),
                k -> new LinkedHashSet<>()
        );
        for (Coordinate coordinate : coords) {
            list.add(new Coordinate(coordinate.getRow(), coordinate.getColumn()));
        }
        list.add(new Coordinate(target.getRow(), target.getColumn()));
    }

    private Set<Coordinate> single(Coordinate coordinate) {
        LinkedHashSet<Coordinate> coords = new LinkedHashSet<>();
        coords.add(new Coordinate(coordinate.getRow(), coordinate.getColumn()));
        return coords;
    }
}
