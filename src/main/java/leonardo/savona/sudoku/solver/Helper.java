package leonardo.savona.sudoku.solver;

import leonardo.savona.sudoku.solver.model.Cell;
import leonardo.savona.sudoku.solver.model.Coordinate;
import leonardo.savona.sudoku.solver.model.Sudoku;
import leonardo.savona.sudoku.solver.model.square.Square;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Helper {

    public static Map<Coordinate, Set<Coordinate>> getCoordinatesSquare(int size) {
        Map<Coordinate, Set<Coordinate>> coordinatesSquares = new HashMap<>();

        int sq = (int) Math.sqrt(size);

        for (int r = 0; r < size; r += sq) {
            for (int c = 0; c < size; c += sq) {
                Coordinate coordinate = new Coordinate(r,c);
                Set<Coordinate> otherCoordinates = getOtherCoordinates(coordinate, sq);

                coordinatesSquares.put(coordinate, otherCoordinates);
            }
        }

        Map<Coordinate, Set<Coordinate>> toAdd = new HashMap<>();
        for (Map.Entry<Coordinate, Set<Coordinate>> entry : coordinatesSquares.entrySet()) {
            for (Coordinate coordinate : entry.getValue()) {

                Set<Coordinate> coordinatesToAdd = new HashSet<>(entry.getValue());
                coordinatesToAdd.add(entry.getKey());
                coordinatesToAdd.remove(coordinate);
                toAdd.put(coordinate, coordinatesToAdd);
            }
        }

        coordinatesSquares.putAll(toAdd);
        return coordinatesSquares;
    }

    private static Set<Coordinate> getOtherCoordinates(Coordinate coordinate, int sq) {
        int raw = coordinate.getRow();
        int col = coordinate.getColumn();

        Set<Coordinate> result = new HashSet<>();
        for (int r = raw; r < raw + sq; r++){
            for (int c = col; c < col + sq; c++) {
                result.add(new Coordinate(r, c));
            }
        }

        return result;
    }

    public static Set<Square> getSquares(Sudoku sudoku, Map<Coordinate, Set<Coordinate>> coordinatesSquare) {
        Set<Square> squares = new HashSet<>();
        for (Coordinate coordinate : coordinatesSquare.keySet()) {
            Set<Cell> cells = coordinatesSquare.get(coordinate).stream()
                    .map(sudoku::getCellByCoordinate)
                    .collect(Collectors.toSet());
            cells.add(sudoku.getCellByCoordinate(coordinate));
            Square square = new Square(cells);
            squares.add(square);
        }

        return squares;
    }

    public static void clearOtherCellsPossibleValues(Cell cell, Sudoku sudoku) {
        clearOtherRowCellsPossibleValues(cell, sudoku);
        clearOtherColumnCellsPossibleValues(cell, sudoku);
        clearOtherSquareCellsPossibleValues(cell, sudoku);
    }

    private static void clearOtherRowCellsPossibleValues(Cell cell, Sudoku sudoku) {
        sudoku.getSudoku().forEach(c -> {
            if (c.getCoordinate().getRow() == cell.getCoordinate().getRow() && !c.getPossibleValues().isEmpty()){
                c.removePossibleValue(cell.getValue());
                if (c.isNumberFound()) {
                    Helper.clearOtherCellsPossibleValues(c, sudoku);
                }
            }
        });
    }

    private static void clearOtherColumnCellsPossibleValues(Cell cell, Sudoku sudoku) {
        sudoku.getSudoku().forEach(c -> {
            if (c.getCoordinate().getColumn() == cell.getCoordinate().getColumn() && !c.getPossibleValues().isEmpty()){
                c.removePossibleValue(cell.getValue());
                if (c.isNumberFound()) {
                    Helper.clearOtherCellsPossibleValues(c, sudoku);
                }
            }
        });
    }

    private static void clearOtherSquareCellsPossibleValues(Cell cell, Sudoku sudoku) {
        Set<Coordinate> coordinates = sudoku.getCoordinatesSquares().get(cell.getCoordinate());
        for (Coordinate coordinate : coordinates) {
            Cell cell1 = sudoku.getCellByCoordinate(coordinate);
            if (!cell1.getPossibleValues().isEmpty()) {
                cell1.removePossibleValue(cell.getValue());
                if (cell1.isNumberFound()) {
                    Helper.clearOtherCellsPossibleValues(cell1, sudoku);
                }
            }
        }
    }

    public static Square getSquareFromCell(Sudoku sudoku, Cell cell) {
        return sudoku.getSquares().stream().filter(s -> s.getCells().contains(cell)).findFirst().get();
    }
}
